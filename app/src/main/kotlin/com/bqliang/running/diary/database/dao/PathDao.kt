package com.bqliang.running.diary.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bqliang.running.diary.database.entity.Path
import com.bqliang.running.diary.database.entity.Point
import kotlinx.coroutines.flow.Flow

typealias PathWithPoints = Map<Path, List<Point>>

@Dao
interface PathDao {

    @Insert
    suspend fun insertPaths(paths: List<Path>): List<Long>

    @Query("SELECT * FROM path WHERE path.runId = :runId")
    fun getPathsByRunId(runId: Long): Flow<List<Path>>
}