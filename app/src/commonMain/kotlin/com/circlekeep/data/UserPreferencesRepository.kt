package com.circlekeep.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val IS_PAID = booleanPreferencesKey("is_paid")
        val DEFAULT_GROUPS = stringPreferencesKey("default_groups")
        val GROUP_ORDER = stringPreferencesKey("group_order")
        val GROUP_FILTER_MODE = stringPreferencesKey("group_filter_mode")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val LANGUAGE = stringPreferencesKey("language")
        val HIDE_QR = booleanPreferencesKey("hide_qr")
    }

    val themeStream: Flow<String> = dataStore.data
        .catch {
            emit(emptyPreferences())
        }
        .map { preferences ->
            preferences[PreferencesKeys.THEME] ?: "System"
        }

    val isPaidStream: Flow<Boolean> = dataStore.data
        .catch {
            emit(emptyPreferences())
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_PAID] ?: false
        }

    val defaultGroupsStream: Flow<Set<String>> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_GROUPS]?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
        }

    val groupOrderStream: Flow<List<String>> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[PreferencesKeys.GROUP_ORDER]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        }

    val groupFilterModeStream: Flow<String> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[PreferencesKeys.GROUP_FILTER_MODE] ?: "OR"
        }

    val remindersEnabledStream: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[PreferencesKeys.REMINDERS_ENABLED] ?: false
        }

    val languageStream: Flow<String> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[PreferencesKeys.LANGUAGE] ?: "English"
        }

    val hideQrStream: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[PreferencesKeys.HIDE_QR] ?: false
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

    suspend fun updateDefaultGroups(groups: Set<String>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_GROUPS] = groups.joinToString(",")
        }
    }

    suspend fun updateGroupOrder(order: List<String>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.GROUP_ORDER] = order.joinToString(",")
        }
    }

    suspend fun updateGroupFilterMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.GROUP_FILTER_MODE] = mode
        }
    }

    suspend fun updateRemindersEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDERS_ENABLED] = enabled
        }
    }

    suspend fun updateLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language
        }
    }

    suspend fun updateHideQr(hide: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDE_QR] = hide
        }
    }
}
