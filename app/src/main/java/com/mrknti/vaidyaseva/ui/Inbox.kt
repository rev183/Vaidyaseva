package com.mrknti.vaidyaseva.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Inbox(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.windowInsetsPadding(
            WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
        )
    ) {
        var selectedTab by remember { mutableStateOf(InboxTab.Notifications)}
        val onTabSelected: (InboxTab) -> Unit = { tab ->
            selectedTab = tab
        }
        InboxCategoryTabs(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            modifier = Modifier.heightIn(min = 48.dp)
        )
        when (selectedTab) {
            InboxTab.Notifications -> Notifications(modifier = Modifier
                .fillMaxWidth()
                .weight(1f))
            InboxTab.Messages -> Messages(modifier = Modifier
                .fillMaxWidth()
                .weight(1f))
        }
    }
}

@Composable
fun Messages(modifier: Modifier) {
    Text(text = "Messages")
}

@Composable
fun Notifications(modifier: Modifier) {
    Text(text = "Notifications")
}

@Composable
private fun InboxCategoryTabs(
    selectedTab: InboxTab,
    onTabSelected: (InboxTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedPosition = when (selectedTab) {
        InboxTab.Notifications -> 0
        InboxTab.Messages -> 1
    }

    val indicator = @Composable { tabPositions: List<TabPosition> ->
        InboxTabIndicator(
            Modifier.tabIndicatorOffset(tabPositions[selectedPosition])
        )
    }

    TabRow(
        selectedTabIndex = selectedPosition,
        modifier = modifier,
        indicator = indicator
    ) {
        InboxTab.values().forEachIndexed { index, inboxTab ->
            Tab(
                selected = index == selectedPosition,
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