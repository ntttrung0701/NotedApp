package com.example.appquizlet.api.retrofit

import com.example.appquizlet.util.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelperQuote {
    private const val baseUrlQuote = Constants.baseUrlQuote

    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrlQuote)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }
}