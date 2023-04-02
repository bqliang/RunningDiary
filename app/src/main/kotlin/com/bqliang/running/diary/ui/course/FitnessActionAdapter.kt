package com.bqliang.running.diary.ui.course

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bqliang.running.diary.databinding.ItemActionBinding
import com.bqliang.running.diary.ui.course.model.FitnessAction
import com.bumptech.glide.Glide

class FitnessActionAdapter(
    private val fitnessActionList: List<FitnessAction>,
    private val onItemClick: (index: Int) -> Unit
) : RecyclerView.Adapter<FitnessActionAdapter.FitnessActionViewHolder>() {

    inner class FitnessActionViewHolder(private val binding: ItemActionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(fitnessAction: FitnessAction) {
            Glide.with(binding.root).load(fitnessAction.videoUrl).into(binding.imageView)
            binding.headline.text = fitnessAction.name
            binding.supportingText.text = fitnessAction.countAndDurationStr
            binding.itemFitnessAction.setOnClickListener {
                onItemClick(absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FitnessActionViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemActionBinding.inflate(layoutInflater, parent, false)
        return FitnessActionViewHolder(binding)
    }

    override fun getItemCount() = fitnessActionList.size

    override fun onBindViewHolder(holder: FitnessActionViewHolder, position: Int) {
        holder.bind(fitnessActionList[position])
    }
}