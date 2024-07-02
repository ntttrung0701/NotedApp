package com.example.appquizlet.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    private val userData = MutableLiveData<UserResponse>()

    fun setUserData(data: UserResponse) {
        userData.value = data
    }

    fun getUserData(): LiveData<UserResponse> {
        Log.d("UserViewModel", "getUserData() called")
        return userData
    }
}