package com.example.appquizlet.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.appquizlet.FragmentCreatedSet
import com.example.appquizlet.FragmentFolderSet

class ViewPagerTabAddSet(fragment: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragment, lifecycle) {

    private var folderId: String? = null
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                val fragment = FragmentCreatedSet()
                folderId?.let { fragment.setFolderId(it) }
                fragment
            }

            1 -> {
                val fragment = FragmentFolderSet()
                folderId?.let {
                    fragment.setFolderId(it)
                }
                fragment
            }

            else -> {
                FragmentFolderSet()
            }
        }
    }

    fun setFolderId(folderId: String) {
        this.folderId = folderId
    }
}