package com.bqliang.running.diary.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 在LifecycleOwner的生命周期范围内收集流的最新数据
 *
 * @param flow 要收集的流
 * @param collect 用于处理收集到的数据的函数
 */
fun <T> LifecycleOwner.collectLifecycleFlow(flow: Flow<T>, collect: (T) -> Unit) =
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest(collect)
        }
    }


/**
 * 在 LifecycleOwner 的生命周期范围内收集流的最新数据
 *
 * @param lifecycleOwner LifecycleOwner
 * @param collect 用于处理收集到的数据的函数
 */
fun <T> Flow<T>.collectLifecycleFlow(lifecycleOwner: LifecycleOwner, collect: (T) -> Unit) =
    lifecycleOwner.collectLifecycleFlow(this, collect)
