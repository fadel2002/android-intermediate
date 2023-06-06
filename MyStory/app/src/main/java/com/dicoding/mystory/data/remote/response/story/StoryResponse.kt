package com.dicoding.mystory.data.remote.response.story

import com.dicoding.mystory.data.model.Story
import com.google.gson.annotations.SerializedName

data class StoryResponse(

    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("listStory")
    val listStory: List<Story>
)