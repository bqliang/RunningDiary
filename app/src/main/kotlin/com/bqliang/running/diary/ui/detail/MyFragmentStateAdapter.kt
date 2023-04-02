package com.bqliang.running.diary.ui.detail

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MyFragmentStateAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount() = 3

    override fun createFragment(position: Int) = when (position) {
        0 -> TrackFragment()
        1 -> DetailFragment()
        else -> NoteFragment()
    }
}
