package com.dicoding.mystory.data.remote.response.story

import com.google.gson.annotations.SerializedName

data class AddStoryResponse (
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String
)