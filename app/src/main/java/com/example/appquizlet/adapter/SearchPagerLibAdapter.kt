package com.example.appquizlet.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.appquizlet.FragmentAllResults
import com.example.appquizlet.FragmentSearchSet
import com.example.appquizlet.FragmentSearchUser

class SearchPagerLibAdapter(fragment: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragment, lifecycle) {

    private val allResultsFragment = FragmentAllResults()
    private val setsFragment = FragmentSearchSet()
    private val userFragment = FragmentSearchUser()

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> {
                allResultsFragment
            }

            1 -> {
                setsFragment
            }

            2 -> {
                userFragment
            }

            else -> {
                userFragment
            }
        }
    }


    fun updateSetsData(query: String) {
        setsFragment.updateData(query)
    }

    // Phương thức để cập nhật dữ liệu cho tab user khi tìm kiếm
    fun updateUserData(query: String) {
        userFragment.updateData(query)
    }

}