package com.example.flashcard.data.local.dao

import androidx.room.*
import com.example.flashcard.data.local.entity.Deck
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {
    @Query("SELECT * FROM decks ORDER BY createdAt DESC")
    fun getAllDecks(): Flow<List<Deck>>

    @Query("""
        SELECT decks.*, 
        (SELECT COUNT(id) FROM flashcards WHERE deckId = decks.id) as totalCount,
        (SELECT COUNT(id) FROM flashcards WHERE deckId = decks.id AND repetitions > 0 AND nextReview <= :currentTime) as dueCount,
        (SELECT COUNT(id) FROM flashcards WHERE deckId = decks.id AND repetitions = 0 AND lastModified == createdAt) as newCount,
        (SELECT COUNT(id) FROM flashcards WHERE deckId = decks.id AND repetitions = 0 AND lastModified > createdAt) as forgottenCount
        FROM decks ORDER BY createdAt DESC
    """)
    fun getAllDecksWithCount(currentTime: Long): Flow<List<com.example.flashcard.data.local.entity.DeckWithCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: Deck): Long

    @Update
    suspend fun updateDeck(deck: Deck): Int

    @Delete
    suspend fun deleteDeck(deck: Deck): Int

    @Query("SELECT * FROM decks WHERE id = :id")
    suspend fun getDeckById(id: Int): Deck?

    @Query("SELECT * FROM decks WHERE isSynced = 0")
    suspend fun getUnsyncedDecks(): List<Deck>
}
