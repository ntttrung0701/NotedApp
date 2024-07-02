package com.example.appquizlet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appquizlet.R

class DayOfWeekAdapter(private val daysOfWeek: List<String>) :
    RecyclerView.Adapter<DayOfWeekAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.day_of_week_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dayOfWeek = daysOfWeek[position]
        holder.bind(dayOfWeek)
    }

    override fun getItemCount(): Int {
        return daysOfWeek.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(dayOfWeek: String) {
            val textView: TextView = itemView.findViewById(R.id.txtDayOfWeek)
            textView.text = dayOfWeek
        }
    }
}
