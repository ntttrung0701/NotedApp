package com.example.appquizlet

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appquizlet.adapter.SolutionItemAdapter
import com.example.appquizlet.api.retrofit.ApiService
import com.example.appquizlet.api.retrofit.RetrofitHelper
import com.example.appquizlet.custom.CustomToast
import com.example.appquizlet.databinding.FragmentSearchSetBinding
import com.example.appquizlet.interfaceFolder.RvClickSearchSet
import com.example.appquizlet.model.SearchSetModel
import com.example.appquizlet.model.UserM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentSearchSet : Fragment() {
    private lateinit var apiService: ApiService
    private lateinit var progressDialog: ProgressDialog
    private var listSearch = mutableListOf<SearchSetModel>()
    private lateinit var binding: FragmentSearchSetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchSetBinding.inflate(inflater, container, false)
        apiService = RetrofitHelper.getInstance().create(ApiService::class.java)
        return binding.root
    }

    companion object {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dataSearch = UserM.getDataSetSearch()
        dataSearch.observe(viewLifecycleOwner) {
            if (it != null) {
                listSearch.clear()
                listSearch.addAll(it)
                if (listSearch.isEmpty()) {
                    binding.layoutHasData.visibility = View.GONE
                    binding.layoutNoData.visibility = View.VISIBLE
                } else {
                    binding.layoutHasData.visibility = View.VISIBLE
                    binding.layoutNoData.visibility = View.GONE
                    val solutionItemAdapter = SolutionItemAdapter(listSearch, object :
                        RvClickSearchSet {
                        override fun handleClickSetSearch(position: Int) {
                            val intent = Intent(context, SearchDetail::class.java)
                            intent.putExtra("setId", listSearch[position].id)
                            startActivity(intent)
                        }
                    })
                    binding.rvSolution.layoutManager =
                        LinearLayoutManager(
                            context,
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                    binding.rvSolution.adapter = solutionItemAdapter
                }
            }
        }
    }

    fun updateData(query: String) {
//        findSetByKeyword(query)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvSolution.visibility = View.GONE
    }
}