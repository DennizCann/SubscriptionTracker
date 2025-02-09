package com.denizcan.subscriptiontracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.subscriptiontracker.data.SettingsData
import com.denizcan.subscriptiontracker.data.SettingsDataStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsDataStore = SettingsDataStore(application)
    val settingsState: StateFlow<SettingsData> = settingsDataStore.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsData(
                isDarkMode = false,
                isSystemTheme = true,
                isNotificationsEnabled = true,
                selectedCurrency = "TRY",
                selectedLanguage = "Türkçe"
            )
        )

    fun updateDarkMode(isDarkMode: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateDarkMode(isDarkMode)
        }
    }

    fun updateSystemTheme(isSystemTheme: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateSystemTheme(isSystemTheme)
        }
    }

    fun updateNotifications(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateNotifications(isEnabled)
        }
    }

    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            settingsDataStore.updateCurrency(currency)
        }
    }

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            settingsDataStore.updateLanguage(language)
        }
    }
} 