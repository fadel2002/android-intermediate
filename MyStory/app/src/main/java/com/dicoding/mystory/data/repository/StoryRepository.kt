package com.dicoding.mystory.data.repository

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.paging.*
import com.dicoding.mystory.data.Result
import com.dicoding.mystory.data.StoryRemoteMediator
import com.dicoding.mystory.data.local.room.MyRoomDatabase
import com.dicoding.mystory.data.local.room.StoryDao
import com.dicoding.mystory.data.model.Story
import com.dicoding.mystory.data.remote.response.story.StoryResponse
import com.dicoding.mystory.data.remote.retrofit.ApiService
import com.dicoding.mystory.helper.ExecutorService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class StoryRepository (
    private val apiService: ApiService,
    private val database: MyRoomDatabase,
    private val storyUserDao: StoryDao,
    private val appExecutors: ExecutorService,
){
    private val result = MediatorLiveData<Result<List<Story>>>()

    fun getPagingStories(token: String): LiveData<PagingData<Story>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 4
            ),
            remoteMediator = StoryRemoteMediator(token, database, apiService),
            pagingSourceFactory = {
                database.storyDao().getStoriesPaging()
            }
        ).liveData
    }

    fun getAllStory(token: String, isWithLocation: Int): LiveData<Result<List<Story>>>{
        var client = if (isWithLocation == 1){
            apiService.getAllStoryCall(token, null ,null, 1)
        }else {
            apiService.getAllStoryCall(token, null ,null, 0)
        }
        client.enqueue(object : Callback<StoryResponse>{
            override fun onResponse(
                call: Call<StoryResponse>,
                response: Response<StoryResponse>
            ) {
                if(response.isSuccessful){
                    val allStory = response.body()?.listStory
                    val storyList = ArrayList<Story>()
                    appExecutors.diskIO.execute{
                        allStory?.forEach { allStory ->
                            val story = Story(
                                allStory.id,
                                allStory.name,
                                allStory.description,
                                allStory.photoUrl,
                                allStory.createdAt,
                                allStory.lat,
                                allStory.lon
                            )
                            storyList.add(story)
                        }
                        storyUserDao.deleteAll()
                        storyUserDao.insertStories(storyList)
                    }
                }
                Log.e(TAG,response.message())
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                result.value = Result.Error(IOException("Error logging in", t))
                t.message?.let { Log.e(TAG,it) }
            }
        })
        val data = storyUserDao.getStories()
        result.addSource(data){storyData: List<Story> ->
            result.value = Result.Success(storyData)
        }
        return result
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            apiService: ApiService,
            database: MyRoomDatabase,
            storyDao: StoryDao,
            appExecutors: ExecutorService
        ) : StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, database, storyDao, appExecutors)
            }.also { instance = it }
    }
}