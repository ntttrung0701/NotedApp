package com.example.appquizlet.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appquizlet.repository.QuoteRepository
import com.example.appquizlet.repository.RemoteQuoteRepository

class MainViewModelFactory(private val quoteRepository: RemoteQuoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MyViewModel(quoteRepository) as T
    }
}