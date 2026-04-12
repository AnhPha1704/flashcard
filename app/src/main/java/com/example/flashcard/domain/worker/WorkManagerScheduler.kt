package com.example.flashcard.domain.worker

import android.content.Context
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

object WorkManagerScheduler {

    private const val WORK_NAME = "study_reminder_work"

    fun scheduleDailyReminder(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // Tính toán độ trễ để thông báo vào lúc 20:00 (8 giờ tối)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val delay = calendar.timeInMillis - System.currentTimeMillis()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<StudyReminderWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(WORK_NAME)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }
}
