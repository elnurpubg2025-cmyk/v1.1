package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kivutv_playback_prefs")

class AppStateStore(private val context: Context) {
    companion object {
        val LAST_CHANNEL_URL_KEY = stringPreferencesKey("last_channel_url")
        val IS_FIRST_LAUNCH_KEY = booleanPreferencesKey("is_first_launch")
        val CURRENT_THEME_KEY = stringPreferencesKey("current_theme")
    }

    val lastChannelUrl: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[LAST_CHANNEL_URL_KEY]
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_FIRST_LAUNCH_KEY] ?: true
    }

    val currentTheme: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CURRENT_THEME_KEY] ?: "Elegant Dark"
    }

    suspend fun saveLastChannelUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[LAST_CHANNEL_URL_KEY] = url
        }
    }

    suspend fun setFirstLaunchFinished() {
        context.dataStore.edit { prefs ->
            prefs[IS_FIRST_LAUNCH_KEY] = false
        }
    }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[CURRENT_THEME_KEY] = theme
        }
    }
}
