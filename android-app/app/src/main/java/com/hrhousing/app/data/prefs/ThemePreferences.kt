package com.hrhousing.app.data.prefs

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Plain (synchronous) SharedPreferences instead of DataStore for the theme flag specifically:
 * it must be readable on the very first frame in [android.app.Activity.onCreate], before
 * `setContent`, so the dark theme applies immediately with no light-theme flash on launch.
 */
class ThemePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean(KEY_DARK, readSystemDefault(context)))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    fun toggle() = setDarkTheme(!_isDarkTheme.value)

    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK, enabled).apply()
        _isDarkTheme.value = enabled
    }

    companion object {
        private const val KEY_DARK = "is_dark_theme"

        private fun readSystemDefault(context: Context): Boolean {
            val uiMode = context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
            return uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }

        /** Synchronous read usable before the ViewModel/Compose tree exists, to pick the launch theme. */
        fun readInitialDarkTheme(context: Context): Boolean {
            val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_DARK, readSystemDefault(context))
        }
    }
}
