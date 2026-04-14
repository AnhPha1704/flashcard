package com.example.flashcard.di

import android.content.Context
import com.example.flashcard.domain.util.TtsHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TtsModule {

    @Provides
    @Singleton
    fun provideTtsHelper(@ApplicationContext context: Context): TtsHelper {
        return TtsHelper(context)
    }
}
