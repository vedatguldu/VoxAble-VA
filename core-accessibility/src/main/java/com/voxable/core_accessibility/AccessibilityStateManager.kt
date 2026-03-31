package com.voxable.core_accessibility

import android.content.Context
import android.view.accessibility.AccessibilityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccessibilityStateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val accessibilityManager =
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

    val isTalkBackEnabled: Boolean
        get() = accessibilityManager.isTouchExplorationEnabled

    val isAccessibilityEnabled: Boolean
        get() = accessibilityManager.isEnabled

    val accessibilityStateFlow: Flow<Boolean> = callbackFlow {
        val listener = AccessibilityManager.AccessibilityStateChangeListener { enabled ->
            trySend(enabled)
        }
        accessibilityManager.addAccessibilityStateChangeListener(listener)
        trySend(accessibilityManager.isEnabled)

        awaitClose {
            accessibilityManager.removeAccessibilityStateChangeListener(listener)
        }
    }.distinctUntilChanged()

    val touchExplorationStateFlow: Flow<Boolean> = callbackFlow {
        val listener = AccessibilityManager.TouchExplorationStateChangeListener { enabled ->
            trySend(enabled)
        }
        accessibilityManager.addTouchExplorationStateChangeListener(listener)
        trySend(accessibilityManager.isTouchExplorationEnabled)

        awaitClose {
            accessibilityManager.removeTouchExplorationStateChangeListener(listener)
        }
    }.distinctUntilChanged()
}
