package id.ac.stmi.todolist

import android.content.Context

object ThemeManager {

    private const val PREF_NAME = "todo_theme"
    private const val KEY_THEME = "selected_theme"

    fun saveTheme(context: Context, theme: String) {
        val preferences = context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )

        preferences.edit()
            .putString(KEY_THEME, theme)
            .apply()
    }

    fun getTheme(context: Context): String {
        val preferences = context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )

        return preferences.getString(KEY_THEME, "Light") ?: "Light"
    }
}