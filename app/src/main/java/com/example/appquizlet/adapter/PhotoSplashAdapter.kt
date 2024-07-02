package com.example.appquizlet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appquizlet.model.PhotoSplash
import com.example.appquizlet.R

class PhotoSplashAdapter(val list: List<PhotoSplash>) : RecyclerView.Adapter<PhotoSplashAdapter.SplashViewHolder>() {

    inner class SplashViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SplashViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_photo_splash, parent, false)
        return SplashViewHolder(view)
    }

    override fun onBindViewHolder(holder: SplashViewHolder, position: Int) {
            val photoItem = list[position]
        holder.itemView.apply {
            val txtSplash = findViewById<TextView>(R.id.txtSplash)
            val imgPhoto = findViewById<ImageView>(R.id.img_photo)

            txtSplash.setText(list[position].textSplash)
            imgPhoto.setImageResource(list[position].resourceId)
        }
    }

    override fun getItemCount(): Int {
            return list.size
    }
}