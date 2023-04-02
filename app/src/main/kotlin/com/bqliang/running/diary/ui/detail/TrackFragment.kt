package com.bqliang.running.diary.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.Polyline
import com.bqliang.running.diary.databinding.FragmentTrackBinding
import com.bqliang.running.diary.utils.collectLifecycleFlow
import com.bqliang.running.diary.utils.millsSecondsDateTimeFormat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackFragment : Fragment() {

    private val viewModel: DetailViewModel by activityViewModels()
    private var _binding: FragmentTrackBinding? = null
    private val binding get() = _binding!!
    private lateinit var aMap: AMap
    private val polylines = mutableListOf<Polyline>()
    private val createGpxLauncher = registerForActivityResult(CreateDocument("application/gpx+xml")) { uri ->
        uri?.also {
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                viewModel.exportGpxFile(outputStream)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textureMap.onCreate(savedInstanceState)
        aMap = binding.textureMap.map
        aMap.uiSettings.apply {
            // 隐藏定位按钮
            isMyLocationButtonEnabled = false
            // 隐藏缩放按钮
            isZoomControlsEnabled = false
            // 显示指南针
            isCompassEnabled = true
            // 显示比例尺
            isScaleControlsEnabled = true
        }


        binding.locateBtn.setOnClickListener {
            zoomToWholeTrack()
        }

        binding.layerSwitchBtn.setOnClickListener {
            viewModel.switchMapType()
        }

        binding.exportBtn.setOnClickListener {
            val runStartDateTimeString = millsSecondsDateTimeFormat(viewModel.run.value!!.startTime)
            createGpxLauncher.launch("Running Diary - track_$runStartDateTimeString.gpx")
        }

        viewModel.polylineOptions.collectLifecycleFlow(this) { polylineOptions ->
            // 清除
            polylines.forEach { polyline -> polyline.remove() }
            polylines.clear()

            // 添加
            polylineOptions?.forEach { polylineOption ->
                val newPolyline = aMap.addPolyline(polylineOption)
                polylines.add(newPolyline)
            }
        }

        viewModel.allPoints.collectLifecycleFlow(this) {
            zoomToWholeTrack()
        }
    }


    override fun onPause() {
        super.onPause()
        binding.textureMap.onPause()
    }


    override fun onResume() {
        super.onResume()
        binding.textureMap.onResume()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.textureMap.onSaveInstanceState(outState)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.textureMap.onDestroy()
        _binding = null
    }


    private fun zoomToWholeTrack() {

        val bounds = LatLngBounds.Builder()

        viewModel.allPoints
            .value
            ?.map { point ->
                point.toGdLatLng()
            }?.forEach { latLng ->
                bounds.include(latLng)
            }

        val width = binding.textureMap.width
        val height = binding.textureMap.height - (binding.backgroundView.height / 2)

        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(
            /* Bounds */ bounds.build(),
            /* 宽 */ width,
            /* 高 */ height,
            /* padding */ 70
        )

        aMap.animateCamera(cameraUpdate)
    }
}