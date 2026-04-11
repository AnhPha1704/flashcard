package com.example.flashcard.data.local.dao

import androidx.room.*
import com.example.flashcard.data.local.entity.Deck
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {
    @Query("SELECT * FROM decks ORDER BY createdAt DESC")
    fun getAllDecks(): Flow<List<Deck>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: Deck): Long

    @Update
    suspend fun updateDeck(deck: Deck): Int

    @Delete
    suspend fun deleteDeck(deck: Deck): Int

    @Query("SELECT * FROM decks WHERE id = :id")
    suspend fun getDeckById(id: Int): Deck?
}
