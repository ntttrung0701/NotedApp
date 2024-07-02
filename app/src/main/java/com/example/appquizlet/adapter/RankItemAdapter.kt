package com.example.appquizlet.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.appquizlet.ItemAchievementBottomSheet
import com.example.appquizlet.ItemUserRankDetail
import com.example.appquizlet.databinding.RankLeaderBoardItemBinding
import com.example.appquizlet.model.RankItemModel
import com.example.appquizlet.model.RankResultModel
import com.example.appquizlet.model.TaskData
import com.example.appquizlet.util.Helper

class RankItemAdapter(
    private var listItemRank: List<RankItemModel>,
    private val context: Context,
) : RecyclerView.Adapter<RankItemAdapter.RankItemHolder>() {

    var isExpanded: Boolean = false

    inner class RankItemHolder(val binding: RankLeaderBoardItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankItemHolder {
        return RankItemHolder(
            RankLeaderBoardItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RankItemHolder, position: Int) {
        val currentItem = listItemRank[position]
        val txtOrder = holder.binding.txtOrder
        val txtTopName = holder.binding.txtTopName
        val txtPoint = holder.binding.txtTopPoint
        val imgAvatar = holder.binding.imgAvatar
        val imgTopOrder = holder.binding.imgTopOrder
        listItemRank[position].order = position + 1
        val imageName = "medal_${position + 1}"
        val imageResourceId =
            context.resources.getIdentifier(imageName, "drawable", context.packageName)
        if (position == 0 || position == 1 || position == 2) {
            imgTopOrder.visibility = View.VISIBLE
            imgTopOrder.setImageResource(imageResourceId)
            txtOrder.visibility = View.GONE
        }
        if(Helper.getDataUsername(context) == listItemRank[position].userName || Helper.getDataUsername(context) == listItemRank[position].email) {
            txtTopName.text = "Me"
        } else {
            txtTopName.text = currentItem.userName
        }
        txtPoint.text = currentItem.score.toString()
        txtOrder.text = (position + 1).toString()
//        imgAvatar.setImageResource(currentItem.topUserImage)

        holder.itemView.apply {
            setOnClickListener {
                showAchievementDialog(listItemRank[position])
            }
        }
    }


    override fun getItemCount(): Int {
//        return if (isExpanded) {
//            listItemRank.size
//        }
//        else {
//            3
//        }
        return listItemRank.size
    }

    fun setIsExpanded() {
        isExpanded = isExpanded.not()
    }

    private fun showAchievementDialog(rankItemModel: RankItemModel) {
        val addBottomSheet = ItemUserRankDetail.newInstance(rankItemModel)
        addBottomSheet.show(
            (context as AppCompatActivity).supportFragmentManager,
            addBottomSheet.tag
        )
    }
}
