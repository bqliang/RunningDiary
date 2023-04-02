package com.bqliang.running.diary.ui.body

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import cn.leancloud.LCUser
import com.bqliang.running.diary.R
import com.bqliang.running.diary.databinding.ActivityWeightChartBinding
import com.bqliang.running.diary.ui.base.BaseActivity
import com.bqliang.running.diary.utils.collectLifecycleFlow
import com.bqliang.running.diary.utils.shortId
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.google.android.material.color.MaterialColors
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class WeightChartActivity : BaseActivity() {

    private val binding: ActivityWeightChartBinding by lazy {
        ActivityWeightChartBinding.inflate(layoutInflater)
    }
    val viewModel: BodyListViewModel by viewModels()
    private lateinit var chart: LineChart
    private val colorPrimary by lazy {
        MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorPrimary,
            Color.BLACK
        )
    }
    private val colorTertiary by lazy {
        MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorTertiary,
            Color.WHITE
        )
    }

    private val colorTertiaryContainer by lazy {
        MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorTertiaryContainer,
            Color.WHITE
        )
    }

    private val colorOnSurface by lazy {
        MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorOnSurface,
            Color.WHITE
        )
    }

    private val colorSurface by lazy {
        MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorSurface,
            Color.WHITE
        )
    }

    private val simpleDateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

    private fun msFormat(ms: Long): String {
        val date = Date(ms)
        return simpleDateFormat.format(date)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        window.statusBarColor = Color.TRANSPARENT
        binding.lifecycleOwner = this
        setContentView(binding.root)
        chart = binding.weightChart

        binding.btnRotate.setOnClickListener {
            requestedOrientation = when (requestedOrientation) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                else -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }


        chart.setNoDataText("暂无数据")
        chart.setNoDataTextColor(
            MaterialColors.getColor(
                this,
                com.google.android.material.R.attr.colorOnSurface,
                Color.WHITE
            )
        )


        viewModel.allBody(LCUser.currentUser().shortId).map { bodyList ->
            val sortBodyList = bodyList.sortedBy { it.dateInMillisSeconds }
            val entries = sortBodyList.map { body ->
                Entry((body.dateInMillisSeconds / 1000).toFloat(), body.weightInKg)
            }
            entries

        }.collectLifecycleFlow(this) { entries ->
            if (entries.isEmpty()) return@collectLifecycleFlow

            val lineDataSet = LineDataSet(entries, "体重").apply {
                color = colorPrimary
                lineWidth = 2f
                setDrawCircles(true) // 显示圆点
                circleRadius = 4f // 圆点半径
                circleHoleColor = colorTertiary // 圆点内部颜色
                circleColors = listOf(colorTertiary) // 圆点外部颜色
                mode = LineDataSet.Mode.CUBIC_BEZIER // 平滑曲线
                valueTextSize = 14f // 设置数值字体大小
                valueTextColor = colorOnSurface // 设置数值字体颜色
                fillColor = colorTertiaryContainer // 填充颜色
                setDrawFilled(true) // 填充曲线下方区域
            }

            // LineData: 多条曲线
            val lineData = LineData(lineDataSet).apply {
                setDrawValues(true) // 显示曲线点的具体数值
            }

            chart.marker = MyMarkerView(this)
            chart.setBackgroundColor(colorSurface) // 背景色
            chart.xAxis.textColor = colorOnSurface // X轴字体颜色
            chart.isScaleYEnabled = false // 禁止Y轴缩放
            chart.description.isEnabled = false // 禁止显示描述
            chart.legend.isEnabled = false // 禁止显示图例
            chart.axisLeft.isEnabled = false // 禁止显示左侧Y轴
            chart.axisRight.isEnabled = false // 禁止显示右侧Y轴
            chart.xAxis.position = XAxis.XAxisPosition.BOTTOM // X轴显示在底部
            chart.xAxis.granularity = (60 * 60 * 24).toFloat()  // X轴最小间隔 = 1 day
            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float) = msFormat((value * 1000).toLong())
            }
            chart.data = lineData

            chart.invalidate()
        }
    }
}


class MyMarkerView(context: Context) : MarkerView(context, R.layout.marker_view) {
    private val tvWeight: MaterialTextView = findViewById(R.id.tv_weight)
    private val tvDate: MaterialTextView = findViewById(R.id.tv_date)
    private val simpleDateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())

    @SuppressLint("SetTextI18n")
    override fun refreshContent(e: Entry, highlight: Highlight?) {
        tvWeight.text = "%.2f KG".format(e.y)
        tvDate.text = simpleDateFormat.format(Date(e.x.toLong() * 1000))
        super.refreshContent(e, highlight)
    }

//    override fun getOffset(): MPPointF {
//        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
//    }
}
