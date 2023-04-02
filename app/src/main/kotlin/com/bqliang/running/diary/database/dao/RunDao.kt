package com.bqliang.running.diary.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bqliang.running.diary.database.entity.Run
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {

    @Insert
    suspend fun insertRun(run: Run): Long


    @Query("SELECT * FROM run WHERE id = :id")
    fun getRunById(id: Long): Flow<Run>


    @Query(
        "SELECT * FROM run " +
                "WHERE entityName = :entityName " +
                "ORDER BY run.startTime DESC"
    )
    fun getAllByEntityName(entityName: String): Flow<List<Run>>


    @Query("UPDATE run SET note = :note WHERE id = :id")
    suspend fun updateNoteById(note: String, id: Long)


    @Query("SELECT SUM(distanceInMeter) FROM run WHERE entityName = :entityName AND startTime >= :startTime")
    fun getTotalDistance(entityName: String, startTime: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM run WHERE entityName = :entityName")
    fun getRunCount(entityName: String): Flow<Int>

    @Query("SELECT SUM(stepCount) FROM run WHERE entityName = :entityName")
    fun getTotalSteps(entityName: String): Flow<Int?>

    @Query("SELECT SUM(durationInMilliseconds) FROM run WHERE entityName = :entityName")
    fun getDurationInMillisSeconds(entityName: String): Flow<Long?>

    @Query("SELECT SUM(caloriesInKcal) FROM run WHERE entityName = :entityName")
    fun getTotalCaloriesInKcal(entityName: String): Flow<Int?>
}