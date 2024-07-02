package com.example.appquizlet.model

import android.app.ProgressDialog
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appquizlet.R
import com.example.appquizlet.custom.CustomToast
import com.example.appquizlet.entity.QuoteEntity
import com.example.appquizlet.repository.RemoteQuoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyViewModel(private val quoteRepository: RemoteQuoteRepository) : ViewModel() {

    //    val dataQuote: MutableLiveData<List<QuoteModel>> = MutableLiveData()
    val isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    private var currentPosition = 0
    fun getCurrentPosition(): Int {
        return currentPosition
    }

    fun setCurrentPosition(position: Int) {
        currentPosition = position
    }

    fun getNextQuotePosition(): Int {
        return if (currentPosition < quotes.value?.results?.size!!) {
            currentPosition + 1
        } else {
            currentPosition
        }
    }

    fun getPrevQuotePosition(): Int {
        return if (currentPosition > 0) {
            currentPosition - 1
        } else {
            currentPosition
        }
    }

    val quotes: LiveData<QuoteResponse>
        get() = quoteRepository.quotes

    val localQuote: LiveData<List<QuoteEntity>>
        get() = quoteRepository.localQuotes

    //    init {
    fun getRemoteQuote(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            // Set isLoading to true before making the network call
            isLoading.postValue(true)

            try {
                quoteRepository.getQuotes(1)
            } catch (e: Exception) {
                CustomToast(context).makeText(
                    context,
                    e.message.toString(),
                    CustomToast.LONG,
                    CustomToast.ERROR
                ).show()
            } finally {
                // Set isLoading to false after the network call is complete
                isLoading.postValue(false)
            }
        }
}
//    }

    fun getLocalQuotes(userId: String) {
        Log.d("getLocalQuotes", userId.toString())
        viewModelScope.launch(Dispatchers.IO) {
            quoteRepository.getLocalQuotes(userId)
        }
    }

    fun insertQuote(quoteModel: QuoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            quoteRepository.insertQuote(quoteModel)
        }
    }

    fun deleteQuote(quoteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            quoteRepository.deleteLocalQuote(quoteId)
        }
    }

//    fun updateQuote(quoteModel: QuoteEntity) {
//        viewModelScope.launch(Dispatchers.IO) {
//            quoteRepository.updateQuote(quoteModel)
//        }
//    }

}