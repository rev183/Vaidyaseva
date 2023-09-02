package com.mrknti.vaidyaseva.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mrknti.vaidyaseva.ui.theme.VaidyasevaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VaidyasevaTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VaidyasevaApp()
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Main", apiLevel = 33)
@Composable
fun GreetingPreview() {
    VaidyasevaTheme {
        VaidyasevaApp()
    }
}