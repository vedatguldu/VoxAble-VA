package com.voxable.feature_converter.domain

/**
 * Birim dönüşüm motoru.
 * Her birim önce temel birime çevrilir, sonra hedef birime dönüştürülür.
 */
object ConversionEngine {

    fun convert(value: Double, from: String, to: String, category: String): Double? {
        if (from == to) return value

        return when (category) {
            "Uzunluk" -> convertLength(value, from, to)
            "Ağırlık" -> convertWeight(value, from, to)
            "Sıcaklık" -> convertTemperature(value, from, to)
            "Alan" -> convertArea(value, from, to)
            "Hacim" -> convertVolume(value, from, to)
            "Hız" -> convertSpeed(value, from, to)
            else -> null
        }
    }

    // Uzunluk: temel birim = Metre
    private val lengthToMetre = mapOf(
        "Milimetre" to 0.001,
        "Santimetre" to 0.01,
        "Metre" to 1.0,
        "Kilometre" to 1000.0,
        "İnç" to 0.0254,
        "Feet" to 0.3048,
        "Mil" to 1609.344
    )

    private fun convertLength(value: Double, from: String, to: String): Double? {
        val fromFactor = lengthToMetre[from] ?: return null
        val toFactor = lengthToMetre[to] ?: return null
        return value * fromFactor / toFactor
    }

    // Ağırlık: temel birim = Gram
    private val weightToGram = mapOf(
        "Miligram" to 0.001,
        "Gram" to 1.0,
        "Kilogram" to 1000.0,
        "Ton" to 1_000_000.0,
        "Ons" to 28.3495,
        "Pound" to 453.592
    )

    private fun convertWeight(value: Double, from: String, to: String): Double? {
        val fromFactor = weightToGram[from] ?: return null
        val toFactor = weightToGram[to] ?: return null
        return value * fromFactor / toFactor
    }

    // Sıcaklık: özel dönüşüm formülleri
    private fun convertTemperature(value: Double, from: String, to: String): Double? {
        val celsius = when (from) {
            "Celsius" -> value
            "Fahrenheit" -> (value - 32) * 5.0 / 9.0
            "Kelvin" -> value - 273.15
            else -> return null
        }
        return when (to) {
            "Celsius" -> celsius
            "Fahrenheit" -> celsius * 9.0 / 5.0 + 32
            "Kelvin" -> celsius + 273.15
            else -> null
        }
    }

    // Alan: temel birim = Metrekare
    private val areaToSqMetre = mapOf(
        "Metrekare" to 1.0,
        "Kilometre Kare" to 1_000_000.0,
        "Hektar" to 10_000.0,
        "Dönüm" to 1000.0,
        "Feet Kare" to 0.092903
    )

    private fun convertArea(value: Double, from: String, to: String): Double? {
        val fromFactor = areaToSqMetre[from] ?: return null
        val toFactor = areaToSqMetre[to] ?: return null
        return value * fromFactor / toFactor
    }

    // Hacim: temel birim = Litre
    private val volumeToLitre = mapOf(
        "Mililitre" to 0.001,
        "Litre" to 1.0,
        "Metreküp" to 1000.0,
        "Galon" to 3.78541,
        "Bardak" to 0.2365
    )

    private fun convertVolume(value: Double, from: String, to: String): Double? {
        val fromFactor = volumeToLitre[from] ?: return null
        val toFactor = volumeToLitre[to] ?: return null
        return value * fromFactor / toFactor
    }

    // Hız: temel birim = m/s
    private val speedToMs = mapOf(
        "m/s" to 1.0,
        "km/h" to 1.0 / 3.6,
        "mph" to 0.44704,
        "Knot" to 0.514444
    )

    private fun convertSpeed(value: Double, from: String, to: String): Double? {
        val fromFactor = speedToMs[from] ?: return null
        val toFactor = speedToMs[to] ?: return null
        return value * fromFactor / toFactor
    }
}
