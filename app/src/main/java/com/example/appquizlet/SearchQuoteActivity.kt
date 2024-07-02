package com.example.appquizlet

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import com.example.appquizlet.adapter.SearchPagerLibAdapter
import com.example.appquizlet.databinding.ActivitySearchQuoteBinding
import com.google.android.material.tabs.TabLayoutMediator

class SearchQuoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchQuoteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_quote)

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
//                    findQuoteByKeyword(query)
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
}