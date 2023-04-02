package com.bqliang.running.diary.database.dao

import androidx.room.*
import com.bqliang.running.diary.database.entity.Body
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBody(body: Body): Long

    @Update
    suspend fun updateBody(newBody: Body)

    @Delete
    suspend fun deleteBody(body: Body)

    @Query("SELECT * FROM body WHERE id = :id")
    fun getBodyById(id: Long): Body

    @Query("SELECT * FROM body WHERE entityName = :entityName ORDER BY dateInMillisSeconds DESC LIMIT 1")
    fun getLatestBody(entityName: String): Body?

    @Query("SELECT * FROM body WHERE entityName = :entityName ORDER BY dateInMillisSeconds DESC")
    fun getAllBodyByEntityName(entityName: String) : Flow<List<Body>>
}