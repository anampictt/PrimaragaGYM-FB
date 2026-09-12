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

    LaunchedEffect(Unit) {
        loginViewModel.resetState()
        loginViewModel.events.collectLatest { event ->
            when (event) {
                is LoginEvent.LoginSuccess -> {
                    authViewModel?.setUser(event.user)
                    if (onLoginSuccess != null) {
                        onLoginSuccess(event.user)
                        val destination = AppScreen.SuperAdminRoot.route
                        rootNavController?.navigate(destination) {
                            popUpTo(AppScreen.Splash.route) { inclusive = true }
                        }
                    }
                }
                is LoginEvent.LoginError -> { /* handled by state */ }
            }
        }
    }

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
