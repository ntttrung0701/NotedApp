package com.example.appquizlet

import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import com.example.appquizlet.databinding.FragmentStudyThisSetBinding
import com.example.appquizlet.model.FlashCardModel
import com.example.appquizlet.model.UserM
import com.example.appquizlet.util.Helper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson

class StudyThisSetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentStudyThisSetBinding
    private var setId: String? = null
    private var jsonList : String ?= ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Nhận dữ liệu từ Bundle
        arguments?.let {
            setId = it.getString("setIdTo")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStudyThisSetBinding.inflate(inflater, container, false)

        val listCards = mutableListOf<FlashCardModel>()

        var userData = UserM.getUserData()
        userData.observe(this, Observer { userResponse ->
            val studySet = Helper.getAllStudySets(userResponse).find { listStudySets ->
                listStudySets.id == setId
            }
            if (studySet != null) {
                listCards.clear()
                listCards.addAll(studySet.cards)
            }

            jsonList = Gson().toJson(listCards)


        })

        binding.layoutLearn.setOnClickListener {
            val i = Intent(context, FlashcardLearn::class.java)
            i.putExtra("listCard", jsonList)
            jsonList?.let { it1 -> Log.d("ggg", it1) }
            startActivity(i)
        }
        binding.layoutTest.setOnClickListener {
            val i = Intent(context, ReviewLearn::class.java)
            i.putExtra("listCardTest", jsonList)
            jsonList?.let { it1 -> Log.d("ggg1", it1) }
            startActivity(i)
        }

        return binding.root
    }

    companion object {

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setContentView(R.layout.fragment_study_this_set)

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet =
                (dialogInterface as BottomSheetDialog).findViewById<View>(R.id.fragmentStudyThisSetBottomsheet)
            val closeIcon = dialog.findViewById<TextView>(R.id.txtStudyThisSetCloseIcon)
            closeIcon.setOnClickListener {
                dismiss()
            }
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            val screenHeight = Resources.getSystem().displayMetrics.heightPixels - 60
            bottomSheet?.minimumHeight = screenHeight
            val behavior = dialogInterface.behavior
            behavior.peekHeight = screenHeight
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

}