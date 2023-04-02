package com.bqliang.running.diary.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.amap.api.maps.model.LatLng

/**
 * Point 轨迹点
 *
 * @property id 轨迹点 ID
 * @property pathId 所属轨迹 ID
 * @property latitude 纬度
 * @property longitude 经度
 * @property altitude 海拔
 * @property speed 瞬时速度
 * @property locateTime 定位时间
 * @property direction 方向
 * @property accuracy 定位精度
 */
@Entity(
    tableName = "point",
    indices = [Index("pathId")],
    foreignKeys = [ForeignKey(
        entity = Path::class,
        parentColumns = ["id"],
        childColumns = ["pathId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class Point(
    val pathId: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Double,
    val locateTime: Long,
    val direction: Int,
    val accuracy: Double
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    fun toGdLatLng() = LatLng(latitude, longitude)
}



