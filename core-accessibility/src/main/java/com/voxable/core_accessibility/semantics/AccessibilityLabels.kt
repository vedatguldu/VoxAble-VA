package com.voxable.core_accessibility.semantics

/**
 * contentDescription stratejisi için merkezi metin şablonları.
 *
 * TalkBack duyurularındaki tutarlılığı sağlar.
 * Tüm erisilebilirlik etiketleri bu nesne üzerinden
 * oluşturulmalıdır.
 *
 * Kullanım:
 * ```
 * val label = AccessibilityLabels.button("Gönder")
 * val errorLabel = AccessibilityLabels.error("E-posta geçersiz")
 * ```
 */
object AccessibilityLabels {

    // ─── Bileşen Etiketleri ─────────────────────────────

    /** Buton etiketi: "[action] butonu" */
    fun button(action: String): String = "$action butonu"

    /** Giriş alanı etiketi: "[fieldName] giriş alanı" */
    fun textField(fieldName: String): String = "$fieldName giriş alanı"

    /** Giriş alanı + değer: "[fieldName] giriş alanı, mevcut değer: [value]" */
    fun textFieldWithValue(fieldName: String, value: String): String =
        if (value.isEmpty()) "$fieldName giriş alanı, boş"
        else "$fieldName giriş alanı, mevcut değer: $value"

    /** Görsel tanım: "[description] görseli" */
    fun image(description: String): String = "$description görseli"

    /** Simge buton: "[action] simgesi" */
    fun iconButton(action: String): String = "$action simgesi"

    // ─── Durum Etiketleri ───────────────────────────────

    /** Yükleniyor durumu */
    fun loading(context: String = ""): String =
        if (context.isEmpty()) "Yükleniyor, lütfen bekleyin"
        else "$context yükleniyor, lütfen bekleyin"

    /** Hata durumu: "Hata: [message]" */
    fun error(message: String): String = "Hata: $message"

    /** Başarı durumu: "Başarılı: [message]" */
    fun success(message: String): String = "Başarılı: $message"

    /** Uyarı durumu */
    fun warning(message: String): String = "Uyarı: $message"

    // ─── Navigasyon Etiketleri ─────────────────────────

    /** Geri butonu */
    fun backButton(screenName: String = ""): String =
        if (screenName.isEmpty()) "Geri dön"
        else "$screenName ekranından geri dön"

    /** Sekme etiketi: "[name] sekmesi, [x]/[total]" */
    fun tab(name: String, index: Int, total: Int): String =
        "$name sekmesi, ${index + 1} / $total"

    /** Sayfa etiketi */
    fun screenTitle(title: String): String = "$title ekranı"

    // ─── Liste Etiketleri ────────────────────────────────

    /** Liste öğesi: "[content], [x]/[total] öğe" */
    fun listItem(content: String, index: Int, total: Int): String =
        "$content, ${index + 1} / $total öğe"

    /** Boş liste durumu */
    fun emptyList(context: String): String = "$context listesi boş"

    // ─── Form Etiketleri ────────────────────────────────

    /** Form hatası: "[field] gerekli" */
    fun requiredFieldError(fieldName: String): String = "$fieldName gerekli"

    /** Form hatası ile mesaj */
    fun fieldError(fieldName: String, error: String): String =
        "$fieldName alanında hata: $error"

    /** Şifre alanı (maskelenmiş) */
    fun passwordField(fieldName: String = "Şifre"): String =
        "$fieldName giriş alanı, metin gizli"

    // ─── Medya Etiketleri ───────────────────────────────

    /** Oynat/duraklat durumu */
    fun playPause(isPlaying: Boolean): String =
        if (isPlaying) "Duraklat butonu, şu an çalıyor" else "Oynat butonu, duraklatilmış"

    /** İlerleme durumu: "%[percent] tamamlandı" */
    fun progress(percent: Int): String = "Yüzde $percent tamamlandı"

    /** Ses seviyesi */
    fun volume(level: Int): String = "Ses seviyesi: yüzde $level"

    // ─── Özel VoxAble Etiketleri ───────────────────────

    /** OCR sonuç etiketi */
    fun ocrResult(text: String): String =
        if (text.isEmpty()) "Metin tanınması sonucu boş"
        else "Tanınan metin: $text"

    /** Döviz kuru etiketi */
    fun currencyRate(from: String, to: String, rate: String): String =
        "1 $from = $rate $to"

    /** İndirme durumu */
    fun downloadStatus(fileName: String, percent: Int): String =
        "$fileName indiriliyor, yüzde $percent"

    /** Birim dönüştürme sonucu */
    fun conversionResult(value: String, fromUnit: String, result: String, toUnit: String): String =
        "$value $fromUnit = $result $toUnit"
}
