package com.example.flashcard.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcard.data.local.entity.Deck
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

    // Hàm thêm dữ liệu mẫu
    fun addDemoDeck() {
        viewModelScope.launch {
            val newDeck = Deck(
                name = "Bộ thẻ Demo ${System.currentTimeMillis() % 1000}",
                description = "Tạo lúc ${System.currentTimeMillis()}"
            )
            repository.insertDeck(newDeck)
        }
    }
}
