package com.bqliang.running.diary.database.entity

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Run
 *
 * @property id Run ID
 * @property entityName 实体名称
 * @property startTime 开始时间
 * @property distanceInMeter 跑步总距离（单位：米）
 * @property durationInMilliseconds 跑步时长（单位：毫秒）
 * @property caloriesInKcal 消耗卡路里
 * @property stepCount 步数
 * @property img 轨迹图片
 * @property note 跑步日记
 */
@Entity(
    tableName = "run",
    indices = [Index("entityName")]
)
data class Run(
    val entityName: String,
    val startTime: Long,
    val durationInMilliseconds: Long,
    val distanceInMeter: Double,
    val caloriesInKcal: Int,
    val stepCount: Int,
    val img: Bitmap,
    val note: String = ""
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L
}
