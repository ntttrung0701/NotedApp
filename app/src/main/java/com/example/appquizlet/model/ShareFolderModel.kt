package com.example.appquizlet.model

class ShareFolderModel(
    val nameOwner: String,
    val avatarOwner: ByteArray,
    val name: String,
    val timeCreated: Long,
    val description: String,
    val studySets: List<StudySetModel>
)
