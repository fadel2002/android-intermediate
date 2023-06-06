package com.dicoding.mystory.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.mystory.data.local.datastore.UserPreference
import com.dicoding.mystory.data.model.User
import kotlinx.coroutines.launch

class LoginViewModel (private val preferences: UserPreference): ViewModel(){
    fun saveUser(user: User){
        viewModelScope.launch {
            preferences.saveUser(user)
        }
    }
}