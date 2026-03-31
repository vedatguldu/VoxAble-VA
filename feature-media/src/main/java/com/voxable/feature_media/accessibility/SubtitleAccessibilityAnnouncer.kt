package com.voxable.feature_media.accessibility

import android.speech.tts.TextToSpeech
import com.voxable.feature_media.domain.model.Subtitle
import com.voxable.feature_media.domain.model.SubtitleData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Singleton

@Singleton
class SubtitleAccessibilityAnnouncer(private val textToSpeech: TextToSpeech?) {
    private val _currentlyAnnouncedSubtitle = MutableStateFlow<Subtitle?>(null)
    val currentlyAnnouncedSubtitle: Flow<Subtitle?> = _currentlyAnnouncedSubtitle.asStateFlow()

    suspend fun announceSubtitle(subtitle: Subtitle?, language: String) {
        if (subtitle == null || subtitle.text.isEmpty()) {
            stopAnnouncement()
            return
        }

        _currentlyAnnouncedSubtitle.update { subtitle }

        textToSpeech?.apply {
            val langArray = language.split("-").take(2).toTypedArray()
            language(android.util.Locale(langArray[0], langArray.getOrNull(1) ?: ""))
            speak(
                subtitle.text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "subtitle-${subtitle.startTimeMs}"
            )
        }
    }

    fun stopAnnouncement() {
        textToSpeech?.stop()
        _currentlyAnnouncedSubtitle.update { null }
    }

    fun release() {
        textToSpeech?.shutdown()
    }
}