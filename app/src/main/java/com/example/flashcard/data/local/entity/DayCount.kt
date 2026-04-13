package com.example.flashcard.data.local.entity

import androidx.room.ColumnInfo

/** Kết quả truy vấn tổng hợp từ Room: số thẻ học theo từng ngày */
data class DayCount(
    @ColumnInfo(name = "dayTimestamp") val dayTimestamp: Long,
    @ColumnInfo(name = "count") val count: Int
)
