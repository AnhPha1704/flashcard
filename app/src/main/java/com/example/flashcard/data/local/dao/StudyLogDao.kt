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
        WHERE quality = 1 AND timestamp >= :startOfDay
    """)
    fun getTodayLearnedCount(startOfDay: Long): Flow<Int>

    /** 
     * Lấy lịch sử học tập (số thẻ học mỗi ngày) trong khoảng thời gian xác định.
     * Trả về danh sách DayCount (dayTimestamp, count)
     */
    @Query("""
        SELECT 
            (timestamp / 86400000) * 86400000 AS dayTimestamp,
            COUNT(DISTINCT cardId) AS count
        FROM study_logs
        WHERE quality = 1 AND timestamp >= :since
        GROUP BY dayTimestamp
        ORDER BY dayTimestamp ASC
    """)
    fun getStudyHistorySince(since: Long): Flow<List<DayCount>>

    /** Lấy tất cả các ngày duy nhất có hoạt động học tập để tính Streak */
    @Query("""
        SELECT (timestamp / 86400000) AS dayIndex
        FROM study_logs 
        WHERE quality = 1 
        GROUP BY dayIndex
        ORDER BY dayIndex DESC
    """)
    fun getDistinctStudyDays(): Flow<List<Long>>

    /** Xóa log cũ (nếu cần bảo trì database) */
    @Query("DELETE FROM study_logs WHERE timestamp < :timestamp")
    suspend fun deleteOldLogs(timestamp: Long): Int
}
