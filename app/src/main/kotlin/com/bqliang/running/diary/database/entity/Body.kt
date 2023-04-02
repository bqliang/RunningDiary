package com.bqliang.running.diary.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "body",
    indices = [Index(value = ["entityName", "dateInMillisSeconds"], unique = true)]
)
data class Body(
    val entityName: String,
    val dateInMillisSeconds: Long,
    val heightInCm: Float,
    val weightInKg: Float
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
