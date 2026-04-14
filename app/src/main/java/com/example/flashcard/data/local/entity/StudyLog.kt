package com.example.flashcard.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Thực thể lưu trữ lịch sử học tập chi tiết.
 * Mỗi lần người dùng nhấn "Thuộc" hoặc "Quên", một bản ghi sẽ được tạo ra.
 */
@Entity(
    tableName = "study_logs",
    indices = [Index(value = ["cardId"])]
)
data class StudyLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cardId: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val quality: Int, // 1: Thuộc (Easy), 0: Quên (Hard)
    val deckId: Int // Lưu thêm deckId để dễ dàng truy vấn thống kê theo bộ thẻ nếu cần
)
