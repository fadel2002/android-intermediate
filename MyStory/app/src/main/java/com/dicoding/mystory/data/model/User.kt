package com.dicoding.mystory.data.model

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

@Entity(tableName = "user")
@Parcelize
data class User (
    val userId: String,
    val name: String,
    val token: String,
    val isLogin: Boolean
) : Parcelable