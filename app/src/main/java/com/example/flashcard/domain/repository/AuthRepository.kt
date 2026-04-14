package com.example.flashcard.domain.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<FirebaseUser?>
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<Unit>
    suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<Unit>
    fun signOut()
}
