package com.dicoding.mystory.data.remote.response.auth

import com.google.gson.annotations.SerializedName

data class LoginResult (
    @field:SerializedName("userId")
    val userId: String,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("token")
    val token: String
)