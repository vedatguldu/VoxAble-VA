package com.voxable.core_database.sync

/**
 * Bir sync işleminin olası durumları.
 */
enum class SyncQueueStatus {
    /** Kuyrukta bekliyor, henüz işlenmedi */
    PENDING,
    /** Şu an gönderilmeye çalışılıyor */
    IN_PROGRESS,
    /** Başarıyla tamamlandı */
    COMPLETED,
    /** Tüm denemeler başarısız oldu */
    FAILED,
    /** Sunucu verisi ile çakışma tespit edildi */
    CONFLICT,
    /** İşlem iptal edildi veya gereksiz hale geldi */
    CANCELLED
}
