package com.example.flashcard.data.local.dao

import androidx.room.*
import com.example.flashcard.data.local.entity.StudyLog
import com.example.flashcard.data.local.entity.DayCount
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyLog(log: StudyLog): Long

    /** Đếm số thẻ duy nhất đã học hôm nay (quality = 1) */
    @Query("""
        SELECT COUNT(DISTINCT cardId) AS count
        FROM study_logs 
        WHERE quality >= 0 AND timestamp >= :startOfDay
    """)
    fun getTodayLearnedCount(startOfDay: Long): Flow<Int>

    /** 
     * Lấy lịch sử học tập (số thẻ học mỗi ngày) trong khoảng thời gian xác định.
     * Trả về danh sách DayCount (dayString, count) theo giờ địa phương.
     */
    @Query("""
        SELECT 
            strftime('%Y-%m-%d', datetime(timestamp / 1000, 'unixepoch', 'localtime')) AS dayString,
            COUNT(DISTINCT cardId) AS count
        FROM study_logs
        WHERE quality >= 0 AND timestamp >= :since
        GROUP BY dayString
        ORDER BY dayString ASC
    """)
    fun getStudyHistorySince(since: Long): Flow<List<DayCount>>

    /** Lấy tất cả các ngày duy nhất có hoạt động học tập để tính Streak (định dạng YYYY-MM-DD) */
    @Query("""
        SELECT strftime('%Y-%m-%d', datetime(timestamp / 1000, 'unixepoch', 'localtime')) AS dayString
        FROM study_logs 
        WHERE quality >= 0 
        GROUP BY dayString
        ORDER BY dayString DESC
    """)
    fun getDistinctStudyDays(): Flow<List<String>>

    /** Xóa log cũ (nếu cần bảo trì database) */
    @Query("DELETE FROM study_logs WHERE timestamp < :timestamp")
    suspend fun deleteOldLogs(timestamp: Long): Int
}
