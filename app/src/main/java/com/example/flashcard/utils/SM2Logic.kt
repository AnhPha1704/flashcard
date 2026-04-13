package com.example.flashcard.utils

import com.example.flashcard.data.local.entity.Flashcard
import kotlin.math.roundToInt

/**
 * Triển khai thuật toán SuperMemo-2 (SM-2) chuẩn.
 * Chứa logic thuần túy để tính toán khoảng cách ôn tập tiếp theo.
 */
object SM2Logic {

    /**
     * Tính toán các thông số mới cho Flashcard dựa trên chất lượng phản hồi.
     * @param card Thẻ hiện tại
     * @param quality Chất lượng phản hồi (0-5)
     * @return Thẻ mới với các thông số đã cập nhật
     */
    fun calculate(card: Flashcard, quality: Int): Flashcard {
        var n = card.repetitions
        var ef = card.easiness
        var interval = card.interval

        // Áp dụng thuật toán SM-2 chuẩn
        if (quality >= 3) {
            // Đáp án đúng (Correct response)
            when (n) {
                0 -> interval = 1
                1 -> interval = 6
                else -> interval = (interval * ef).roundToInt()
            }
            n++
        } else {
            // Đáp án sai (Incorrect response)
            n = 0
            interval = 0
        }

        // Cập nhật hệ số Easiness (EF)
        // Công thức: EF = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        ef = ef + (0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f))
        
        // EASINESS không bao giờ được nhỏ hơn 1.3
        if (ef < 1.3f) ef = 1.3f

        // Tính ngày ôn tập tiếp theo: milliseconds = days * 24h * 60m * 60s * 1000ms
        val nextReview = System.currentTimeMillis() + (interval.toLong() * 24 * 60 * 60 * 1000)

        return card.copy(
            repetitions = n,
            easiness = ef,
            interval = interval,
            nextReview = nextReview,
            lastModified = System.currentTimeMillis(),
            isSynced = false
        )
    }
}
