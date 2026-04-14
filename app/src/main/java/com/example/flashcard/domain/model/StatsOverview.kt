package com.example.flashcard.domain.model

/** Tổng quan thống kê học tập */
data class StatsOverview(
    val totalCards: Int = 0,
    val easyCards: Int = 0,   // repetitions >= 1
    val hardCards: Int = 0,   // repetitions == 0
    val streak: Int = 0,      // số ngày học liên tục
    val todayStudied: Int = 0,// số thẻ học hôm nay
    val dailyGoal: Int = 10   // mục tiêu mặc định
)
