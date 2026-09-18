package com.pws.primaragagym.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.pws.primaragagym.screens.auth.ForgotPasswordScreen
import com.pws.primaragagym.screens.auth.LoginScreen
import com.pws.primaragagym.ui.components.common.AnimatedStatusPopup
import com.pws.primaragagym.ui.components.common.StatusPopupType
import com.pws.primaragagym.ui.viewmodel.AuthViewModel
import com.pws.primaragagym.ui.viewmodel.ForgotPasswordViewModel
import com.pws.primaragagym.ui.viewmodel.LoginEvent
import com.pws.primaragagym.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.delay

enum class AuthScreen {
    LOGIN,
    FORGOT_PASSWORD
}

@Composable
fun LoginNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    var currentScreen by remember { mutableStateOf(AuthScreen.LOGIN) }
    val loginViewModel = remember { LoginViewModel() }
    val forgotPasswordViewModel = remember { ForgotPasswordViewModel() }
    var showLoginSuccess by remember { mutableStateOf(false) }
    var pendingDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loginViewModel.events.collect { event ->
            when (event) {
                is LoginEvent.LoginSuccess -> {
                    authViewModel.setUser(event.user)
                    val destination = AppScreen.SuperAdminRoot.route
                    showLoginSuccess = true
                    pendingDestination = destination
                }
                is LoginEvent.LoginError -> { /* error handled in uiState */ }
            }
        }
    }

    // Auto-dismiss success popup and navigate
    LaunchedEffect(showLoginSuccess) {
        if (showLoginSuccess && pendingDestination != null) {
            delay(1500)
            val destination = pendingDestination!!
            pendingDestination = null
            showLoginSuccess = false
            navController.navigate(destination) {
                popUpTo(AppScreen.Login.route) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            AuthScreen.LOGIN -> {
                LoginScreen(
                    viewModel = loginViewModel,
                    onForgotPasswordClick = {
                        currentScreen = AuthScreen.FORGOT_PASSWORD
                    }
                )
            }

            AuthScreen.FORGOT_PASSWORD -> {
                ForgotPasswordScreen(
                    viewModel = forgotPasswordViewModel,
                    onBackToLoginClick = {
                        currentScreen = AuthScreen.LOGIN
                    }
                )
            }
        }

        // Success Popup Overlay
        AnimatedStatusPopup(
            visible = showLoginSuccess,
            type = StatusPopupType.SUCCESS_LOGIN,
            title = "Login Berhasil!",
            message = "Selamat datang di Primaraga Gym.",
            autoDismissMs = 1300L,
            onDismiss = {
                val destination = pendingDestination
                pendingDestination = null
                showLoginSuccess = false
                if (destination != null) {
                    navController.navigate(destination) {
                        popUpTo(AppScreen.Login.route) { inclusive = true }
                    }
                }
            }
        )
    }
}
