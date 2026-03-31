package com.voxable.feature_reader.domain.repository

import com.voxable.core.util.Resource

interface ReaderRepository {
    suspend fun textToSpeech(text: String, language: String = "tr"): Resource<Unit>
    suspend fun stopSpeech(): Resource<Unit>
    fun isSpeaking(): Boolean
}
