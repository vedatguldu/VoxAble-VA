package com.voxable.core_database.sync

/**
 * Çakışma çözüm stratejisi.
 *
 * LATEST_WINS  → updatedAt timestamp'e göre hangisi daha yeniyse kazanır
 * SERVER_WINS  → Server verisi her zaman önceliklidir (salt okunur cihaz modeli)
 * CLIENT_WINS  → Local veri her zaman önceliklidir (optimistic local model)
 * MANUAL       → Çakışma işaretlenir, kullanıcı çözüm sağlar
 */
enum class ConflictResolutionStrategy {
    LATEST_WINS,
    SERVER_WINS,
    CLIENT_WINS,
    MANUAL
}
