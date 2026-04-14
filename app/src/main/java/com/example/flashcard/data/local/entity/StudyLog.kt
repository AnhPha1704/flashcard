package com.example.flashcard.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

/**
 * Thực thể lưu trữ lịch sử học tập chi tiết.
 * Mỗi lần người dùng nhấn "Thuộc" hoặc "Quên", một bản ghi sẽ được tạo ra.
 */
@Entity(
    tableName = "study_logs",
    indices = [
        Index(value = ["cardId"]),
        Index(value = ["timestamp"], unique = true)
    ]
)
data class StudyLog(
    @PrimaryKey(autoGenerate = true)
    @get:PropertyName("id")
    val id: Int = 0,
    
    @get:PropertyName("cardId")
    val cardId: Int = 0,
    
    @get:PropertyName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    
    @get:PropertyName("quality")
    val quality: Int = 0,
    
    @get:PropertyName("deckId")
    val deckId: Int = 0
)
