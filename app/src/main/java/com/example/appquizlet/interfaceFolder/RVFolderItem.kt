package com.example.appquizlet.interfaceFolder

import com.example.appquizlet.model.FolderModel

interface RVFolderItem {
    fun handleClickFolderItem(folderItem: FolderModel, position: Int)
}