package com.mark216tw.minitaiwanradio

import android.content.Context

class UiPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun loadDisplayMode(): String = preferences.getString(DISPLAY_MODE, MODE_SYSTEM) ?: MODE_SYSTEM

    fun saveDisplayMode(mode: String) {
        preferences.edit().putString(DISPLAY_MODE, mode).apply()
    }

    fun loadThemeColor(): Int = preferences.getInt(THEME_COLOR, DEFAULT_THEME_COLOR)

    fun saveThemeColor(color: Int) {
        preferences.edit().putInt(THEME_COLOR, color).apply()
    }

    private companion object {
        const val NAME = "ui_preferences"
        const val DISPLAY_MODE = "display_mode"
        const val THEME_COLOR = "theme_color"
        const val MODE_SYSTEM = "system"
        const val DEFAULT_THEME_COLOR = 0xFFFF765F.toInt()
    }
}
