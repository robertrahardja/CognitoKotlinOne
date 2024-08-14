package com.rr.cognitoone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rr.cognitoone.ui.theme.CognitooneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CognitooneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthScreen()
                }
            }
        }
    }
}

@Composable
fun AuthScreen(authViewModel: AuthViewModel = viewModel()) {
    val uiState by authViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            uiState.isSignedIn -> SignedInScreen(
                username = uiState.username ?: "",
                onSignOut = { authViewModel.signOut() }
            )
            uiState.isSignUpComplete -> SignInScreen(
                onSignIn = { username, password -> authViewModel.signIn(username, password) }
            )
            else -> SignUpScreen(
                onSignUp = { username, email, password -> authViewModel.signUp(username, email, password) },
                onConfirmSignUp = { username, code -> authViewModel.confirmSignUp(username, code) }
            )
        }

        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun SignedInScreen(username: String, onSignOut: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Welcome, $username!")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSignOut) {
            Text("Sign Out")
        }
    }
}

@Composable
fun SignInScreen(onSignIn: (String, String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onSignIn(username, password) }) {
            Text("Sign In")
        }
    }
}

@Composable
fun SignUpScreen(
    onSignUp: (String, String, String) -> Unit,
    onConfirmSignUp: (String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmationCode by remember { mutableStateOf("") }
    var isConfirmationRequired by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (!isConfirmationRequired) {
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                onSignUp(username, email, password)
                isConfirmationRequired = true
            }) {
                Text("Sign Up")
            }
        } else {
            TextField(
                value = confirmationCode,
                onValueChange = { confirmationCode = it },
                label = { Text("Confirmation Code") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onConfirmSignUp(username, confirmationCode) }) {
                Text("Confirm Sign Up")
            }
        }
    }
}