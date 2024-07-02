package com.example.appquizlet.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.appquizlet.ItemAchievementBottomSheet
import com.example.appquizlet.R
import com.example.appquizlet.model.TaskData
import com.example.appquizlet.util.Helper
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.math.min

class AchievementAdapter(
    private val listAchievements: List<TaskData>,
    private val context: Context,
    private val numberAdapter: Int,
) :
    RecyclerView.Adapter<AchievementAdapter.AchievementHolder>() {
    var isExpanded = false
    var isExpandedStreak = false

    inner class AchievementHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return AchievementHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementHolder, position: Int) {
        val imgAchievement = holder.itemView.findViewById<ImageView>(R.id.imgAchievement)
        val txtAchievementName = holder.itemView.findViewById<TextView>(R.id.txtNameAchievement)
        val txtAchievementStatus = holder.itemView.findViewById<TextView>(R.id.txtStatus)

        val imageName = "ac${listAchievements[position].id}"
        val imageResourceId =
            context.resources.getIdentifier(imageName, "drawable", context.packageName)
        // Check if drawable is set
        if (listAchievements[position].status != 2) {
            val originalBitmap: Bitmap? =
                BitmapFactory.decodeResource(
                    context.resources,
                    imageResourceId
                )
            val grayscaleBitmap = originalBitmap?.let { Helper.toGrayscale(it) }
            imgAchievement.setImageBitmap(grayscaleBitmap)
        } else {
            imgAchievement.setImageResource(imageResourceId)
            // Retrieve the FCM token
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FCMToken", "Current token: $token")
                    // Use the token as needed
                } else {
                    Log.e("FCMToken", "Failed to get token")
                }
            }
        }
        txtAchievementName.text = listAchievements[position].taskName
        txtAchievementStatus.text = listAchievements[position].description

        holder.itemView.apply {
            setOnClickListener {
//                onClickItemAchievement.handleClickAchievement(position)
                showAchievementDialog(listAchievements[position])
            }
        }

    }

    override fun getItemCount(): Int {
        if (numberAdapter == 1) { // Check if it's a study achievement adapter
            return if (!isExpanded) min(5, listAchievements.size) else listAchievements.size
        } else if (numberAdapter == 2) { // Check if it's a streak achievement adapter
            return if (!isExpandedStreak) min(5, listAchievements.size) else listAchievements.size
        }
        return 0
    }

    private fun showAchievementDialog(taskData: TaskData) {
        val addBottomSheet = ItemAchievementBottomSheet.newInstance(taskData)
        addBottomSheet.show(
            (context as AppCompatActivity).supportFragmentManager,
            ItemAchievementBottomSheet.TAG
        )
    }

    fun setIsExpaned() {
        isExpanded = !isExpanded
        notifyDataSetChanged()
    }

    fun setIsExpandStreak() {
        isExpandedStreak = !isExpandedStreak
        notifyDataSetChanged()
    }
}