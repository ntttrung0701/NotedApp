package com.example.appquizlet

import android.app.Application
import android.util.Log
import com.example.appquizlet.api.retrofit.QuoteApiService
import com.example.appquizlet.api.retrofit.RetrofitHelperQuote
import com.example.appquizlet.repository.RemoteQuoteRepository
import com.example.appquizlet.roomDatabase.QuoteDatabase
import com.google.gson.Gson

class QuoteApplication : Application() {
     lateinit var remoteQuoteRepository: RemoteQuoteRepository
    override fun onCreate() {
        super.onCreate()
        initialize()
    }

    private fun initialize() {
        val quoteApiService = RetrofitHelperQuote.getInstance().create(QuoteApiService::class.java)
        val database = QuoteDatabase.getDatabase(applicationContext)
        remoteQuoteRepository = RemoteQuoteRepository(quoteApiService, database, applicationContext)
    }
}