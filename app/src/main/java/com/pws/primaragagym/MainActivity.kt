package com.pws.primaragagym

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pws.primaragagym.screens.SplashScreen
import com.pws.primaragagym.screens.auth.AuthScreen
import com.pws.primaragagym.screens.auth.ForgotPasswordScreen
import com.pws.primaragagym.screens.auth.ForgotPasswordState
import com.pws.primaragagym.screens.auth.ForgotPasswordUiState
import com.pws.primaragagym.screens.auth.LoginScreen
import com.pws.primaragagym.screens.auth.LoginUiState
import com.pws.primaragagym.ui.theme.LightBackground
import com.pws.primaragagym.ui.theme.PrimaragagymTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrimaragagymTheme(darkTheme = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LightBackground
                ) {
                    PrimaragaGymAuthApp()
                }
            }
        }
    }
}

@Composable
private fun PrimaragaGymAuthApp() {
    // App flow state: true = Splash, false = Auth
    var showSplash by remember { mutableStateOf(true) }
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

    // Splash screen
    if (showSplash) {
        SplashScreen(
            onSplashComplete = {
                showSplash = false
            }
        )
        return
    }

    // Navigation between screens
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
                    // Simple validation
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
                        // Simulate login with Handler (mock - no Firebase yet)
                        Handler(Looper.getMainLooper()).postDelayed({
                            isLoginLoading = false
                            // In real app: navigate to role-based dashboard
                        }, 1500)
                    }
                },
                onForgotPasswordClick = {
                    currentScreen = AuthScreen.FORGOT_PASSWORD
                    // Clear states
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
                    // Validation
                    if (forgotEmail.isBlank()) {
                        forgotEmailError = "Email wajib diisi"
                        return@ForgotPasswordScreen
                    }
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(forgotEmail).matches()) {
                        forgotEmailError = "Format email tidak valid"
                        return@ForgotPasswordScreen
                    }

                    forgotPasswordState = ForgotPasswordState.LOADING

                    // Simulate sending with Handler (mock - no Firebase yet)
                    Handler(Looper.getMainLooper()).postDelayed({
                        forgotPasswordState = ForgotPasswordState.SUCCESS
                    }, 2000)
                },
                onBackToLoginClick = {
                    currentScreen = AuthScreen.LOGIN
                    // Clear forgot password state
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
