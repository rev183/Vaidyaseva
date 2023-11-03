package com.mrknti.vaidyaseva.ui.search

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mrknti.vaidyaseva.R
import com.mrknti.vaidyaseva.data.user.User

@Composable
fun UserSearch(onUploadClick: (String) -> Unit) {
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
                viewModel::setSelectedUser,
                viewState.searchResults,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
            )
            Spacer(modifier = Modifier.size(20.dp))
            if (viewState.selectedUser != null) {
                SelectedUserDetails(
                    user = viewState.selectedUser!!,
                    passportUrl = viewState.passportUrl,
                    passportUri = viewState.passportUri,
                    visaUrl = viewState.visaUrl,
                    visaUri = viewState.visaUri,
                    token = viewModel.authToken,
                    onUploadClick = {
                        onUploadClick(viewModel.selectedUserJson)
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

@OptIn(ExperimentalMaterial3Api::class)
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
                Text(
                    text = "Roles: ${user.roles?.joinToString(", ")}",
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
    passportUrl: String?,
    passportUri: Uri?,
    visaUrl: String?,
    visaUri: Uri?,
    token: String,
    onUploadClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val passportImageModel by remember(passportUri, passportUrl) { derivedStateOf {
        if (passportUrl != null) {
            ImageRequest.Builder(context)
                .data(passportUrl)
                .addHeader("token", token)
                .build()
        } else passportUri
    } }
    val visaImageModel by remember(visaUri, visaUrl) { derivedStateOf {
        if (visaUrl != null) {
            ImageRequest.Builder(context)
                .data(visaUrl)
                .addHeader("token", token)
                .build()
        } else visaUri
    } }

    Column(modifier = modifier.padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)) {
        Text(text = "Selected User-", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = user.displayName, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.size(4.dp))
        Text(text = "Roles: ${user.roles?.joinToString(", ")}")
        Spacer(modifier = Modifier.size(8.dp))
        Divider()
        TextButton(onClick = { expanded = !expanded }) {
            Text(text = "Documents", style = MaterialTheme.typography.titleSmall)
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
                if (passportImageModel != null) {
                    Text(text = "Passport")
                    Spacer(modifier = Modifier.size(8.dp))
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(passportUrl)
                            .addHeader("token", token)
                            .build(),
                        contentDescription = "Documents",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    )
                }
                Spacer(modifier = Modifier.size(16.dp))
                if (visaImageModel != null) {
                    Text(text = "Visa")
                    Spacer(modifier = Modifier.size(8.dp))
                    AsyncImage(model = ImageRequest.Builder(LocalContext.current)
                        .data(visaUrl)
                        .addHeader("token", token)
                        .build(),
                        contentDescription = "Documents",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    )
                }
                if (visaImageModel == null || passportImageModel == null) {
                    TextButton(
                        onClick = onUploadClick,
                        modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
                    ) {
                        Text(text = "Upload Documents")
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
}