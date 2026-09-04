package com.pws.primaragagym.navigation

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.pws.primaragagym.screens.auth.AuthScreen
import com.pws.primaragagym.screens.auth.ForgotPasswordScreen
import com.pws.primaragagym.screens.auth.ForgotPasswordState
import com.pws.primaragagym.screens.auth.ForgotPasswordUiState
import com.pws.primaragagym.screens.auth.LoginScreen
import com.pws.primaragagym.screens.auth.LoginUiState

/**
 * Navigation host for authentication screens (Login & Forgot Password).
 * Manages auth flow using existing screen components.
 */
@Composable
fun LoginNavHost(
    navController: androidx.navigation.NavHostController
) {
    var currentScreen by remember { mutableStateOf(AuthScreen.LOGIN) }

    // Login state
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginEmailError by remember { mutableStateOf<String?>(null) }
    var loginPasswordError by remember { mutableStateOf<String?>(null) }
    var isLoginLoading by remember { mutableStateOf(false) }
    var loginErrorMessage by remember { mutableStateOf<String?>(null) }
    var isEmailFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }

    // Forgot Password state
    var forgotEmail by remember { mutableStateOf("") }
    var forgotEmailError by remember { mutableStateOf<String?>(null) }
    var forgotPasswordState by remember { mutableStateOf(ForgotPasswordState.IDLE) }
    var forgotErrorMessage by remember { mutableStateOf<String?>(null) }
    var isForgotEmailFocused by remember { mutableStateOf(false) }

    when (currentScreen) {
        AuthScreen.LOGIN -> {
            val loginUiState = LoginUiState(
                email = loginEmail,
                password = loginPassword,
                emailError = loginEmailError,
                passwordError = loginPasswordError,
                isLoading = isLoginLoading,
                errorMessage = loginErrorMessage
            )

            LoginScreen(
                uiState = loginUiState,
                onEmailChange = { value ->
                    loginEmail = value
                    loginEmailError = null
                    loginErrorMessage = null
                },
                onPasswordChange = { value ->
                    loginPassword = value
                    loginPasswordError = null
                    loginErrorMessage = null
                },
                onLoginClick = {
                    var hasError = false

                    if (loginEmail.isBlank()) {
                        loginEmailError = "Email wajib diisi"
                        hasError = true
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(loginEmail).matches()) {
                        loginEmailError = "Format email tidak valid"
                        hasError = true
                    }

                    if (loginPassword.isBlank()) {
                        loginPasswordError = "Password wajib diisi"
                        hasError = true
                    } else if (loginPassword.length < 6) {
                        loginPasswordError = "Password minimal 6 karakter"
                        hasError = true
                    }

                    if (!hasError) {
                        isLoginLoading = true
                        // Mock login - Firebase integration will replace this
                        Handler(Looper.getMainLooper()).postDelayed({
                            isLoginLoading = false
                            // Navigate to Admin Dashboard for now (role-based navigation will be added with Firebase)
                            navController.navigate(AppScreen.AdminDashboard.route) {
                                popUpTo(AppScreen.Login.route) { inclusive = true }
                            }
                        }, 1500)
                    }
                },
                onForgotPasswordClick = {
                    currentScreen = AuthScreen.FORGOT_PASSWORD
                    forgotEmail = ""
                    forgotEmailError = null
                    forgotPasswordState = ForgotPasswordState.IDLE
                    forgotErrorMessage = null
                },
                isEmailFocused = isEmailFocused,
                isPasswordFocused = isPasswordFocused,
                onEmailFocusChange = { isEmailFocused = it },
                onPasswordFocusChange = { isPasswordFocused = it }
            )
        }

        AuthScreen.FORGOT_PASSWORD -> {
            val forgotUiState = ForgotPasswordUiState(
                email = forgotEmail,
                emailError = forgotEmailError,
                state = forgotPasswordState,
                errorMessage = forgotErrorMessage
            )

            ForgotPasswordScreen(
                uiState = forgotUiState,
                onEmailChange = { value ->
                    forgotEmail = value
                    forgotEmailError = null
                    forgotErrorMessage = null
                    if (forgotPasswordState == ForgotPasswordState.SUCCESS) {
                        forgotPasswordState = ForgotPasswordState.IDLE
                    }
                },
                onSendClick = {
                    if (forgotEmail.isBlank()) {
                        forgotEmailError = "Email wajib diisi"
                        return@ForgotPasswordScreen
                    }
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(forgotEmail).matches()) {
                        forgotEmailError = "Format email tidak valid"
                        return@ForgotPasswordScreen
                    }

                    forgotPasswordState = ForgotPasswordState.LOADING

                    // Mock sending - Firebase integration will replace this
                    Handler(Looper.getMainLooper()).postDelayed({
                        forgotPasswordState = ForgotPasswordState.SUCCESS
                    }, 2000)
                },
                onBackToLoginClick = {
                    currentScreen = AuthScreen.LOGIN
                    forgotEmail = ""
                    forgotEmailError = null
                    forgotPasswordState = ForgotPasswordState.IDLE
                    forgotErrorMessage = null
                },
                isEmailFocused = isForgotEmailFocused,
                onEmailFocusChange = { isForgotEmailFocused = it }
            )
        }
    }
}
