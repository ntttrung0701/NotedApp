package com.example.appquizlet.model

class RankItemModel(
    val score: Int,
    val seqId: Int,
    val userName: String,
    val email: String,
    val dateOfBirth: String,
    var order: Int? = 0
)