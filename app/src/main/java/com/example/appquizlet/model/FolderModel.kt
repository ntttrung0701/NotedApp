package com.example.appquizlet.model

class FolderModel(
    val id: String,
    val name: String,
    val timeCreated: Long,
    val description: String,
    val countSets: Int,
    val studySets: List<StudySetModel>,
    var isSelected: Boolean? = false
) {
}