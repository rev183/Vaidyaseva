package com.mrknti.vaidyaseva.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrknti.vaidyaseva.R
import com.mrknti.vaidyaseva.data.UserRole
import com.mrknti.vaidyaseva.data.user.User
import com.mrknti.vaidyaseva.ui.onboarding.ImageGallery
import com.mrknti.vaidyaseva.util.DateFormat
import com.mrknti.vaidyaseva.util.formatDate
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Date

@Composable
fun UserSearch(
    onUploadClick: (String) -> Unit,
    onSearchDone: (User) -> Unit,
    navigateToFullScreenImage: (String) -> Unit
) {
    val viewModel: UserSearchViewModel = viewModel()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle(initialValue = "")
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    Scaffold { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            UserSearchBar(
                searchQuery,
                viewModel::onSearchQueryChange,
                viewModel::onClearSearch,
                onUserClick = {
                    if (viewModel.searchType == SearchType.GET_USER) {
                        onSearchDone(it)
                    } else {
                        viewModel.setSelectedUser(it)
                    }
                },
                viewState.searchResults,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
            )
            Spacer(modifier = Modifier.size(20.dp))
            if (viewState.selectedUser != null) {
                SelectedUserDetails(
                    user = viewState.selectedUser!!,
                    buildingName = viewState.selectedUserInfo?.buildingName,
                    roomName = viewState.selectedUserInfo?.roomName,
                    passportData = viewState.passportData,
                    passportExpiry = viewState.passportExpiry,
                    visaData = viewState.visaData,
                    visaExpiry = viewState.visaExpiry,
                    token = viewModel.authToken,
                    onUploadClick = {
                        onUploadClick(viewModel.selectedUserJson)
                    },
                    onImageClick = {
                        val url = URLEncoder.encode(it as String, StandardCharsets.UTF_8.toString())
                        navigateToFullScreenImage(url)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSearchBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onUserClick: (User) -> Unit,
    searchResult: List<User>,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = SearchBarDefaults.windowInsets
) {
    var active by remember { mutableStateOf(false) }

    SearchBar(
        query = searchQuery,
        onQueryChange = onQueryChange,
        onSearch = { active = false },
        active = active,
        onActiveChange = { active = it },
        modifier = modifier,
        placeholder = { Text("Search user...") },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.search_24),
                contentDescription = "search icon"
            )
        },
        trailingIcon = {
            IconButton(onClick = {
                active = false
                onClearSearch()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.close_24),
                    contentDescription = "search icon"
                )
            }
        },
        windowInsets = windowInsets
    ) {
        SearchUserList(users = searchResult, onUserClick = {
            active = false
            onUserClick(it)
        })
    }
}

@Composable
fun SearchUserList(users: List<User>, onUserClick: (User) -> Unit) {
    LazyColumn {
        items(users, key = { it.id }) { user ->
            Card(
                onClick = { onUserClick(user) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 8.dp, top = 8.dp)
            ) {
                Text(text = user.displayName, modifier = Modifier.padding(8.dp))
                val rolesString = user.roles.joinToString(", ") {
                    UserRole.getByValue(it).uiString
                }
                Text(
                    text = "Roles: $rolesString",
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, end = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SelectedUserDetails(
    modifier: Modifier = Modifier,
    user: User,
    buildingName: String?,
    roomName: String?,
    passportData: List<Any>,
    passportExpiry: Date?,
    visaData: List<Any>,
    visaExpiry: Date?,
    token: String,
    onUploadClick: () -> Unit,
    onImageClick: (Any) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)) {
        Text(text = user.displayName, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = "Roles: ${user.roles.joinToString(", ") { UserRole.getByValue(it).uiString }}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.size(4.dp))
        val occupancyText = if (buildingName != null && roomName != null) {
            "Assigned Room: $buildingName - $roomName"
        } else {
            "No room assigned"
        }
        Text(text = occupancyText, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.size(12.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.size(12.dp))
        TextButton(onClick = { expanded = !expanded }) {
            Text(text = "Documents", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                painter = if (expanded) painterResource(id = R.drawable.expand_less_24)
                else painterResource(id = R.drawable.expand_more_24),
                contentDescription = "arrow down",
                modifier = Modifier.size(24.dp)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 10.dp, end = 10.dp)) {
                if (passportData.isNotEmpty()) {
                    Row {
                        Text(text = "Passport", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        if (passportExpiry != null) {
                            Text(
                                text = "Expiry: ${passportExpiry.formatDate(DateFormat.DAY_MONTH_YEAR)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    ImageGallery(
                        imageData = passportData,
                        token = token,
                        showClose = false,
                        onImageClick = {
                            val data = passportData[it]
                            onImageClick(data)
                        }
                    )
                }
                Spacer(modifier = Modifier.size(16.dp))
                if (visaData.isNotEmpty()) {
                    Row {
                        Text(text = "Visa", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        if (visaExpiry != null) {
                            Text(
                                text = "Expiry: ${visaExpiry.formatDate(DateFormat.DAY_MONTH_YEAR)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    ImageGallery(
                        imageData = visaData,
                        token = token,
                        showClose = false,
                        onImageClick = {
                            val data = visaData[it]
                            onImageClick(data)
                        }
                    )
                }
                val hasDocs = passportData.isNotEmpty() && visaData.isNotEmpty()
                val docText = if (hasDocs) "Edit Documents" else "Upload Documents"
                TextButton(
                    onClick = onUploadClick,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
                ) {
                    Text(text = docText)
                    Spacer(modifier = Modifier.size(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.upload_24),
                        contentDescription = "upload button",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

enum class SearchType {
    MAIN, GET_USER
}