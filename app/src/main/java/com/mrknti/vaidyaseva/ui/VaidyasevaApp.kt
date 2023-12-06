package com.mrknti.vaidyaseva.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@SuppressLint("InlinedApi")
@Composable
fun VaidyasevaApp(
    appState: VaidyasevaAppState = rememberVaidyasevaAppState(),
    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    val navController = appState.navController
    val actions by viewModel.actions.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showNotifPermissionDialog: Boolean by remember { mutableStateOf(false) }

    val settingsIntent = remember {
        Intent().apply {
            action = "android.settings.APP_NOTIFICATION_SETTINGS"
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
    }

    if (showNotifPermissionDialog) {
        NotifPermissionDialog {
            context.startActivity(settingsIntent)
            showNotifPermissionDialog = false
        }
    }

    if (actions == MainViewModelActions.ShowReLoginDialog) {
        ReLoginDialog {
            viewModel.performLogout()
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    } else if (actions == MainViewModelActions.ShowNotifPromptDialog) {
        showNotifPermissionDialog = true
    }
    VaidyasevaNavigationGraph(navController)
}

@Composable
fun ReLoginDialog(onLogout: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text(text = "Session Expired", style = MaterialTheme.typography.bodyLarge) },
        text = {
            Text(
                text = "Please login again to continue",
                style = MaterialTheme.typography.bodySmall
            )
        },
        confirmButton = {
            TextButton(onClick = onLogout) {
                Text("Confirm")
            }
        }
    )
}

@Composable
fun NotifPermissionDialog(onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text(text = "Notification Permission", style = MaterialTheme.typography.bodyLarge) },
        text = {
            Text(
                text = "Please enable notification permission to continue",
                style = MaterialTheme.typography.bodySmall
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm")
            }
        }
    )
}