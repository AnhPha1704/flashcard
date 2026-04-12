package com.example.flashcard.domain.util

import kotlin.math.roundToInt

/**
 * Hiện thực hóa thuật toán SM-2 (SuperMemo-2).
 * Giúp tính toán khoảng cách ngày ôn tập tiếp theo dựa trên chất lượng trả lời của người dùng.
 */
data class SM2Result(
    val interval: Int,
    val easiness: Float,
    val repetitions: Int
)

object SM2Algorithm {
    /**
     * @param quality Chất lượng câu trả lời (0-5)
     * @param previousInterval Khoảng cách cũ (những ngày)
     * @param previousEasiness Hệ số độ dễ cũ (EF - Easiness Factor)
     * @param previousRepetitions Số lần lặp lại thành công trước đó
     */
    fun calculate(
        quality: Int,
        previousInterval: Int,
        previousEasiness: Float,
        previousRepetitions: Int
    ): SM2Result {
        var nextInterval: Int
        var nextEasiness: Float
        var nextRepetitions: Int

        if (quality >= 3) {
            // Trả lời đúng (3: Khá, 4: Tốt, 5: Hoàn hảo)
            when (previousRepetitions) {
                0 -> nextInterval = 1
                1 -> nextInterval = 6
                else -> nextInterval = (previousInterval * previousEasiness).roundToInt()
            }
            nextRepetitions = previousRepetitions + 1
            
            // Công thức cập nhật hệ số độ dễ (Easiness Factor)
            nextEasiness = previousEasiness + (0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f))
        } else {
            // Trả lời sai (0, 1, 2) - Reset lại việc học cho thẻ này
            nextRepetitions = 0
            nextInterval = 1
            nextEasiness = previousEasiness
        }

        // Giá trị tối thiểu của EF là 1.3
        if (nextEasiness < 1.3f) {
            nextEasiness = 1.3f
        }

        return SM2Result(nextInterval, nextEasiness, nextRepetitions)
    }
}
