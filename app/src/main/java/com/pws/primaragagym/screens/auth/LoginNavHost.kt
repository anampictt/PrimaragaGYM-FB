package com.pws.primaragagym.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pws.primaragagym.navigation.AppScreen
import com.pws.primaragagym.ui.viewmodel.ForgotPasswordViewModel
import com.pws.primaragagym.ui.viewmodel.LoginEvent
import com.pws.primaragagym.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginNavHost(
    rootNavController: NavHostController,
    loginViewModel: LoginViewModel = LoginViewModel(),
    forgotPasswordViewModel: ForgotPasswordViewModel = ForgotPasswordViewModel()
) {
    val navController = rememberNavController()
    val loginState by loginViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        loginViewModel.events.collectLatest { event ->
            when (event) {
                is LoginEvent.LoginSuccess -> {
                    val destination = when (event.user.role.name) {
                        "SUPER_ADMIN" -> AppScreen.SuperAdminRoot.route
                        else -> AppScreen.AdminDashboard.route
                    }
                    rootNavController.navigate(destination) {
                        popUpTo(0) { inclusive = true }
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
