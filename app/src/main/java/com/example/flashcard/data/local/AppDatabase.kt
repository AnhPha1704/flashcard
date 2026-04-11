package com.example.flashcard.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.flashcard.data.local.dao.DeckDao
import com.example.flashcard.data.local.dao.FlashcardDao
import com.example.flashcard.data.local.entity.Deck
import com.example.flashcard.data.local.entity.Flashcard

@Database(entities = [Deck::class, Flashcard::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun flashcardDao(): FlashcardDao

    companion object {
        const val DATABASE_NAME = "flashcard_db"
    }
}
