package com.dicoding.mystory.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.mystory.data.local.datastore.UserPreference
import com.dicoding.mystory.data.model.User
import kotlinx.coroutines.launch

class UserViewModel(private val userPreference: UserPreference) : ViewModel() {
    fun getUser(): LiveData<User> = userPreference.getUser().asLiveData()
    fun logout() {
        viewModelScope.launch { userPreference.logout() }
    }
}