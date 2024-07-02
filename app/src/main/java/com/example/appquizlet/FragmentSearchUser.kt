package com.example.appquizlet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class FragmentSearchUser : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_user, container, false)
    }

    companion object {

    }

    fun updateData(query: String) {
        // Thực hiện các thay đổi trong dữ liệu dựa trên kết quả tìm kiếm (query)
        // Ví dụ: setsAdapter.updateData(searchSets(query))
    }
}