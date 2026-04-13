package com.example.flashcard.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "decks")
data class Deck(
    @PrimaryKey(autoGenerate = true)
    @get:PropertyName("id")
    val id: Int = 0,
    
    @get:PropertyName("name")
    val name: String = "",
    
    @get:PropertyName("description")
    val description: String? = null,
    
    @get:PropertyName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @get:PropertyName("lastModified")
    val lastModified: Long = System.currentTimeMillis(),
    
    @get:PropertyName("isSynced")
    val isSynced: Boolean = false
)
