package com.dicoding.mystory.di

import android.content.Context
import com.dicoding.mystory.data.local.room.MyRoomDatabase
import com.dicoding.mystory.data.repository.StoryRepository
import com.dicoding.mystory.data.remote.retrofit.ApiConfig
import com.dicoding.mystory.helper.ExecutorService

object Injection {
    fun storyRepository(context: Context) : StoryRepository {
        val apiService = ApiConfig.getApiService()
        val database = MyRoomDatabase.getDatabase(context)
        val dao = database.storyDao()
        val appExecutors = ExecutorService()
        return StoryRepository.getInstance(apiService, database, dao, appExecutors)
    }
}