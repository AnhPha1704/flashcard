package com.example.flashcard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcard.domain.model.DayStudyCount
import com.example.flashcard.domain.model.StatsOverview
import com.example.flashcard.domain.repository.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: FlashcardRepository
) : ViewModel() {

    /** Tổng quan thống kê: Easy/Hard/Streak/Mục tiêu hôm nay */
    val statsOverview: StateFlow<StatsOverview> = repository.getStatsOverview()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatsOverview()
        )

    /** Lịch sử học 7 ngày gần nhất cho biểu đồ */
    val weeklyHistory: StateFlow<List<DayStudyCount>> = repository.getStudyHistoryLast7Days()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
