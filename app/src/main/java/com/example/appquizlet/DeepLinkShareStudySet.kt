package com.example.appquizlet

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appquizlet.adapter.FlashcardItemAdapter
import com.example.appquizlet.adapter.StudySetItemAdapter
import com.example.appquizlet.api.retrofit.ApiService
import com.example.appquizlet.api.retrofit.RetrofitHelper
import com.example.appquizlet.custom.CustomToast
import com.example.appquizlet.databinding.ActivityDeepLinkShareStudySetBinding
import com.example.appquizlet.interfaceFolder.RvFlashCard
import com.example.appquizlet.model.FlashCardModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeepLinkShareStudySet : AppCompatActivity() {
    private lateinit var binding: ActivityDeepLinkShareStudySetBinding
    private lateinit var apiService: ApiService
    private lateinit var progressDialog: ProgressDialog
    private var listFlashcardDetails: MutableList<FlashCardModel> = mutableListOf()
    private var listCards: MutableList<FlashCardModel> = mutableListOf()
    private lateinit var adapterStudySet: StudySetItemAdapter
    private lateinit var adapterFlashcardDetail: FlashcardItemAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeepLinkShareStudySetBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        // Xử lý thông tin từ liên kết
        val studySetId = extractStudySetIdFromIntent(intent)
        val userId = extractUserIdFromIntent(intent)
        Log.d("rrr",studySetId.toString() + " " + userId)
        if (studySetId != null && userId != null) {
            getSetShareView(userId, studySetId)
        } else {
            CustomToast(this).makeText(
                this,
                resources.getString(R.string.sth_went_wrong),
                CustomToast.LONG,
                CustomToast.ERROR
            ).show()
        }
    }

    private fun getSetShareView(userId: String, setId: String) {
        lifecycleScope.launch {
            showLoading(this@DeepLinkShareStudySet, resources.getString(R.string.loading_data))
            try {
                val result = apiService.getSetShareView(userId, setId)
                if (result.isSuccessful) {
                    result.body().let {
                        withContext(Dispatchers.Main) {
                            // UI updates should be done on the main thread
                            CustomToast(this@DeepLinkShareStudySet).makeText(
                                this@DeepLinkShareStudySet,
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
                                    LinearLayoutManager(this@DeepLinkShareStudySet)
                                binding.rvAllFlashCards.adapter = adapterFlashcardDetail
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        result.errorBody()?.string()?.let {
                            CustomToast(this@DeepLinkShareStudySet).makeText(
                                this@DeepLinkShareStudySet,
                                it,
                                CustomToast.LONG,
                                CustomToast.ERROR
                            ).show()
                        }
                        binding.layoutNoData.visibility = View.VISIBLE
                        binding.layoutHasData.visibility = View.GONE
                    }
                }

            } catch (e: Exception) {
                CustomToast(this@DeepLinkShareStudySet).makeText(
                    this@DeepLinkShareStudySet,
                    e.message.toString(),
                    CustomToast.LONG,
                    CustomToast.ERROR
                ).show()
                binding.layoutNoData.visibility = View.VISIBLE
                binding.layoutHasData.visibility = View.GONE
            } finally {
                progressDialog.dismiss()
            }
        }
    }

    private fun showLoading(context: Context, msg: String) {
        progressDialog = ProgressDialog.show(context, null, msg)
    }

    private fun extractStudySetIdFromIntent(intent: Intent): String? {
        val uri: Uri? = intent.data
        return uri?.lastPathSegment
    }

    private fun extractUserIdFromIntent(intent: Intent): String? {
        val uri: Uri? = intent.data
        val regex = Regex("/studyset/([^/]+)/([^/]+)$")
        return uri?.path?.let {
            regex.find(it)?.groupValues?.get(1)
        }
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
}