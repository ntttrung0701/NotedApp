package com.example.appquizlet.interfaceFolder

import com.example.appquizlet.model.StudySetModel

interface RVStudySetItem {

    fun handleClickStudySetItem(setItem: StudySetModel,position : Int)
}