package com.example.appquizlet

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appquizlet.adapter.RvStudySetItemAdapter
import com.example.appquizlet.api.retrofit.ApiService
import com.example.appquizlet.api.retrofit.RetrofitHelper
import com.example.appquizlet.custom.CustomToast
import com.example.appquizlet.databinding.ActivityFolderClickBinding
import com.example.appquizlet.interfaceFolder.RVStudySetItem
import com.example.appquizlet.model.StudySetModel
import com.example.appquizlet.model.UserM
import com.example.appquizlet.util.Helper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class FolderClickActivity : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog
    private lateinit var binding: ActivityFolderClickBinding
    private lateinit var apiService: ApiService
    private lateinit var folderId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFolderClickBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        folderId = intent.getStringExtra("idFolder").toString()

        apiService = RetrofitHelper.getInstance().create(ApiService::class.java)

//        set toolbar back display
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Hiển thị biểu tượng quay lại
// Tắt tiêu đề của Action Bar
        supportActionBar?.setDisplayShowTitleEnabled(false)


        val listStudySet = mutableListOf<StudySetModel>()

        val adapterStudySet = RvStudySetItemAdapter(this, listStudySet, object : RVStudySetItem {
            override fun handleClickStudySetItem(setItem: StudySetModel, position: Int) {
                val i = Intent(this@FolderClickActivity, StudySetDetail::class.java)
                i.putExtra("setId", listStudySet[position].id)
                i.putExtra("folderIdCl", folderId)
                startActivity(i)
            }
        }, true)


        val userData = UserM.getUserData()
        userData.observe(this) { userResponse ->
            listStudySet.clear()
            val targetFolder =
                userResponse.documents.folders.find { folderItem -> folderItem.id == folderId }
            if (targetFolder != null) {
                listStudySet.addAll(targetFolder.studySets)
            }
            if (targetFolder != null) {
                binding.txtCountSet.text = if (targetFolder.studySets.size == 1)
                    targetFolder.studySets.size.toString() + " term" else
                    targetFolder.studySets.size.toString() + (" terms")
            }
            if (targetFolder != null) {
                binding.txtFolderName.text = targetFolder.name
                if (targetFolder.description.isEmpty()) {
                    binding.txtDesc.visibility = View.GONE
                } else {
                    binding.txtDesc.visibility = View.VISIBLE
                    binding.txtDesc.text = targetFolder.description
                }
            }
            binding.txtFolderClickUsername.text = userResponse.loginName
            if (listStudySet.isEmpty()) {
                binding.rvStudySet.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            }
            adapterStudySet.notifyDataSetChanged()
        }
        val rvStudySet = binding.rvStudySet
        rvStudySet.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvStudySet.adapter = adapterStudySet

        adapterStudySet.setOnItemClickListener(object : RvStudySetItemAdapter.onClickSetItem {

            override fun handleClickDelete(setId: String) {
                MaterialAlertDialogBuilder(this@FolderClickActivity)
                    .setTitle(resources.getString(R.string.warning))
                    .setMessage(resources.getString(R.string.confirm_remove_set_from_folder))
                    .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
                        lifecycleScope.launch {
                            showLoading(resources.getString(R.string.remove_set_folder_loading))
                            try {
                                val result = apiService.removeSetFromFolder(
                                    Helper.getDataUserId(this@FolderClickActivity),
                                    folderId,
                                    setId
                                )
                                if (result.isSuccessful) {
                                    result.body()?.let {
                                        this@FolderClickActivity.let { it1 ->
                                            CustomToast(it1).makeText(
                                                this@FolderClickActivity,
                                                resources.getString(R.string.update_study_set_success),
                                                CustomToast.LONG,
                                                CustomToast.SUCCESS
                                            ).show()
                                            UserM.setUserData(it)
                                        }
                                    }
                                } else {
                                    result.errorBody()?.string()?.let {
                                        this@FolderClickActivity.let { it1 ->
                                            CustomToast(it1).makeText(
                                                this@FolderClickActivity,
                                                it,
                                                CustomToast.LONG,
                                                CustomToast.ERROR
                                            ).show()
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                CustomToast(this@FolderClickActivity).makeText(
                                    this@FolderClickActivity,
                                    e.message.toString(),
                                    CustomToast.LONG,
                                    CustomToast.ERROR
                                ).show()
                            } finally {
                                progressDialog.dismiss()
                            }
                        }
                    }
                    .show()


            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            R.id.option_edit -> {
                showEditCustomDialog(
                    resources.getString(R.string.edit_folder),
                    resources.getString(R.string.folder_name),
                    resources.getString(R.string.desc_optional)
                )
                return true
            }

            R.id.option_add_sets -> {
                val i = Intent(this, AddSetToFolder::class.java)
                i.putExtra("folderAddSet", folderId)
                startActivity(i)
            }

            R.id.option_delete -> {
                showDeleteDialog(
                    resources.getString(R.string.delete_text),
                )
            }

            R.id.option_share -> {
                shareDialog(Helper.getDataUserId(this), folderId)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.edit_menu, menu)
        return true
    }

    private fun showEditCustomDialog(
        title: String,
        folderNameHint: String,
        folderDescHint: String,
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(60)

        val editTextFolderName = EditText(this)
        editTextFolderName.hint = folderNameHint
        layout.addView(editTextFolderName)


        val editTextFolderDesc = EditText(this)
        editTextFolderDesc.hint = folderDescHint
        layout.addView(editTextFolderDesc)

        // Set layout parameters with margins
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.topMargin = 10
        layoutParams.bottomMargin = 10
        editTextFolderName.layoutParams = layoutParams
        editTextFolderDesc.layoutParams = layoutParams

        builder.setView(layout)

        builder.setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
            updateFolder(
                editTextFolderName.text.toString(),
                editTextFolderDesc.text.toString(),
                Helper.getDataUserId(this),
                folderId
            )
            dialog.dismiss()
        }

        builder.setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()

    }

    private fun showDeleteDialog(desc: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(desc)
        builder.setPositiveButton(resources.getString(R.string.delete)) { dialog, _ ->
            deleteFolder(Helper.getDataUserId(this), folderId)
            dialog.dismiss()
        }
        builder.setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun shareDialog(userId: String, folderId: String) {
        val deepLinkBaseUrl = "www.ttcs_quizlet.com/folder"
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, "$deepLinkBaseUrl/$userId/$folderId")
        val packageNames =
            arrayOf("com.facebook.katana", "com.facebook.orca", "com.google.android.gm")
        val chooserIntent = Intent.createChooser(sharingIntent, "Share via")
        chooserIntent.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, packageNames)
        startActivity(chooserIntent)
    }


    private fun updateFolder(name: String, desc: String? = "", userId: String, folderId: String) {
        lifecycleScope.launch {
            showLoading(resources.getString(R.string.updateLoadMes))
            try {
                val body = JsonObject().apply {
                    addProperty(resources.getString(R.string.createFolderNameField), name)
                    addProperty(resources.getString(R.string.descriptionField), desc)
                }
                val result = apiService.updateFolder(userId, folderId, body)
                if (result.isSuccessful) {
                    result.body().let { it ->
                        if (it != null) {
                            this@FolderClickActivity.let { it1 ->
                                CustomToast(it1).makeText(
                                    this@FolderClickActivity,
                                    resources.getString(R.string.update_folder_success),
                                    CustomToast.LONG,
                                    CustomToast.SUCCESS
                                ).show()
                                UserM.setUserData(it)
                            }
                        }
                    }
                } else {
                    result.errorBody()?.string()?.let {
                        this@FolderClickActivity.let { it1 ->
                            CustomToast(it1).makeText(
                                this@FolderClickActivity,
                                it,
                                CustomToast.LONG,
                                CustomToast.ERROR
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                CustomToast(this@FolderClickActivity).makeText(
                    this@FolderClickActivity,
                    e.message.toString(),
                    CustomToast.LONG,
                    CustomToast.ERROR
                )
            } finally {
                progressDialog.dismiss()
            }
        }
    }

    private fun deleteFolder(userId: String, folderId: String) {
//        MaterialAlertDialogBuilder(this)
//            .setTitle(resources.getString(R.string.warning))
//            .setMessage(resources.getString(R.string.confirm_delete_folder))
//            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, which ->
//                dialog.dismiss()
//            }
//            .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
        lifecycleScope.launch {
            showLoading(resources.getString(R.string.deleteFolderLoading))
            try {
                val result = apiService.deleteFolder(userId, folderId)
                if (result.isSuccessful) {

                    result.body().let {
                        if (it != null) {
                            CustomToast(this@FolderClickActivity).makeText(
                                this@FolderClickActivity,
                                resources.getString(R.string.deleteFolderSuccessful),
                                CustomToast.LONG,
                                CustomToast.SUCCESS
                            ).show()
                            UserM.setUserData(it)
                        }
                        val i = Intent(this@FolderClickActivity, MainActivity_Logged_In::class.java)
                        i.putExtra("selectedFragment", "Library")
                        i.putExtra("createMethod", "createFolder")
                        startActivity(i)
                    }
                } else {
                    CustomToast(this@FolderClickActivity).makeText(
                        this@FolderClickActivity,
                        resources.getString(R.string.deleteFolderErr),
                        CustomToast.LONG,
                        CustomToast.ERROR
                    ).show()
                }
            } catch (e: Exception) {
                CustomToast(this@FolderClickActivity).makeText(
                    this@FolderClickActivity,
                    e.message.toString(),
                    CustomToast.LONG,
                    CustomToast.ERROR
                ).show()
            } finally {
                progressDialog.dismiss()
            }
        }
//            }
//            .show()

    }

    private fun showLoading(msg: String) {
        progressDialog = ProgressDialog.show(this, null, msg)
    }
}