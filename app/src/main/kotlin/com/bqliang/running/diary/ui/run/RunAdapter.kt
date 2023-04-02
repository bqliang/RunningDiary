package com.bqliang.running.diary.ui.list


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bqliang.running.diary.database.entity.Run
import com.bqliang.running.diary.databinding.RunItemBinding
import com.bqliang.running.diary.utils.millisecondsHmsFormat
import com.bqliang.running.diary.utils.millsSecondsDateTimeFormat


class RunAdapter(
    private val onItemClicked: (Run) -> Unit
) : ListAdapter<Run, RunAdapter.RunViewHolder>(RunDiffCallback) {

    companion object {
        private val RunDiffCallback = object : DiffUtil.ItemCallback<Run>() {
            override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class RunViewHolder(private var binding: RunItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(run: Run) {
            binding.apply {
                img.setImageBitmap(run.img)
                headline.text = String.format("%.2f KM", run.distanceInMeter / 1000)
                supportingText.text = "${millisecondsHmsFormat(run.durationInMilliseconds)}\n${run.caloriesInKcal} Kcal"
                trailingSupportingText.text = millsSecondsDateTimeFormat(run.startTime).replace(" ", "\n")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        val binding = RunItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        val viewHolder = RunViewHolder(binding)
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition
            onItemClicked(getItem(position))
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}