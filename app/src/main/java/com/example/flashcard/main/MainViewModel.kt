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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.flashcard.data.local.entity.DeckWithCount

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FlashcardRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Quan sát danh sách bộ thẻ (Decks) cùng số lượng thẻ từ Database và lọc theo search query
    val decks: StateFlow<List<DeckWithCount>> = combine(
        repository.getAllDecksWithCount(),
        _searchQuery
    ) { deckList, query ->
        if (query.isBlank()) {
            deckList
        } else {
            deckList.filter { 
                it.deck.name.contains(query, ignoreCase = true) || 
                (it.deck.description?.contains(query, ignoreCase = true) == true) 
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Quan sát trạng thái mạng
    val networkStatus: StateFlow<ConnectivityObserver.Status> = connectivityObserver.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectivityObserver.Status.Unavailable)

    // Thêm dữ liệu mẫu (Gồm Deck và Flashcards)
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

    // --- CRUD Operations cho DECK ---
    fun upsertDeck(name: String, description: String, id: Int = 0) {
        viewModelScope.launch {
            if (id == 0) {
                repository.insertDeck(Deck(name = name, description = description))
            } else {
                repository.updateDeck(Deck(id = id, name = name, description = description))
            }
        }
    }

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch {
            repository.deleteDeck(deck)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
