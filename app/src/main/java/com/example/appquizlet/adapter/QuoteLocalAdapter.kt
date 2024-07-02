package com.example.appquizlet.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appquizlet.databinding.QuoteItemWithDeleteBinding
import com.example.appquizlet.model.MyViewModel

class QuoteLocalAdapter(
    private val viewModel: MyViewModel,
    private val recyclerView: RecyclerView
) :
    RecyclerView.Adapter<QuoteLocalAdapter.QuotifyViewHolder>() {

    interface OnQuotifyLocalListener {
        fun handleShareQuote(position: Int)
        fun handleDeleteQuote(quoteId: Long)
    }

    private var onQuotifyListener: OnQuotifyLocalListener? = null

    class QuotifyViewHolder(val binding: QuoteItemWithDeleteBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuotifyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = QuoteItemWithDeleteBinding.inflate(inflater, parent, false)
        return QuotifyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuotifyViewHolder, position: Int) {
        val item = viewModel.localQuote.value?.get(position)
//        item?.let {
//            holder.bind(it, onQuotifyListener)
//        }
        holder.binding.quoteText.text = item?.content
        holder.binding.quoteAuthor.text = item?.author
        holder.binding.btnShareQuote.setOnClickListener {
            onQuotifyListener?.handleShareQuote(position)
        }

        holder.binding.btnDeleteQuote.setOnClickListener {
            if (item != null) {
                onQuotifyListener?.handleDeleteQuote(item.quoteId)
            }
        }
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return viewModel.localQuote.value?.size ?: 0
    }

    fun handleNextQuote() {
        val nextPosition = viewModel.getNextQuotePosition()
        viewModel.setCurrentPosition(nextPosition)
        recyclerView.smoothScrollToPosition(nextPosition)
    }

    fun handlePrevQuote() {
        val prevPosition = viewModel.getPrevQuotePosition()
        viewModel.setCurrentPosition(prevPosition)
        recyclerView.smoothScrollToPosition(prevPosition)
    }

    fun getQuoteText(position: Int): List<String?> {
        val item = viewModel.quotes.value?.results?.get(position)
        return listOf(item?.content, item?.author)
    }

    fun setOnQuoteShareListener(listener: OnQuotifyLocalListener) {
        onQuotifyListener = listener
    }

}
