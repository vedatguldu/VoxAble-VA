package com.voxable.core_database.sync

/**
 * Kuyruklanmış bir sync işleminin türü.
 */
enum class SyncOperation(val value: String) {
    INSERT("insert"),
    UPDATE("update"),
    DELETE("delete")
}
