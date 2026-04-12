package com.example.flashcard.data.remote

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.example.flashcard.data.local.entity.Deck
import com.example.flashcard.data.local.entity.Flashcard
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
