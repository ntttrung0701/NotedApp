package com.example.appquizlet.model
data class RankSystem(
    val userRanking: List<RankItemModel>
)

data class RankResultModel(
    val currentScore: Int,
    val currentRank: Int,
    val rankSystem: RankSystem
)
