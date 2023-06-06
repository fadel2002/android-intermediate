package com.dicoding.mystory.ui.story

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.mystory.data.model.Story
import com.dicoding.mystory.databinding.ItemRowStoryBinding
import com.dicoding.mystory.helper.withDateFormat

class StoryPagingAdapter(
    private val onClick: (Story) -> Unit
) :
    PagingDataAdapter<Story, StoryPagingAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemRowStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }

    inner class StoryViewHolder(private val binding: ItemRowStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: Story) {
            binding.apply {
                binding.tvCreateAt.withDateFormat(story.createdAt)
                binding.tvDescription.text = story.description
                itemView.setOnClickListener {
                    onClick(story)
                }
                binding.tvName.text = story.name
                Glide.with(binding.root.context)
                    .load(story.photoUrl)
                    .into(binding.imgPhotoStory)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(
                oldItem: Story,
                newItem: Story
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: Story,
                newItem: Story
            ): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}