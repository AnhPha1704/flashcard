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
import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.flashcard.data.worker.SyncWorker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlashcardRepositoryImpl @Inject constructor(
    private val deckDao: DeckDao,
    private val flashcardDao: FlashcardDao,
    private val studyLogDao: StudyLogDao,
    private val firestoreDataSource: FirestoreDataSource,
    @ApplicationContext private val context: Context
) : FlashcardRepository {

    // ===== DECK OPERATIONS =====

    override fun getAllDecks(): Flow<List<Deck>> = deckDao.getAllDecks()

    override fun getAllDecksWithCount(currentTime: Long): Flow<List<com.example.flashcard.data.local.entity.DeckWithCount>> =
        deckDao.getAllDecksWithCount(currentTime)

    override suspend fun getDeckById(id: Int): Deck? = deckDao.getDeckById(id)

    override suspend fun insertDeck(deck: Deck): Long {
        val id = deckDao.insertDeck(deck)
        val newDeck = deck.copy(id = id.toInt())
        try {
            firestoreDataSource.syncDeck(newDeck)
            deckDao.updateDeck(newDeck.copy(isSynced = true))
        } catch (e: Exception) {
            Log.e("FlashcardRepo", "Lỗi đồng bộ", e)
            scheduleSyncWorker()
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
            scheduleSyncWorker()
        }
        return result
    }

    override suspend fun deleteDeck(deck: Deck): Int {
        val result = deckDao.deleteDeck(deck)
        try {
            firestoreDataSource.deleteDeck(deck.id)
        } catch (e: Exception) {
            Log.e("FlashcardRepo", "Lỗi đồng bộ", e)
            scheduleSyncWorker()
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
            Log.e("FlashcardRepo", "Lỗi đồng bộ", e)
            scheduleSyncWorker()
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
            scheduleSyncWorker()
        }
        return result
    }

    override suspend fun deleteFlashcard(flashcard: Flashcard): Int {
        val result = flashcardDao.deleteFlashcard(flashcard)
        try {
            firestoreDataSource.deleteFlashcard(flashcard.deckId, flashcard.id)
        } catch (e: Exception) {
            Log.e("FlashcardRepo", "Lỗi đồng bộ", e)
            scheduleSyncWorker()
        }
        return result
    }

    override fun getCardsToReview(deckId: Int, currentTime: Long): Flow<List<Flashcard>> =
        flashcardDao.getCardsToReview(deckId, currentTime)

    override fun getNewCards(deckId: Int): Flow<List<Flashcard>> =
        flashcardDao.getNewCards(deckId)

    override fun getForgottenCards(deckId: Int): Flow<List<Flashcard>> =
        flashcardDao.getForgottenCards(deckId)

    override suspend fun recordStudyEvent(flashcard: Flashcard, quality: Int) {
        // 1. Tính toán logic SM2 thông qua lớp tiện ích
        val updatedCard = com.example.flashcard.utils.SM2Logic.calculate(flashcard, quality)
        
        // 2. Cập nhật thẻ flashcard vào database địa phương
        flashcardDao.updateFlashcard(updatedCard)
        
        // 3. Ghi log học tập
        val log = StudyLog(
            cardId = flashcard.id,
            quality = quality,
            deckId = flashcard.deckId,
            timestamp = System.currentTimeMillis()
        )
        studyLogDao.insertStudyLog(log)
        
        // 4. Đồng bộ (Async)
        try {
            firestoreDataSource.syncFlashcard(updatedCard)
        } catch (e: Exception) {
            Log.e("FlashcardRepo", "Lỗi đồng bộ trong recordStudyEvent", e)
            scheduleSyncWorker()
        }
    }

    override suspend fun syncAllData() {
        try {
            val cloudDecks = firestoreDataSource.getAllDecks()
            for (cloudDeck in cloudDecks) {
                val localDeck = deckDao.getDeckById(cloudDeck.id)
                if (localDeck == null || cloudDeck.lastModified > localDeck.lastModified) {
                    deckDao.insertDeck(cloudDeck.copy(isSynced = true))
                }
                val cloudCards = firestoreDataSource.getAllFlashcards(cloudDeck.id)
                Log.d("Sync", "Tải được ${cloudCards.size} thẻ từ Cloud cho bộ ${cloudDeck.id}")
                for (cloudCard in cloudCards) {
                    val localCard = flashcardDao.getFlashcardById(cloudCard.id)
                    
                    // Dự đoán thẻ cần cập nhật: Đảm bảo deckId luôn chính xác
                    val cardWithCorrectDeck = cloudCard.copy(
                        deckId = cloudDeck.id,
                        isSynced = true
                    )

                    if (localCard == null || 
                        cloudCard.lastModified > localCard.lastModified ||
                        localCard.deckId != cloudDeck.id // Trường hợp khẩn cấp: Sửa deckId bị lỗi
                    ) {
                        Log.d("Sync", "Đang cập nhật/chèn thẻ ID: ${cloudCard.id}")
                        flashcardDao.insertFlashcard(cardWithCorrectDeck)
                    }
                }
            }
            Log.d("FlashcardRepo", "Đồng bộ hoàn tất thành công")
        } catch (e: Exception) {
            Log.e("FlashcardRepo", "Lỗi trong quá trình đồng bộ toàn diện", e)
            scheduleSyncWorker()
        }
    }

    // ===== STATISTICS =====

    override fun getStatsOverview(): Flow<StatsOverview> {
        val startOfToday = getStartOfToday()
        return combine(
            flashcardDao.getTotalCardCount(),
            flashcardDao.getEasyCardCount(),
            flashcardDao.getHardCardCount(),
            studyLogDao.getTodayLearnedCount(startOfToday), 
            studyLogDao.getDistinctStudyDays() 
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
            list.map { DayStudyCount(it.dayString, it.count) }
        }
    }

    private fun calculateStreak(studyDates: List<String>): Int {
        if (studyDates.isEmpty()) return 0
        
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val today = sdf.format(java.util.Date())
        val calendar = java.util.Calendar.getInstance()
        
        var streak = 0
        var currentDateStr = today
        
        // Kiểm tra xem có học hôm nay không
        if (studyDates.contains(today)) {
            streak = 1
            calendar.time = sdf.parse(today)!!
        } else {
            // Nếu không học hôm nay, kiểm tra xem có học hôm qua không để giữ streak
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
            val yesterday = sdf.format(calendar.time)
            if (studyDates.contains(yesterday)) {
                streak = 0 // Streak vẫn còn hiệu lực nhưng không tăng cho đến khi học hôm nay
                currentDateStr = yesterday
            } else {
                return 0
            }
        }
        
        // Đếm ngược về quá khứ
        while (true) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
            val prevDateStr = sdf.format(calendar.time)
            if (studyDates.contains(prevDateStr)) {
                streak++
            } else {
                break
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

    private fun scheduleSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
            
        WorkManager.getInstance(context).enqueue(syncWorkRequest)
        Log.d("FlashcardRepo", "Đã lên lịch SyncWorker để đồng bộ lại sau.")
    }
}
