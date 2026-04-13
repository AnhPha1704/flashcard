package com.example.flashcard.data.remote

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.example.flashcard.data.local.entity.Deck
import com.example.flashcard.data.local.entity.Flashcard
import com.example.flashcard.data.local.entity.StudyLog
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) {
    @SuppressLint("HardwareIds")
    private val deviceId = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    ) ?: "unknown_device"

    private val userDoc = firestore.collection("users").document(deviceId)

    suspend fun syncDeck(deck: Deck) {
        userDoc.collection("decks")
            .document(deck.id.toString())
            .set(deck)
            .await()
    }

    suspend fun syncFlashcard(flashcard: Flashcard) {
        userDoc.collection("decks")
            .document(flashcard.deckId.toString())
            .collection("flashcards")
            .document(flashcard.id.toString())
            .set(flashcard)
            .await()
    }

    suspend fun syncStudyLog(log: StudyLog) {
        userDoc.collection("study_logs")
            .document(log.timestamp.toString()) // Sử dụng timestamp làm ID duy nhất cho log
            .set(log)
            .await()
    }

    // Xóa dữ liệu trên Cloud nếu cần (hỗ trợ sync ngược)
    suspend fun deleteDeck(deckId: Int) {
        userDoc.collection("decks")
            .document(deckId.toString())
            .delete()
            .await()
    }

    suspend fun deleteFlashcard(deckId: Int, flashcardId: Int) {
        userDoc.collection("decks")
            .document(deckId.toString())
            .collection("flashcards")
            .document(flashcardId.toString())
            .delete()
            .await()
    }

    suspend fun getAllDecks(): List<Deck> {
        return userDoc.collection("decks")
            .get()
            .await()
            .toObjects(Deck::class.java)
    }

    suspend fun getAllFlashcards(deckId: Int): List<Flashcard> {
        return userDoc.collection("decks")
            .document(deckId.toString())
            .collection("flashcards")
            .get()
            .await()
            .toObjects(Flashcard::class.java)
    }
}
