package com.example.flashcard.data.repository

import com.example.flashcard.data.local.dao.DeckDao
import com.example.flashcard.data.local.dao.FlashcardDao
import com.example.flashcard.data.local.entity.Deck
import com.example.flashcard.data.local.entity.Flashcard
import com.example.flashcard.domain.repository.FlashcardRepository
import com.example.flashcard.data.remote.FirestoreDataSource
import kotlinx.coroutines.flow.Flow
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlashcardRepositoryImpl @Inject constructor(
    private val deckDao: DeckDao,
    private val flashcardDao: FlashcardDao,
    private val firestoreDataSource: FirestoreDataSource
) : FlashcardRepository {

    // Deck operations
    override fun getAllDecks(): Flow<List<Deck>> = deckDao.getAllDecks()

    override suspend fun getDeckById(id: Int): Deck? = deckDao.getDeckById(id)

    override suspend fun insertDeck(deck: Deck): Long {
        val id = deckDao.insertDeck(deck)
        val newDeck = deck.copy(id = id.toInt())
        try {
            firestoreDataSource.syncDeck(newDeck)
            deckDao.updateDeck(newDeck.copy(isSynced = true))
        } catch (e: Exception) {
            Log.e("FlashcardRepo", "Lỗi đồng bộ", e)
        }
        return id
    }

    override suspend fun updateDeck(deck: Deck): Int {
        val lastModifiedDeck = deck.copy(lastModified = System.currentTimeMillis(), isSynced = false)
        val result = deckDao.updateDeck(lastModifiedDeck)
        try {
            firestoreDataSource.syncDeck(lastModifiedDeck)
            deckDao.updateDeck(lastModifiedDeck.copy(isSynced = true))
        } catch (e: Exception) {
            Log.e("FlashcardRepo", "Lỗi đồng bộ", e)
        }
        return result
    }

    override suspend fun deleteDeck(deck: Deck): Int {
        val result = deckDao.deleteDeck(deck)
        try {
            firestoreDataSource.deleteDeck(deck.id)
        } catch (e: Exception) {
            Log.e("FlashcardRepo", "Lỗi đồng bộ", e)
        }
        return result
    }

    // Flashcard operations
    override fun getFlashcardsByDeck(deckId: Int): Flow<List<Flashcard>> = 
        flashcardDao.getFlashcardsByDeck(deckId)

    override suspend fun getFlashcardById(id: Int): Flashcard? = 
        flashcardDao.getFlashcardById(id)

    override suspend fun insertFlashcard(flashcard: Flashcard): Long {
        val id = flashcardDao.insertFlashcard(flashcard)
        val newCard = flashcard.copy(id = id.toInt())
        try {
            firestoreDataSource.syncFlashcard(newCard)
            flashcardDao.updateFlashcard(newCard.copy(isSynced = true))
        } catch (e: Exception) {
            Log.e("FlashcardRepo", "Lỗi đồng bộ", e)
        }
        return id
    }

    override suspend fun updateFlashcard(flashcard: Flashcard): Int {
        val lastModifiedCard = flashcard.copy(lastModified = System.currentTimeMillis(), isSynced = false)
        val result = flashcardDao.updateFlashcard(lastModifiedCard)
        try {
            firestoreDataSource.syncFlashcard(lastModifiedCard)
            flashcardDao.updateFlashcard(lastModifiedCard.copy(isSynced = true))
        } catch (e: Exception) {
            Log.e("FlashcardRepo", "Lỗi đồng bộ", e)
        }
        return result
    }

    override suspend fun deleteFlashcard(flashcard: Flashcard): Int {
        val result = flashcardDao.deleteFlashcard(flashcard)
        try {
            firestoreDataSource.deleteFlashcard(flashcard.deckId, flashcard.id)
        } catch (e: Exception) {
            Log.e("FlashcardRepo", "Lỗi đồng bộ", e)
        }
        return result
    }

    override fun getCardsToReview(deckId: Int, currentTime: Long): Flow<List<Flashcard>> = 
        flashcardDao.getCardsToReview(deckId, currentTime)

    override suspend fun syncAllData() {
        try {
            val cloudDecks = firestoreDataSource.getAllDecks()
            for (cloudDeck in cloudDecks) {
                val localDeck = deckDao.getDeckById(cloudDeck.id)
                // Nếu local chưa có HOẶC bản trên cloud mới hơn bản local
                if (localDeck == null || cloudDeck.lastModified > localDeck.lastModified) {
                    deckDao.insertDeck(cloudDeck.copy(isSynced = true))
                }

                // Đồng bộ Flashcards cho từng Deck
                val cloudCards = firestoreDataSource.getAllFlashcards(cloudDeck.id)
                for (cloudCard in cloudCards) {
                    val localCard = flashcardDao.getFlashcardById(cloudCard.id)
                    if (localCard == null || cloudCard.lastModified > localCard.lastModified) {
                        flashcardDao.insertFlashcard(cloudCard.copy(isSynced = true))
                    }
                }
            }
            Log.d("FlashcardRepo", "Đồng bộ hoàn tất thành công")
        } catch (e: Exception) {
            Log.e("FlashcardRepo", "Lỗi trong quá trình đồng bộ toàn diện", e)
        }
    }
}
