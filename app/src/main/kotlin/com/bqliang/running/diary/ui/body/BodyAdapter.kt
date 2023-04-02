package com.bqliang.running.diary.ui.body

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bqliang.running.diary.database.entity.Body
import com.bqliang.running.diary.databinding.ItemBodyBinding
import com.bqliang.running.diary.utils.millisSecondsDateFormat


class BodyAdapter(
    private val onItemClicked: (Body) -> Unit
) : ListAdapter<Body, BodyAdapter.BodyViewHolder>(RunDiffCallback) {

    companion object {
        private val RunDiffCallback = object : DiffUtil.ItemCallback<Body>() {
            override fun areItemsTheSame(oldItem: Body, newItem: Body): Boolean {
                return oldItem.dateInMillisSeconds == newItem.dateInMillisSeconds
            }

            override fun areContentsTheSame(oldItem: Body, newItem: Body): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class BodyViewHolder(private var binding: ItemBodyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(body: Body, lastBody: Body) {
            binding.apply {
                headline.text = String.format("%.1f Kg  %.1f Cm", body.weightInKg, body.heightInCm)
                supportingText.text = "%s  %s".format(
                    millisSecondsDateFormat(body.dateInMillisSeconds),
                    bmiString(body.heightInCm, body.weightInKg)
                )
                val weightChange = body.weightInKg - lastBody.weightInKg
                val symbol = if (weightChange > 0) "+" else ""
                trailingSupportingText.text = "$symbol%.1f".format(body.weightInKg - lastBody.weightInKg)
            }
        }

        private fun bmiString(heightInCm: Float, weightInKg: Float): String {
            val heightInM = heightInCm / 100
            val bmi =  weightInKg / (heightInM * heightInM)
            val desc = when(bmi) {
                in 0.0..18.5 -> "过轻"
                in 18.5..24.0 -> "正常"
                in 24.0..28.0 -> "过重"
                in 28.0..32.0 -> "肥胖"
                else -> "非常肥胖"
            }

            return "BMI: %.1f %s".format(bmi, desc)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BodyViewHolder {
        val binding = ItemBodyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        val viewHolder = BodyViewHolder(binding)
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition
            onItemClicked(getItem(position))
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: BodyViewHolder, position: Int) {
        holder.bind(getItem(position), getLastItem(position))
    }

    private fun getLastItem(position: Int) = try {
        getItem(position + 1)
    } catch (e: Exception) {
        getItem(position)
    }
}