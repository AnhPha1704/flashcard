package com.example.flashcard.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SM2AlgorithmTest {

    @Test
    fun `first review with perfect response should return interval 1`() {
        val result = SM2Algorithm.calculate(
            quality = 5,
            previousInterval = 0,
            previousEasiness = 2.5f,
            previousRepetitions = 0
        )
        assertEquals(1, result.interval)
        assertEquals(1, result.repetitions)
        assertEquals(2.6f, result.easiness, 0.001f)
    }

    @Test
    fun `second review with good response should return interval 6`() {
        val result = SM2Algorithm.calculate(
            quality = 4,
            previousInterval = 1,
            previousEasiness = 2.6f,
            previousRepetitions = 1
        )
        assertEquals(6, result.interval)
        assertEquals(2, result.repetitions)
        assertEquals(2.6f, result.easiness, 0.001f) // quality 4 doesn't change EF (0.1 - (1)*(0.08+0.02) = 0)
    }

    @Test
    fun `third review with perfect response should increase interval based on EF`() {
        // EF = 2.6, interval = 6
        val result = SM2Algorithm.calculate(
            quality = 5,
            previousInterval = 6,
            previousEasiness = 2.6f,
            previousRepetitions = 2
        )
        // 6 * 2.6 = 15.6 -> round to 16
        assertEquals(16, result.interval)
        assertEquals(3, result.repetitions)
        assertEquals(2.7f, result.easiness, 0.001f)
    }

    @Test
    fun `incorrect response should reset repetitions and interval`() {
        val result = SM2Algorithm.calculate(
            quality = 2,
            previousInterval = 16,
            previousEasiness = 2.7f,
            previousRepetitions = 3
        )
        assertEquals(1, result.interval)
        assertEquals(0, result.repetitions)
        assertEquals(2.7f, result.easiness, 0.001f)
    }

    @Test
    fun `easiness factor should not go below 1_3`() {
        var currentEasiness = 1.35f
        // Quality 2 or below doesn't change EF in my impl (it stays same). 
        // Quality 3: EF = EF + (0.1 - (2) * (0.08 + 2 * 0.02)) = EF + (0.1 - 0.24) = EF - 0.14
        val result = SM2Algorithm.calculate(
            quality = 3,
            previousInterval = 1,
            previousEasiness = 1.35f,
            previousRepetitions = 1
        )
        // 1.35 - 0.14 = 1.21 -> should be 1.3
        assertEquals(1.3f, result.easiness, 0.001f)
    }
}
