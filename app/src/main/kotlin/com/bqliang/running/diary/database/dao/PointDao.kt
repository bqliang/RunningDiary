package com.bqliang.running.diary.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bqliang.running.diary.database.entity.Point
import kotlinx.coroutines.flow.Flow

@Dao
interface PointDao {

    @Insert
    suspend fun insertPoints(points: List<Point>): List<Long>

    @Query("SELECT * FROM point WHERE point.pathId in (:pathIds)")
    fun getPointsByPathIds(pathIds: List<Long>): Flow<List<Point>>

    @Query("SELECT * FROM point WHERE pathId in (SELECT id FROM path WHERE runId = :runId)")
    fun getPointsByRunId(runId: Long): Flow<List<Point>>
}