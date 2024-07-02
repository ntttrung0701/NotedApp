package com.example.appquizlet

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.example.appquizlet.adapter.QuoteLocalAdapter
import com.example.appquizlet.databinding.ActivityMyQuoteBinding
import com.example.appquizlet.model.MainViewModelFactory
import com.example.appquizlet.model.MyViewModel
import com.example.appquizlet.util.Helper
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MyQuote : AppCompatActivity(), QuoteLocalAdapter.OnQuotifyLocalListener {
    private lateinit var quoteAdapter: QuoteLocalAdapter
    private lateinit var binding: ActivityMyQuoteBinding
    private val repository by lazy { (application as QuoteApplication).remoteQuoteRepository }
    private val myViewModel: MyViewModel by viewModels { MainViewModelFactory(repository) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_quote)

        myViewModel.getLocalQuotes(Helper.getDataUserId(this))

        quoteAdapter = QuoteLocalAdapter(myViewModel, binding.rvQuote)
        quoteAdapter.setOnQuoteShareListener(this)
        binding.rvQuote.adapter = quoteAdapter
        binding.rvQuote.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val snapHelperQuotify = PagerSnapHelper()
        snapHelperQuotify.attachToRecyclerView(binding.rvQuote)
        myViewModel.localQuote.observe(this) {
            if (myViewModel.localQuote.value?.isEmpty() == true) {
                binding.rvQuote.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            } else {
                binding.rvQuote.visibility = View.VISIBLE
                binding.layoutNoData.visibility = View.GONE
            }
            quoteAdapter.notifyDataSetChanged()
        }

        binding.txtCreateNewSet.setOnClickListener {
            val i = Intent(this, CreateQuote::class.java)
            startActivity(i)
        }

        binding.txtBack.setOnClickListener {
            finish()
        }
    }

    override fun handleShareQuote(position: Int) {
        val quoteText = quoteAdapter.getQuoteText(position)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, quoteText.joinToString("\n"))
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, null))
    }

    override fun handleDeleteQuote(quoteId: Long) {
        MaterialAlertDialogBuilder(this).setTitle(resources.getString(R.string.warning))
            .setMessage(resources.getString(R.string.confirm_delete_quote))
            .setNeutralButton(resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
                myViewModel.deleteQuote(quoteId)
            }.show()
    }
}