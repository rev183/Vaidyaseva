package com.mrknti.vaidyaseva.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shared_prefs")

class DataStoreManager(context: Context) {

    private val dataStore = context.dataStore

    val authToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN]
    }

    suspend fun saveAuthToken(authToken: String?) {
        dataStore.edit { preferences ->
            if (authToken == null) {
                preferences.remove(AUTH_TOKEN)
                return@edit
            }
            preferences[AUTH_TOKEN] = authToken
        }
    }

    companion object {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
    }
}