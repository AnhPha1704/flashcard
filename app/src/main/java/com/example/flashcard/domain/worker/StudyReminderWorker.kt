package com.example.flashcard.domain.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.flashcard.domain.repository.FlashcardRepository
import com.example.flashcard.domain.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class StudyReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: FlashcardRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val decks = repository.getAllDecks().first()
        
        if (decks.isNotEmpty()) {
            val notificationHelper = NotificationHelper(applicationContext)
            notificationHelper.showStudyReminder(
                title = "Đến giờ ôn tập rồi! 🚀",
                message = "Bạn có ${decks.size} bộ thẻ đang chờ bạn chinh phục. Đừng để kiến thức bị lãng quên!"
            )
        }

        return Result.success()
    }
}
