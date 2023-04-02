package com.bqliang.running.diary.ui.base

import android.content.Context
import android.util.AttributeSet
import androidx.databinding.BindingAdapter
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.TextureMapView
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng

class MyTextureMapView: TextureMapView {

    init {
        val nfu = LatLng(23.63, 113.68)
        setDefaultDisplayArea(nfu)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    )

    constructor(context: Context, aMapOptions: AMapOptions?) : super(context, aMapOptions)

    private fun setDefaultDisplayArea(latLng: LatLng) {
        val cameraPosition = CameraPosition(
            /* 目标位置的屏幕中心点经纬度坐标 */ latLng,
            /* 目标可视区域的缩放级别 */ 17.5f,
            /* 目标可视区域的倾斜度，以角度为单位 */ 0f,
            /* 可视区域指向的方向，以角度为单位，从正北向顺时针方向计算，从 0 度到 360 度 */ 0f
        )
        val aMapOptions = AMapOptions()
        aMapOptions.camera(cameraPosition)
        super.getMapFragmentDelegate().setOptions(aMapOptions)
    }
}


@BindingAdapter("android:mapType")
fun setMapType(mapView: MyTextureMapView, mapType: Int) {
    mapView.map.mapType = mapType
}