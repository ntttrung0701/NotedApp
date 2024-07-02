package com.example.appquizlet.util

// CustomTheme.kt
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

object Theme {
    private const val THEME_PREF = "theme_preference"
    private const val THEME_DEFAULT = "system"
    private const val THEME_LIGHT = "light"
    private const val THEME_DARK = "dark"

    fun applyTheme(context: Context) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val themePreference = sharedPreferences.getString(THEME_PREF, THEME_DEFAULT)

        when (themePreference) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun setThemePreference(context: Context, theme: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(THEME_PREF, theme)
        editor.apply()
    }
}
