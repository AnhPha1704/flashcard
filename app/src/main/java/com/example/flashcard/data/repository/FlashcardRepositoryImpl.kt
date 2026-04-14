package com.example.flashcard.data.repository

import com.example.flashcard.data.local.AppDatabase
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
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
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
    private val database: AppDatabase,
    @ApplicationContext private val context: Context
) : FlashcardRepository {

    private var isSyncing = false

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

    private suspend fun updateDeckUnsync(deck: Deck) {
        val lastModifiedDeck = deck.copy(lastModified = System.currentTimeMillis(), isSynced = false)
        deckDao.updateDeck(lastModifiedDeck)
        try {
            firestoreDataSource.syncDeck(lastModifiedDeck)
            deckDao.updateDeck(lastModifiedDeck.copy(isSynced = true))
        } catch (e: Exception) {
            scheduleSyncWorker()
        }
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
            
            // Cập nhật lastModified của Deck để kích hoạt Real-time sync
            getDeckById(flashcard.deckId)?.let { deck ->
                updateDeck(deck)
            }
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
            
            // Cập nhật lastModified của Deck để kích hoạt Real-time sync
            getDeckById(flashcard.deckId)?.let { deck ->
                updateDeck(deck)
            }
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
            
            // Cập nhật lastModified của Deck để kích hoạt Real-time sync
            getDeckById(flashcard.deckId)?.let { deck ->
                updateDeckUnsync(deck) // Dùng hàm mới để tránh vòng lặp delete/update vô tận
            }
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

    override fun getAllCardsToReview(currentTime: Long): Flow<List<Flashcard>> =
        flashcardDao.getAllCardsToReview(currentTime)

    override fun getNearestUpcomingReview(currentTime: Long): Flow<Long?> =
        flashcardDao.getNearestUpcomingReview(currentTime)

    override suspend fun debugMakeCardDue(): Int {
        return flashcardDao.makeNearestCardDue(System.currentTimeMillis())
    }

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
            firestoreDataSource.syncStudyLog(log)
            
            // Cập nhật lastModified của Deck để kích hoạt Real-time sync
            getDeckById(flashcard.deckId)?.let { deck ->
                updateDeck(deck)
            }
        } catch (e: Exception) {
            Log.e("FlashcardRepo", "Lỗi đồng bộ trong recordStudyEvent", e)
            scheduleSyncWorker()
        }
    }

    override suspend fun syncAllData() {
        if (isSyncing) return
        isSyncing = true
        try {
            Log.d("Sync", "Bắt đầu đồng bộ hóa toàn diện...")
            val cloudDecks = firestoreDataSource.getAllDecks()
            val localDecks = deckDao.getAllDecks().first()

            // 1. Xử lý xóa Deck (Local có nhưng Cloud không có)
            val cloudDeckIds = cloudDecks.map { it.id }.toSet()
            val decksToDelete = localDecks.filter { it.id !in cloudDeckIds }
            for (deck in decksToDelete) {
                Log.d("Sync", "Xóa bộ thẻ local không còn trên Cloud: ${deck.id}")
                deckDao.deleteDeck(deck)
            }

            // 2. Cập nhật/Thêm Deck và Flashcards
            for (cloudDeck in cloudDecks) {
                val localDeck = localDecks.find { it.id == cloudDeck.id }
                if (localDeck == null || cloudDeck.lastModified > localDeck.lastModified) {
                    deckDao.insertDeck(cloudDeck.copy(isSynced = true))
                }

                // Đồng bộ Flashcards cho bộ thẻ này
                val cloudCards = firestoreDataSource.getAllFlashcards(cloudDeck.id)
                val localCards = flashcardDao.getFlashcardsByDeck(cloudDeck.id).first()

                // Xóa Flashcards local không còn trên Cloud
                val cloudCardIds = cloudCards.map { it.id }.toSet()
                val cardsToDelete = localCards.filter { it.id !in cloudCardIds }
                for (card in cardsToDelete) {
                    flashcardDao.deleteFlashcard(card)
                }

                // Cập nhật/Thêm Flashcards
                for (cloudCard in cloudCards) {
                    val localCard = localCards.find { it.id == cloudCard.id }
                    val cardWithCorrectDeck = cloudCard.copy(deckId = cloudDeck.id, isSynced = true)

                    if (localCard == null || cloudCard.lastModified > localCard.lastModified) {
                        flashcardDao.insertFlashcard(cardWithCorrectDeck)
                    }
                }
            }

            // 3. Đồng bộ Study Logs nội bộ
            Log.d("Sync", "Bắt đầu đồng bộ Study Logs...")
            val cloudLogs = firestoreDataSource.getAllStudyLogs()
            studyLogDao.insertStudyLogs(cloudLogs)

            Log.d("FlashcardRepo", "Đồng bộ hoàn tất thành công")
        } catch (e: Exception) {
            Log.e("FlashcardRepo", "Lỗi trong quá trình đồng bộ toàn diện", e)
            scheduleSyncWorker()
        } finally {
            isSyncing = false
        }
    }

    override fun listenToRealtimeUpdates(): Flow<Unit> = callbackFlow {
        val jobs = listOf(
            launch {
                firestoreDataSource.getDecksFlow().collect {
                    syncAllData()
                    trySend(Unit)
                }
            },
            launch {
                firestoreDataSource.getStudyLogsFlow().collect {
                    // Khi có log mới từ máy khác, cập nhật local để giữ streak đồng bộ
                    syncAllData()
                    trySend(Unit)
                }
            }
        )
        awaitClose { jobs.forEach { it.cancel() } }
    }

    override suspend fun clearLocalData() {
        try {
            database.clearAllTables()
            Log.d("FlashcardRepo", "Đã xóa sạch dữ liệu local")
        } catch (e: Exception) {
            Log.e("FlashcardRepo", "Lỗi khi xóa dữ liệu local", e)
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

    override fun getReviewForecast(): Flow<List<DayStudyCount>> {
        val startOfToday = getStartOfToday()
        return flashcardDao.getReviewForecast(startOfToday).map { list ->
            list.map { DayStudyCount(it.dayString, it.count) }
        }
    }

    override suspend fun updateSessionId(sessionId: String) {
        firestoreDataSource.updateSessionId(sessionId)
    }

    override fun getSessionIdFlow(): Flow<String?> = firestoreDataSource.getSessionIdFlow()

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
