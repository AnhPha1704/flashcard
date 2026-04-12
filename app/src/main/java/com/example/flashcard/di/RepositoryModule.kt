package com.example.flashcard.di

import android.content.Context
import com.example.flashcard.data.repository.FlashcardRepositoryImpl
import com.example.flashcard.data.util.NetworkConnectivityObserver
import com.example.flashcard.domain.repository.FlashcardRepository
import com.example.flashcard.domain.util.ConnectivityObserver
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFlashcardRepository(
        flashcardRepositoryImpl: FlashcardRepositoryImpl
    ): FlashcardRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: com.example.flashcard.data.repository.AuthRepositoryImpl
    ): com.example.flashcard.domain.repository.AuthRepository

    companion object {
        @Provides
        @Singleton
        fun provideConnectivityObserver(
            @ApplicationContext context: Context
        ): ConnectivityObserver {
            return NetworkConnectivityObserver(context)
        }
    }
}
