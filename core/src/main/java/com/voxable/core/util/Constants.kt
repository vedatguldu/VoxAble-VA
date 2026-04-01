package com.voxable.core.util

object Constants {
    const val DATABASE_NAME = "voxable_database"
    const val DATASTORE_NAME = "voxable_preferences"
    const val BASE_URL = "https://api.voxable.com/"

    object Firebase {
        const val USERS_COLLECTION = "users"
        const val SETTINGS_COLLECTION = "settings"
        const val DOWNLOADS_COLLECTION = "downloads"
        const val SYNC_METADATA_COLLECTION = "sync_metadata"
    }

    object Preferences {
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_FONT_SIZE = "font_size"
        const val KEY_HIGH_CONTRAST = "high_contrast"
        const val KEY_TALKBACK_HINTS = "talkback_hints"
        const val KEY_LANGUAGE = "language"

        // Voice settings
        const val KEY_VOICE_ENABLED = "voice_enabled"
        const val KEY_VOICE_SPEED = "voice_speed"
        const val KEY_VOICE_PITCH = "voice_pitch"
        const val KEY_AUTO_READ = "auto_read"

        // Accessibility settings
        const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        const val KEY_REDUCE_MOTION = "reduce_motion"
        const val KEY_SCREEN_READER_OPT = "screen_reader_optimization"
        const val KEY_TOUCH_TARGET_SIZE = "touch_target_size"
    }

    object Sync {
        const val SYNC_WORKER_NAME = "voxable_periodic_sync"
        const val IMMEDIATE_SYNC_WORKER_NAME = "voxable_immediate_sync"
        const val SYNC_INTERVAL_MINUTES = 15L
        const val MAX_RETRY_COUNT = 3
        const val INITIAL_BACKOFF_SECONDS = 30L

        // Entity types
        const val ENTITY_USER = "user"
        const val ENTITY_DOWNLOAD = "download"
        const val ENTITY_SETTINGS = "settings"

        // Firestore document fields
        const val FIELD_VERSION = "version"
        const val FIELD_UPDATED_AT = "updatedAt"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_CLIENT_ID = "clientId"
        const val FIELD_ENTITY_TYPE = "entityType"
    }
}
