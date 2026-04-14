package com.example.flashcard.utils

import com.example.flashcard.data.local.entity.Flashcard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit Test cho lớp SM2Logic.
 * Kiểm tra tính chính xác của thuật toán ôn tập.
 */
class SM2LogicTest {

    private val baseCard = Flashcard(
        id = 1,
        deckId = 1,
        front = "Question",
        back = "Answer"
    )

    @Test
    fun testInitialCorrectResponse() {
        // Lần đầu học đúng (Quality 5)
        val result = SM2Logic.calculate(baseCard, 5)
        
        assertEquals(1, result.repetitions)
        assertEquals(1, result.interval)
        assertTrue(result.easiness > 2.5f) // Quality 5 làm tăng Easiness
    }

    @Test
    fun testSecondCorrectResponse() {
        // Giả sử thẻ đã học 1 lần đúng trước đó
        val card = baseCard.copy(repetitions = 1, interval = 1, easiness = 2.6f)
        
        // Lần thứ hai học đúng (Quality 5)
        val result = SM2Logic.calculate(card, 5)
        
        assertEquals(2, result.repetitions)
        assertEquals(6, result.interval)
        assertTrue(result.easiness > 2.6f)
    }

    @Test
    fun testSubsequentCorrectResponse() {
        // Thẻ đã học 2 lần (interval = 6), độ dễ 2.5
        val card = baseCard.copy(repetitions = 2, interval = 6, easiness = 2.5f)
        
        // Lần thứ ba học đúng (Quality 5)
        val result = SM2Logic.calculate(card, 5)
        
        assertEquals(3, result.repetitions)
        // Interval mới = 6 * 2.5 = 15
        assertEquals(15, result.interval)
    }

    @Test
    fun testIncorrectResponseReset() {
        // Thẻ đang ở chuỗi học tốt (n=3, interval=15)
        val card = baseCard.copy(repetitions = 3, interval = 15, easiness = 2.5f)
        
        // Học sai (Quality 0 - Review)
        val result = SM2Logic.calculate(card, 0)
        
        assertEquals(0, result.repetitions)
        assertEquals(1, result.interval)
        assertTrue(result.easiness < 2.5f) // Easiness bị giảm mạnh khi học sai
    }

    @Test
    fun testEasinessFloor() {
        // Giả sử độ dễ đã rất thấp (1.35f)
        val card = baseCard.copy(repetitions = 1, easiness = 1.35f)
        
        // Học sai liên tục
        val result = SM2Logic.calculate(card, 0)
        
        // Easiness không được thấp hơn 1.3f
        assertEquals(1.3f, result.easiness)
    }
}
