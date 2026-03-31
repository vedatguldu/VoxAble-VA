package com.voxable.core_accessibility

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sesli geri bildirim (Voice Feedback) sistemi.
 *
 * İki farklı kanal üzerinden ses geri bildirimi sağlar:
 * 1. **TalkBack Duyuruları** — Erişilebilirlik servisi aktifken
 *    AccessibilityEvent üzerinden duyuru gönderir.
 * 2. **TTS (Text-to-Speech)** — Uygulama içi sesli rehberlik için
 *    Android TextToSpeech motoru kullanır.
 *
 * Ayrıca dokunsal (haptic) geri bildirim desteği sunar.
 *
 * Kullanım:
 * ```
 * voiceFeedbackManager.announce("Dosya başarıyla yüklendi")
 * voiceFeedbackManager.speak("Hoş geldiniz, Ana Sayfa'dasınız")
 * voiceFeedbackManager.hapticTick()
 * ```
 */
@Singleton
class VoiceFeedbackManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stateManager: AccessibilityStateManager
) {
    private val accessibilityManager =
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

    private var tts: TextToSpeech? = null
    private val _ttsReady = MutableStateFlow(false)

    /** TTS motoru hazır mı? */
    val ttsReady: StateFlow<Boolean> = _ttsReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    /** Şu an konuşma devam ediyor mu? */
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    // ─── TTS Yaşam Döngüsü ────────────────────────────────

    /**
     * TTS motorunu başlatır. Activity.onCreate() veya Application seviyesinde çağrılmalıdır.
     */
    fun initTts(locale: Locale = Locale("tr", "TR")) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { engine ->
                    val result = engine.setLanguage(locale)
                    _ttsReady.value = result != TextToSpeech.LANG_MISSING_DATA
                            && result != TextToSpeech.LANG_NOT_SUPPORTED
                    engine.setSpeechRate(0.95f)
                    engine.setPitch(1.0f)
                }
            }
        }
    }

    /** TTS kaynaklarını serbest bırakır. Activity.onDestroy() veya uygulama kapanışında çağrılmalıdır. */
    fun releaseTts() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _ttsReady.value = false
        _isSpeaking.value = false
    }

    // ─── TalkBack Duyuru ──────────────────────────────────

    /**
     * TalkBack kullanıcılarına anlık duyuru yapar.
     * Erişilebilirlik servisi aktif değilse sessizce atlar.
     *
     * @param message Duyurulacak mesaj
     */
    fun announce(message: String) {
        if (!accessibilityManager.isEnabled || message.isBlank()) return

        val event = AccessibilityEvent().apply {
            eventType = AccessibilityEvent.TYPE_ANNOUNCEMENT
            text.add(message)
        }
        accessibilityManager.sendAccessibilityEvent(event)
    }

    // ─── TTS Konuşma ──────────────────────────────────────

    /**
     * Metni TTS motoru ile sesli okur.
     *
     * @param text Okunacak metin
     * @param queueMode [TextToSpeech.QUEUE_FLUSH] mevcut sırayı temizler,
     *                  [TextToSpeech.QUEUE_ADD] sıraya ekler
     * @param onDone Okuma bittiğinde çağrılacak callback
     */
    fun speak(
        text: String,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH,
        onDone: (() -> Unit)? = null
    ) {
        val engine = tts ?: return
        if (!_ttsReady.value || text.isBlank()) return

        val utteranceId = UUID.randomUUID().toString()

        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(id: String?) {
                _isSpeaking.value = false
                onDone?.invoke()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {
                _isSpeaking.value = false
            }
        })

        engine.speak(text, queueMode, null, utteranceId)
    }

    /**
     * TTS konuşma akışını Flow olarak döndürür.
     * Flow tamamlandığında konuşma bitmiş demektir.
     */
    fun speakFlow(text: String): Flow<SpeechState> = callbackFlow {
        val engine = tts
        if (engine == null || !_ttsReady.value || text.isBlank()) {
            trySend(SpeechState.Error("TTS kullanılamıyor"))
            close()
            return@callbackFlow
        }

        val utteranceId = UUID.randomUUID().toString()

        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {
                trySend(SpeechState.Speaking)
            }

            override fun onDone(id: String?) {
                trySend(SpeechState.Done)
                close()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {
                trySend(SpeechState.Error("TTS hatası"))
                close()
            }
        })

        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

        awaitClose {
            engine.stop()
        }
    }

    /** Devam eden konuşmayı durdurur */
    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    /** TTS konuşma hızını ayarlar (0.5 – 2.0 arası) */
    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
    }

    // ─── Dokunsal Geri Bildirim ───────────────────────────

    /** Kısa titreşim — buton tıklama, seçim onayı vb. */
    fun hapticTick() {
        if (!stateManager.userPreferHapticFeedback.value) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(30L, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30L)
        }
    }

    /** Orta titreşim — uyarı, dikkat çekme */
    fun hapticWarning() {
        if (!stateManager.userPreferHapticFeedback.value) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 60, 40, 60), -1)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 60, 40, 60), -1)
        }
    }

    /** Başarı titreşimi — işlem tamamlandı */
    fun hapticSuccess() {
        if (!stateManager.userPreferHapticFeedback.value) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 20, 60, 40), -1)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 20, 60, 40), -1)
        }
    }

    /** Hata titreşimi — uzun tek titreşim */
    fun hapticError() {
        if (!stateManager.userPreferHapticFeedback.value) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(150L, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(150L)
        }
    }
}

/** TTS konuşma durumu */
sealed interface SpeechState {
    data object Speaking : SpeechState
    data object Done : SpeechState
    data class Error(val message: String) : SpeechState
}
