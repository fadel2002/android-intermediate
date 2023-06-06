package com.dicoding.mystory.helper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.mystory.data.local.datastore.UserPreference
import com.dicoding.mystory.ui.UserViewModel
import com.dicoding.mystory.ui.login.LoginViewModel

class AuthViewModelFactory (private val preference: UserPreference) : ViewModelProvider.NewInstanceFactory(){
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(preference) as T
        }else if(modelClass.isAssignableFrom(UserViewModel::class.java)){
            return UserViewModel(preference) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}