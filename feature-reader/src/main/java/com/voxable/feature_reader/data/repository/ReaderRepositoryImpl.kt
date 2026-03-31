package com.voxable.feature_reader.data.repository

import android.content.Context
import android.speech.tts.TextToSpeech
import com.voxable.core.base.BaseRepository
import com.voxable.core.util.Resource
import com.voxable.feature_reader.domain.repository.ReaderRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class ReaderRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BaseRepository(), ReaderRepository {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private suspend fun initTts(): TextToSpeech = suspendCancellableCoroutine { cont ->
        if (tts != null && isInitialized) {
            cont.resume(tts!!)
            return@suspendCancellableCoroutine
        }

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.language = Locale("tr", "TR")
                cont.resume(tts!!)
            }
        }
    }

    override suspend fun textToSpeech(text: String, language: String): Resource<Unit> {
        return safeCall {
            val engine = initTts()
            engine.language = Locale(language)
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "voxable_utterance")
        }
    }

    override suspend fun stopSpeech(): Resource<Unit> {
        return safeCall {
            tts?.stop()
            Unit
        }
    }

    override fun isSpeaking(): Boolean {
        return tts?.isSpeaking == true
    }
}
