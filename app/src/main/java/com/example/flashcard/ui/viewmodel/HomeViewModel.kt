package com.example.flashcard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcard.data.local.entity.DeckWithCount
import com.example.flashcard.domain.model.StatsOverview
import com.example.flashcard.domain.repository.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FlashcardRepository
) : ViewModel() {

    /** Thông số tổng quan (Streak, Hôm nay, Tổng thẻ) */
    val statsOverview: StateFlow<StatsOverview> = repository.getStatsOverview()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatsOverview()
        )

    /** Danh sách các bộ thẻ kèm số lượng thẻ đến hạn, ưu tiên các bộ thẻ có thẻ đến hạn học */
    val dueDecks: StateFlow<List<DeckWithCount>> = repository.getAllDecksWithCount(System.currentTimeMillis())
        .map { decks ->
            decks.filter { it.dueCount > 0 || it.newCount > 0 }
                .sortedByDescending { it.dueCount }
                .take(3) // Chỉ lấy 3 bộ thẻ quan trọng nhất để nhắc nhở
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Tổng số thẻ thực sự cần ôn tập trên toàn bộ ứng dụng */
    val totalDueCount: StateFlow<Int> = repository.getAllDecksWithCount(System.currentTimeMillis())
        .map { decks ->
            decks.sumOf { it.dueCount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
}
