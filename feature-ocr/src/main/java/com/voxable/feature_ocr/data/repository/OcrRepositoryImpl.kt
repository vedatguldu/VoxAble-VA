package com.voxable.feature_ocr.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.googlecode.tesseract.android.TessBaseAPI
import com.voxable.core.util.Resource
import com.voxable.core.util.map
import com.voxable.feature_ocr.domain.model.DetectedBarcode
import com.voxable.feature_ocr.domain.model.DetectedColor
import com.voxable.feature_ocr.domain.model.OcrImageAnalysis
import com.voxable.feature_ocr.domain.repository.OcrRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OcrRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : OcrRepository {

    private data class NamedColor(
        val name: String,
        val rgb: Int,
        val hex: String
    )

    private val namedColors = listOf(
        NamedColor("Beyaz", Color.rgb(245, 245, 245), "#F5F5F5"),
        NamedColor("Siyah", Color.rgb(33, 33, 33), "#212121"),
        NamedColor("Gri", Color.rgb(128, 128, 128), "#808080"),
        NamedColor("Kırmızı", Color.rgb(211, 47, 47), "#D32F2F"),
        NamedColor("Turuncu", Color.rgb(245, 124, 0), "#F57C00"),
        NamedColor("Sarı", Color.rgb(251, 192, 45), "#FBC02D"),
        NamedColor("Yeşil", Color.rgb(56, 142, 60), "#388E3C"),
        NamedColor("Turkuaz", Color.rgb(0, 137, 123), "#00897B"),
        NamedColor("Mavi", Color.rgb(25, 118, 210), "#1976D2"),
        NamedColor("Mor", Color.rgb(123, 31, 162), "#7B1FA2"),
        NamedColor("Pembe", Color.rgb(194, 24, 91), "#C2185B"),
        NamedColor("Kahverengi", Color.rgb(93, 64, 55), "#5D4037")
    )

    override suspend fun recognizeTextFromImage(imageUri: Uri, language: String): Resource<String> {
        return analyzeImage(imageUri, language).map { it.recognizedText }
    }

    override suspend fun analyzeImage(imageUri: Uri, language: String): Resource<OcrImageAnalysis> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val bitmap = context.contentResolver.openInputStream(imageUri)?.use(BitmapFactory::decodeStream)
                    ?: return@withContext Resource.Error("Görüntü açılamadı")
                try {
                    val recognizedText = correctText(recognizeHybrid(bitmap, language), language)
                    val detectedBarcodes = detectBarcodes(bitmap)
                    val detectedColors = extractDominantColors(bitmap)

                    if (recognizedText.isBlank() && detectedBarcodes.isEmpty() && detectedColors.isEmpty()) {
                        Resource.Error("Görüntüden metin, barkod veya baskın renk çıkarılamadı")
                    } else {
                        Resource.Success(
                            OcrImageAnalysis(
                                recognizedText = recognizedText,
                                detectedColors = detectedColors,
                                detectedBarcodes = detectedBarcodes,
                                summary = buildSummary(recognizedText, detectedBarcodes, detectedColors)
                            )
                        )
                    }
                } finally {
                    if (!bitmap.isRecycled) bitmap.recycle()
                }
            }.getOrElse { throwable ->
                Resource.Error(throwable.message ?: "Görüntü analizi sırasında hata oluştu", throwable)
            }
        }
    }

    private suspend fun recognizeHybrid(bitmap: Bitmap, language: String): String {
        val mlKitText = runCatching { recognizeWithMlKit(bitmap, language) }.getOrDefault("")
        val tessText = if (shouldTryTesseract(mlKitText)) {
            runCatching { recognizeWithTesseract(bitmap, language) }.getOrDefault("")
        } else {
            ""
        }
        return if (qualityScore(tessText) > qualityScore(mlKitText)) tessText else mlKitText
    }

    private suspend fun recognizeWithMlKit(bitmap: Bitmap, language: String): String {
        val recognizer = getRecognizer(language)
        return try {
            recognizer.process(InputImage.fromBitmap(bitmap, 0)).await().text.orEmpty().trim()
        } finally {
            recognizer.close()
        }
    }

    private suspend fun recognizeWithTesseract(bitmap: Bitmap, language: String): String = withContext(Dispatchers.IO) {
        val tessLanguage = mapLanguage(language)
        val tessDataDir = File(context.filesDir, "tesseract/tessdata")
        if (!ensureTrainedData(tessDataDir, tessLanguage)) return@withContext ""

        val api = TessBaseAPI()
        try {
            val dataPath = tessDataDir.parentFile?.absolutePath.orEmpty()
            if (!api.init(dataPath, tessLanguage)) return@withContext ""
            api.setImage(bitmap)
            api.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO
            api.utF8Text.orEmpty().trim()
        } finally {
            api.end()
        }
    }

    private fun getRecognizer(language: String): TextRecognizer {
        return when (language.lowercase()) {
            "zh", "zh-cn", "chinese" -> TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            "ja", "japanese" -> TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
            "ko", "korean" -> TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            "hi", "devanagari" -> TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
            else -> TextRecognition.getClient(TextRecognizerOptions.DEFAULT)
        }
    }

    private fun ensureTrainedData(tessDataDir: File, language: String): Boolean {
        if (!tessDataDir.exists()) tessDataDir.mkdirs()
        val outputFile = File(tessDataDir, "$language.traineddata")
        if (outputFile.exists() && outputFile.length() > 0L) return true
        return runCatching {
            context.assets.open("tessdata/$language.traineddata").use { input ->
                outputFile.outputStream().use { output -> input.copyTo(output) }
            }
            true
        }.getOrDefault(false)
    }

    private fun shouldTryTesseract(text: String): Boolean {
        if (text.isBlank()) return true
        val compact = text.filterNot(Char::isWhitespace)
        return compact.length < 24 || compact.count { it.isLetterOrDigit() } < compact.length * 0.6
    }

    private fun qualityScore(text: String): Int {
        if (text.isBlank()) return 0
        val compact = text.filterNot(Char::isWhitespace)
        return compact.count { it.isLetterOrDigit() } * 2 - compact.count { !it.isLetterOrDigit() } + compact.length
    }

    private suspend fun detectBarcodes(bitmap: Bitmap): List<DetectedBarcode> {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC,
                Barcode.FORMAT_DATA_MATRIX,
                Barcode.FORMAT_PDF417,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E
            )
            .enableAllPotentialBarcodes()
            .build()
        val scanner = BarcodeScanning.getClient(options)

        return try {
            scanner.process(InputImage.fromBitmap(bitmap, 0)).await()
                .mapNotNull { barcode ->
                    val rawValue = barcode.rawValue ?: return@mapNotNull null
                    DetectedBarcode(
                        rawValue = rawValue,
                        displayValue = barcode.displayValue ?: rawValue,
                        formatLabel = barcode.format.toFormatLabel(),
                        valueTypeLabel = barcode.valueType.toValueTypeLabel()
                    )
                }
                .distinctBy { it.rawValue }
        } finally {
            scanner.close()
        }
    }

    private fun extractDominantColors(bitmap: Bitmap): List<DetectedColor> {
        val scaled = Bitmap.createScaledBitmap(bitmap, 48, 48, true)
        return try {
            val counts = mutableMapOf<NamedColor, Int>()
            var sampled = 0

            for (x in 0 until scaled.width) {
                for (y in 0 until scaled.height) {
                    val pixel = scaled.getPixel(x, y)
                    if (Color.alpha(pixel) < 96) continue
                    val nearest = nearestColor(pixel)
                    counts[nearest] = (counts[nearest] ?: 0) + 1
                    sampled++
                }
            }

            if (sampled == 0) return emptyList()

            counts.entries
                .sortedByDescending { it.value }
                .take(4)
                .map { (color, count) ->
                    DetectedColor(
                        name = color.name,
                        hex = color.hex,
                        coverage = count.toFloat() / sampled.toFloat()
                    )
                }
                .filter { it.coverage >= 0.08f }
        } finally {
            if (scaled !== bitmap && !scaled.isRecycled) {
                scaled.recycle()
            }
        }
    }

    private fun nearestColor(pixel: Int): NamedColor {
        val red = Color.red(pixel)
        val green = Color.green(pixel)
        val blue = Color.blue(pixel)
        return namedColors.minBy { named ->
            val dr = red - Color.red(named.rgb)
            val dg = green - Color.green(named.rgb)
            val db = blue - Color.blue(named.rgb)
            dr * dr + dg * dg + db * db
        }
    }

    private fun buildSummary(
        recognizedText: String,
        detectedBarcodes: List<DetectedBarcode>,
        detectedColors: List<DetectedColor>
    ): String {
        val parts = mutableListOf<String>()
        if (recognizedText.isNotBlank()) {
            parts += "Metin algılandı"
        }
        if (detectedBarcodes.isNotEmpty()) {
            parts += "${detectedBarcodes.size} barkod bulundu"
        }
        if (detectedColors.isNotEmpty()) {
            parts += "Baskın renkler: ${detectedColors.joinToString(", ") { it.name }}"
        }
        return parts.joinToString(". ")
    }

    private fun correctText(text: String, language: String): String {
        var result = text
            .replace("\u00A0", " ")
            .replace(Regex("(?<=[A-Za-zÇĞİÖŞÜçğıöşü])-[\\r\\n]+(?=[A-Za-zÇĞİÖŞÜçğıöşü])"), "")
            .replace(Regex("[\\t ]+"), " ")
            .replace(Regex(" *([,.;:!?])"), "$1")
            .replace(Regex("([,.;:!?])(?=\\S)"), "$1 ")
            .trim()
        if (language.startsWith("tr", ignoreCase = true)) {
            result = result.replace("’", "'").replace("‘", "'").replace("“", "\"").replace("”", "\"")
        }
        return result
    }

    private fun mapLanguage(language: String): String = when (language.lowercase()) {
        "tr", "tr-tr", "tur" -> "tur"
        "en", "eng", "en-us", "en-gb" -> "eng"
        "de", "deu" -> "deu"
        "fr", "fra" -> "fra"
        "es", "spa" -> "spa"
        "it", "ita" -> "ita"
        "pt", "por" -> "por"
        else -> "eng"
    }

    private fun Int.toFormatLabel(): String = when (this) {
        Barcode.FORMAT_QR_CODE -> "QR Kod"
        Barcode.FORMAT_AZTEC -> "Aztec"
        Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
        Barcode.FORMAT_PDF417 -> "PDF417"
        Barcode.FORMAT_CODE_128 -> "Code 128"
        Barcode.FORMAT_CODE_39 -> "Code 39"
        Barcode.FORMAT_EAN_13 -> "EAN-13"
        Barcode.FORMAT_EAN_8 -> "EAN-8"
        Barcode.FORMAT_UPC_A -> "UPC-A"
        Barcode.FORMAT_UPC_E -> "UPC-E"
        else -> "Barkod"
    }

    private fun Int.toValueTypeLabel(): String = when (this) {
        Barcode.TYPE_URL -> "Bağlantı"
        Barcode.TYPE_WIFI -> "Wi-Fi"
        Barcode.TYPE_CONTACT_INFO -> "Kişi"
        Barcode.TYPE_EMAIL -> "E-posta"
        Barcode.TYPE_PHONE -> "Telefon"
        Barcode.TYPE_PRODUCT -> "Ürün"
        Barcode.TYPE_SMS -> "SMS"
        Barcode.TYPE_TEXT -> "Metin"
        Barcode.TYPE_GEO -> "Konum"
        Barcode.TYPE_CALENDAR_EVENT -> "Takvim"
        Barcode.TYPE_DRIVER_LICENSE -> "Kimlik"
        else -> "Genel"
    }
}
