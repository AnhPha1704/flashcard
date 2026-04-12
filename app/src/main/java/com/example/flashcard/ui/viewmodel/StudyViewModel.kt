package com.example.flashcard.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcard.data.local.entity.Flashcard
import com.example.flashcard.domain.repository.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val repository: FlashcardRepository
) : ViewModel() {

    private val _cards = MutableStateFlow<List<Flashcard>>(emptyList())
    val cards: StateFlow<List<Flashcard>> = _cards.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isFlipped = MutableStateFlow(false)
    val isFlipped: StateFlow<Boolean> = _isFlipped.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    // Trạng thái tải dữ liệu
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadDeck(deckId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _isCompleted.value = false
            _currentIndex.value = 0
            repository.getFlashcardsByDeck(deckId).collect {
                _cards.value = it
                _isLoading.value = false
            }
        }
    }

    fun flipCard() {
        _isFlipped.value = !_isFlipped.value
    }

    fun nextCard() {
        if (_currentIndex.value < _cards.value.size - 1) {
            _currentIndex.value += 1
            _isFlipped.value = false
        } else {
            _isCompleted.value = true
        }
    }

    fun previousCard() {
        if (_isCompleted.value) {
            _isCompleted.value = false
            return
        }
        if (_currentIndex.value > 0) {
            _currentIndex.value -= 1
            _isFlipped.value = false
        }
    }
    
    // Hàm để reset phiên học
    fun restartSession() {
        _currentIndex.value = 0
        _isFlipped.value = false
        _isCompleted.value = false
    }
}

