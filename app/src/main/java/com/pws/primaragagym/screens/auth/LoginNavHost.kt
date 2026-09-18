package com.pws.primaragagym.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pws.primaragagym.domain.model.User
import com.pws.primaragagym.navigation.AppScreen
import com.pws.primaragagym.ui.viewmodel.AuthViewModel
import com.pws.primaragagym.ui.viewmodel.ForgotPasswordViewModel
import com.pws.primaragagym.ui.viewmodel.LoginEvent
import com.pws.primaragagym.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.flow.collectLatest

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.pws.primaragagym.ui.components.common.AnimatedStatusPopup
import com.pws.primaragagym.ui.components.common.StatusPopupType

@Composable
fun LoginNavHost(
    rootNavController: NavHostController? = null,
    authViewModel: AuthViewModel? = null,
    onLoginSuccess: ((User) -> Unit)? = null,
    loginViewModel: LoginViewModel = LoginViewModel(),
    forgotPasswordViewModel: ForgotPasswordViewModel = ForgotPasswordViewModel()
) {
    val navController = rememberNavController()
    val loginState by loginViewModel.uiState.collectAsState()

    var showLoginSuccess by remember { mutableStateOf(false) }
    var loggedInUser by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        loginViewModel.resetState()
        loginViewModel.events.collectLatest { event ->
            when (event) {
                is LoginEvent.LoginSuccess -> {
                    authViewModel?.setUser(event.user)
                    loggedInUser = event.user
                    showLoginSuccess = true
                }
                is LoginEvent.LoginError -> { /* handled by state */ }
            }
        }
    }

    AnimatedStatusPopup(
        visible = showLoginSuccess,
        type = StatusPopupType.SUCCESS_LOGIN,
        title = "Login Berhasil!",
        message = loggedInUser?.name?.let { "Selamat datang kembali, $it!" } ?: "Selamat datang kembali di Primaraga Gym.",
        autoDismissMs = 1300L,
        onDismiss = {
            showLoginSuccess = false
            loggedInUser?.let { user ->
                if (onLoginSuccess != null) {
                    onLoginSuccess(user)
                    val destination = AppScreen.SuperAdminRoot.route
                    rootNavController?.navigate(destination) {
                        popUpTo(AppScreen.Splash.route) { inclusive = true }
                    }
                }
            }
        }
    )

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                onForgotPasswordClick = {
                    navController.navigate("forgot_password")
                }
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                viewModel = forgotPasswordViewModel,
                onBackToLoginClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
