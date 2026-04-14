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

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val flashcardRepository: FlashcardRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    
    val deviceId: String
        get() {
            var id = sharedPrefs.getString("device_id", null)
            if (id == null) {
                id = UUID.randomUUID().toString()
                sharedPrefs.edit().putString("device_id", id).apply()
            }
            return id
        }

    private val _sessionExpired = MutableStateFlow(false)
    val sessionExpired = _sessionExpired.asStateFlow()

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
            if (result.isSuccess) {
                // Cập nhật Session ID lên Firestore
                flashcardRepository.updateSessionId(deviceId)
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Đăng nhập thất bại"
            }
            _isLoading.value = false
        }
    }

    fun claimSession() {
        if (currentUser.value != null) {
            viewModelScope.launch {
                try {
                    flashcardRepository.updateSessionId(deviceId)
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    fun signUp(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepository.signUpWithEmailAndPassword(email, pass)
            if (result.isSuccess) {
                // Cập nhật Session ID cho tài khoản mới
                flashcardRepository.updateSessionId(deviceId)
            } else {
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

    fun notifySessionExpired() {
        _sessionExpired.value = true
    }

    fun clearSessionExpired() {
        _sessionExpired.value = false
    }

    fun clearError() {
        _error.value = null
    }
}
