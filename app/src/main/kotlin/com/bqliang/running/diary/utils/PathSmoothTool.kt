package com.bqliang.running.diary.utils

import com.amap.api.maps.model.LatLng
import com.bqliang.running.diary.utils.PathSmoothTool.mKalmanFilterIntensity
import com.bqliang.running.diary.utils.PathSmoothTool.mNoiseReductionThreshold
import com.bqliang.running.diary.utils.PathSmoothTool.mVacuateThreshold
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


/**
 * ...
 *
 * @property mKalmanFilterIntensity 卡尔曼滤波强度（默认：3）
 * @property mVacuateThreshold 抽稀阈值
 * @property mNoiseReductionThreshold 去噪阈值
 */
object PathSmoothTool {


    private var mKalmanFilterIntensity = 3
    private var mVacuateThreshold = 0.3f
    private var mNoiseReductionThreshold = 10f

    fun getKalmanFilterIntensity() = mKalmanFilterIntensity

    /**
     * 设置滤波强度，默认为 3
     *
     * @param intensity 滤波强度，范围：1-5
     */
    fun setKalmanFilterIntensity(intensity: Int) {
        this.mKalmanFilterIntensity = intensity.coerceIn(1, 5)
    }

    fun getVacuateThreshold() = mVacuateThreshold

    fun setVacuateThreshold(threshold: Float) {
        this.mVacuateThreshold = threshold
    }

    fun getNoiseThreshold() = mNoiseReductionThreshold

    fun setNoiseThreshold(threshold: Float) {
        this.mNoiseReductionThreshold = threshold
    }


    /**
     * 轨迹优化
     *
     * @param originList 原始点集
     * @return 优化后的点集
     */
    fun pathOptimize(originList: List<LatLng>): List<LatLng> {
        val cleanedList = reduceNoisePoint(originList) //去噪
        val filteredList = kalmanFilterPath(cleanedList, mKalmanFilterIntensity) //滤波
        val optimizedList = vacuate(filteredList, mVacuateThreshold) //抽稀
        return optimizedList
    }


    /**
     * 去噪
     *
     * @param inPoints 原始点集
     * @param threshHold 阈值
     * @return 去噪后的点集
     */
    fun reduceNoisePoint(
        inPoints: List<LatLng>,
        threshHold: Float = mNoiseReductionThreshold
    ): List<LatLng> {
        if (inPoints.size <= 2) return inPoints

        val ret = mutableListOf<LatLng>()
        for (i in inPoints.indices) {
            val pre = ret.lastOrNull()
            val cur = inPoints[i]
            if (pre == null || i == inPoints.lastIndex) {
                ret.add(cur)
                continue
            }
            val next = inPoints[i + 1]
            val distance = perpendicularDistance(cur, pre, next)
            if (distance < threshHold) ret.add(cur)
        }
        return ret
    }


    // 上次位置
    private var lastLocation_x = 0.0
    private var lastLocation_y = 0.0

    // 这次位置
    private var currentLocation_x = 0.0
    private var currentLocation_y = 0.0

    // 修正后数据
    private var estimate_x = 0.0
    private var estimate_y = 0.0

    // 自预估偏差
    private var pdelt_x = 0.0
    private var pdelt_y = 0.0

    //上次模型偏差
    private var mdelt_x = 0.0
    private var mdelt_y = 0.0

    // 高斯噪音偏差
    private var gauss_x = 0.0
    private var gauss_y = 0.0

    // 卡尔曼增益
    private var kalmanGain_x = 0.0
    private var kalmanGain_y = 0.0
    private var m_R = 0.0 //测量噪声?
    private var m_Q = 0.0 //过程噪声?


    private fun initial() {
        pdelt_x = 0.001
        pdelt_y = 0.001
        // mdelt_x = 0
        // mdelt_y = 0
        mdelt_x = 5.698402909980532E-4
        mdelt_y = 5.698402909980532E-4
    }


    /**
     * 卡尔曼滤波
     *
     * @param originList 原始点集
     * @param intensity 滤波强度（1-5）
     * @return 滤波后的点集
     */
    fun kalmanFilterPath(
        originList: List<LatLng>,
        intensity: Int = mKalmanFilterIntensity
    ): List<LatLng> {
        if (originList.size <= 2) return originList
        // 初始化滤波参数
        initial()
        val mIntensity = intensity.coerceIn(1, 5)
        val kalmanFilterList = mutableListOf<LatLng>()
        var lastLoc = originList.first()
        kalmanFilterList.add(lastLoc)
        for (i in 1 until originList.size) {
            val curLoc: LatLng = originList[i]
            val latLng = kalmanFilterPoint(lastLoc, curLoc, mIntensity)
            kalmanFilterList.add(latLng)
            lastLoc = latLng
        }
        return kalmanFilterList
    }


    /**
     * 单点卡尔曼滤波
     *
     * @param lastLoc 上次定位点坐标
     * @param curLoc 本次定位点坐标
     * @param intensity 滤波强度
     * @return 滤波后的本次定位点坐标
     */
    private fun kalmanFilterPoint(lastLoc: LatLng, curLoc: LatLng, intensity: Int): LatLng {
        if (pdelt_x == 0.0 || pdelt_y == 0.0) initial()
        var mCurLoc: LatLng = curLoc
        var kalmanLatLng: LatLng? = null

        for (j in 0 until intensity) {
            kalmanLatLng =
                kalmanFilter(
                    lastLoc.longitude,
                    mCurLoc.longitude,
                    lastLoc.latitude,
                    mCurLoc.latitude
                )
            mCurLoc = kalmanLatLng
        }

        return kalmanLatLng!!
    }


