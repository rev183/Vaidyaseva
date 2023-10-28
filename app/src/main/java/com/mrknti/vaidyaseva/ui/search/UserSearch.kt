package com.mrknti.vaidyaseva.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
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
import com.mrknti.vaidyaseva.data.user.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSearch() {
    val viewModel: UserSearchViewModel = viewModel()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle(initialValue = "")
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    var active by remember { mutableStateOf(false) }

    Scaffold { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onSearch = { active = false },
                active = active,
                onActiveChange = { active = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 8.dp,
                        bottom = 8.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                placeholder = { Text("Search user...") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.search_24),
                        contentDescription = "search icon"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = viewModel::onClearSearch) {
                        Icon(
                            painter = painterResource(id = R.drawable.close_24),
                            contentDescription = "search icon"
                        )
                    }
                }
            ) {
                SearchUserList(users = viewState.searchResults, onUserClick = {
                    active = false
                    viewModel.setSelectedUser(it)
                })
            }
            Spacer(modifier = Modifier.size(20.dp))
            if (viewState.selectedUser != null) {
                SelectedUserDetails(user = viewState.selectedUser!!)
            }
        }
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
                    text = "Roles: ${user.role?.joinToString(", ")}",
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, end = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SelectedUserDetails(modifier: Modifier = Modifier, user: User) {
    Column(modifier = modifier.padding(8.dp)) {
        Text(text = "Selected User-", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = user.displayName)
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = "Roles: ${user.role?.joinToString(", ")}")
        Spacer(modifier = Modifier.size(8.dp))
        DocumentsSection {

        }
    }
}

@Composable
fun DocumentsSection(onToggleSection: () -> Unit) {
    val expanded by remember { mutableStateOf(false) }
    Column {
        TextButton(onClick = onToggleSection) {
            Text(text = "Documents")
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                painter = if (expanded) painterResource(id = R.drawable.expand_less_24)
                else painterResource(id = R.drawable.expand_more_24),
                contentDescription = "arrow down",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}