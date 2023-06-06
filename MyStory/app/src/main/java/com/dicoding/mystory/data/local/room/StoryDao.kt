package com.dicoding.mystory.data.local.room

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dicoding.mystory.data.model.Story

@Dao
interface StoryDao {
    @Query("SELECT * FROM story")
    fun getStoriesPaging(): PagingSource<Int, Story>

    @Query("SELECT * FROM story")
    fun getStories(): LiveData<List<Story>>

    @Query("DELETE FROM story")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertStories(stories: List<Story>)
}