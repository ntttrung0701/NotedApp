package com.example.appquizlet

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appquizlet.adapter.RvStudySetItemAdapter
import com.example.appquizlet.api.retrofit.ApiService
import com.example.appquizlet.api.retrofit.RetrofitHelper
import com.example.appquizlet.custom.CustomToast
import com.example.appquizlet.databinding.FragmentStudySetsBinding
import com.example.appquizlet.interfaceFolder.RVStudySetItem
import com.example.appquizlet.model.StudySetModel
import com.example.appquizlet.model.UserM
import com.example.appquizlet.util.Helper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch


class StudySets : Fragment(R.layout.fragment_study_sets) {
    private lateinit var binding: FragmentStudySetsBinding
    private lateinit var apiService: ApiService
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStudySetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = RetrofitHelper.getInstance().create(ApiService::class.java)

        binding.txtCreateNewSet.setOnClickListener {
            val i = Intent(context, CreateSet::class.java)
            startActivity(i)
        }

        val listStudySet = mutableListOf<StudySetModel>()

        val adapterStudySet =
            RvStudySetItemAdapter(requireContext(), listStudySet, object : RVStudySetItem {
                override fun handleClickStudySetItem(setItem: StudySetModel, position: Int) {
                    val intent = Intent(requireContext(), StudySetDetail::class.java)
                    intent.putExtra("setId", listStudySet[position].id)
                    startActivity(intent)
//                setItem.isSelected = !setItem.isSelected!!
                }

            }, true)

        val userDataStudySet = UserM.getUserData()
        userDataStudySet.observe(viewLifecycleOwner) {
            listStudySet.clear()
            val allSets = Helper.getAllStudySets(it)

            listStudySet.addAll(allSets)
            if (listStudySet.isEmpty()) {
                binding.rvStudySet.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            }

            // Thông báo cho adapter rằng dữ liệu đã thay đổi để cập nhật giao diện người dùng
            adapterStudySet.notifyDataSetChanged()
        }


        val rvStudySet = binding.rvStudySet
        rvStudySet.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvStudySet.adapter = adapterStudySet
        adapterStudySet.setOnItemClickListener(object : RvStudySetItemAdapter.onClickSetItem {
            override fun handleClickDelete(setId: String) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.warning))
                    .setMessage(resources.getString(R.string.confirm_delete_set))
                    .setNeutralButton(resources.getString(R.string.cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
                        lifecycleScope.launch {
                            binding.progressCircular.visibility = View.VISIBLE
                            try {
                                val result =
                                    apiService.deleteStudySet(
                                        Helper.getDataUserId(requireContext()),
                                        setId
                                    )
                                if (result.isSuccessful) {
                                    result.body().let {
                                        if (it != null) {
                                            CustomToast(requireContext()).makeText(
                                                requireContext(),
                                                resources.getString(R.string.deleteSetSuccessful),
                                                CustomToast.LONG,
                                                CustomToast.SUCCESS
                                            ).show()
                                            UserM.setUserData(it)
                                        }
                                    }
                                } else {
                                    CustomToast(requireContext()).makeText(
                                        requireContext(),
                                        resources.getString(R.string.deleteSetErr),
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