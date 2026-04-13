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

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId AND repetitions > 0 AND nextReview <= :currentTime")
    fun getCardsToReview(deckId: Int, currentTime: Long): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId AND repetitions = 0 AND lastModified == createdAt")
    fun getNewCards(deckId: Int): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId AND repetitions = 0 AND lastModified > createdAt")
    fun getForgottenCards(deckId: Int): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE isSynced = 0")
    suspend fun getUnsyncedFlashcards(): List<Flashcard>

    // ===== THỐNG KÊ =====

    @Query("SELECT COUNT(*) AS count FROM flashcards")
    fun getTotalCardCount(): Flow<Int>

    /** Thẻ đã học (Easy): repetitions >= 1 */
    @Query("SELECT COUNT(*) AS count FROM flashcards WHERE repetitions >= 1")
    fun getEasyCardCount(): Flow<Int>

    /** Thẻ chưa học / cần học lại (Hard): repetitions == 0 */
    @Query("SELECT COUNT(*) AS count FROM flashcards WHERE repetitions = 0")
    fun getHardCardCount(): Flow<Int>

    /** Số thẻ được ôn tập hôm nay (lastModified trong khoảng ngày hôm nay) */
    @Query("SELECT COUNT(*) AS count FROM flashcards WHERE lastModified >= :startOfDay AND repetitions >= 1")
    fun getTodayStudiedCount(startOfDay: Long): Flow<Int>

    /**
     * Lịch sử học 7 ngày gần nhất: trả về danh sách (dayString, count) theo giờ địa phương.
     */
    @Query("""
        SELECT 
            strftime('%Y-%m-%d', datetime(lastModified / 1000, 'unixepoch', 'localtime')) AS dayString,
            COUNT(*) AS count
        FROM flashcards
        WHERE lastModified >= :since AND repetitions >= 1
        GROUP BY dayString
        ORDER BY dayString ASC
    """)
    fun getStudyHistorySince(since: Long): Flow<List<DayCount>>

    /** Lấy tất cả các ngày duy nhất có hoạt động học tập (định dạng YYYY-MM-DD địa phương) */
    @Query("""
        SELECT strftime('%Y-%m-%d', datetime(lastModified / 1000, 'unixepoch', 'localtime')) AS dayString
        FROM flashcards 
        WHERE repetitions >= 1 
        GROUP BY dayString 
        ORDER BY dayString DESC
    """)
    fun getDistinctStudyDays(): Flow<List<String>>
}
