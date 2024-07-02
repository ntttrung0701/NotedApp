package com.example.appquizlet.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quoteEntity")
class QuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val quoteId: Long = 0,
    val _id: String ?= "",
    val content: String,
    val author: String,
    val authorSlug: String ?= "",
    val length: Int ?= 0,
    val userId: String ?= ""
)