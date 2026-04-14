package com.example.flashcard.domain.repository

import com.example.flashcard.data.local.entity.Deck
import com.example.flashcard.data.local.entity.Flashcard
import com.example.flashcard.domain.model.StatsOverview
import com.example.flashcard.domain.model.DayStudyCount
import kotlinx.coroutines.flow.Flow

interface FlashcardRepository {
    // Deck operations
    fun getAllDecks(): Flow<List<Deck>>
    fun getAllDecksWithCount(currentTime: Long): Flow<List<com.example.flashcard.data.local.entity.DeckWithCount>>
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
    fun getNewCards(deckId: Int): Flow<List<Flashcard>>
    fun getForgottenCards(deckId: Int): Flow<List<Flashcard>>
    fun getAllCardsToReview(currentTime: Long): Flow<List<Flashcard>>
    fun getNearestUpcomingReview(currentTime: Long): Flow<Long?>
    suspend fun debugMakeCardDue(): Int
    suspend fun recordStudyEvent(flashcard: Flashcard, quality: Int)
    suspend fun syncAllData()

    // Statistics
    fun getStatsOverview(): Flow<StatsOverview>
    fun getStudyHistoryLast7Days(): Flow<List<DayStudyCount>>
    fun getReviewForecast(): Flow<List<DayStudyCount>>
}
