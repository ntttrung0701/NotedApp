package com.example.appquizlet.repository

import androidx.lifecycle.LiveData
import com.example.appquizlet.dao.QuoteDao
import com.example.appquizlet.entity.QuoteEntity

class QuoteRepository(private val quoteDao: QuoteDao) {
//    fun getQuote(userId: String): LiveData<List<QuoteEntity>> {
//        return quoteDao.getQuotes(userId)
//    }

    suspend fun insertQuote(quoteModel: QuoteEntity) {
        return quoteDao.insertQuote(quoteModel)
    }

    suspend fun updateQuote(quoteModel: QuoteEntity) {
        return quoteDao.updateQuote(quoteModel)
    }
}