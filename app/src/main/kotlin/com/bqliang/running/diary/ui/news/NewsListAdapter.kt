package com.bqliang.running.diary.ui.news

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bqliang.running.diary.databinding.ItemNewsBinding
import com.bumptech.glide.Glide

class NewsListAdapter(
    private val onItemClick: (News) -> Unit
) : ListAdapter<News, NewsListAdapter.NewsViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<News>() {
            override fun areItemsTheSame(oldItem: News, newItem: News): Boolean {
                return oldItem.mdContent == newItem.mdContent
            }

            override fun areContentsTheSame(oldItem: News, newItem: News): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class NewsViewHolder(val binding: ItemNewsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(news: News) {
            binding.apply {
                tvNewsTitle.text = news.title
                tvNewsHighlight.text = news.highlight
                binding.root.context
                Glide.with(binding.root).load(news.imgUrl).into(ivNewsImage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemNewsBinding.inflate(inflater, parent, false)
        val viewHolder = NewsViewHolder(binding)
        viewHolder.binding.newsItem.setOnClickListener {
            val news = getItem(viewHolder.absoluteAdapterPosition)
            onItemClick(news)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}