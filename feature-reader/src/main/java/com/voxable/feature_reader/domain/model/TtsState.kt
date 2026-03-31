package com.voxable.feature_reader.domain.model

data class TtsState(
    val isSpeaking: Boolean = false,
    val isPaused: Boolean = false,
    val currentWordStart: Int = 0,
    val currentWordEnd: Int = 0,
    val utteranceId: String? = null,
    val language: String = "tr-TR",
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f
)

sealed interface TtsEvent {
    data class WordStarted(val start: Int, val end: Int) : TtsEvent
    data class UtteranceDone(val utteranceId: String) : TtsEvent
    data class Error(val message: String) : TtsEvent
    object Started : TtsEvent
    object Paused : TtsEvent
    object Stopped : TtsEvent
}
