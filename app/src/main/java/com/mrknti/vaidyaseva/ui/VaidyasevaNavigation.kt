package com.mrknti.vaidyaseva.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.mrknti.vaidyaseva.data.userService.Service
import com.mrknti.vaidyaseva.ui.auth.LoginPage
import com.mrknti.vaidyaseva.ui.chats.ChatDetail
import com.mrknti.vaidyaseva.ui.services.BookService
import com.mrknti.vaidyaseva.ui.services.ServiceDetail

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object HomeTabs : Screen("home_tabs")
    data object Services : Screen("services")
    data object Inbox : Screen("inbox")
    data object Login : Screen("login")
    data object BookServices : Screen("book_services")
    data object ChatDetail : Screen("chat_detail")
    data object ServiceDetail : Screen("service_detail")
}

object NavGraph {
    const val ROOT_ROUTE = "route_root"
    const val HOME_ROUTE = "route_home_tabs"
    const val AUTH_ROUTE = "route_auth"
}

object NavArgKeys {
    const val SERVICE_TYPE = "service_type"
    const val CHAT_THREAD_ID = "thread_id"
    const val SERVICE_DATA = "service_data"
}

@Composable
fun VaidyasevaNavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavGraph.HOME_ROUTE,
        route = NavGraph.ROOT_ROUTE
    ) {
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
        composable(
            route = "${Screen.BookServices.route}/{${NavArgKeys.SERVICE_TYPE}}}",
            arguments = listOf(
                navArgument(NavArgKeys.SERVICE_TYPE) { type = NavType.StringType }
            )
        ) {
            BookService()
        }
        composable(
            route = "${Screen.ChatDetail.route}/{${NavArgKeys.CHAT_THREAD_ID}}}",
            arguments = listOf(
                navArgument(NavArgKeys.CHAT_THREAD_ID) { type = NavType.IntType }
            )
        ) {
            ChatDetail()
        }
        composable(
            route = "${Screen.ServiceDetail.route}/{${NavArgKeys.SERVICE_DATA}}}",
            arguments = listOf(
                navArgument(NavArgKeys.SERVICE_DATA) {
                    type = NavType.StringType
                }
            )
        ) {
            ServiceDetail()
        }
    }
}