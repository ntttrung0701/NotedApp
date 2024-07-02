package com.example.appquizlet

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.appquizlet.databinding.FragmentAddCourseBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FragmentAddCourse : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentAddCourseBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddCourseBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "BottomSheetAddCourse"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listAuto = resources.getStringArray(R.array.academy_autocomple)
        val autoAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listAuto)
        binding.autoSchool.setAdapter(autoAdapter)

        binding.autoSchool.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
//                binding.autoSchool.background = resources.getDrawable(R.drawable.border_bottom_only)
//                binding.inputLayoutAutoSchool.boxBackgroundMode =
//                    TextInputLayout.BOX_BACKGROUND_NONE
                binding.autoSchool.showDropDown()
            }
        }

        binding.btnDoneCourse.setOnClickListener {
            context?.let { it1 ->
                MaterialAlertDialogBuilder(it1)
                    .setTitle(R.string.notification)
                    .setMessage(R.string.the_feature_will_coming_soon)
                    .show()
            }
        }
//
//        binding.txtCourseName.setOnFocusChangeListener { v, hasFocus ->
//            if (hasFocus) {
//                binding.txtCourseName.background = resources.getDrawable(R.drawable.border_bottom_only)
//                binding.inputLayoutCourseName.boxBackgroundMode =
//                    TextInputLayout.BOX_BACKGROUND_NONE
//            } else {
//                binding.inputLayoutCourseName.boxBackgroundMode =
//                    TextInputLayout.BOX_BACKGROUND_OUTLINE
//            }
//        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setContentView(R.layout.fragment_add_course)

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet =
                (dialogInterface as BottomSheetDialog).findViewById<View>(R.id.add_course_bottomsheet)
            val closeIcon = dialog.findViewById<TextView>(R.id.txtCloseIcon)
            closeIcon.setOnClickListener {
                dismiss()
            }
            bottomSheet?.layoutParams?.height = LayoutParams.MATCH_PARENT
            var screenHeight = Resources.getSystem().displayMetrics.heightPixels - 60
            bottomSheet?.minimumHeight = screenHeight
            val behavior = (dialogInterface as BottomSheetDialog).behavior
            behavior.peekHeight = screenHeight
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }
}