    /**
     * 卡尔曼滤波
     *
     * @param oldValue_x 上次定位点经度
     * @param value_x 本次定位点经度
     * @param oldValue_y 上次定位点纬度
     * @param value_y 本次定位点纬度
     * @return 滤波后的本次定位点坐标
     */
    private fun kalmanFilter(
        oldValue_x: Double,
        value_x: Double,
        oldValue_y: Double,
        value_y: Double
    ): LatLng {
        lastLocation_x = oldValue_x
        currentLocation_x = value_x
        gauss_x = sqrt(pdelt_x * pdelt_x + mdelt_x * mdelt_x) + m_Q //计算高斯噪音偏差
        kalmanGain_x =
            sqrt((gauss_x * gauss_x) / (gauss_x * gauss_x + pdelt_x * pdelt_x)) + m_R //计算卡尔曼增益
        estimate_x = kalmanGain_x * (currentLocation_x - lastLocation_x) + lastLocation_x //修正定位点
        mdelt_x = sqrt((1 - kalmanGain_x) * gauss_x * gauss_x) //修正模型偏差


        lastLocation_y = oldValue_y
        currentLocation_y = value_y
        gauss_y = sqrt(pdelt_y * pdelt_y + mdelt_y * mdelt_y) + m_Q //计算高斯噪音偏差
        kalmanGain_y =
            sqrt((gauss_y * gauss_y) / (gauss_y * gauss_y + pdelt_y * pdelt_y)) + m_R //计算卡尔曼增益
        estimate_y = kalmanGain_y * (currentLocation_y - lastLocation_y) + lastLocation_y //修正定位点
        mdelt_y = sqrt((1 - kalmanGain_y) * gauss_y * gauss_y) //修正模型偏差


        return LatLng(estimate_y, estimate_x)
    }


    /**
     * 轨迹抽稀，删除垂距小于阈值的点
     *
     * @param inPoints 轨迹点集合
     * @param threshHold 阈值
     * @return 抽稀后的轨迹点集合
     */
    private fun vacuate(
        inPoints: List<LatLng>,
        threshHold: Float = mVacuateThreshold
    ): List<LatLng> {
        if (inPoints.size <= 2) return inPoints
        val ret = mutableListOf<LatLng>()
        for (i in inPoints.indices) {
            val pre = ret.lastOrNull()
            val cur = inPoints[i]
            if (pre == null || i == inPoints.lastIndex) {
                ret.add(cur)
                continue
            }
            val next = inPoints[i + 1]
            val distance = perpendicularDistance(cur, pre, next)
            if (distance > threshHold) ret.add(cur)
        }
        return ret
    }


    /**
     * 计算点到线段的垂距
     *
     * @param point 点
     * @param lineStart 线段起点
     * @param lineEnd 线段终点
     * @return 垂距
     */
    private fun perpendicularDistance(point: LatLng, lineStart: LatLng, lineEnd: LatLng): Float {
        val A = point.longitude - lineStart.longitude
        val B = point.latitude - lineStart.latitude
        val C = lineEnd.longitude - lineStart.longitude
        val D = lineEnd.latitude - lineStart.latitude

        val dot = A * C + B * D
        val len_sq = C * C + D * D
        val param = dot / len_sq

        val xx: Double
        val yy: Double

        val sameStartEnd =
            lineStart.latitude == lineEnd.latitude && lineStart.longitude == lineEnd.longitude
        if (param < 0 || sameStartEnd) {
            xx = lineStart.longitude
            yy = lineStart.latitude
        } else if (param > 1) {
            xx = lineEnd.longitude
            yy = lineEnd.latitude
        } else {
            xx = lineStart.longitude + param * C
            yy = lineStart.latitude + param * D
        }

        return calculateLineDistance(point, LatLng(yy, xx))
    }


    /**
     * 计算两点之间的距离（单位：米）
     *
     * @param start 起点
     * @param end 终点
     * @return 距离（单位：米）
     */
    fun calculateLineDistance(start: LatLng, end: LatLng): Float = try {
        val degreeToRadian = Math.PI / 180
        // 经纬度转换成弧度
        val startLng = start.longitude * degreeToRadian
        val startLat = start.latitude * degreeToRadian
        val endLng = end.longitude * degreeToRadian
        val endLat = end.latitude * degreeToRadian

        val startLngSin = sin(startLng)
        val startLatSin = sin(startLat)
        val startLngCos = cos(startLng)
        val startLatCos = cos(startLat)
        val endLngSin = sin(endLng)
        val endLatSin = sin(endLat)
        val endLngCos = cos(endLng)
        val endLatCos = cos(endLat)

        val startPointVector =
            doubleArrayOf(startLatCos * startLngCos, startLatCos * startLngSin, startLatSin)
        val endPointVector = doubleArrayOf(endLatCos * endLngCos, endLatCos * endLngSin, endLatSin)

        (asin(sqrt((startPointVector[0] - endPointVector[0]) * (startPointVector[0] - endPointVector[0]) + (startPointVector[1] - endPointVector[1]) * (startPointVector[1] - endPointVector[1]) + (startPointVector[2] - endPointVector[2]) * (startPointVector[2] - endPointVector[2])) / 2.0) * 1.27420015798544E7).toFloat()
    } catch (e: Throwable) {
        e.printStackTrace()
        0.0F
    }
}