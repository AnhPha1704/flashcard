package com.example.flashcard.domain.model

/** Số thẻ học theo từng ngày (dùng cho biểu đồ 7 ngày) */
data class DayStudyCount(
    val dayDate: String, // Định dạng YYYY-MM-DD
    val count: Int
)
