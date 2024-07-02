package com.example.appquizlet.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appquizlet.databinding.LayoutFlashcardStudyBinding
import com.example.appquizlet.interfaceFolder.LearnCardClick
import com.example.appquizlet.model.FlashCardModel
import com.example.appquizlet.util.Helper

class LearnFlashcardAdapter(
    private val context: Context,
    private val listFlashcards: List<FlashCardModel>,
    private val onClickLearnCard: LearnCardClick,
    private var itemClickListener: onLearnCardClick? = null,
) :
    RecyclerView.Adapter<LearnFlashcardAdapter.LearnFlashcardHolder>() {
    private val flippedPositions = HashSet<Int>()

    interface onLearnCardClick {
        fun handleClickAudio(term: String)
    }

    inner class LearnFlashcardHolder(val binding: LayoutFlashcardStudyBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LearnFlashcardHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.layout_flashcard_study, parent, false)
        return LearnFlashcardHolder(
            LayoutFlashcardStudyBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LearnFlashcardHolder, position: Int) {
        val currentItem = listFlashcards[position]
        holder.itemView.apply {
            val cardView = holder.binding.cardViewLearn
            val txtCardLearn = holder.binding.txtFlashcardStudyTerm
            if(currentItem.isUnMark == true) {
                txtCardLearn.text = currentItem.definition
            } else {
                txtCardLearn.text = currentItem.term
            }
            val btnSpeak = holder.binding.iconSpeak
            btnSpeak.setOnClickListener {
                if (currentItem.isUnMark == true) {
                    listFlashcards[position].definition?.let { it1 ->
                        itemClickListener?.handleClickAudio(it1)
                    }
                } else {
                    listFlashcards[position].term?.let { it1 ->
                        itemClickListener?.handleClickAudio(it1)
                    }
                }

            }

            setOnClickListener {
                onClickLearnCard.handleLearnCardClick(position, currentItem)
                Helper.flipCard(cardView, txtCardLearn, currentItem)

//                if (!flippedPositions.contains(position)) {
//                    // Nếu thẻ chưa được flip, thực hiện flip và thêm vào danh sách đã flip
//                    Helper.flipCard(cardView, txtCardLearn, currentItem)
//                    flippedPositions.add(position)
//                } else {
//                    // Nếu thẻ đã được flip, tiếp tục xử lý sự kiện auto play
//                    onClickLearnCard.handleLearnCardClick(position, currentItem)
//                    Helper.flipCard(cardView, txtCardLearn, currentItem)
//                    // Thêm vào danh sách đã flip để tránh flip liên tục khi auto play
//                    flippedPositions.add(position)
//                }
            }
        }
    }

    fun isFlipped(position: Int): Boolean {
        return flippedPositions.contains(position)
    }

    fun flipCard(position: Int) {
        flippedPositions.add(position)
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return listFlashcards.size
    }

    fun setOnLearnCardClick(listener: onLearnCardClick) {
        this.itemClickListener = listener
    }
}