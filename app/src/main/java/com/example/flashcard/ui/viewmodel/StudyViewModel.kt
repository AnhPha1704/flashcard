package com.example.flashcard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcard.data.local.entity.Flashcard
import com.example.flashcard.domain.repository.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import android.util.Log

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

    // Số lượng thẻ gốc khi bắt đầu (để tính tiến độ chuẩn)
    private val _initialSize = MutableStateFlow(0)
    val initialSize: StateFlow<Int> = _initialSize.asStateFlow()

    // Đếm số thẻ đã thuộc/cần ôn trong phiên này
    private val _sessionLearnedCount = MutableStateFlow(0)
    val sessionLearnedCount: StateFlow<Int> = _sessionLearnedCount.asStateFlow()

    private val _sessionReviewCount = MutableStateFlow(0)
    val sessionReviewCount: StateFlow<Int> = _sessionReviewCount.asStateFlow()

    fun loadDeck(deckId: Int, mode: com.example.flashcard.StudyMode = com.example.flashcard.StudyMode.ALL) {
        viewModelScope.launch {
            Log.d("StudyViewModel", "Đang tải bộ thẻ: deckId=$deckId, mode=$mode")
            _isLoading.value = true
            _isCompleted.value = false
            _currentIndex.value = 0
            _sessionLearnedCount.value = 0
            _sessionReviewCount.value = 0
            
            val flow = if (deckId == -1) {
                // Chế độ Ôn tập toàn cục (tất cả thẻ đến hạn từ mọi bộ thẻ)
                repository.getAllCardsToReview(System.currentTimeMillis())
            } else {
                when (mode) {
                    com.example.flashcard.StudyMode.DUE -> 
                        repository.getCardsToReview(deckId, System.currentTimeMillis())
                    com.example.flashcard.StudyMode.NEW -> 
                        repository.getNewCards(deckId)
                    com.example.flashcard.StudyMode.FORGOTTEN -> 
                        repository.getForgottenCards(deckId)
                    else -> 
                        repository.getFlashcardsByDeck(deckId)
                }
            }
            
            // Sử dụng first() để lấy Snapshot dữ liệu (không lấy luồng trực tiếp)
            // Điều này ngăn danh sách thẻ bị rỗng đột ngột khi dữ liệu Database cập nhật
            val loadedCards = flow.first()
            Log.d("StudyViewModel", "Đã tải xong: ${loadedCards.size} thẻ.")
            _cards.value = loadedCards
            _initialSize.value = loadedCards.size
            _isLoading.value = false
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
            // Quality 5: Perfect response (Đã thuộc)
            repository.recordStudyEvent(card, 5)
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
        val currentCards = _cards.value
        val card = currentCards.getOrNull(index) ?: return
        
        viewModelScope.launch {
            // Quality 0: Complete blackout (Ôn lại)
            repository.recordStudyEvent(card, 0)
            _sessionReviewCount.value += 1
            
            // KHÔNG CÒN lặp lại thẻ ngay lập tức trong phiên học (Theo yêu cầu người dùng)
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
