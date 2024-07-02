package com.example.appquizlet.util

import android.content.Context
import android.util.Log

object SharedPreferencesManager {

    private const val CURRENT_STREAK = "currentStreak"
    private const val ID_USER = "idUser"
    private const val CHANGE_LANGUAGE = "ChangeLanguage"
    private const val CHANGE_THEME = "changeTheme"
    private const val TYPE_SELECTED = "TypeSelected"
    private const val LANGUAGE_CHOOSE = "languageChoose"
    private const val COUNT_DETECT = "countDetect"

    // Thêm các tên SharedPreferences khác nếu cần
    fun clearAllPreferences(context: Context) {
        Log.d("SharedPreferences", "Clearing all preferences")
        // Xóa SharedPreferences
        context.getSharedPreferences(CURRENT_STREAK, Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences(ID_USER, Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences(CHANGE_LANGUAGE, Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences(CHANGE_THEME, Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences(TYPE_SELECTED, Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences(LANGUAGE_CHOOSE, Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences(COUNT_DETECT, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
