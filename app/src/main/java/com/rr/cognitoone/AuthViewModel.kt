package com.rr.cognitoone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.auth.result.AuthSignInResult
import com.amplifyframework.auth.result.AuthSignUpResult
import com.amplifyframework.core.Amplify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthStatus()
    }

    fun signUp(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val options = AuthSignUpOptions.builder()
                    .userAttribute(AuthUserAttributeKey.email(), email)
                    .build()
                val result = suspendCoroutine<AuthSignUpResult> { continuation ->
                    Amplify.Auth.signUp(
                        username,
                        password,
                        options,
                        { continuation.resume(it) },
                        { continuation.resumeWithException(it) }
                    )
                }
                handleSignUpResult(result)
            } catch (error: AuthException) {
                _uiState.update { it.copy(errorMessage = error.message ?: "Sign up failed") }
            }
        }
    }

    fun confirmSignUp(username: String, code: String) {
        viewModelScope.launch {
            try {
                suspendCoroutine<Boolean> { continuation ->
                    Amplify.Auth.confirmSignUp(
                        username,
                        code,
                        { continuation.resume(true) },
                        { continuation.resumeWithException(it) }
                    )
                }
                _uiState.update { it.copy(isSignUpComplete = true, errorMessage = null) }
            } catch (error: AuthException) {
                _uiState.update { it.copy(errorMessage = error.message ?: "Confirmation failed") }
            }
        }
    }

    fun signIn(username: String, password: String) {
        viewModelScope.launch {
            try {
                val result = suspendCoroutine<AuthSignInResult> { continuation ->
                    Amplify.Auth.signIn(
                        username,
                        password,
                        { continuation.resume(it) },
                        { continuation.resumeWithException(it) }
                    )
                }
                handleSignInResult(result)
            } catch (error: AuthException) {
                _uiState.update { it.copy(errorMessage = error.message ?: "Sign in failed") }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                suspendCoroutine<Unit> { continuation ->
                    Amplify.Auth.signOut {
                        continuation.resume(Unit)
                    }
                }
                _uiState.update { it.copy(isSignedIn = false, username = null, errorMessage = null) }
            } catch (error: AuthException) {
                _uiState.update { it.copy(errorMessage = error.message ?: "Sign out failed") }
            }
        }
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                val session = suspendCoroutine { continuation ->
                    Amplify.Auth.fetchAuthSession(
                        { continuation.resume(it) },
                        { continuation.resumeWithException(it) }
                    )
                }
                val username = suspendCoroutine<String?> { continuation ->
                    Amplify.Auth.getCurrentUser(
                        { continuation.resume(it.username) },
                        { continuation.resume(null) }
                    )
                }
                _uiState.update { it.copy(
                    isSignedIn = session.isSignedIn,
                    username = username
                ) }
            } catch (error: AuthException) {
                _uiState.update { it.copy(errorMessage = error.message ?: "Failed to fetch auth session") }
            }
        }
    }

    private fun handleSignUpResult(result: AuthSignUpResult) {
        when {
            result.isSignUpComplete -> _uiState.update { it.copy(isSignUpComplete = true, errorMessage = null) }
            else -> _uiState.update { it.copy(isSignUpComplete = false, errorMessage = "Account needs confirmation") }
        }
    }

    private fun handleSignInResult(result: AuthSignInResult) {
        // Assuming a successful sign-in if no exception was thrown
        checkAuthStatus()
    }
}

data class AuthUiState(
    val isSignedIn: Boolean = false,
    val isSignUpComplete: Boolean = false,
    val username: String? = null,
    val errorMessage: String? = null
)