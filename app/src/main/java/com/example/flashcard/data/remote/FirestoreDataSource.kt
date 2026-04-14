package com.example.flashcard.data.remote

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.example.flashcard.data.local.entity.Deck
import com.example.flashcard.data.local.entity.Flashcard
import com.example.flashcard.data.local.entity.StudyLog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    private val userDoc: DocumentReference
        get() {
            val uid = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
            return firestore.collection("users").document(uid)
        }

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
        val snapshot = userDoc.collection("decks")
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Deck::class.java)?.copy(id = doc.id.toInt())
        }
    }

    suspend fun getAllFlashcards(deckId: Int): List<Flashcard> {
        val snapshot = userDoc.collection("decks")
            .document(deckId.toString())
            .collection("flashcards")
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Flashcard::class.java)?.copy(id = doc.id.toInt())
        }
    }
}
