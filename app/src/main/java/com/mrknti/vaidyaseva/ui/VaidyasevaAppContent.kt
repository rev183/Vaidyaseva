package com.mrknti.vaidyaseva.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun VaidyasevaAppContent(navController: NavHostController) {
    VaidyasevaNavigationGraph(navController = navController)
}

