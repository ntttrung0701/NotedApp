package com.example.appquizlet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.example.appquizlet.adapter.SearchPagerLibAdapter
import com.example.appquizlet.api.retrofit.ApiService
import com.example.appquizlet.api.retrofit.RetrofitHelper
import com.example.appquizlet.custom.CustomToast
import com.example.appquizlet.databinding.ActivitySplashSearchBinding
import com.example.appquizlet.model.UserM
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch


class SplashSearch : AppCompatActivity() {
    lateinit var binding: ActivitySplashSearchBinding
    private lateinit var apiService: ApiService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        Khoi tao viewbinding
        binding = ActivitySplashSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
//Set back icon
        setSupportActionBar(binding.toolbarSearch)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        apiService = RetrofitHelper.getInstance().create(ApiService::class.java)

        //        Adapter
        val adapterLibPager =
            SearchPagerLibAdapter(supportFragmentManager, lifecycle)
        binding.pagerLib.adapter = adapterLibPager
        TabLayoutMediator(binding.tabLib, binding.pagerLib) { tab, pos ->
            when (pos) {
                0 -> {
                    tab.text = resources.getString(R.string.all_results)
                    tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.all_apps, null)
                    val badge = tab.orCreateBadge
                    badge.backgroundColor =
                        ResourcesCompat.getColor(resources, R.color.semi_blue, null)
                }

                1 -> {
                    tab.text = resources.getString(R.string.sets)
                    tab.icon =
                        ResourcesCompat.getDrawable(resources, R.drawable.note, null)
                    val badge = tab.orCreateBadge
                    badge.backgroundColor =
                        ResourcesCompat.getColor(resources, R.color.semi_blue, null)
                }

                2 -> {
                    tab.text = resources.getString(R.string.user)
                    tab.icon =
                        ResourcesCompat.getDrawable(resources, R.drawable.user, null)
                    val badge = tab.orCreateBadge
                    badge.backgroundColor =
                        ResourcesCompat.getColor(resources, R.color.semi_blue, null)
                    badge.number = 0
                }
            }
        }.attach()




        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    findSetByKeyword(query)
                }
                binding.searchView.clearFocus();
//                query?.let {
//                    adapterLibPager.updateSetsData(it)
//                    adapterLibPager.updateUserData(it)
//                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Xử lý khi nút "Quay lại" được bấm
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun findSetByKeyword(keyword: String) {
        showLoading()
        lifecycleScope.launch {
            try {
                val result = apiService.findStudySet(keyword)
                if (result.isSuccessful) {
                    result.body().let { it ->
                        if (it != null) {
                            UserM.setDataSetSearch(it)
                        }
                    }
                    binding.layoutShowDataSearch.visibility = View.VISIBLE
                    binding.layoutSuggestionSearch.visibility = View.GONE
                } else {
                    result.errorBody().let {
                        CustomToast(this@SplashSearch).makeText(
                            this@SplashSearch,
                            this@SplashSearch.toString(),
                            CustomToast.LONG,
                            CustomToast.ERROR
                        ).show()
                    }
                }
            } catch (e: Exception) {
                CustomToast(this@SplashSearch).makeText(
                    this@SplashSearch, e.message.toString(), CustomToast.LONG, CustomToast.ERROR
                ).show()
            } finally {
//                binding.progressBar.visibility = View.GONE
//                binding.rvSolution.visibility = View.VISIBLE
            }
        }
    }

    private fun showLoading() {
//        binding.progressBar.visibility = View.VISIBLE
//        binding.rvSolution.visibility = View.GONE
    }


}