package com.example.flashcard.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = Deck::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["deckId"])]
)
data class Flashcard(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val deckId: Int,
    val front: String,
    val back: String,
    // SM-2 fields
    val interval: Int = 0,
    val easiness: Float = 2.5f,
    val repetitions: Int = 0,
    val nextReview: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
