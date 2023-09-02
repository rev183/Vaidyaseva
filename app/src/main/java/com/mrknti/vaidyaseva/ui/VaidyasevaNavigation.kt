package com.mrknti.vaidyaseva.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.mrknti.vaidyaseva.ui.auth.LoginPage

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object HomeTabs : Screen("home_tabs")
    object Services : Screen("services")
    object Inbox : Screen("inbox")
    object Login : Screen("login")
}

object NavGraph {
    const val ROOT_ROUTE = "route_root"
    const val HOME_ROUTE = "route_home_tabs"
    const val AUTH_ROUTE = "route_auth"
}

@Composable
fun VaidyasevaNavigationGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = NavGraph.HOME_ROUTE, route = NavGraph.ROOT_ROUTE) {
        authGraph(navController)
        homeGraph(navController)
    }
}

fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation(startDestination = Screen.Login.route, route = NavGraph.AUTH_ROUTE) {
        composable(Screen.Login.route) {
            LoginPage {
                navController.popBackStack(NavGraph.AUTH_ROUTE , false)
            }
        }
    }
}

fun NavGraphBuilder.homeGraph(navController: NavHostController) {
    navigation(startDestination = Screen.HomeTabs.route, route = NavGraph.HOME_ROUTE) {
        composable(Screen.HomeTabs.route) {
            HomeBottomTabs(navController = navController)
        }
    }
}