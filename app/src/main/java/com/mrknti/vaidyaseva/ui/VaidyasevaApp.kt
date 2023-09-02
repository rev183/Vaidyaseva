package com.mrknti.vaidyaseva.ui

import androidx.compose.runtime.Composable

@Composable
fun VaidyasevaApp(
    appState: VaidyasevaAppState = rememberVaidyasevaAppState()
) {
    VaidyasevaAppContent(navController = appState.navController)
}