package com.mrknti.vaidyaseva.ui.onboarding

import android.widget.Toast
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrknti.vaidyaseva.data.UserRole
import com.mrknti.vaidyaseva.ui.components.LoadingView

@Composable
fun OnboardClient(onRegister: (String) -> Unit) {

    val viewModel: OnboardingViewModel = viewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val actions = viewModel.actions.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val isRegisterEnabled by remember {
        derivedStateOf {
            viewState.username.isNotEmpty() && viewState.password.isNotEmpty() &&
                    viewState.firstName.isNotEmpty() && viewState.lastName.isNotEmpty()
        }
    }

    if (actions.value == OnboardActions.Signup) {
        val userJson = requireNotNull(viewState.userJson)
        onRegister(userJson)
    }

    if (viewState.isLoading) {
        LoadingView(alignment = Alignment.TopCenter)
    } else if (viewState.error.isNotEmpty()) {
        LaunchedEffect(key1 = viewState.error) {
            Toast.makeText(context, viewState.error, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .scrollable(scrollState, Orientation.Vertical),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Add User",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            label = { Text(text = "First name") },
            value = viewState.firstName,
            onValueChange = { viewModel.setFirstName(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            label = { Text(text = "Last name") },
            value = viewState.lastName,
            onValueChange = { viewModel.setLastName(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            label = { Text(text = "Username") },
            value = viewState.username,
            onValueChange = { viewModel.setUsername(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            label = { Text(text = "Password") },
            value = viewState.password,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            onValueChange = { viewModel.setPassword(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            label = { Text(text = "Confirm Password") },
            value = viewState.confirmPassword,
            onValueChange = { viewModel.setConfirmPassword(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            label = { Text(text = "Phone (optional)") },
            value = viewState.phoneNum,
            onValueChange = { viewModel.setPhoneNum(it) })

        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            label = { Text(text = "Email (optional)") },
            value = viewState.email,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            onValueChange = { viewModel.setEmail(it) })

        Spacer(modifier = Modifier.height(20.dp))
        // role selection
        RoleSelectDropdown(viewModel::setRole, viewModel.selfUser.roles)

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (viewState.password != viewState.confirmPassword) {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                viewModel.performSignup()
            },
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp, 0.dp, 40.dp, 0.dp)
                .height(50.dp),
            enabled = isRegisterEnabled
        ) {
            Text(text = "Register")
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectDropdown(onRoleSelected: (UserRole) -> Unit, roles: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    val options = UserRole.creatableRolesByMe(roles)
    var selectedOption by remember { mutableStateOf(options.firstOrNull() ?: UserRole.CLIENT ) }
    val textValue by remember { derivedStateOf { selectedOption.uiString } }
    ExposedDropdownMenuBox(expanded = false, onExpandedChange = { expanded = !expanded } ) {
        TextField(
            modifier = Modifier.menuAnchor(),
            readOnly = true,
            value = textValue,
            onValueChange = {},
            label = { Text("Select Role") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.uiString) },
                    onClick = {
                        selectedOption = option
                        onRoleSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}