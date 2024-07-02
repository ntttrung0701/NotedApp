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
import com.example.appquizlet.adapter.RvStudySetItemAdapter
import com.example.appquizlet.api.retrofit.ApiService
import com.example.appquizlet.api.retrofit.RetrofitHelper
import com.example.appquizlet.custom.CustomToast
import com.example.appquizlet.databinding.ActivityDeepLinkShareFolderBinding
import com.example.appquizlet.interfaceFolder.RVStudySetItem
import com.example.appquizlet.model.StudySetModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeepLinkShareFolder : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog
    private lateinit var binding: ActivityDeepLinkShareFolderBinding
    private lateinit var apiService: ApiService
    private lateinit var folderId: String
    private val listStudySet = mutableListOf<StudySetModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeepLinkShareFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = RetrofitHelper.getInstance().create(ApiService::class.java)


        binding.btnSignIn.setOnClickListener {
            val i = Intent(this, SignIn::class.java)
            startActivity(i)
        }

        binding.btnSignUp.setOnClickListener {
            val i = Intent(this, SignUp::class.java)
            startActivity(i)
        }


        // Xử lý thông tin từ liên kết
        folderId = extractStudySetIdFromIntent(intent).toString()
        val userId = extractUserIdFromIntent(intent)

        Log.d("rrr",folderId.toString() + " " + userId)

        if (userId != null) {
            getFolderShareView(userId, folderId)
        } else {
            CustomToast(this).makeText(
                this,
                resources.getString(R.string.sth_went_wrong),
                CustomToast.LONG,
                CustomToast.ERROR
            ).show()
        }

    }

    private fun extractStudySetIdFromIntent(intent: Intent): String? {
        val uri: Uri? = intent.data
        return uri?.lastPathSegment
    }

    private fun extractUserIdFromIntent(intent: Intent): String? {
        val uri: Uri? = intent.data
        val regex = Regex("/folder/([^/]+)/([^/]+)$")
        return uri?.path?.let {
            regex.find(it)?.groupValues?.get(1)
        }
    }

    private fun getFolderShareView(userId: String, setId: String) {
        lifecycleScope.launch {
            showLoading(this@DeepLinkShareFolder, resources.getString(R.string.loading_data))
            try {
                val result = apiService.getFolderShareView(userId, setId)
                if (result.isSuccessful) {
                    result.body().let {
                        withContext(Dispatchers.Main) {
                            // UI updates should be done on the main thread
                            CustomToast(this@DeepLinkShareFolder).makeText(
                                this@DeepLinkShareFolder,
                                resources.getString(R.string.loading_data_successful),
                                CustomToast.LONG,
                                CustomToast.SUCCESS
                            ).show()

                            result.body()?.let {
                                listStudySet.addAll(it.studySets)
                                val adapterStudySet = RvStudySetItemAdapter(
                                    this@DeepLinkShareFolder,
                                    listStudySet,
                                    object : RVStudySetItem {
                                        override fun handleClickStudySetItem(
                                            setItem: StudySetModel,
                                            position: Int
                                        ) {
                                            showInviteDialog(resources.getString(R.string.no_available_in_preview))
                                        }
                                    }, false
                                )
                                binding.rvStudySet.layoutManager =
                                    LinearLayoutManager(this@DeepLinkShareFolder)
                                binding.rvStudySet.adapter = adapterStudySet
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        result.errorBody()?.string()?.let {
                            CustomToast(this@DeepLinkShareFolder).makeText(
                                this@DeepLinkShareFolder,
                                it,
                                CustomToast.LONG,
                                CustomToast.ERROR
                            ).show()
                        }
                        binding.layoutNoData.visibility = View.VISIBLE
                    }
                }

            } catch (e: Exception) {
                CustomToast(this@DeepLinkShareFolder).makeText(
                    this@DeepLinkShareFolder,
                    e.message.toString(),
                    CustomToast.LONG,
                    CustomToast.ERROR
                ).show()
                binding.layoutNoData.visibility = View.VISIBLE
            } finally {
                progressDialog.dismiss()
            }
        }
    }

    private fun showLoading(context: Context, msg: String) {
        progressDialog = ProgressDialog.show(context, null, msg)
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