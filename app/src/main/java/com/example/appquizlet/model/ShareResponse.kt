package com.example.appquizlet.model

class ShareResponse(
    val idOwner: String,
    val nameOwner: String,
    val avatarOwner: ByteArray,
    val name: String,
    val timeCreated: Long,
    val description: String,
    val cards: List<FlashCardModel>
) {
}