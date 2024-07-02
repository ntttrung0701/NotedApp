package com.example.appquizlet.model

data class CreateSetRequest(
    val name: String,
    val description: String,
    val idFolderOwner: String? = "",
    val allNewCards: List<FlashCardModel>,
    var isPublish: Boolean? = false
)