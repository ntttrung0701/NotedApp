package com.example.appquizlet

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appquizlet.adapter.RVFolderItemAdapter
import com.example.appquizlet.api.retrofit.ApiService
import com.example.appquizlet.api.retrofit.RetrofitHelper
import com.example.appquizlet.custom.CustomToast
import com.example.appquizlet.databinding.ActivityFlashcardAddSetToFolderBinding
import com.example.appquizlet.interfaceFolder.RVFolderItem
import com.example.appquizlet.model.FolderModel
import com.example.appquizlet.model.UserM
import com.example.appquizlet.util.Helper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import kotlinx.coroutines.launch

class FlashcardAddSetToFolder : AppCompatActivity() {
    private lateinit var binding: ActivityFlashcardAddSetToFolderBinding
    private lateinit var apiService: ApiService
    private lateinit var progressDialog: ProgressDialog
    private val listFolderSelected: MutableList<FolderModel> = mutableListOf()
    private val listFolderId: MutableSet<String> = mutableSetOf()
    private lateinit var setId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlashcardAddSetToFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = RetrofitHelper.getInstance().create(ApiService::class.java)


        //        set toolbar back display
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Hiển thị biểu tượng quay lại

        setId = intent.getStringExtra("addSetId").toString()


        val listFolderItems = mutableListOf<FolderModel>()


        val adapterFolder =
            RVFolderItemAdapter(this, listFolderItems, object : RVFolderItem {
                override fun handleClickFolderItem(folderItem: FolderModel, position: Int) {
                    folderItem.isSelected = folderItem.isSelected?.not() ?: true
                    val selectedFolder = listFolderItems.filter { it.isSelected == true }
                    if (selectedFolder.isNotEmpty()) {
                        listFolderSelected.addAll(selectedFolder)
                        listFolderSelected.map {
                            listFolderId.add(it.id)
                        }
                        Log.d("ids", Gson().toJson(listFolderId))
                    }
                }
            })

        // Thêm một Observer cho userData
        val userData = UserM.getUserData()
        userData.observe(this) { userResponse ->
            listFolderItems.clear()
            val filteredFolders = userResponse.documents.folders.filter { folder ->
                folder.studySets.none { it.id == setId }
            }
            listFolderItems.addAll(filteredFolders)

            if (listFolderItems.isEmpty()) {
                binding.rvFlashcardAddToFolder.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            } else {
                binding.rvFlashcardAddToFolder.visibility = View.VISIBLE
                binding.layoutNoData.visibility = View.GONE
            }

            // Thông báo cho adapter rằng dữ liệu đã thay đổi để cập nhật giao diện người dùng
            adapterFolder.notifyDataSetChanged()
        }
        // Access the RecyclerView through the binding
        val rvFolder = binding.rvFlashcardAddToFolder
        rvFolder.layoutManager = LinearLayoutManager(this)
        rvFolder.adapter = adapterFolder


        binding.btnAddSet.setOnClickListener {
            insertSetToFolder(setId)
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun insertSetToFolder(
        setId: String
    ) {
        lifecycleScope.launch {
            if (listFolderId.isEmpty()) {
                MaterialAlertDialogBuilder(this@FlashcardAddSetToFolder)
                    .setTitle(resources.getString(R.string.no_folder_add))
                    .setMessage(resources.getString(R.string.no_folder_to_add))
                    .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                        finish()
                    }
                    .show()
            } else {
                showLoading(resources.getString(R.string.add_set_to_folder_processing))
                try {
                    val body: MutableSet<String> = listFolderId
                    Log.d("body", Gson().toJson(body))
                    val result = apiService.addSetToManyFolder(
                        Helper.getDataUserId(this@FlashcardAddSetToFolder),
                        setId,
                        body
                    )
                    if (result.isSuccessful) {
                        result.body()?.let {
                            this@FlashcardAddSetToFolder.let { it1 ->
                                CustomToast(it1).makeText(
                                    this@FlashcardAddSetToFolder,
                                    resources.getString(R.string.update_study_set_success),
                                    CustomToast.LONG,
                                    CustomToast.SUCCESS
                                ).show()
                                UserM.setUserData(it)
                                val intent =
                                    Intent(
                                        this@FlashcardAddSetToFolder,
                                        MainActivity_Logged_In::class.java
                                    )
                                intent.putExtra(
                                    "selectedFragment",
                                    "Home"
                                ) // "YourFragmentTag" là tag của Fragment cần hiển thị
                                startActivity(intent)

                            }
                        }
                    } else {
                        result.errorBody()?.string()?.let {
                            this@FlashcardAddSetToFolder.let { it1 ->
                                CustomToast(it1).makeText(
                                    this@FlashcardAddSetToFolder,
                                    it,
                                    CustomToast.LONG,
                                    CustomToast.ERROR
                                ).show()
                            }
                            Log.d("err", it)
                        }
                    }
                } catch (e: Exception) {
                    CustomToast(this@FlashcardAddSetToFolder).makeText(
                        this@FlashcardAddSetToFolder,
                        e.message.toString(),
                        CustomToast.LONG,
                        CustomToast.ERROR
                    ).show()
                } finally {
                    progressDialog.dismiss()
                }
            }
        }
    }

    private fun showLoading(msg: String) {
        progressDialog = ProgressDialog.show(this, null, msg)
    }
}