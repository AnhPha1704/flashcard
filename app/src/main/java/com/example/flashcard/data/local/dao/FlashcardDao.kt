package com.example.flashcard.data.local.dao

import androidx.room.*
import com.example.flashcard.data.local.entity.Flashcard
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards WHERE deckId = :deckId ORDER BY createdAt DESC")
    fun getFlashcardsByDeck(deckId: Int): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard): Long

    @Update
    suspend fun updateFlashcard(flashcard: Flashcard): Int

    @Delete
    suspend fun deleteFlashcard(flashcard: Flashcard): Int

    @Query("SELECT * FROM flashcards WHERE id = :id")
    suspend fun getFlashcardById(id: Int): Flashcard?

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId AND nextReview <= :currentTime")
    fun getCardsToReview(deckId: Int, currentTime: Long): Flow<List<Flashcard>>
}
