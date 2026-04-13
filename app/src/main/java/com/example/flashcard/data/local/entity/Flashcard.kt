package com.example.flashcard.data.local.entity

import androidx.room.*
import com.google.firebase.firestore.PropertyName

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
    @get:PropertyName("id")
    val id: Int = 0,
    
    @get:PropertyName("deckId")
    val deckId: Int = 0,
    
    @get:PropertyName("front")
    val front: String = "",
    
    @get:PropertyName("back")
    val back: String = "",
    
    // SM-2 fields
    @get:PropertyName("interval")
    val interval: Int = 0,
    
    @get:PropertyName("easiness")
    val easiness: Float = 2.5f,
    
    @get:PropertyName("repetitions")
    val repetitions: Int = 0,
    
    @get:PropertyName("nextReview")
    val nextReview: Long = System.currentTimeMillis(),
    
    @get:PropertyName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @get:PropertyName("lastModified")
    val lastModified: Long = System.currentTimeMillis(),
    
    @get:PropertyName("isSynced")
    val isSynced: Boolean = false
)
