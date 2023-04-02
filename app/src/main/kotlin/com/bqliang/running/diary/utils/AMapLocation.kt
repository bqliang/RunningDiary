package com.bqliang.running.diary.utils

import com.amap.api.location.AMapLocation
import com.baidu.trace.model.CoordType
import com.baidu.trace.model.LatLng
import com.baidu.trace.model.Point


/**
 * 将高德地图的 AMapLocation 转换为百度地图的 Point
 *
 * @return
 */
fun AMapLocation.toBdPoint(): Point {
    val point =  Point()
    point.coordType = CoordType.gcj02
    point.location = LatLng(latitude, longitude)
    point.speed = speed.toDouble()
    point.height = altitude
    point.direction = bearing.toInt()
    point.radius = accuracy.toDouble()
    point.locTime = time / 1000
    return point
}