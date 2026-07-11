package com.circlekeep.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(context: Context) {
    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val IS_PAID = androidx.datastore.preferences.core.booleanPreferencesKey("is_paid")
    }

    val themeStream: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME] ?: "System"
    }

    val isPaidStream: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_PAID] ?: false
    }

    suspend fun updateTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme
        }
    }

    suspend fun updateIsPaid(isPaid: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PAID] = isPaid
        }
    }
}
