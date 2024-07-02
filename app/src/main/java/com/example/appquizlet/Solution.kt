package com.example.appquizlet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.example.appquizlet.adapter.SolutionItemAdapter
import com.example.appquizlet.api.retrofit.ApiService
import com.example.appquizlet.api.retrofit.RetrofitHelper
import com.example.appquizlet.custom.CustomToast
import com.example.appquizlet.databinding.FragmentSolutionBinding
import com.example.appquizlet.interfaceFolder.RvClickSearchSet
import com.example.appquizlet.model.SearchSetModel
import com.example.appquizlet.model.UserM
import kotlinx.coroutines.launch


class Solution : Fragment() {
    private lateinit var binding: FragmentSolutionBinding
    private lateinit var apiService: ApiService
    private var listSearchItems = mutableListOf<SearchSetModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSolutionBinding.inflate(inflater, container, false)
        apiService = RetrofitHelper.getInstance().create(ApiService::class.java)
        return binding.root
    }

    companion object {
        const val TAG = "SolutionT"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getAllSet()

        binding.searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                var i = Intent(context, SplashSearch::class.java)
                binding.searchView.clearFocus()
                startActivity(i)
            }
        }


        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvSolution.visibility = View.GONE
    }

    private fun getAllSet() {
        showLoading()
        lifecycleScope.launch {
            try {
                val result = apiService.getAllSet()
                if (result.isSuccessful) {
                    result.body().let {
                        if (it != null) {
                            listSearchItems.clear()
                            listSearchItems.addAll(it)
                            UserM.setDataSetSearch(it)
                            val solutionItemAdapter =
                                SolutionItemAdapter(listSearchItems, object : RvClickSearchSet {
                                    override fun handleClickSetSearch(position: Int) {
                                        val intent =
                                            Intent(context, SearchDetail::class.java)
                                        intent.putExtra("setId", listSearchItems[position].id)
                                        startActivity(intent)
                                    }
                                })
                            val snapHelper = PagerSnapHelper()
                            snapHelper.attachToRecyclerView(binding.rvSolution)
                            binding.rvSolution.layoutManager =
                                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                            binding.rvSolution.adapter = solutionItemAdapter
                        }
                    }
                } else {
                    result.errorBody().let {
                        context?.let { it1 ->
                            CustomToast(it1).makeText(
                                it1, it1.toString(), CustomToast.LONG, CustomToast.ERROR
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
//                context?.let {
//                    CustomToast(it).makeText(
//                        it, e.message.toString(), CustomToast.LONG, CustomToast.ERROR
//                    ).show()
//                }
                Log.d("Error search", e.message.toString())
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.rvSolution.visibility = View.VISIBLE
            }
        }
    }
}