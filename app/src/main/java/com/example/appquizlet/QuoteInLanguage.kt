package com.example.appquizlet

import QuotifyAdapter
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.example.appquizlet.databinding.ActivityQuoteInLanguageBinding
import com.example.appquizlet.entity.QuoteEntity
import com.example.appquizlet.model.MainViewModelFactory
import com.example.appquizlet.model.MyViewModel
import com.example.appquizlet.util.Helper
import com.google.android.material.snackbar.Snackbar


class QuoteInLanguage : AppCompatActivity(), QuotifyAdapter.OnQuotifyListener {
    lateinit var binding: ActivityQuoteInLanguageBinding
    private lateinit var quoteAdapter: QuotifyAdapter
    private var progressDialog: ProgressDialog? = null
    private val repository by lazy { (application as QuoteApplication).remoteQuoteRepository }
    private val myViewModel: MyViewModel by viewModels { MainViewModelFactory(repository) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_quote_in_language)

        myViewModel.getRemoteQuote(this)

        binding.txtBack.setOnClickListener {
            finish()
        }

        quoteAdapter = QuotifyAdapter(myViewModel, binding.rvQuote)
        binding.rvQuote.adapter = quoteAdapter
        binding.rvQuote.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val snapHelperQuotify = PagerSnapHelper()
        snapHelperQuotify.attachToRecyclerView(binding.rvQuote)
        myViewModel.quotes.observe(this) {
            quoteAdapter.notifyDataSetChanged()
        }

        myViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                progressDialog = ProgressDialog.show(this, null, getString(R.string.loading_data))
            } else {
                // Dismiss loading indicator
                progressDialog?.dismiss()
            }
        }

        binding.txtNextQuote.setOnClickListener {
            quoteAdapter.handleNextQuote()
        }

        binding.txtPrevQuote.setOnClickListener {
            quoteAdapter.handlePrevQuote()
        }

        quoteAdapter.setOnQuoteShareListener(this)


        binding.txtCreateNewQuote.setOnClickListener {
            val i = Intent(this, CreateQuote::class.java)
            startActivity(i)
        }

        binding.txtSaved.setOnClickListener {
            val i = Intent(this, MyQuote::class.java)
            startActivity(i)
        }

//            GlobalScope.launch {
//                quoteDatabase.quoteDao().insertQuote(
//                    QuoteEntity(
//                        textQuote = "hehehe",
//                        quoteAuthor = "cong tuan2",
//                        userId = "12"
//                    )
//                )
//            }

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

    override fun handleAddToMyQuote(position: Int) {
        val quoteText = quoteAdapter.getQuoteText(position)
        val contentQuote = quoteText[0].toString()
        val authorQuote = quoteText[1].toString()
        myViewModel.insertQuote(
            QuoteEntity(
                0, "", contentQuote, authorQuote, "", 0, Helper.getDataUserId(this)
            )
        )
        Snackbar.make(binding.txtSaved, resources.getString(R.string.add_to_your_quote_success), Snackbar.LENGTH_LONG)
            .setAction(resources.getString(R.string.view)) {
                val i = Intent(this, MyQuote::class.java)
                startActivity(i)
            }
            .show()

    }
    //        fun getQuote(view: View) {
//        quoteDatabase.quoteDao().getQuotes("12").observe(this) {
//            Log.d("dataQuote", Gson().toJson(it))
//        }
//    }
}