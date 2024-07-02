package com.example.appquizlet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.appquizlet.databinding.FragmentItemUserRankDetailBinding
import com.example.appquizlet.model.RankItemModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ItemUserRankDetail : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentItemUserRankDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentItemUserRankDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Retrieve data from arguments
        val score = arguments?.getInt(ARG_SCORE, 0) ?: 0
        val userName = arguments?.getString(ARG_USER_NAME, "") ?: ""
        val dateOfBirth = arguments?.getString(ARG_DATE_OF_BIRTH, "") ?: ""
        val order = arguments?.getInt(ARG_ORDER, 0) ?: 0


        binding.txtBxh.text = "${resources.getString(R.string.rank)} ${order}"
        binding.txtUsername.text = userName
        binding.txtDob.text = dateOfBirth
        binding.txtTotalPoint.text = score.toString()

        binding.imgAvatar.setOnClickListener {
            val i = Intent(requireContext(), ViewImage::class.java)
            startActivity(i)
        }
    }

    companion object {
        const val ARG_SCORE = "score"
        const val ARG_SEQ_ID = "seq_id"
        const val ARG_USER_NAME = "user_name"
        const val ARG_EMAIL = "email"
        const val ARG_DATE_OF_BIRTH = "date_of_birth"
        const val ARG_ORDER = "order"

        fun newInstance(rankItemModel: RankItemModel): ItemUserRankDetail {
            val fragment = ItemUserRankDetail()
            val args = Bundle()
            args.putInt(ARG_SCORE, rankItemModel.score)
            args.putInt(ARG_SEQ_ID, rankItemModel.seqId)
            args.putString(ARG_USER_NAME, rankItemModel.userName)
            args.putString(ARG_EMAIL, rankItemModel.email)
            args.putString(ARG_DATE_OF_BIRTH, rankItemModel.dateOfBirth)
            args.putString(ARG_DATE_OF_BIRTH, rankItemModel.dateOfBirth)
            rankItemModel.order?.let { args.putInt(ARG_ORDER, it) }
            fragment.arguments = args
            return fragment
        }
    }
}