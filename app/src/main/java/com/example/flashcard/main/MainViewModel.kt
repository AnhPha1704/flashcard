package com.example.flashcard.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcard.data.local.entity.Deck
import com.example.flashcard.data.local.entity.Flashcard
import com.example.flashcard.domain.repository.FlashcardRepository
import com.example.flashcard.domain.util.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FlashcardRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    // Quan sát danh sách bộ thẻ (Decks) từ Database
    val decks: StateFlow<List<Deck>> = repository.getAllDecks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Quan sát trạng thái mạng
    val networkStatus: StateFlow<ConnectivityObserver.Status> = connectivityObserver.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectivityObserver.Status.Unavailable)

    // Hàm thêm dữ liệu mẫu (Gồm Deck và Flashcards)
    fun addDemoDeck() {
        viewModelScope.launch {
            val deckId = repository.insertDeck(
                Deck(
                    name = "Tiếng Anh IT ${System.currentTimeMillis() % 100}",
                    description = "Từ vựng lập trình viên cần biết"
                )
            ).toInt()

            val demoCards = listOf(
                Flashcard(deckId = deckId, front = "Mutable", back = "Có thể thay đổi (giá trị hoặc trạng thái)"),
                Flashcard(deckId = deckId, front = "Immutable", back = "Bất biến, không thể thay đổi sau khi tạo"),
                Flashcard(deckId = deckId, front = "Asynchronous", back = "Bất đồng bộ - xử lý không theo tuần tự thời gian"),
                Flashcard(deckId = deckId, front = "Middleware", back = "Phần mềm trung gian kết nối các thành phần hệ thống")
            )

            demoCards.forEach { repository.insertFlashcard(it) }
        }
    }
}
