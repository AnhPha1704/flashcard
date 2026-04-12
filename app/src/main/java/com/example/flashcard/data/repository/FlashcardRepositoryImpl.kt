package com.example.flashcard.data.repository

import com.example.flashcard.data.local.dao.DeckDao
import com.example.flashcard.data.local.dao.FlashcardDao
import com.example.flashcard.data.local.entity.Deck
import com.example.flashcard.data.local.entity.Flashcard
import com.example.flashcard.domain.repository.FlashcardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlashcardRepositoryImpl @Inject constructor(
    private val deckDao: DeckDao,
    private val flashcardDao: FlashcardDao
) : FlashcardRepository {

    // Deck operations
    override fun getAllDecks(): Flow<List<Deck>> = deckDao.getAllDecks()
    override fun getAllDecksWithCount(): Flow<List<com.example.flashcard.data.local.entity.DeckWithCount>> = deckDao.getAllDecksWithCount()

    override suspend fun getDeckById(id: Int): Deck? = deckDao.getDeckById(id)

    override suspend fun insertDeck(deck: Deck): Long = deckDao.insertDeck(deck)

    override suspend fun updateDeck(deck: Deck): Int = deckDao.updateDeck(deck)

    override suspend fun deleteDeck(deck: Deck): Int = deckDao.deleteDeck(deck)

    // Flashcard operations
    override fun getFlashcardsByDeck(deckId: Int): Flow<List<Flashcard>> = 
        flashcardDao.getFlashcardsByDeck(deckId)

    override suspend fun getFlashcardById(id: Int): Flashcard? = 
        flashcardDao.getFlashcardById(id)

    override suspend fun insertFlashcard(flashcard: Flashcard): Long = 
        flashcardDao.insertFlashcard(flashcard)

    override suspend fun updateFlashcard(flashcard: Flashcard): Int = 
        flashcardDao.updateFlashcard(flashcard)

    override suspend fun deleteFlashcard(flashcard: Flashcard): Int = 
        flashcardDao.deleteFlashcard(flashcard)

    override fun getCardsToReview(deckId: Int, currentTime: Long): Flow<List<Flashcard>> = 
        flashcardDao.getCardsToReview(deckId, currentTime)
}
