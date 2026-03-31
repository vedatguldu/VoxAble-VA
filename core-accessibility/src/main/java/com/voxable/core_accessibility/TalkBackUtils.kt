package com.voxable.core_accessibility

import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TalkBack ile doğrudan iletişim kurmak için yardımcı sınıf.
 *
 * [VoiceFeedbackManager] daha yüksek seviyeli bir API sunarken,
 * bu sınıf TalkBack'e özgü düşük seviyeli işlemleri sağlar:
 * - Anlık duyuru gönderme
 * - TalkBack aktiflik kontrolü
 * - Traversal ipuçları
 */
@Singleton
class TalkBackUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val accessibilityManager =
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

    /**
     * TalkBack kullanıcılarına anlık duyuru yapar.
     * @param message Duyurulacak metin
     */
    fun announce(message: String) {
        if (!accessibilityManager.isEnabled || message.isBlank()) return

        val event = AccessibilityEvent().apply {
            eventType = AccessibilityEvent.TYPE_ANNOUNCEMENT
            text.add(message)
        }
        accessibilityManager.sendAccessibilityEvent(event)
    }

    /** TalkBack (dokunarak keşfetme) aktif mi? */
    fun isTalkBackActive(): Boolean {
        return accessibilityManager.isTouchExplorationEnabled
    }

    /** Herhangi bir erişilebilirlik servisi aktif mi? */
    fun isAnyServiceActive(): Boolean {
        return accessibilityManager.isEnabled
    }

    /**
     * Erişilebilirlik pencere değişikliği duyurusu.
     * Yeni ekrana geçişte TalkBack kullanıcısını bilgilendirir.
     */
    fun announceScreenChange(screenName: String) {
        announce("$screenName ekranına geçildi")
    }

    /**
     * İşlem sonucu duyurusu.
     * Başarılı veya başarısız işlemler için.
     */
    fun announceResult(success: Boolean, successMsg: String, failureMsg: String) {
        announce(if (success) successMsg else failureMsg)
    }
}
