package com.pws.primaragagym.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pws.primaragagym.R
import com.pws.primaragagym.ui.components.auth.AuthHeader
import com.pws.primaragagym.ui.components.auth.AuthPasswordField
import com.pws.primaragagym.ui.components.auth.AuthPrimaryButton
import com.pws.primaragagym.ui.components.auth.AuthTextField
import com.pws.primaragagym.ui.theme.Dimens
import com.pws.primaragagym.ui.theme.GreenPrimary
import com.pws.primaragagym.ui.theme.GreenPrimaryDark
import com.pws.primaragagym.ui.theme.TextPrimaryDark
import com.pws.primaragagym.ui.theme.TextSecondaryDark
import com.pws.primaragagym.ui.theme.Error
import com.pws.primaragagym.ui.theme.LightBackground
import com.pws.primaragagym.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onForgotPasswordClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isCompact = screenWidthDp < 600
    val isMedium = screenWidthDp in 600..839

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        if (!isCompact) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(if (isMedium) 0.4f else 0.45f)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    GreenPrimaryDark,
                                    GreenPrimaryDark.copy(alpha = 0.85f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(Dimens.spacing_8)
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.logogym),
                            contentDescription = "Logo Primaraga Gym",
                            modifier = Modifier.size(Dimens.auth_logo_size_tablet),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacing_4))
                        Text(
                            text = "Strong Body, Strong Mind",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimaryDark.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacing_8))
                        Text(
                            text = "Kelola gym Anda dengan mudah\ndan efisien",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimaryDark.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .background(LightBackground),
                    contentAlignment = Alignment.Center
                ) {
                    LoginFormContent(
                        uiState = uiState,
                        onEmailChange = viewModel::onEmailChange,
                        onPasswordChange = viewModel::onPasswordChange,
                        onLoginClick = viewModel::onLoginClick,
                        onForgotPasswordClick = onForgotPasswordClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacing_8)
                            .imePadding()
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.screen_padding_horizontal_compact)
                    .padding(top = Dimens.spacing_12)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(Dimens.spacing_8))
                AuthHeader(
                    logoSize = Dimens.auth_logo_size,
                    showTagline = true
                )
                Spacer(modifier = Modifier.height(Dimens.spacing_10))
                LoginFormContent(
                    uiState = uiState,
                    onEmailChange = viewModel::onEmailChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onLoginClick = viewModel::onLoginClick,
                    onForgotPasswordClick = onForgotPasswordClick,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Dimens.spacing_8))
            }
        }
    }
}

@Composable
private fun LoginFormContent(
    uiState: com.pws.primaragagym.ui.viewmodel.LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Selamat datang kembali!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryDark,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.spacing_2)
        )
        Text(
            text = "Masukkan email dan password untuk masuk ke akun Anda.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryDark,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.spacing_8)
        )

        if (uiState.errorMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.spacing_4),
                shape = RoundedCornerShape(Dimens.input_corner_radius),
                color = Error.copy(alpha = 0.1f)
            ) {
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = Error,
                    modifier = Modifier.padding(Dimens.spacing_4),
                    textAlign = TextAlign.Center
                )
            }
        }

        AuthTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = "Email",
            placeholder = "nama@email.com",
            keyboardType = KeyboardType.Email,
            isError = uiState.emailError != null,
            errorMessage = uiState.emailError,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(Dimens.spacing_5))

        AuthPasswordField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = "Password",
            placeholder = "Masukkan password",
            isError = uiState.passwordError != null,
            errorMessage = uiState.passwordError,
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.spacing_3, bottom = Dimens.spacing_8),
            contentAlignment = Alignment.CenterEnd
        ) {
            TextButton(onClick = onForgotPasswordClick) {
                Text(
                    text = "Lupa Password?",
                    style = MaterialTheme.typography.bodySmall,
                    color = GreenPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        AuthPrimaryButton(
            text = "Masuk",
            onClick = onLoginClick,
            isLoading = uiState.isLoading,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
