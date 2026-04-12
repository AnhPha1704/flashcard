package com.example.flashcard.data.local.dao

import androidx.room.*
import com.example.flashcard.data.local.entity.Flashcard
import com.example.flashcard.data.local.entity.DayCount
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards WHERE deckId = :deckId ORDER BY createdAt DESC")
    fun getFlashcardsByDeck(deckId: Int): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard): Long

    @Update
    suspend fun updateFlashcard(flashcard: Flashcard): Int

    @Delete
    suspend fun deleteFlashcard(flashcard: Flashcard): Int

    @Query("SELECT * FROM flashcards WHERE id = :id")
    suspend fun getFlashcardById(id: Int): Flashcard?

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId AND nextReview <= :currentTime")
    fun getCardsToReview(deckId: Int, currentTime: Long): Flow<List<Flashcard>>

    // ===== THỐNG KÊ =====

    @Query("SELECT COUNT(*) FROM flashcards")
    fun getTotalCardCount(): Flow<Int>

    /** Thẻ đã học (Easy): repetitions >= 1 */
    @Query("SELECT COUNT(*) FROM flashcards WHERE repetitions >= 1")
    fun getEasyCardCount(): Flow<Int>

    /** Thẻ chưa học / cần học lại (Hard): repetitions == 0 */
    @Query("SELECT COUNT(*) FROM flashcards WHERE repetitions = 0")
    fun getHardCardCount(): Flow<Int>

    /** Số thẻ được ôn tập hôm nay (lastModified trong khoảng ngày hôm nay) */
    @Query("SELECT COUNT(*) FROM flashcards WHERE lastModified >= :startOfDay AND repetitions >= 1")
    fun getTodayStudiedCount(startOfDay: Long): Flow<Int>

    /**
     * Lịch sử học 7 ngày gần nhất: trả về danh sách (dayOffset, count)
     * dayOffset = số milliseconds bắt đầu của ngày (tính theo UTC midnight)
     */
    @Query("""
        SELECT 
            (lastModified / 86400000) * 86400000 AS dayTimestamp,
            COUNT(*) AS count
        FROM flashcards
        WHERE lastModified >= :since AND repetitions >= 1
        GROUP BY dayTimestamp
        ORDER BY dayTimestamp ASC
    """)
    fun getStudyHistorySince(since: Long): Flow<List<DayCount>>

    /** Lấy tất cả flashcard để tính streak */
    @Query("SELECT DISTINCT (lastModified / 86400000) FROM flashcards WHERE repetitions >= 1 ORDER BY 1 DESC")
    fun getDistinctStudyDays(): Flow<List<Long>>
}
