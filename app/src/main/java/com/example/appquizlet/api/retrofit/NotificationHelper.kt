package com.example.appquizlet.api.retrofit

import com.example.appquizlet.util.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NotificationHelper {
    const val baseUrl = Constants.notiBaseUrl

    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }
}