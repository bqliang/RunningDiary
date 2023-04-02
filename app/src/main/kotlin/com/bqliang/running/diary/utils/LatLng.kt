package com.bqliang.running.diary.utils

import com.amap.api.maps.model.LatLng
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


/**
 * 对给定的经纬度点列表进行抽稀操作，并返回抽稀后的经纬度点列表。
 *
 * @param list 经纬度点列表
 * @param tolerance 抽稀容差，即最大允许的点到直线的垂直距离
 * @return 抽稀后的经纬度点列表
 */
fun vacuate(list: List<LatLng>, tolerance: Double): List<LatLng> {
    if (list.size < 3) return list
    val result = mutableListOf<LatLng>()
    val first = list.first()
    val last = list.last()
    result.add(first)
    simplify(list, first, last, tolerance, result)
    result.add(last)
    return result
}


/**
 * 对经纬度点列表进行递归抽稀操作，并将结果存入指定的结果列表中。
 *
 * @param list 经纬度点列表
 * @param first 直线的起点
 * @param last 直线的终点
 * @param tolerance 抽稀容差，即最大允许的点到直线的垂直距离
 * @param result 抽稀结果存储的列表
 */
private fun simplify(list: List<LatLng>, first: LatLng, last: LatLng, tolerance: Double, result: MutableList<LatLng>) {
    var maxDistance = 0.0
    var index = 0

    for (i in 1 until list.size - 1) {
        val distance = perpendicularDistance(list[i], first, last)
        if (distance > maxDistance) {
            maxDistance = distance
            index = i
        }
    }

    if (maxDistance > tolerance && index != 0) {
        val point = list[index]
        result.add(point)
        simplify(list.subList(0, index), first, point, tolerance, result)
        simplify(list.subList(index, list.size), point, last, tolerance, result)
    }
}


/**
 * 计算给定点到指定直线的垂直距离。
 *
 * @param point 给定的经纬度点
 * @param first 直线的起点
 * @param last 直线的终点
 * @return 点到直线的垂直距离
 */
private fun perpendicularDistance(point: LatLng, first: LatLng, last: LatLng): Double {
    // 三角形面积公式：S = 1/2 * (x1y2 + x2y3 + x3y1 - x1y3 - x2y1 - x3y2)
    val area = abs(
        0.5 * (first.longitude * last.latitude + last.longitude * point.latitude + point.longitude * first.latitude
                - first.longitude * point.latitude - last.longitude * first.latitude - point.longitude * last.latitude)
    )

    // 三角形底边公式：a = sqrt( (x1 - x2)^2 + (y1 - y2)^2 )
    val bottom = sqrt(
        (first.longitude - last.longitude).pow(2.0) + (first.latitude - last.latitude).pow(2.0)
    )

    // 三角形高度公式：h = S / a * 2
    val height = area / bottom * 2
    return height
}