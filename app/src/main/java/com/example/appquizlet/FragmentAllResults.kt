package com.example.appquizlet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appquizlet.adapter.SolutionItemAdapter
import com.example.appquizlet.databinding.FragmentAllResultsBinding
import com.example.appquizlet.interfaceFolder.RvClickSearchSet
import com.example.appquizlet.model.SearchSetModel
import com.example.appquizlet.model.UserM
import com.example.appquizlet.model.UserResponse
import com.google.gson.Gson

class FragmentAllResults : Fragment() {
    private lateinit var binding: FragmentAllResultsBinding
    private var listSearch = mutableListOf<SearchSetModel>()
    private var listUser = mutableListOf<UserResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllResultsBinding.inflate(inflater, container, false)

        val userAllResults = UserM.getDataSetSearch()
        userAllResults.observe(viewLifecycleOwner) {
            listSearch.clear()
            listSearch.addAll(it)
            Log.d("list", Gson().toJson(listSearch))
            if (listUser.isEmpty() && listSearch.isEmpty()) {
                binding.layoutHasData.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            } else {
                binding.layoutHasData.visibility = View.VISIBLE
                binding.layoutNoData.visibility = View.GONE
            }
            if (listSearch.isEmpty()) {
                binding.layoutSet.visibility = View.GONE
            } else {
                binding.layoutSet.visibility = View.VISIBLE
                val solutionItemAdapter =
                    SolutionItemAdapter(listSearch, object : RvClickSearchSet {
                        override fun handleClickSetSearch(position: Int) {
                            val intent = Intent(requireContext(), SearchDetail::class.java)
                            intent.putExtra("setId", listSearch[position].id)
                            startActivity(intent)
                        }
                    })
                binding.rvSearchStudySet.layoutManager =
                    LinearLayoutManager(
                        context,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                binding.rvSearchStudySet.adapter = solutionItemAdapter

                binding.txtStudySetViewAll.setOnClickListener {
                    (activity as SplashSearch).binding.pagerLib.currentItem = 1
                }
                binding.txtFolderViewAll.setOnClickListener {
                    (activity as SplashSearch).binding.pagerLib.currentItem = 2
                }
            }

            if (listUser.isEmpty()) {
                binding.layoutUser.visibility = View.GONE
            }

        }

        return binding.root
    }

    companion object {

    }
}