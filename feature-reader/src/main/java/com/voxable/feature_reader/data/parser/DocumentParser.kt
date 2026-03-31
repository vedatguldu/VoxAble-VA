package com.voxable.feature_reader.data.parser

import com.voxable.feature_reader.domain.model.BookDocument
import com.voxable.feature_reader.domain.model.WordSpan
import java.io.File

interface DocumentParser {
    suspend fun parse(file: File, documentId: String): BookDocument
}

object WordSpanExtractor {
    fun extract(text: String): List<WordSpan> {
        val spans = mutableListOf<WordSpan>()
        val regex = Regex("\\S+")
        regex.findAll(text).forEach { match ->
            spans.add(WordSpan(match.range.first, match.range.last + 1, match.value))
        }
        return spans
    }
}
