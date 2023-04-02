package com.bqliang.running.diary.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Track entity移动所产生的连续轨迹
 *
 * @property id 轨迹 ID
 * @property runId 轨迹所属的 Run ID
 */
@Entity(
    tableName = "path",
    indices = [Index("runId")],
    foreignKeys = [ForeignKey(
        entity = Run::class,
        parentColumns = ["id"],
        childColumns = ["runId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class Path(
    val runId: Long
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}