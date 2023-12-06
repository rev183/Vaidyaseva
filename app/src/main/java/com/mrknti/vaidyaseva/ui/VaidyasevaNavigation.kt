package com.mrknti.vaidyaseva.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.mrknti.vaidyaseva.data.user.User
import com.mrknti.vaidyaseva.ui.auth.LoginPage
import com.mrknti.vaidyaseva.ui.building.BuildingDetail
import com.mrknti.vaidyaseva.ui.chats.ChatDetail
import com.mrknti.vaidyaseva.ui.home.HomeBottomTabs
import com.mrknti.vaidyaseva.ui.onboarding.DocumentUpload
import com.mrknti.vaidyaseva.ui.onboarding.OnboardClient
import com.mrknti.vaidyaseva.ui.search.SearchType
import com.mrknti.vaidyaseva.ui.search.UserSearch
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
    data object OnboardUser : Screen("onboard_user")
    data object DocUpload : Screen("doc_upload")
    data object UserSearch : Screen("user_search")
    data object BuildingDetail : Screen("building_detail")
}

object NavGraph {
    const val ROOT_ROUTE = "route_root"
    const val HOME_ROUTE = "route_home_tabs"
    const val AUTH_ROUTE = "route_auth"
}

object NavArgKeys {
    const val REQUESTER = "requester"
    const val SEARCH_TYPE = "search_type"
    const val SERVICE_TYPE = "service_type"
    const val CHAT_THREAD_ID = "thread_id"
    const val SERVICE_DATA = "service_data"
    const val USER_DATA = "user_data"
    const val BUILDING_ID = "building_id"
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

fun docUploadRoute(userJson: String) = "${Screen.DocUpload.route}/${userJson}}"

fun searchRoute(searchType: SearchType) = "${Screen.UserSearch.route}/${searchType.name}}"

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
        ) { entry ->
            val requesterFlow = entry.savedStateHandle.getStateFlow<User?>(NavArgKeys.REQUESTER, null)

            BookService(
                onFinishClick = { navController.popBackStack(Screen.HomeTabs.route, false) },
                onGotoSearch = { navController.navigate(searchRoute(SearchType.GET_USER)) },
                requesterFlow = requesterFlow
            )
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
        composable(route = Screen.OnboardUser.route) {
            OnboardClient { userJson ->
                navController.navigate(docUploadRoute(userJson))
            }
        }
        composable(route = "${Screen.DocUpload.route}/{${NavArgKeys.USER_DATA}}}",
            arguments = listOf(
                navArgument(NavArgKeys.USER_DATA) {
                    type = NavType.StringType
                }
            )
        ) {
            DocumentUpload {
                navController.popBackStack(Screen.HomeTabs.route, false)
            }
        }
        composable(
            route = "${Screen.UserSearch.route}/{${NavArgKeys.SEARCH_TYPE}}}",
            arguments = listOf(
                navArgument(NavArgKeys.SEARCH_TYPE) {
                    type = NavType.EnumType(SearchType::class.java)
                },
            )
        ) {
            UserSearch(
                onUploadClick = { userJson ->
                    navController.navigate(docUploadRoute(userJson))
                },
                onSearchDone = {
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        NavArgKeys.REQUESTER,
                        it
                    )
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = "${Screen.BuildingDetail.route}/{${NavArgKeys.BUILDING_ID}}}",
            arguments = listOf(
                navArgument(NavArgKeys.BUILDING_ID) {
                    type = NavType.IntType
                }
            )
        ) {
            BuildingDetail()
        }
    }
}