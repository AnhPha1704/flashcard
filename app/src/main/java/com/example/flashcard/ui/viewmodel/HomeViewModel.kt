package com.example.flashcard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcard.data.local.entity.DeckWithCount
import com.example.flashcard.domain.model.DayStudyCount
import com.example.flashcard.domain.model.StatsOverview
import com.example.flashcard.domain.repository.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

    /** Mốc thời gian hiện tại để truy vấn (tự cập nhật mỗi phút hoặc ép cập nhật thủ công) */
    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    
    init {
        // Cập nhật mốc thời gian mỗi phút một lần
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000 * 60)
                _currentTime.value = System.currentTimeMillis()
            }
        }
    }

    /** Danh sách các bộ thẻ hiển thị trên Dashboard (Ưu tiên thẻ đến hạn, nếu không có thì hiện bộ thẻ gần đây) */
    val dueDecks: StateFlow<List<DeckWithCount>> = _currentTime.flatMapLatest { currentTime ->
        repository.getAllDecksWithCount(currentTime)
    }.map { decks ->
        val activeDecks = decks.filter { it.dueCount > 0 || it.newCount > 0 }
            .sortedByDescending { it.dueCount }
            .take(3)
        
        if (activeDecks.isNotEmpty()) {
            activeDecks
        } else {
            // Nếu không có gì đến hạn, lấy 3 bộ thẻ mới nhất để làm lối tắt nhanh
            decks.take(3)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    /** Kiểm tra xem có thực sự là đang "thảnh thơi" (không có thẻ nào đến hạn/mới) không */
    val isAllCaughtUp: StateFlow<Boolean> = _currentTime.flatMapLatest { currentTime ->
        repository.getAllDecksWithCount(currentTime)
    }.map { decks ->
        decks.all { it.dueCount == 0 && it.newCount == 0 }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    /** Tổng số thẻ thực sự cần ôn tập trên toàn bộ ứng dụng */
    val totalDueCount: StateFlow<Int> = _currentTime.flatMapLatest { currentTime ->
        repository.getAllDecksWithCount(currentTime)
    }.map { decks ->
        decks.sumOf { it.dueCount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    /** Dự báo ôn tập 7 ngày tới */
    val reviewForecast: StateFlow<List<DayStudyCount>> = repository.getReviewForecast()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Thông tin thông minh: Ngày có lượng thẻ cần ôn lớn nhất trong tương lai gần */
    val srsInsight: StateFlow<String> = reviewForecast.map { forecast ->
        if (forecast.size <= 1) return@map "Lịch trình của bạn đang rất thoáng! 🍃"
        
        // Bỏ ngày đầu tiên (hôm nay) để tìm ngày cao điểm trong tương lai
        val futureDays = forecast.drop(1)
        if (futureDays.isEmpty()) return@map "Hiện chưa có dự báo cho các ngày tới."
        
        val peakDay = futureDays.maxByOrNull { it.count }
        if (peakDay != null && peakDay.count > 0) {
            val dateLabel = formatInsightDate(peakDay.dayDate)
            "Chuẩn bị nhé! $dateLabel sẽ là cao điểm với ${peakDay.count} thẻ."
        } else {
            "Lịch học sắp tới của bạn khá nhẹ nhàng. ✨"
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Đang phân tích lịch trình..."
    )

    /** Thời điểm sớm nhất sẽ có thêm thẻ đến hạn trong tương lai */
    val nearestUpcomingReview: StateFlow<Long?> = _currentTime.flatMapLatest { currentTime ->
        repository.getNearestUpcomingReview(currentTime)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    /** Chuỗi ký tự đếm ngược hiển thị trên UI */
    val countdownText: StateFlow<String?> = combine(nearestUpcomingReview, _currentTime) { timestamp, currentTime ->
        if (timestamp == null) return@combine null
        
        val diff = timestamp - currentTime
        if (diff <= 0) return@combine "Ngay bây giờ"
        
        val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(diff)
        val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(diff) % 60
        
        when {
            hours > 24 -> "${hours / 24} ngày nữa"
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes} phút"
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Đang tính toán..."
    )

    /** DEBUG: Ép thẻ sắp đến hạn trở thành đến hạn ngay lập tức */
    fun triggerDebugDue() {
        viewModelScope.launch {
            repository.debugMakeCardDue()
            // Ép cập nhật mốc thời gian ngay lập tức để UI nhận ra thay đổi
            _currentTime.value = System.currentTimeMillis()
        }
    }

    private fun formatInsightDate(dateStr: String): String {
        return try {
            val date = java.time.LocalDate.parse(dateStr)
            val today = java.time.LocalDate.now()
            when {
                date == today.plusDays(1) -> "Ngày mai"
                date == today.plusDays(2) -> "Ngày kia"
                else -> {
                    when (date.dayOfWeek) {
                        java.time.DayOfWeek.MONDAY -> "Thứ Hai"
                        java.time.DayOfWeek.TUESDAY -> "Thứ Ba"
                        java.time.DayOfWeek.WEDNESDAY -> "Thứ Tư"
                        java.time.DayOfWeek.THURSDAY -> "Thứ Năm"
                        java.time.DayOfWeek.FRIDAY -> "Thứ Sáu"
                        java.time.DayOfWeek.SATURDAY -> "Thứ Bảy"
                        java.time.DayOfWeek.SUNDAY -> "Chủ Nhật"
                        else -> dateStr
                    }
                }
            }
        } catch (e: Exception) {
            dateStr
        }
    }
}
