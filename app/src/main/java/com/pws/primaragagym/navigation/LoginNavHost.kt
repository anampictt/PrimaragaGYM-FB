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
                    val destination = when (event.user.role.name) {
                        "SUPER_ADMIN" -> AppScreen.SuperAdminRoot.route
                        else -> AppScreen.AdminDashboard.route
                    }
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
        AnimatedVisibility(
            visible = showLoginSuccess,
            enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f),
            exit = fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.8f),
            modifier = Modifier.fillMaxSize()
        ) {
            LoginSuccessPopup()
        }
    }
}

@Composable
private fun LoginSuccessPopup() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                .padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Success",
                    tint = Color(0xFF32A060),
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Login Berhasil!",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Selamat datang di Primaraga GYM",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B6B6B),
                textAlign = TextAlign.Center
            )
        }
    }
}
