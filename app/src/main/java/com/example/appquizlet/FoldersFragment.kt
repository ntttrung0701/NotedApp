package com.example.appquizlet

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appquizlet.adapter.RVFolderItemAdapter
import com.example.appquizlet.api.retrofit.ApiService
import com.example.appquizlet.api.retrofit.RetrofitHelper
import com.example.appquizlet.custom.CustomToast
import com.example.appquizlet.databinding.FragmentFoldersBinding
import com.example.appquizlet.interfaceFolder.RVFolderItem
import com.example.appquizlet.model.FolderModel
import com.example.appquizlet.model.UserM
import com.example.appquizlet.util.Helper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class FoldersFragment : Fragment() {

    // Declare the binding property with late initialization
    private lateinit var binding: FragmentFoldersBinding
    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize the binding
        binding = FragmentFoldersBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = RetrofitHelper.getInstance().create(ApiService::class.java)
        val listFolderItems = mutableListOf<FolderModel>()


        val adapterFolder =
            RVFolderItemAdapter(requireContext(), listFolderItems, object : RVFolderItem {
                override fun handleClickFolderItem(folderItem: FolderModel, position: Int) {
                    val i = Intent(context, FolderClickActivity::class.java)
                    i.putExtra("idFolder", listFolderItems[position].id)
                    startActivity(i)
                }
            })
        // Thêm một Observer cho userData
        val userData = UserM.getUserData()
        userData.observe(viewLifecycleOwner, Observer { userResponse ->
            // Khi dữ liệu thay đổi, cập nhật danh sách listFolderItems
            // Lưu ý: Trong trường hợp thực tế, bạn có thể cần xử lý dữ liệu từ userResponse một cách thích hợp.
            // Ở đây, ta giả sử userResponse có một thuộc tính là danh sách các FolderItemData.
            listFolderItems.clear()
            listFolderItems.addAll(userResponse.documents.folders)
            if (listFolderItems.isEmpty()) {
                binding.rvFolderFragment.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            }

            // Thông báo cho adapter rằng dữ liệu đã thay đổi để cập nhật giao diện người dùng
            adapterFolder.notifyDataSetChanged()
        })
        // Access the RecyclerView through the binding
        val rvFolder = binding.rvFolderFragment
        rvFolder.layoutManager = LinearLayoutManager(context)
        rvFolder.adapter = adapterFolder

        adapterFolder.setOnClickFolderListener(object : RVFolderItemAdapter.onClickFolder {
            override fun handleDeleteFolder(folderId: String) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.warning))
                    .setMessage(resources.getString(R.string.confirm_delete_folder))
                    .setNegativeButton(resources.getString(R.string.cancel)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                        lifecycleScope.launch {
                            binding.progressCircular.visibility = View.VISIBLE
                            try {
                                val result = apiService.deleteFolder(
                                    Helper.getDataUserId(requireContext()),
                                    folderId
                                )
                                if (result.isSuccessful) {

                                    result.body().let {
                                        if (it != null) {
                                            CustomToast(requireContext()).makeText(
                                                requireContext(),
                                                resources.getString(R.string.deleteFolderSuccessful),
                                                CustomToast.LONG,
                                                CustomToast.SUCCESS
                                            ).show()
                                            UserM.setUserData(it)
                                        }
                                    }
                                } else {
                                    CustomToast(requireContext()).makeText(
                                        requireContext(),
                                        resources.getString(R.string.deleteFolderErr),
                                        CustomToast.LONG,
                                        CustomToast.ERROR
                                    ).show()
                                }
                            } catch (e: Exception) {
                                CustomToast(requireContext()).makeText(
                                    requireContext(),
                                    e.message.toString(),
                                    CustomToast.LONG,
                                    CustomToast.ERROR
                                ).show()
                            } finally {
                                binding.progressCircular.visibility = View.GONE
                            }
                        }
                    }
                    .show()

            }
        })
    }


}