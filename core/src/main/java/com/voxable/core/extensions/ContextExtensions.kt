package com.voxable.core.extensions

import android.content.Context
import android.view.accessibility.AccessibilityManager
import android.widget.Toast

fun Context.isAccessibilityEnabled(): Boolean {
    val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    return am.isEnabled
}

fun Context.isTalkBackEnabled(): Boolean {
    val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    return am.isTouchExplorationEnabled
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}
