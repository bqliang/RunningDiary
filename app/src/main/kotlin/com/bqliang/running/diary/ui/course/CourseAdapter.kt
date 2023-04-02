package com.bqliang.running.diary.ui.course

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bqliang.running.diary.databinding.ItemCourseBinding
import com.bqliang.running.diary.ui.course.model.Course
import com.bumptech.glide.Glide

class CourseAdapter(
    private val courseList: List<Course>,
    private val onItemClick: (Course) -> Unit
    ) :
    RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    inner class CourseViewHolder(private val binding: ItemCourseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(course: Course) {
            Glide.with(binding.root).load(course.imageUrl).into(binding.imageView)
            binding.tvCourseName.text = course.name
            binding.tvCourseInfo.text =
                "${course.levelString}  ${course.distanceInMinute} 分钟  ${course.caloriesInKcal}千卡"
            binding.tvCourseParticipants.text = (100..90000).random().toString() + " 人已参与"
            binding.mask.setOnClickListener {
                onItemClick(course)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemCourseBinding.inflate(layoutInflater, parent, false)
        return CourseViewHolder(binding)
    }


    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(courseList[position])
    }


    override fun getItemCount() = courseList.size
}