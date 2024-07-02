package com.example.appquizlet

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appquizlet.adapter.RvStudySetItemAdapter
import com.example.appquizlet.api.retrofit.ApiService
import com.example.appquizlet.api.retrofit.RetrofitHelper
import com.example.appquizlet.custom.CustomToast
import com.example.appquizlet.databinding.FragmentCreatedSetBinding
import com.example.appquizlet.interfaceFolder.RVStudySetItem
import com.example.appquizlet.model.StudySetModel
import com.example.appquizlet.model.UserM
import com.example.appquizlet.util.Helper
import kotlinx.coroutines.launch

class FragmentCreatedSet : Fragment(), RvStudySetItemAdapter.onClickSetItem {
    private lateinit var binding: FragmentCreatedSetBinding
    private lateinit var apiService: ApiService
    private lateinit var progressDialog: ProgressDialog
    private val listSetSelected: MutableList<StudySetModel> = mutableListOf()
    private lateinit var adapterStudySet: RvStudySetItemAdapter
    private var folderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCreatedSetBinding.inflate(inflater, container, false)
        apiService = RetrofitHelper.getInstance().create(ApiService::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listStudySet = mutableListOf<StudySetModel>()

        adapterStudySet =
            RvStudySetItemAdapter(requireContext(), listStudySet, object : RVStudySetItem {
                override fun handleClickStudySetItem(setItem: StudySetModel, position: Int) {
                    setItem.isSelected = setItem.isSelected?.not() ?: true
                    adapterStudySet.notifyItemChanged(position)

                    val selectedItems = listStudySet.filter { it.isSelected == true }
                    listSetSelected.clear()
                    if (selectedItems.isNotEmpty()) {
                        listSetSelected.addAll(selectedItems)
                    } else {
                        Log.d("Selected Items", "No items selected")
                    }
                }
            }, enableSwipe = false, true)

        val userDataStudySet = UserM.getUserData()
        userDataStudySet.observe(viewLifecycleOwner) {
            listStudySet.clear()

            val listSets = Helper.getAllStudySets(it)
//                .filter {
//                it.folderOwnerId != folderId
//            }
            listStudySet.addAll(listSets)

            if (listStudySet.isEmpty()) {
                binding.layoutNoData.visibility = View.VISIBLE
                binding.rvStudySet.visibility = View.GONE
            } else {
                binding.layoutNoData.visibility = View.GONE
                binding.rvStudySet.visibility = View.VISIBLE
            }
            // Thông báo cho adapter rằng dữ liệu đã thay đổi để cập nhật giao diện người dùng
            adapterStudySet.notifyDataSetChanged()
        }


        val rvStudySet = binding.rvStudySet
        rvStudySet.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvStudySet.adapter = adapterStudySet
    }

    companion object {
    }

    override fun handleClickDelete(setId: String) {

    }

    fun insertSetToFolder(
        folderId: String
    ) {
        lifecycleScope.launch {
            showLoading("Add set to folder processing ...")
            try {
                val body: MutableSet<String> = listSetSelected.map { it.id }.toMutableSet()
                if (body.isEmpty()) {
                    context?.let {
                        CustomToast(it).makeText(
                            requireContext(),
                            resources.getString(R.string.there_no_set_selected),
                            CustomToast.LONG,
                            CustomToast.WARNING
                        ).show()
                    }
                } else {
                    val result = apiService.addSetToFolder(
                        Helper.getDataUserId(requireContext()),
                        folderId,
                        body
                    )
                    if (result.isSuccessful) {
                        result.body()?.let {
                            requireContext().let { it1 ->
                                CustomToast(it1).makeText(
                                    requireContext(),
                                    resources.getString(R.string.update_study_set_success),
                                    CustomToast.LONG,
                                    CustomToast.SUCCESS
                                ).show()
                                UserM.setUserData(it)
//                                val intent =
//                                    Intent(requireContext(), MainActivity_Logged_In::class.java)
//                                intent.putExtra(
//                                    "selectedFragment",
//                                    "Home"
//                                ) // "YourFragmentTag" là tag của Fragment cần hiển thị
//                                startActivity(intent)
                                val i = Intent(context, FolderClickActivity::class.java)
                                i.putExtra("idFolder", folderId)
                                startActivity(i)
                            }
                        }
                    } else {
                        result.errorBody()?.string()?.let {
                            requireContext().let { it1 ->
                                CustomToast(it1).makeText(
                                    requireContext(),
                                    it,
                                    CustomToast.LONG,
                                    CustomToast.ERROR
                                ).show()
                            }
                            Log.d("err", it)
                        }
                    }
                }

            } catch (e: Exception) {
                CustomToast(requireContext()).makeText(
                    requireContext(),
                    e.message.toString(),
                    CustomToast.LONG,
                    CustomToast.ERROR
                ).show()
            } finally {
                progressDialog.dismiss()
            }
        }
    }

    private fun showLoading(msg: String) {
        progressDialog = ProgressDialog.show(requireContext(), null, msg)
    }


    fun setFolderId(folderId: String) {
        this.folderId = folderId
    }


}