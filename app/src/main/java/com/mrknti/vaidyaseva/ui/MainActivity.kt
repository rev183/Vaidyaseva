package com.mrknti.vaidyaseva.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VaidyasevaTheme {
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
            val isLoggedIn = Graph.dataStoreManager.isLoggedIn
            val isFCMRegistrationPending = Graph.dataStoreManager.isFCMRegistrationPending
            val previousToken = runBlocking { Graph.dataStoreManager.fcmToken.first() }
            if (previousToken == token && !isFCMRegistrationPending) return@OnCompleteListener
            if (isLoggedIn) {
                CoroutineScope(Dispatchers.IO).launch {
                    repository.registerFCMToken(token)
                        .handleError { Log.e(TAG, "Failed to register FCM token", it) }
                        .collect {
                            Graph.dataStoreManager.saveFCMToken(token)
                            Graph.dataStoreManager.isFCMRegistrationPending = false
                        }
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    Graph.dataStoreManager.saveFCMToken(token)
                    Graph.dataStoreManager.isFCMRegistrationPending = true
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