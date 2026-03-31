package com.voxable.feature_reader.data.tts

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.voxable.feature_reader.domain.model.TtsEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class WordTrackingTtsEngine @Inject constructor(
    private val context: android.content.Context
) {
    private var tts: TextToSpeech? = null
    private val _events = Channel<TtsEvent>(Channel.BUFFERED)
    val events: Flow<TtsEvent> = _events.receiveAsFlow()

    private suspend fun ensureInitialized(): TextToSpeech {
        tts?.let { return it }
        return suspendCancellableCoroutine { cont ->
            val engine = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    cont.resume(engine = tts!!)
                }
            }.also { tts = it }

            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _events.trySend(TtsEvent.Started)
                }

                override fun onDone(utteranceId: String?) {
                    _events.trySend(TtsEvent.UtteranceDone(utteranceId ?: ""))
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _events.trySend(TtsEvent.Error("TTS hatası"))
                }

                override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                    _events.trySend(TtsEvent.WordStarted(start, end))
                }
            })
        }
    }

    suspend fun speak(text: String, language: String = "tr-TR", speed: Float = 1.0f, pitch: Float = 1.0f) {
        val engine = ensureInitialized()
        val parts = language.split("-")
        engine.language = Locale(parts[0], parts.getOrElse(1) { "" })
        engine.setSpeechRate(speed)
        engine.setPitch(pitch)
        val utteranceId = UUID.randomUUID().toString()
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun pause() {
        tts?.stop()
        _events.trySend(TtsEvent.Paused)
    }

    fun stop() {
        tts?.stop()
        _events.trySend(TtsEvent.Stopped)
    }

    fun release() {
        tts?.shutdown()
        tts = null
    }
}
