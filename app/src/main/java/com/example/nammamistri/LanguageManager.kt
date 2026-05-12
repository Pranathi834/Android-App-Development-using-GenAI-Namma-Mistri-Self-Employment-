package com.example.nammamistri

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import java.util.Locale

// ── Global mutable language state ─────────────────────────────────────────────
// A single source of truth that ALL composables can read reactively.
val AppLanguage = mutableStateOf("en")

// CompositionLocal so any composable can get/set language without passing it down
val LocalIsKannada = compositionLocalOf { false }

object LanguageManager {
    private const val PREFS_NAME = "settings"
    private const val KEY_LANGUAGE = "language"

    fun applyLanguage(context: Context): Context {
        val langCode = getLanguage(context)
        AppLanguage.value = langCode          // sync global state on startup
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun getLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "en") ?: "en"
    }

    fun setLanguage(context: Context, langCode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, langCode)
            .apply()
        AppLanguage.value = langCode          // instantly update all screens
    }
}