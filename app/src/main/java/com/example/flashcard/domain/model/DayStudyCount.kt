package com.example.flashcard.domain.model

/** Số thẻ học theo từng ngày (dùng cho biểu đồ 7 ngày) */
data class DayStudyCount(
    val dayTimestamp: Long, // UTC midnight timestamp
    val count: Int
)
