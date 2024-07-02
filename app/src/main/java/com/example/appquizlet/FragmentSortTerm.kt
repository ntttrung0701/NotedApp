package com.example.appquizlet

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.appquizlet.databinding.FragmentSortTermBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FragmentSortTerm : BottomSheetDialogFragment() {
    interface SortTermListener {
        fun onSortTermSelected(selectedType: String)
    }

    private lateinit var binding: FragmentSortTermBinding
    private lateinit var sharedPreferences: SharedPreferences
    var sortTermListener: SortTermListener? = null
    private var selectedType: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSortTermBinding.inflate(inflater, container, false)

        sharedPreferences =
            requireActivity().getSharedPreferences("TypeSelected", Context.MODE_PRIVATE)
        selectedType = sharedPreferences.getString("selectedT", "")
        Log.d("type",selectedType.toString())

        if (selectedType.equals("OriginalSort") || selectedType?.isEmpty() == true) {
            binding.iconTick2.visibility = View.GONE
            binding.imageTick1.visibility = View.VISIBLE
        } else {
            binding.imageTick1.visibility = View.GONE
            binding.iconTick2.visibility = View.VISIBLE
        }

        binding.txtOriginalSort.setOnClickListener {
            selectedType = "OriginalSort"

            selectedType?.let {
                sortTermListener?.onSortTermSelected(it)
            }
            dismiss()
        }

        binding.txtAlphabetically.setOnClickListener {
            selectedType = "AlphabeticalSort"
            selectedType?.let {
                sortTermListener?.onSortTermSelected(it)
            }
            dismiss()
        }

        return binding.root
    }

    companion object {
        const val TAG = "Fragment sort term"
    }
}

