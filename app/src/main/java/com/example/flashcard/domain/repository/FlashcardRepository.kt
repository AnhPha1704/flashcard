package com.example.flashcard.domain.repository

import com.example.flashcard.data.local.entity.Deck
import com.example.flashcard.data.local.entity.Flashcard
import kotlinx.coroutines.flow.Flow

interface FlashcardRepository {
    // Deck operations
    fun getAllDecks(): Flow<List<Deck>>
    suspend fun getDeckById(id: Int): Deck?
    suspend fun insertDeck(deck: Deck): Long
    suspend fun updateDeck(deck: Deck): Int
    suspend fun deleteDeck(deck: Deck): Int

    // Flashcard operations
    fun getFlashcardsByDeck(deckId: Int): Flow<List<Flashcard>>
    suspend fun getFlashcardById(id: Int): Flashcard?
    suspend fun insertFlashcard(flashcard: Flashcard): Long
    suspend fun updateFlashcard(flashcard: Flashcard): Int
    suspend fun deleteFlashcard(flashcard: Flashcard): Int
    fun getCardsToReview(deckId: Int, currentTime: Long): Flow<List<Flashcard>>
    suspend fun syncAllData()
}
