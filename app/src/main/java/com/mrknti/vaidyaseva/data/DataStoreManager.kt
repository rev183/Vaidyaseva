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
import com.mrknti.vaidyaseva.data.building.BuildingData
import com.mrknti.vaidyaseva.data.user.User
import com.mrknti.vaidyaseva.data.user.UserInfo
import com.mrknti.vaidyaseva.ui.home.LoginState
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shared_prefs")

class DataStoreManager(context: Context, private val moshi: Moshi) {

    private val dataStore = context.dataStore

    val authToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN]
    }

    val authFlow: Flow<LoginState> = authToken.map {
        if (it == null) {
            LoginState.NotLoggedIn
        } else {
            LoginState.LoggedIn
        }
    }.distinctUntilChanged()

    val userId: Flow<Int> = dataStore.data.map { preferences ->
        preferences[USER_ID] ?: 0
    }

    val fcmToken: Flow<String> = dataStore.data.map { preferences ->
        preferences[FCM_TOKEN] ?: ""
    }

    val isLoggedIn: Boolean = runBlocking { authToken.first() != null }

    var isFCMRegistrationCompleted: Boolean = false
        get() {
            return runBlocking {
                dataStore.data.map { pref ->
                    pref[FCM_REGISTRATION_COMPLETED] ?: false
                }.first()
            }
        }
        set(value) {
            runBlocking {
                dataStore.edit { pref ->
                    pref[FCM_REGISTRATION_COMPLETED] = value
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

    suspend fun saveUser(id: Int, displayName: String, roles: List<String>) {
        dataStore.edit { preferences ->
            preferences[USER_ID] = id
            preferences[USER_DISPLAY_NAME] = displayName
            preferences[USER_ROLES] = roles.toSet()
        }
    }

    suspend fun saveUser(user: User) {
        saveUser(user.id, user.displayName, user.roles)
    }

    suspend fun saveUserInfo(userInfo: UserInfo) {
        saveUser(userInfo.user)
        moshi.adapter(UserInfo::class.java).toJson(userInfo).also {
            dataStore.edit { preferences ->
                preferences[USER_INFO] = it
            }
        }
    }

    fun getUserInfo(): Flow<UserInfo?> {
        return dataStore.data.map { preferences ->
            preferences[USER_INFO]?.let {
                moshi.adapter(UserInfo::class.java).fromJson(it)
            }
        }
    }

    suspend fun saveBuildingData(buildings: List<BuildingData>) {
        val adapter = moshi.adapter<List<BuildingData>>(
            Types.newParameterizedType(
                List::class.java,
                BuildingData::class.java
            ), emptySet()
        )
        adapter.toJson(buildings).also {
            dataStore.edit { preferences ->
                preferences[BUILDING_DATA] = it
            }
        }
    }

    private fun getBuildingData(): Flow<List<BuildingData>?> {
        return dataStore.data.map { preferences ->
            val adapter = moshi.adapter<List<BuildingData>>(
                Types.newParameterizedType(
                    List::class.java,
                    BuildingData::class.java
                ), emptySet()
            )
            preferences[BUILDING_DATA]?.let {
                adapter.fromJson(it)
            }
        }
    }

    fun getBuildingById(id: Int): Flow<BuildingData?> {
        return getBuildingData().map { buildings ->
            buildings?.find { it.id == id }
        }
    }

    fun getTransportDetails(source: Int, destination: Int): Flow<Pair<String?, String?>> {
        return combine(
            getBuildingById(source),
            getBuildingById(destination)
        ) { s, d ->
            (s?.name to d?.name)
        }
    }

    fun getSelfBuilding(): Flow<BuildingData?> {
        return getUserInfo().map { userInfo ->
            userInfo?.buildingId?.let { id ->
                getBuildingById(id)
            }
        }.map { building ->
            building?.first()
        }
    }

    fun getUser(): Flow<User?> {
        return combine(
            dataStore.data.map { preferences ->
                preferences[USER_ID]
            },
            dataStore.data.map { preferences ->
                preferences[USER_DISPLAY_NAME]
            },
            dataStore.data.map { preferences ->
                preferences[USER_ROLES]
            }
        ) { id, displayName, roles ->
            if (id != null && displayName != null && roles != null) {
                User(id, displayName = displayName, roles = roles.toList())
            } else {
                null
            }
        }
    }

    suspend fun saveFCMToken(token: String) {
        dataStore.edit { preferences ->
            preferences[FCM_TOKEN] = token
        }
    }

    suspend fun saveRegisteredDevice(id: Int) {
        dataStore.edit { preferences ->
            preferences[DEVICE_ID] = id
        }
    }

    fun getRegisteredDevice(): Flow<Int?> {
        return dataStore.data.map { preferences ->
            preferences[DEVICE_ID]
        }
    }

    suspend fun clearOnLogout() {
        dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN)
            preferences.remove(USER_ID)
            preferences.remove(USER_DISPLAY_NAME)
            preferences.remove(USER_ROLES)
            preferences.remove(FCM_TOKEN)
            preferences.remove(DEVICE_ID)
            preferences[FCM_REGISTRATION_COMPLETED] = false
        }
    }

    companion object {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val USER_ID = intPreferencesKey("user_id")
        val USER_DISPLAY_NAME = stringPreferencesKey("user_display_name")
        val USER_ROLES = stringSetPreferencesKey("user_roles")
        val FCM_TOKEN = stringPreferencesKey("fcm_token")
        val FCM_REGISTRATION_COMPLETED = booleanPreferencesKey("fcm_completed")
        val USER_INFO = stringPreferencesKey("user_info")
        val BUILDING_DATA = stringPreferencesKey("building_data")
        val DEVICE_ID = intPreferencesKey("device_id")
    }
}