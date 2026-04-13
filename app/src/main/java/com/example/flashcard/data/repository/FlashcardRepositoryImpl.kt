package com.example.flashcard.data.repository

import com.example.flashcard.data.local.dao.DeckDao
import com.example.flashcard.data.local.dao.FlashcardDao
import com.example.flashcard.data.local.dao.StudyLogDao
import com.example.flashcard.data.local.entity.Deck
import com.example.flashcard.data.local.entity.Flashcard
import com.example.flashcard.data.local.entity.StudyLog
import com.example.flashcard.domain.model.DayStudyCount
import com.example.flashcard.domain.model.StatsOverview
import com.example.flashcard.domain.repository.FlashcardRepository
import com.example.flashcard.data.remote.FirestoreDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import android.util.Log
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlashcardRepositoryImpl @Inject constructor(
    private val deckDao: DeckDao,
    private val flashcardDao: FlashcardDao,
    private val studyLogDao: StudyLogDao,
    private val firestoreDataSource: FirestoreDataSource
) : FlashcardRepository {

    // ===== DECK OPERATIONS =====

    override fun getAllDecks(): Flow<List<Deck>> = deckDao.getAllDecks()

    override fun getAllDecksWithCount(): Flow<List<com.example.flashcard.data.local.entity.DeckWithCount>> =
        deckDao.getAllDecksWithCount()

    override suspend fun getDeckById(id: Int): Deck? = deckDao.getDeckById(id)

    override suspend fun insertDeck(deck: Deck): Long {
        val id = deckDao.insertDeck(deck)
        val newDeck = deck.copy(id = id.toInt())
        try {
            firestoreDataSource.syncDeck(newDeck)
            deckDao.updateDeck(newDeck.copy(isSynced = true))
        } catch (e: Exception) {
            Log.e("FlashcardRepo", "Lỗi đồng bộ deck", e)
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
            Log.e("FlashcardRepo", "Lỗi đồng bộ deck", e)
        }
        return result
    }

    override suspend fun deleteDeck(deck: Deck): Int {
        val result = deckDao.deleteDeck(deck)
        try {
            firestoreDataSource.deleteDeck(deck.id)
        } catch (e: Exception) {
            Log.e("FlashcardRepo", "Lỗi đồng bộ xóa deck", e)
        }
        return result
    }

    // ===== FLASHCARD OPERATIONS =====

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
            Log.e("FlashcardRepo", "Lỗi đồng bộ flashcard", e)
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
            Log.e("FlashcardRepo", "Lỗi đồng bộ flashcard", e)
        }
        return result
    }

    override suspend fun deleteFlashcard(flashcard: Flashcard): Int {
        val result = flashcardDao.deleteFlashcard(flashcard)
        try {
            firestoreDataSource.deleteFlashcard(flashcard.deckId, flashcard.id)
        } catch (e: Exception) {
            Log.e("FlashcardRepo", "Lỗi đồng bộ xóa flashcard", e)
        }
        return result
    }

    override fun getCardsToReview(deckId: Int, currentTime: Long): Flow<List<Flashcard>> =
        flashcardDao.getCardsToReview(deckId, currentTime)

    override suspend fun recordStudyEvent(flashcard: Flashcard, quality: Int) {
        // 1. Cập nhật thẻ flashcard (SM-2 logic sẽ được thực hiện ở ViewModel hoặc Repo tùy thiết kế)
        flashcardDao.updateFlashcard(flashcard.copy(lastModified = System.currentTimeMillis()))
        
        // 2. Ghi log học tập
        val log = StudyLog(
            cardId = flashcard.id,
            quality = quality,
            deckId = flashcard.deckId,
            timestamp = System.currentTimeMillis()
        )
        studyLogDao.insertStudyLog(log)
        
        // 3. Đồng bộ (Async)
        try {
            firestoreDataSource.syncFlashcard(flashcard)
            // (Tùy chọn: Đồng bộ cả log lên cloud nếu Firestore hỗ trợ)
        } catch (e: Exception) {
            Log.e("FlashcardRepo", "Lỗi đồng bộ trong recordStudyEvent", e)
        }
    }

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

    // ===== STATISTICS =====

    override fun getStatsOverview(): Flow<StatsOverview> {
        val startOfToday = getStartOfToday()
        return combine(
            flashcardDao.getTotalCardCount(),
            flashcardDao.getEasyCardCount(),
            flashcardDao.getHardCardCount(),
            studyLogDao.getTodayLearnedCount(startOfToday), // Lấy chính xác từ Log
            studyLogDao.getDistinctStudyDays() // Tính Streak từ Log sẽ bền vững hơn
        ) { total, easy, hard, todayLearned, studyDays ->
            val streak = calculateStreak(studyDays)
            StatsOverview(
                totalCards = total,
                easyCards = easy,
                hardCards = hard,
                streak = streak,
                todayStudied = todayLearned,
                dailyGoal = 10
            )
        }
    }

    override fun getStudyHistoryLast7Days(): Flow<List<DayStudyCount>> {
        val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
        return studyLogDao.getStudyHistorySince(sevenDaysAgo).map { list ->
            list.map { DayStudyCount(it.dayTimestamp, it.count) }
        }
    }

    /**
     * Tính số ngày học liên tục.
     * studyDayIndices = danh sách "số ngày kể từ epoch UTC" (lastModified / 86400000),
     * sắp xếp giảm dần (ngày gần nhất trước).
     */
    private fun calculateStreak(studyDayIndices: List<Long>): Int {
        if (studyDayIndices.isEmpty()) return 0
        val todayIndex = System.currentTimeMillis() / 86400000L
        var streak = 0
        var expectedDay = todayIndex
        for (dayIndex in studyDayIndices) {
            when (dayIndex) {
                expectedDay -> {
                    streak++
                    expectedDay--
                }
                todayIndex - 1 -> if (streak == 0) {
                    // Hôm nay chưa học, bắt đầu từ hôm qua
                    streak = 1
                    expectedDay = dayIndex - 1
                } else break
                else -> break
            }
        }
        return streak
    }

    private fun getStartOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
