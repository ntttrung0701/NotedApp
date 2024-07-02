package com.example.appquizlet

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.appquizlet.databinding.FragmentLearnFlashCardSettingBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

interface OnClickButton {
    fun handleClickShuffle()

    fun handleClickPlayAudio()

    fun handleClickModeDisplay()

    fun handleResetCard()
}

class LearnFlashCardSetting : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentLearnFlashCardSettingBinding
    private var onCLickListener: OnClickButton? = null
    private var isFront: Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLearnFlashCardSettingBinding.inflate(inflater, container, false)

        binding.btnShuffle.setOnClickListener {
            onCLickListener?.handleClickShuffle()
        }

//        binding.btnPlayAudio.setOnClickListener {
//            onCLickListener?.handleClickPlayAudio()
//        }

        binding.btnToggleMode.setOnClickListener {
            onCLickListener?.handleClickModeDisplay()
        }

        binding.txtResetCard.setOnClickListener {
            onCLickListener?.handleResetCard()
        }

        if(isFront) {
            binding.btnToggleMode.text = "Term"
        } else {
            binding.btnToggleMode.text = "Definition"
        }

        return binding.root
    }

    fun setOnButtonSettingClickListener(listener: OnClickButton) {
        this.onCLickListener = listener
    }

    companion object {

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setContentView(R.layout.fragment_learn_flash_card_setting)

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet =
                (dialogInterface as BottomSheetDialog).findViewById<View>(R.id.fragmentSettingBottomsheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            var screenHeight = Resources.getSystem().displayMetrics.heightPixels / 4
            bottomSheet?.minimumHeight = screenHeight
            val behavior = (dialogInterface as BottomSheetDialog).behavior
            behavior.peekHeight = screenHeight
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }


    fun setIsFront(isFrontData: Boolean) {
        isFront = isFrontData
    }
}