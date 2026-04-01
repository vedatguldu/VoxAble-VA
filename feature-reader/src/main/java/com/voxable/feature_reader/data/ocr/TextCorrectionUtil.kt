package com.voxable.feature_reader.data.ocr

object TextCorrectionUtil {
    fun correct(text: String, language: String = "tr"): String {
        var result = text
            .replace("\u00A0", " ")
            .replace("ﬁ", "fi")
            .replace("ﬂ", "fl")
            .replace(Regex("(?<=[A-Za-zÇĞİÖŞÜçğıöşü])-[\\r\\n]+(?=[A-Za-zÇĞİÖŞÜçğıöşü])"), "")
            .replace(Regex("[\\t ]+"), " ")
            .replace(Regex(" *([,.;:!?])"), "$1")
            .replace(Regex("([,.;:!?])(?=\\S)"), "$1 ")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()

        if (language.startsWith("tr", ignoreCase = true)) {
            result = result
                .replace("’", "'")
                .replace("‘", "'")
                .replace("“", "\"")
                .replace("”", "\"")
        }

        return result
    }
}
