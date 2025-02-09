package com.denizcan.subscriptiontracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val IS_SYSTEM_THEME = booleanPreferencesKey("is_system_theme")
        private val IS_NOTIFICATIONS_ENABLED = booleanPreferencesKey("is_notifications_enabled")
        private val SELECTED_CURRENCY = stringPreferencesKey("selected_currency")
        private val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
    }

    val settingsFlow: Flow<SettingsData> = context.dataStore.data.map { preferences ->
        SettingsData(
            isDarkMode = preferences[IS_DARK_MODE] ?: false,
            isSystemTheme = preferences[IS_SYSTEM_THEME] ?: true,
            isNotificationsEnabled = preferences[IS_NOTIFICATIONS_ENABLED] ?: true,
            selectedCurrency = preferences[SELECTED_CURRENCY] ?: "TRY",
            selectedLanguage = preferences[SELECTED_LANGUAGE] ?: "Türkçe"
        )
    }

    suspend fun updateDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDarkMode
        }
    }

    suspend fun updateSystemTheme(isSystemTheme: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_SYSTEM_THEME] = isSystemTheme
        }
    }

    suspend fun updateNotifications(isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_NOTIFICATIONS_ENABLED] = isEnabled
        }
    }

    suspend fun updateCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_CURRENCY] = currency
        }
    }

    suspend fun updateLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_LANGUAGE] = language
        }
    }
}

data class SettingsData(
    val isDarkMode: Boolean,
    val isSystemTheme: Boolean,
    val isNotificationsEnabled: Boolean,
    val selectedCurrency: String,
    val selectedLanguage: String
) 