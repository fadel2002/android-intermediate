package com.dicoding.mystory.ui.maps

import androidx.lifecycle.*
import com.dicoding.mystory.data.repository.StoryRepository

class MapsViewModel(private val repository: StoryRepository): ViewModel()  {
    fun getAllStory(token: String) = repository.getAllStory(token, 1)
}