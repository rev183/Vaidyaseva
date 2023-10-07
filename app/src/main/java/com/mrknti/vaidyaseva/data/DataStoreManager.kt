package com.mrknti.vaidyaseva.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shared_prefs")

class DataStoreManager(context: Context) {

    private val dataStore = context.dataStore

    val authToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN]
    }

    val userId: Flow<Int> = dataStore.data.map { preferences ->
        preferences[USER_ID] ?: 0
    }

    val fcmToken: Flow<String> = dataStore.data.map { preferences ->
        preferences[FCM_TOKEN] ?: ""
    }

    val isLoggedIn: Boolean = runBlocking { authToken.first() != null }

    var isFCMRegistrationPending: Boolean = false
        get() {
            return runBlocking {
                dataStore.data.map { pref ->
                    pref[FCM_REGISTRATION_PENDING] ?: false
                }.first()
            }
        }
        set(value) {
            runBlocking {
                dataStore.edit { pref ->
                    pref[FCM_REGISTRATION_PENDING] = value
                }
            }
            field = value
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

    suspend fun saveUser(id: Int, roles: List<String>) {
        dataStore.edit { preferences ->
            preferences[USER_ID] = id
            preferences[USER_ROLES] = roles.toSet()
        }
    }

    suspend fun saveFCMToken(token: String) {
        dataStore.edit { preferences ->
            preferences[FCM_TOKEN] = token
        }
    }

    companion object {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val USER_ID = intPreferencesKey("user_id")
        val USER_ROLES = stringSetPreferencesKey("user_roles")
        val FCM_TOKEN = stringPreferencesKey("fcm_token")
        val FCM_REGISTRATION_PENDING = booleanPreferencesKey("fcm_pending")
    }
}