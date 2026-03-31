package com.voxable.core_accessibility

import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TalkBackUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val accessibilityManager =
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

    /**
     * TalkBack kullanıcılarına anlık duyuru yapar.
     */
    fun announce(message: String) {
        if (accessibilityManager.isEnabled) {
            val event = AccessibilityEvent.obtain().apply {
                eventType = AccessibilityEvent.TYPE_ANNOUNCEMENT
                text.add(message)
            }
            accessibilityManager.sendAccessibilityEvent(event)
        }
    }

    /**
     * TalkBack etkin mi kontrol eder.
     */
    fun isTalkBackActive(): Boolean {
        return accessibilityManager.isTouchExplorationEnabled
    }
}
