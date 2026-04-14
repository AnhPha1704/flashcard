package com.example.flashcard.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.flashcard.data.local.dao.DeckDao
import com.example.flashcard.data.local.dao.FlashcardDao
import com.example.flashcard.data.local.dao.StudyLogDao
import com.example.flashcard.data.local.entity.Deck
import com.example.flashcard.data.local.entity.Flashcard
import com.example.flashcard.data.local.entity.StudyLog

@Database(entities = [Deck::class, Flashcard::class, StudyLog::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun studyLogDao(): StudyLogDao

    companion object {
        const val DATABASE_NAME = "flashcard_db"
    }
}
