package com.mrknti.vaidyaseva.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoadingView(modifier: Modifier = Modifier, delayMillis: Long = 100L, alignment: Alignment = Alignment.Center) {
    Delayed(delayMillis = delayMillis) {
        Box(
            contentAlignment = alignment,
            modifier = when (modifier == Modifier) {
                true -> Modifier.fillMaxSize()
                false -> modifier
            }
        ) {
            val indicatorModifier = when (alignment) {
                Alignment.Center -> Modifier
                else -> Modifier.padding(top = 60.dp)
            }
            ProgressIndicator(modifier = indicatorModifier)
        }
    }
}