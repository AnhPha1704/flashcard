package com.example.flashcard.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.flashcard.data.local.dao.DeckDao
import com.example.flashcard.data.local.dao.FlashcardDao
import com.example.flashcard.data.remote.FirestoreDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val deckDao: DeckDao,
    private val flashcardDao: FlashcardDao,
    private val firestoreDataSource: FirestoreDataSource
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("SyncWorker", "Bắt đầu chạy SyncWorker: Đồng bộ dữ liệu ngoại tuyến...")

            // Lấy danh sách Decks cần đồng bộ
            val unsyncedDecks = deckDao.getUnsyncedDecks()
            for (deck in unsyncedDecks) {
                try {
                    firestoreDataSource.syncDeck(deck)
                    deckDao.updateDeck(deck.copy(isSynced = true))
                    Log.d("SyncWorker", "Đã đồng bộ Deck: ${deck.name}")
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Lỗi đồng bộ Deck id=${deck.id}", e)
                    return@withContext Result.retry()
                }
            }

            // Lấy danh sách Flashcards cần đồng bộ
            val unsyncedFlashcards = flashcardDao.getUnsyncedFlashcards()
            for (flashcard in unsyncedFlashcards) {
                try {
                    firestoreDataSource.syncFlashcard(flashcard)
                    flashcardDao.updateFlashcard(flashcard.copy(isSynced = true))
                    Log.d("SyncWorker", "Đã đồng bộ Flashcard: ${flashcard.front}")
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Lỗi đồng bộ Flashcard id=${flashcard.id}", e)
                    return@withContext Result.retry()
                }
            }

            Log.d("SyncWorker", "Hoàn tất đồng bộ toàn bộ dữ liệu ngoại tuyến.")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Lỗi trong quá trình chạy SyncWorker", e)
            Result.retry()
        }
    }
}
