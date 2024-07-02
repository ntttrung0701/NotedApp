package com.example.appquizlet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appquizlet.R

class AdapterCustomDatePicker(private val dateList: List<String>
,private val streakDays: List<String>
) :
    RecyclerView.Adapter<AdapterCustomDatePicker.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewDay: TextView = itemView.findViewById(R.id.textViewDay)
        val imgIconFire: ImageView = itemView.findViewById(R.id.imgFire)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_date, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date = dateList[position]
        // Xử lý logic để lấy ngày và tháng từ date và hiển thị lên TextViews
        holder.textViewDay.text = date
        // Kiểm tra xem ngày có trong danh sách có streak hay không
        if (streakDays.contains(date)) {
            holder.imgIconFire.setImageResource(R.drawable.fire_no_out)
        }
    }

    override fun getItemCount(): Int {
        return dateList.size
    }

}
