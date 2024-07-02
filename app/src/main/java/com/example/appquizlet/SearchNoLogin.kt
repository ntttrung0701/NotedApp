package com.example.appquizlet

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appquizlet.adapter.FlashcardItemAdapter
import com.example.appquizlet.adapter.StudySetItemAdapter
import com.example.appquizlet.api.retrofit.ApiService
import com.example.appquizlet.api.retrofit.RetrofitHelper
import com.example.appquizlet.custom.CustomToast
import com.example.appquizlet.databinding.ActivitySearchNoLoginBinding
import com.example.appquizlet.interfaceFolder.RvFlashCard
import com.example.appquizlet.model.FlashCardModel
import com.example.appquizlet.util.Helper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchNoLogin : AppCompatActivity() {
    private lateinit var binding: ActivitySearchNoLoginBinding
    private lateinit var apiService: ApiService
    private lateinit var progressDialog: ProgressDialog
    private var listFlashcardDetails: MutableList<FlashCardModel> = mutableListOf()
    private var listCards: MutableList<FlashCardModel> = mutableListOf()
    private lateinit var adapterStudySet: StudySetItemAdapter
    private lateinit var adapterFlashcardDetail: FlashcardItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchNoLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val setId = intent.getStringExtra("setId")
        if (setId != null) {
            getSetShareView(Helper.getDataUserId(this), setId)
        }



        apiService = RetrofitHelper.getInstance().create(ApiService::class.java)
        binding.layoutFlashcardLearn.setOnClickListener {
            showInviteDialog(resources.getString(R.string.no_available_in_preview))
        }
        binding.layoutFlashcardTest.setOnClickListener {
            showInviteDialog(resources.getString(R.string.no_available_in_preview))
        }

        binding.btnSignIn.setOnClickListener {
            val i = Intent(this, SignIn::class.java)
            startActivity(i)
        }

        binding.btnSignUp.setOnClickListener {
            val i = Intent(this, SignUp::class.java)
            startActivity(i)
        }
    }

    private fun showLoading(context: Context, msg: String) {
        progressDialog = ProgressDialog.show(context, null, msg)
    }

    private fun showBannerDialog(desc: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(desc)
        builder.create().show()
    }

    private fun showInviteDialog(desc: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(desc)
        builder.setPositiveButton(resources.getString(R.string.sign_up)) { dialog, _ ->
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
        builder.setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun getSetShareView(userId: String, setId: String) {
        lifecycleScope.launch {
            showLoading(this@SearchNoLogin, resources.getString(R.string.loading_data))
            try {
                val result = apiService.getSetShareView(userId, setId)
                if (result.isSuccessful) {
                    result.body().let {
                        withContext(Dispatchers.Main) {
                            // UI updates should be done on the main thread
                            CustomToast(this@SearchNoLogin).makeText(
                                this@SearchNoLogin,
                                resources.getString(R.string.loading_data_successful),
                                CustomToast.LONG,
                                CustomToast.SUCCESS
                            ).show()

                            result.body()?.let {
                                listCards.addAll(it.cards)
                                listFlashcardDetails.addAll(it.cards)

                                binding.txtSetName.text = it.name
                                binding.txtStudySetDetailUsername.text = it.nameOwner

                                adapterStudySet =
                                    StudySetItemAdapter(listCards, object : RvFlashCard {
                                        override fun handleClickFLashCard(flashcardItem: FlashCardModel) {
                                            flashcardItem.isUnMark =
                                                flashcardItem.isUnMark?.not() ?: true
                                            adapterStudySet.notifyDataSetChanged()
                                        }
                                    })
                                binding.viewPagerStudySet.adapter = adapterStudySet

                                val indicators = binding.circleIndicator3
                                indicators.setViewPager(binding.viewPagerStudySet)

                                adapterFlashcardDetail = FlashcardItemAdapter(listFlashcardDetails)
                                binding.rvAllFlashCards.layoutManager =
                                    LinearLayoutManager(this@SearchNoLogin)
                                binding.rvAllFlashCards.adapter = adapterFlashcardDetail
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        result.errorBody()?.string()?.let {
                            CustomToast(this@SearchNoLogin).makeText(
                                this@SearchNoLogin, it, CustomToast.LONG, CustomToast.ERROR
                            ).show()
                        }
                        binding.layoutNoData.visibility = View.VISIBLE
                        binding.layoutHasData.visibility = View.GONE
                    }
                }

            } catch (e: Exception) {
                CustomToast(this@SearchNoLogin).makeText(
                    this@SearchNoLogin, e.message.toString(), CustomToast.LONG, CustomToast.ERROR
                ).show()
                binding.layoutNoData.visibility = View.VISIBLE
                binding.layoutHasData.visibility = View.GONE
            } finally {
                progressDialog.dismiss()
            }
        }
    }
}