package com.example.appquizlet.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appquizlet.databinding.LayoutSolutionItemBinding
import com.example.appquizlet.interfaceFolder.RvClickSearchSet
import com.example.appquizlet.model.SearchSetModel

class SolutionItemAdapter(
    private val listSolutionItem: List<SearchSetModel>,
    private val onClickSetSearch: RvClickSearchSet
) :
    RecyclerView.Adapter<SolutionItemAdapter.SolutionItemHolder>() {
    private var maxItemCountToShow = 3

    class SolutionItemHolder(val binding: LayoutSolutionItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolutionItemHolder {
        return SolutionItemHolder(
            LayoutSolutionItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SolutionItemHolder, position: Int) {
        Log.d("Adapter", "onBindViewHolder called for position $position")
        val txtSolutionTitle = holder.binding.txtStudySetSearchTitle
        val studySetChip = holder.binding.studySetChip
        val txtStudySetUsername = holder.binding.txtStudySetUsername

        txtSolutionTitle.text = listSolutionItem[position].name
        studySetChip.text =
            if (listSolutionItem[position].countTerm > 1) "${listSolutionItem[position].countTerm} terms" else "${listSolutionItem[position].countTerm} term"
        txtStudySetUsername.text = listSolutionItem[position].nameOwner

        holder.itemView.setOnClickListener {
            onClickSetSearch.handleClickSetSearch(position)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
//        return if (listSolutionItem.size > maxItemCountToShow) {
//            maxItemCountToShow
//        } else {
//            listSolutionItem.size
//        }
        return listSolutionItem.size
    }

    fun updateMaxItemCountToShow(newMaxItemCount: Int) {
        maxItemCountToShow = newMaxItemCount
        notifyDataSetChanged()
    }

}