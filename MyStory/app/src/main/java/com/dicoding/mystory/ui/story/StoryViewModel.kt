package com.dicoding.mystory.ui.story

import androidx.lifecycle.ViewModel
import com.dicoding.mystory.data.repository.StoryRepository

class StoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    fun getPagingStory(token: String) = storyRepository.getPagingStories(token)
}