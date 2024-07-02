package com.example.appquizlet.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.example.appquizlet.R
import com.example.appquizlet.databinding.LayoutFoldersItemBinding
import com.example.appquizlet.interfaceFolder.ItemTouchHelperAdapter
import com.example.appquizlet.interfaceFolder.RVFolderItem
import com.example.appquizlet.model.FolderModel
import com.example.appquizlet.util.Helper
import java.util.Collections


class RVFolderItemAdapter(
    private val context: Context,
    private var listFolderItem: List<FolderModel>,
    private val onFolderItemClick: RVFolderItem
) :
    RecyclerView.Adapter<RVFolderItemAdapter.FolderItemHolder>(), ItemTouchHelperAdapter {

    private val viewBinderHelper: ViewBinderHelper = ViewBinderHelper()
    private var onClickFolderItem: onClickFolder? = null

    interface onClickFolder {
        fun handleDeleteFolder(folderId: String)
    }

    inner class FolderItemHolder(val binding: LayoutFoldersItemBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(listFolderItem, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderItemHolder {

        return FolderItemHolder(
            LayoutFoldersItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: FolderItemHolder, position: Int) {
        viewBinderHelper.closeLayout(listFolderItem[position].timeCreated.toString())
        val currentItem = listFolderItem[position]

        viewBinderHelper.bind(
            holder.binding.swipeLayoutFolder,
            listFolderItem[position].timeCreated.toString()
        )

        val txtTitle = holder.binding.txtFolderItemTitle
        val txtUsername = holder.binding.txtFolderItemUsername
        txtTitle.text = listFolderItem[position].name
        txtUsername.text = Helper.getDataUsername(context)
        val cardViewFolder = holder.binding.layoutCardFolder
        holder.binding.cardViewFolder.setOnClickListener {
            onFolderItemClick.handleClickFolderItem(currentItem, position)
            notifyItemChanged(position)
            Log.d("RVFolderItemAdapter", "Item clicked at position $position")
        }
        if (currentItem.isSelected == true) {
            cardViewFolder.background =
                ContextCompat.getDrawable(context, R.drawable.selected_item_border)
            cardViewFolder.alpha = 0.8F
        } else {
            cardViewFolder.background = ContextCompat.getDrawable(context, R.drawable.bg_white)
        }

        holder.binding.btnDeleteFolder.setOnClickListener {
            onClickFolderItem?.handleDeleteFolder(listFolderItem[position].id)
        }
//            imgAvatar.setImageResource(listFolderItem[position].avatar)
    }

    fun setOnClickFolderListener(listener: onClickFolder) {
        this.onClickFolderItem = listener
    }

    override fun getItemCount(): Int {
        return listFolderItem.size
    }
}