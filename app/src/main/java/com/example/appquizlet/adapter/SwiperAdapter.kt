package com.example.appquizlet.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.appquizlet.R
import com.example.appquizlet.model.FlashCardModel

class SwiperAdapter(private val mData: List<FlashCardModel>) : BaseAdapter() {

    private class ViewHolder {
        val term: TextView
        val definition: TextView

        constructor(view: View) {
            term = view.findViewById(R.id.txtFlashcardStudyTerm)
            definition = view.findViewById(R.id.txtFlashcardStudyDefinition)
        }
    }

    override fun getCount(): Int {
        return mData.size
    }

    override fun getItem(position: Int): Any {
        return mData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        val viewHolder: ViewHolder

        if (convertView == null) {
            convertView = LayoutInflater.from(parent!!.context).inflate(R.layout.layout_flashcard_study, parent, false)
            viewHolder = ViewHolder(convertView)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        viewHolder.term.text = mData[position].term
        viewHolder.definition.text = mData[position].definition

        Log.d("ad", viewHolder.term.toString())

        return convertView!!
    }
}
