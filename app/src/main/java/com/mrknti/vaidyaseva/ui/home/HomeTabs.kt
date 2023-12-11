package com.mrknti.vaidyaseva.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.google.firebase.messaging.FirebaseMessaging
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.R
import com.mrknti.vaidyaseva.data.ServiceType
import com.mrknti.vaidyaseva.data.building.BuildingData
import com.mrknti.vaidyaseva.data.chat.ChatThread
import com.mrknti.vaidyaseva.data.user.User
import com.mrknti.vaidyaseva.data.userService.Service
import com.mrknti.vaidyaseva.ui.NavGraph
import com.mrknti.vaidyaseva.ui.Screen
import com.mrknti.vaidyaseva.ui.components.LoadingView
import com.mrknti.vaidyaseva.ui.search.SearchType
import com.mrknti.vaidyaseva.ui.searchRoute
import com.mrknti.vaidyaseva.ui.services.ServicesTabs
import com.mrknti.vaidyaseva.ui.updates.InboxTabs
import kotlinx.coroutines.launch

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
        HorizontalDivider(thickness = 2.dp)
        if (user.canProxyBook()) {
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
        }
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

data class HomeNavItem(
    val name: String,
    val position: Int,
    val icon: ImageVector,
)

val homeNavItems = listOf(
    HomeNavItem(
        name = "Home",
        position = 0,
        icon = Icons.Rounded.Home,
    ),
    HomeNavItem(
        name = "Services",
        position = 1,
        icon = Icons.Rounded.AddCircle,
    ),
    HomeNavItem(
        name = "Inbox",
        position = 2,
        icon = Icons.Rounded.Email,
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(modifier: Modifier, onHamburgerClick: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = modifier.offset((-16).dp)
            ) {
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
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeContent(
    paddingValues: PaddingValues,
    pagerState: PagerState,
    navigateToBooking: (ServiceType) -> Unit,
    navigateToChatDetail: (ChatThread) -> Unit,
    navigateToServiceDetail: (Service) -> Unit,
    navigateToBuildingDetail: (BuildingData) -> Unit,
) {
    val modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues = paddingValues)

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false
    ) { index ->
        when (index) {
            0 -> Home(
                modifier,
                navigateToBooking = navigateToBooking,
                navigateToBuildingDetail = navigateToBuildingDetail
            )
            1 -> ServicesTabs(modifier, onServiceClick = navigateToServiceDetail)
            2 -> InboxTabs(modifier, onChatClick = navigateToChatDetail)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeBottomTabs(navController: NavHostController) {
    val viewModel: HomeTabsViewModel = viewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { homeNavItems.size })

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
            ModalNavigationDrawer(
                drawerContent = {
                    VaidyasevaDrawerContent(
                        user = viewState.selfUser!!,
                        onAddClientClick = {
                            navController.navigate(Screen.OnboardUser.route)
                            coroutineScope.launch { drawerState.close() }
                        },
                        onSearchClick = {
                            navController.navigate(searchRoute(SearchType.MAIN))
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
                        BottomAppBar {
                            homeNavItems.forEach {
                                val selected = pagerState.currentPage == it.position
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.scrollToPage(it.position)
                                        }
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
                    HomeContent(
                        paddingValues = paddingValues,
                        pagerState = pagerState,
                        navigateToBooking = { navController.navigate("${Screen.BookServices.route}/${it.value}}") },
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