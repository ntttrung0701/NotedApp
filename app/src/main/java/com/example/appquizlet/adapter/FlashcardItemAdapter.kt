package com.example.appquizlet.adapter

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appquizlet.R
import com.example.appquizlet.model.FlashCardModel

class FlashcardItemAdapter(
    private val listFlashcard: List<FlashCardModel>,
    private var itemClickListener: OnFlashcardItemClickListener? = null
) : RecyclerView.Adapter<FlashcardItemAdapter.FlashcardItemHolder>() {
    class FlashcardItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    interface OnFlashcardItemClickListener {
        fun onFlashcardItemClick(term: String)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardItemHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_flashcard_detail, parent, false)
        return FlashcardItemHolder(view)
    }

    override fun onBindViewHolder(holder: FlashcardItemHolder, position: Int) {
        val txtTerm = holder.itemView.findViewById<TextView>(R.id.txtFlashcardDetailTerm)
        val txtDefinition = holder.itemView.findViewById<TextView>(R.id.txtFlashcardDetailDefinition)

        txtTerm.text = listFlashcard[position].term
        txtDefinition.text = listFlashcard[position].definition

        holder.itemView.setOnClickListener {
            // Đọc listFlashcard[position].term ngay lập tức
            itemClickListener?.onFlashcardItemClick(listFlashcard[position].term.toString())

            // Tạo một delay 1 giây trước khi đọc listFlashcard[position].definition
            Handler().postDelayed({
                itemClickListener?.onFlashcardItemClick(listFlashcard[position].definition.toString())
            }, 1000) // 1000 milliseconds = 1 second
        }


    }

    override fun getItemCount(): Int {
        return listFlashcard.size
    }
    fun setOnFlashcardItemClickListener(listener: OnFlashcardItemClickListener) {
        this.itemClickListener = listener
    }
}
