package com.example.appquizlet.api.retrofit

import com.example.appquizlet.util.Constants
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {
    private const val baseUrl = Constants.baseUrl
    private val credentials = Credentials.basic("11167378", "60-dayfreetrial")


    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder().addInterceptor(AuthInterceptor(credentials)).build()
            )
            .build()
    }
}

