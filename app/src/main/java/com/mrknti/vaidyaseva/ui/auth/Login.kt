package com.mrknti.vaidyaseva.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrknti.vaidyaseva.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(onLogin: () -> Unit) {

    val viewModel: AuthViewModel = viewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val actions = viewModel.actions.collectAsStateWithLifecycle()
    if (actions.value == AuthActions.Login) {
        onLogin()
    }

    if (viewState.authMode == AuthMode.LOGIN) {
        Box(modifier = Modifier.fillMaxSize()) {
            ClickableText(
                text = AnnotatedString("Sign up here"),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp),
                onClick = { viewModel.setAuthMode(AuthMode.SIGNUP) },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
    if (viewState.isLoading) {
        LoadingView(alignment = Alignment.TopCenter)
    } else if (viewState.error.isNotEmpty()) {
        Toast.makeText(LocalContext.current, viewState.error, Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val title = if (viewState.authMode == AuthMode.LOGIN)  "Login" else "Sign up"
        Text(text = title, style = TextStyle(fontSize = 40.sp, fontFamily = FontFamily.Cursive))

        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            label = { Text(text = "Username") },
            value = viewState.username,
            onValueChange = { viewModel.setUsername(it) })

        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            label = { Text(text = "Password") },
            value = viewState.password,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            onValueChange = { viewModel.setPassword(it) })

        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
            Button(
                onClick = { viewModel.loginOrSignup() },
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = title)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        ClickableText(
            text = AnnotatedString("Forgot password?"),
            onClick = { },
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = FontFamily.Default
            )
        )
    }
}