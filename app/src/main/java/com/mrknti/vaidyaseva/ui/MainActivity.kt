package com.mrknti.vaidyaseva.ui

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.network.handleError
import com.mrknti.vaidyaseva.ui.theme.VaidyasevaTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"
    val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VaidyasevaTheme(dynamicColor = false) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VaidyasevaApp()
                }
            }
        }
        registerFCMToken()
        checkNotificationPermission()
        syncNotificationChannels()
        listenViewModelActions()
    }

    private fun listenViewModelActions() {
        lifecycleScope.launch {
            viewModel.actions.collect { action ->
                when (action) {
                    MainViewModelActions.ReFetchFCMToken -> {
                        registerFCMToken()
                        viewModel.clearAction()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun syncNotificationChannels() {
        val notificationsManager = Graph.notificationsManager
        notificationsManager.syncChannels()
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        if (result) {
            // Permission Granted
        } else {
            // Permission Denied
            viewModel.showNotifPermissionDialog()
        }
    }
    @SuppressLint("InlinedApi")
    private fun checkNotificationPermission() {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun registerFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            val repository = Graph.authRepository
            val dataStoreManager = Graph.dataStoreManager
            val isLoggedIn = dataStoreManager.isLoggedIn
            val isFCMRegistrationCompleted = dataStoreManager.isFCMRegistrationCompleted
            val previousToken = runBlocking { dataStoreManager.fcmToken.first() }
            if (previousToken == token && isFCMRegistrationCompleted) return@OnCompleteListener
            if (isLoggedIn) {
                CoroutineScope(Dispatchers.IO).launch {
                    Log.d(TAG, "Register FCM main activity oldToken: $previousToken, newToken: $token")
                    dataStoreManager.isFCMRegistrationCompleted = true
                    repository.registerFCMToken(token, dataStoreManager.getRegisteredDevice().first())
                        .handleError {
                            Log.e(TAG, "Failed to register FCM token", it)
                            dataStoreManager.isFCMRegistrationCompleted = false
                        }
                        .collect {
                            dataStoreManager.saveFCMToken(token)
                            dataStoreManager.saveRegisteredDevice(it.deviceId)
                        }
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    Graph.dataStoreManager.saveFCMToken(token)
                    Graph.dataStoreManager.isFCMRegistrationCompleted = false
                }
            }
        })
    }
}

@Preview(showBackground = true, name = "Main", apiLevel = 33)
@Composable
fun GreetingPreview() {
    VaidyasevaTheme {
        VaidyasevaApp()
    }
}