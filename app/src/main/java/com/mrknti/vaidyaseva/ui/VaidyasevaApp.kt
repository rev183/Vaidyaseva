package com.mrknti.vaidyaseva.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun VaidyasevaApp(
    appState: VaidyasevaAppState = rememberVaidyasevaAppState(),
    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    val navController = appState.navController
    val actions by viewModel.actions.collectAsStateWithLifecycle()

    if (actions == MainViewModelActions.ShowReLoginDialog) {
        ReLoginDialog {
            viewModel.performLogout()
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
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