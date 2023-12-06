package com.mrknti.vaidyaseva.ui.updates

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mrknti.vaidyaseva.data.chat.ChatThread
import com.mrknti.vaidyaseva.ui.chats.Chats
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InboxTabs(modifier: Modifier = Modifier, onChatClick: (ChatThread) -> Unit) {
    Column(
        modifier = modifier.windowInsetsPadding(
            WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
        )
    ) {
        var selectedTab by remember { mutableStateOf(InboxTab.Notifications)}
        val pagerState = rememberPagerState(pageCount = { 2 })
        val coroutineScope = rememberCoroutineScope()

        val onTabSelected: (InboxTab) -> Unit = { tab ->
            coroutineScope.launch {
                selectedTab = tab
                pagerState.animateScrollToPage(tab.ordinal)
            }
        }

        InboxCategoryTabs(
            selectedTabIndex = pagerState.currentPage,
            onTabSelected = onTabSelected,
            modifier = Modifier.heightIn(min = 48.dp)
        )

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { index ->
            when (index) {
                InboxTab.Messages.ordinal -> Chats(modifier = Modifier
                    .fillMaxSize(), onChatClick = onChatClick)
                InboxTab.Notifications.ordinal -> Inbox(modifier = Modifier
                    .fillMaxSize())
            }
        }
    }
}

@Composable
private fun InboxCategoryTabs(
    selectedTabIndex: Int,
    onTabSelected: (InboxTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val indicator = @Composable { tabPositions: List<TabPosition> ->
        InboxTabIndicator(
            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
        )
    }

    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        indicator = indicator
    ) {
        InboxTab.values().forEachIndexed { index, inboxTab ->
            Tab(
                selected = index == selectedTabIndex,
                onClick = { onTabSelected(inboxTab) }
            ) {
                Text(
                    text = inboxTab.name,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 12.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun InboxTabIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Spacer(
        modifier
            .padding(horizontal = 24.dp)
            .height(4.dp)
            .background(color, RoundedCornerShape(topStartPercent = 100, topEndPercent = 100))
    )
}

enum class InboxTab {
    Notifications, Messages
}