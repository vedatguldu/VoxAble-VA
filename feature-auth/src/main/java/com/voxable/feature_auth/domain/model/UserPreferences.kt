package com.voxable.feature_auth.domain.model

data class UserPreferences(
    val darkMode: Boolean = false,
    val fontSize: Float = 1.0f,
    val highContrast: Boolean = false,
    val talkBackHints: Boolean = true,
    val language: String = "tr",
    val reduceMotion: Boolean = false,
    val hapticFeedback: Boolean = true
)
