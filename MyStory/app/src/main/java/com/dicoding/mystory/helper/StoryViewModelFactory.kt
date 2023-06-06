package com.dicoding.mystory.helper

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.mystory.data.repository.StoryRepository
import com.dicoding.mystory.di.Injection
import com.dicoding.mystory.ui.maps.MapsViewModel
import com.dicoding.mystory.ui.story.StoryViewModel

class StoryViewModelFactory (private val storyRepository: StoryRepository)
    : ViewModelProvider.NewInstanceFactory(){
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(StoryViewModel::class.java) -> {
                StoryViewModel(storyRepository) as T
            }
            modelClass.isAssignableFrom(MapsViewModel::class.java) -> {
                MapsViewModel(storyRepository) as T
            }
            else -> throw IllegalArgumentException("Unkown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var instance: StoryViewModelFactory? = null

        fun getInstance(context: Context): StoryViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: StoryViewModelFactory(Injection.storyRepository(context))
            }.also { instance = it }
    }
}