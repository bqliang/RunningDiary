package com.bqliang.running.diary.ui.statistics

import androidx.databinding.BindingAdapter
import com.mindorks.RadialProgressBar
import com.skydoves.progressview.ProgressView


@BindingAdapter("innerMaxProgress")
fun setInnerMaxProgress(radialProgressBar: RadialProgressBar, max: Int) {
    radialProgressBar.setMaxProgressInnerView(max)
}

@BindingAdapter("outerMaxProgress")
fun setOuterMaxProgress(radialProgressBar: RadialProgressBar, max: Int) {
    radialProgressBar.setMaxProgressOuterView(max)
}

@BindingAdapter("centerMaxProgress")
fun setCenterMaxProgress(radialProgressBar: RadialProgressBar, max: Int) {
    radialProgressBar.setMaxProgressCenterView(max)
}


@BindingAdapter("progressViewMax")
fun setMaxProgress(progressView:ProgressView, max: Float) {
    progressView.max = max
}

@BindingAdapter("progressViewProgress")
fun setProgress(progressView:ProgressView, progress: Float) {
    progressView.progress = progress
}