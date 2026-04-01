package com.voxable.feature_settings.domain.model

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String
) {
    TURKISH("tr", "Türkçe", "Turkish"),
    ENGLISH("en", "English", "English"),
    GERMAN("de", "Deutsch", "German"),
    FRENCH("fr", "Français", "French"),
    SPANISH("es", "Español", "Spanish"),
    ARABIC("ar", "العربية", "Arabic");

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.find { it.code == code } ?: TURKISH
    }
}
