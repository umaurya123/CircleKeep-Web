package com.circlekeep.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val IS_PAID = booleanPreferencesKey("is_paid")
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
