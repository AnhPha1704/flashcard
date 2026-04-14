package com.example.flashcard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcard.domain.repository.AuthRepository
import com.example.flashcard.domain.repository.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val flashcardRepository: FlashcardRepository
) : ViewModel() {

    val currentUser = authRepository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepository.signInWithEmailAndPassword(email, pass)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Đăng nhập thất bại"
            }
            _isLoading.value = false
        }
    }

    fun signUp(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepository.signUpWithEmailAndPassword(email, pass)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Đăng ký thất bại"
            }
            _isLoading.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            flashcardRepository.clearLocalData()
            authRepository.signOut()
        }
    }

    fun clearError() {
        _error.value = null
    }
}
