package com.example.appquizlet.interfaceFolder

import com.example.appquizlet.model.FlashCardModel

interface LearnCardClick {
    fun handleLearnCardClick(position: Int, cardItem: FlashCardModel)
}