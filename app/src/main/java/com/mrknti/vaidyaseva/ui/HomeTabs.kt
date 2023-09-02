package com.mrknti.vaidyaseva.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.R
import com.mrknti.vaidyaseva.ui.components.LoadingView
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(modifier: Modifier) {
    TopAppBar(
        title = {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = modifier.offset((-4).dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_logo),
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.app_name),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        },
        modifier = modifier,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
    )
}

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(
        name = "Home",
        route = Screen.Home.route,
        icon = Icons.Rounded.Home,
    ),
    BottomNavItem(
        name = "Services",
        route = Screen.Services.route,
        icon = Icons.Rounded.AddCircle,
    ),
    BottomNavItem(
        name = "Inbox",
        route = Screen.Inbox.route,
        icon = Icons.Rounded.Email,
    ),
)

@Composable
fun HomeNavHost(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        route = NavGraph.HOME_ROUTE
    ) {
        val modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = paddingValues)
        composable(Screen.Home.route) { Home(modifier) }
        composable(Screen.Services.route) { Services(modifier) }
        composable(Screen.Inbox.route) { Inbox(modifier) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBottomTabs(navController: NavHostController) {
    val viewModel : HomeAppBarViewModel = viewModel()
    val isLoggedIn = viewModel.auth.collectAsStateWithLifecycle()
    when (isLoggedIn.value) {
        HomeViewState.Loading -> {
            LoadingView()
        }
        HomeViewState.NotLoggedIn -> {
            LaunchedEffect(isLoggedIn) {
                navController.navigate(NavGraph.AUTH_ROUTE)
            }
        }
        else -> {
            val tabNavController = rememberNavController()
            Scaffold(
                topBar = {
                    HomeAppBar(modifier = Modifier.fillMaxWidth())
                },
                bottomBar = {
                    BottomAppBar(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        val backStackEntry = tabNavController.currentBackStackEntryAsState()
                        bottomNavItems.forEach {
                            val selected = backStackEntry.value?.destination?.route == it.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    tabNavController.navigate(it.route)
                                },
                                icon = { Icon(imageVector = it.icon, contentDescription = it.name) },
                                label = { Text(text = it.name, style = MaterialTheme.typography.bodySmall) },
                            )
                        }
                    }
                }
            ) { paddingValues ->
                HomeNavHost(navController = tabNavController, paddingValues = paddingValues)
            }
        }
    }
}

class HomeAppBarViewModel : ViewModel() {
    val auth = Graph.dataStoreManager.authToken.map {
        if (it == null) {
            HomeViewState.NotLoggedIn
        } else {
            HomeViewState.LoggedIn
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeViewState.Loading)

    fun saveAuthToken(authToken: String?) {
        viewModelScope.launch {
            Graph.dataStoreManager.saveAuthToken(authToken)
        }
    }
}

sealed class HomeViewState {
    data object Loading : HomeViewState()
    data object NotLoggedIn : HomeViewState()
    data object LoggedIn : HomeViewState()
}