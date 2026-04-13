package com.example.flashcard.ui.viewmodel

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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Đếm số thẻ đã thuộc/cần ôn trong phiên này
    private val _sessionLearnedCount = MutableStateFlow(0)
    val sessionLearnedCount: StateFlow<Int> = _sessionLearnedCount.asStateFlow()

    private val _sessionReviewCount = MutableStateFlow(0)
    val sessionReviewCount: StateFlow<Int> = _sessionReviewCount.asStateFlow()

    fun loadDeck(deckId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _isCompleted.value = false
            _currentIndex.value = 0
            _sessionLearnedCount.value = 0
            _sessionReviewCount.value = 0
            repository.getFlashcardsByDeck(deckId).collect {
                _cards.value = it
                _isLoading.value = false
            }
        }
    }

    fun flipCard() {
        _isFlipped.value = !_isFlipped.value
    }

    /**
     * Đánh dấu thẻ là ĐÃ THUỘC (Easy):
     * - Tăng repetitions lên 1
     * - Cập nhật lastModified → thống kê streak/biểu đồ sẽ ghi nhận hôm nay
     */
    fun swipeLearned() {
        val index = _currentIndex.value
        val card = _cards.value.getOrNull(index) ?: return
        viewModelScope.launch {
            val updatedCard = card.copy(
                repetitions = card.repetitions + 1,
                lastModified = System.currentTimeMillis()
            )
            repository.recordStudyEvent(updatedCard, 1) // Thay vì chỉ updateFlashcard
            // Cập nhật list local để UI phản ánh ngay
            _cards.value = _cards.value.toMutableList().also { it[index] = updatedCard }
            _sessionLearnedCount.value += 1
        }
        nextCard()
    }

    /**
     * Đánh dấu thẻ là CẦN ÔN LẠI (Hard):
     * - Reset repetitions về 0
     * - Cập nhật lastModified
     */
    fun swipeReview() {
        val index = _currentIndex.value
        val card = _cards.value.getOrNull(index) ?: return
        viewModelScope.launch {
            val updatedCard = card.copy(
                repetitions = 0,
                lastModified = System.currentTimeMillis()
            )
            repository.recordStudyEvent(updatedCard, 0) // Thay vì chỉ updateFlashcard
            _cards.value = _cards.value.toMutableList().also { it[index] = updatedCard }
            _sessionReviewCount.value += 1
        }
        nextCard()
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

    fun restartSession() {
        _currentIndex.value = 0
        _isFlipped.value = false
        _isCompleted.value = false
        _sessionLearnedCount.value = 0
        _sessionReviewCount.value = 0
    }
}
