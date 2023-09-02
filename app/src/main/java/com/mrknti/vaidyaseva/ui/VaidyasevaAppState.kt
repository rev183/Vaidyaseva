package com.mrknti.vaidyaseva.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun rememberVaidyasevaAppState(
    navController: NavHostController = rememberNavController(),
    context: Context = LocalContext.current
) = remember(navController, context) {
    VaidyasevaAppState(navController, context)
}

class VaidyasevaAppState(
    val navController: NavHostController,
    private val context: Context
) {

    fun navigateBack() {
        navController.popBackStack()
    }

}