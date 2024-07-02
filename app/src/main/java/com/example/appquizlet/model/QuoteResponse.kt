package com.example.appquizlet.model

import com.example.appquizlet.entity.QuoteEntity

data class QuoteResponse(
    val count: Int,
    val totalCount: Int,
    val page: Int,
    val totalPages: Int,
    val lastItemIndex: Int,
    val results: List<QuoteEntity>
)

data class QuoteRemote(
    val _id: String,
    val content: String,
    val author: String,
    val authorSlug: String,
    val length: Int,
    val tags: List<String>
)
