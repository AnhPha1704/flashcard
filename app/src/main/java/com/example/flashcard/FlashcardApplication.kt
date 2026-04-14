package com.example.flashcard

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.flashcard.domain.util.TtsHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FlashcardApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // Inject sớm để engine TTS khởi động ngay khi App mở, tránh độ trễ khi học
    @Inject
    lateinit var ttsHelper: TtsHelper

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
