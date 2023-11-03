package com.mrknti.vaidyaseva.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.R
import com.mrknti.vaidyaseva.data.building.BuildingData
import com.mrknti.vaidyaseva.data.chat.ChatThread
import com.mrknti.vaidyaseva.data.user.User
import com.mrknti.vaidyaseva.data.userService.Service
import com.mrknti.vaidyaseva.ui.Inbox
import com.mrknti.vaidyaseva.ui.NavGraph
import com.mrknti.vaidyaseva.ui.Screen
import com.mrknti.vaidyaseva.ui.components.LoadingView
import com.mrknti.vaidyaseva.ui.services.Services
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(modifier: Modifier, onHamburgerClick: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = modifier.offset((-16).dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_24),
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.app_name),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onHamburgerClick) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = "Toggle Drawer",
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        } ,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
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
fun HomeNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    navigateToBooking: (ServiceRequest) -> Unit,
    navigateToChatDetail: (ChatThread) -> Unit,
    navigateToServiceDetail: (Service) -> Unit,
    navigateToBuildingDetail: (BuildingData) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        route = NavGraph.HOME_ROUTE
    ) {
        val modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = paddingValues)
        composable(Screen.Home.route) {
            Home(
                modifier,
                navigateToBooking = navigateToBooking,
                navigateToBuildingDetail = navigateToBuildingDetail
            )
        }
        composable(Screen.Services.route) {
            Services(
                modifier,
                onServiceClick = navigateToServiceDetail
            )
        }
        composable(Screen.Inbox.route) {
            Inbox(modifier, onChatClick = navigateToChatDetail)
        }
    }
}

@Composable
fun HomeBottomTabs(navController: NavHostController) {
    val viewModel: HomeTabsViewModel = viewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    when (viewState.loginState) {
        LoginState.Loading -> {
            LoadingView()
        }

        LoginState.NotLoggedIn -> {
            LaunchedEffect(viewState.loginState) {
                navController.navigate(NavGraph.AUTH_ROUTE)
            }
        }

        else -> {
            val tabNavController = rememberNavController()
            ModalNavigationDrawer(
                drawerContent = {
                    VaidyasevaDrawerContent(
                        user = viewState.selfUser!!,
                        onAddClientClick = {
//                    val user = runBlocking { Graph.dataStoreManager.getUser().first() }
//                    val userJson = Graph.moshi.adapter(User::class.java).toJson(user)
//                    navController.navigate(docUploadRoute(userJson))
                            navController.navigate(Screen.OnboardUser.route)
                            coroutineScope.launch { drawerState.close() }
                        },
                        onSearchClick = {
                            navController.navigate(Screen.UserSearch.route)
                            coroutineScope.launch { drawerState.close() }
                        },
                        onLogout = {
                            viewModel.performLogout()
                            FirebaseMessaging.getInstance().deleteToken()
                            coroutineScope.launch { drawerState.close() }
                        }
                    )
                },
                drawerState = drawerState
            ) {
                Scaffold(
                    topBar = {
                        HomeAppBar(modifier = Modifier.fillMaxWidth(), onHamburgerClick = {
                            coroutineScope.launch { drawerState.open() }
                        })
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
                                    icon = {
                                        Icon(
                                            imageVector = it.icon,
                                            contentDescription = it.name
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = it.name,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    HomeNavHost(
                        navController = tabNavController,
                        paddingValues = paddingValues,
                        navigateToBooking = { navController.navigate("${Screen.BookServices.route}/${it.type}}") },
                        navigateToChatDetail = { navController.navigate("${Screen.ChatDetail.route}/${it.id}}") },
                        navigateToServiceDetail = {
                            val moshi = Graph.moshi
                            val jsonAdapter = moshi.adapter(Service::class.java)
                            val serviceJson = jsonAdapter.toJson(it)
                            navController.navigate("${Screen.ServiceDetail.route}/${serviceJson}}")
                        },
                        navigateToBuildingDetail = {
                            navController.navigate("${Screen.BuildingDetail.route}/${it.id}}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VaidyasevaDrawerContent(
    user: User,
    onAddClientClick: () -> Unit,
    onSearchClick: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Hi, ${user.displayName}",
                modifier = Modifier.padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 32.dp),
                style = MaterialTheme.typography.titleLarge
                    .copy(color = MaterialTheme.colorScheme.onPrimary)
            )
        }
        Divider(thickness = 2.dp)
        NavigationDrawerItem(
            label = { Text(text = "Add Client") },
            selected = false,
            onClick = onAddClientClick,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.person_add_24),
                    contentDescription = null
                )
            },
        )
        NavigationDrawerItem(
            label = { Text(text = "User Search") },
            selected = false,
            onClick = onSearchClick,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.search_24),
                    contentDescription = null
                )
            },
        )
        Spacer(modifier = Modifier.weight(1f))
        NavigationDrawerItem(
            label = { Text(text = "Logout") },
            selected = false,
            onClick = onLogout,
            modifier = Modifier.padding(bottom = 16.dp),
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.logout_24),
                    contentDescription = null
                )
            },
        )
    }
}