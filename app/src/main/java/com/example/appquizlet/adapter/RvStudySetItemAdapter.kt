package com.example.appquizlet.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.example.appquizlet.R
import com.example.appquizlet.databinding.LayoutCreateSetItemBinding
import com.example.appquizlet.interfaceFolder.RVStudySetItem
import com.example.appquizlet.model.StudySetModel
import com.example.appquizlet.util.Helper

class RvStudySetItemAdapter(
    private val context: Context,
    private val listStudySet: List<StudySetModel>,
    private val onStudySetItem: RVStudySetItem,
    private val enableSwipe: Boolean,
    private val isCheckBackground: Boolean? = false
) : RecyclerView.Adapter<RvStudySetItemAdapter.StudySetItemHolder>() {

    private val viewBinderHelper: ViewBinderHelper = ViewBinderHelper()
    private var onClickSet: onClickSetItem? = null

    interface onClickSetItem {
        fun handleClickDelete(setId: String)
    }

    inner class StudySetItemHolder(val binding: LayoutCreateSetItemBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudySetItemHolder {

        return StudySetItemHolder(
            LayoutCreateSetItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: StudySetItemHolder, position: Int) {
        val currentItem = listStudySet[position]
        viewBinderHelper.closeLayout(listStudySet[position].timeCreated.toString())

        if (shouldEnableSwipeForItem() == true) {
            viewBinderHelper.bind(
                holder.binding.swipeRevealLayout,
                listStudySet[position].timeCreated.toString()
            )
        } else {
            holder.binding.swipeRevealLayout.close(false)
        }


        holder.binding.btnDeleteCard.setOnClickListener {
            onClickSet?.handleClickDelete(listStudySet[position].id)
        }


        val txtStudySetTitle = holder.binding.txtStudySetTitle
        val studySetChip = holder.binding.studySetChip
        val imgStudySetAvatar = holder.binding.imgStudySetAvatar
        val txtStudySetUsername = holder.binding.txtStudySetUsername
        val cardViewStudySet = holder.binding.studySetCardView

        val countTermText =
            if (currentItem.countTerm!! > 1) "${currentItem.countTerm} terms" else
                "${currentItem.countTerm} term"

        txtStudySetTitle.text = currentItem.name
        studySetChip.text = countTermText
        txtStudySetUsername.text = Helper.getDataUsername(context)
//            imgStudySetAvatar.setImageResource(currentItem.avatar)
//            txtStudySetUsername.text = currentItem.username

        if (currentItem.folderOwnerId.isNotEmpty() && isCheckBackground == true) {
            cardViewStudySet.background =
                ContextCompat.getDrawable(context, R.drawable.selected_item_border)
            cardViewStudySet.alpha = 0.8F
        } else {
            cardViewStudySet.background =
                ContextCompat.getDrawable(context, R.drawable.bg_white)
        }


        if (currentItem.isSelected == true) {
            cardViewStudySet.background =
                ContextCompat.getDrawable(context, R.drawable.selected_item_border)
            cardViewStudySet.alpha = 0.8F
        } else {
            cardViewStudySet.background =
                ContextCompat.getDrawable(context, R.drawable.bg_white)
        }

        // Set item click listener
        cardViewStudySet.setOnClickListener {
            onStudySetItem.handleClickStudySetItem(currentItem, position)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return listStudySet.size
    }

    private fun shouldEnableSwipeForItem(): Boolean? {
        return enableSwipe
    }

    fun setOnItemClickListener(listener: onClickSetItem) {
        this.onClickSet = listener
    }
}
