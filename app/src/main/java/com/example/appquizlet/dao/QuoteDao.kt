package com.example.appquizlet.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.appquizlet.entity.QuoteEntity

@Dao
interface QuoteDao {
    @Insert
    suspend fun insertQuote(quoteModel: QuoteEntity)

    @Insert
    suspend fun insertManyQuotes(quotes: List<QuoteEntity>)

    @Update
    suspend fun updateQuote(quoteModel: QuoteEntity)

    @Query("DELETE FROM quoteEntity WHERE quoteId = :quoteId")
    suspend fun deleteQuote(quoteId: Long)

    @Query("SELECT * FROM quoteEntity WHERE userId = :userId")
    fun getLocalQuotes(userId: String): LiveData<List<QuoteEntity>>

    @Query("SELECT * FROM quoteEntity")
    fun getQuotes(): List<QuoteEntity>
//    fun getQuotes(): LiveData<List<QuoteEntity>>


}