package com.bqliang.running.diary.utils

import com.amap.api.maps.model.LatLng
import com.baidu.trace.api.track.TrackPoint

fun TrackPoint.toGdLatLng(): LatLng = LatLng(location.latitude, location.longitude)
