package com.voxable.core.util

object Constants {
    const val DATABASE_NAME = "voxable_database"
    const val DATASTORE_NAME = "voxable_preferences"
    const val BASE_URL = "https://api.voxable.com/"

    object Firebase {
        const val USERS_COLLECTION = "users"
        const val SETTINGS_COLLECTION = "settings"
    }

    object Preferences {
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_FONT_SIZE = "font_size"
        const val KEY_HIGH_CONTRAST = "high_contrast"
        const val KEY_TALKBACK_HINTS = "talkback_hints"
        const val KEY_LANGUAGE = "language"
    }
}
