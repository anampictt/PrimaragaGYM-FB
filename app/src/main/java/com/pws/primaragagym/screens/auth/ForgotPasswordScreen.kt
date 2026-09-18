package com.pws.primaragagym.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.pws.primaragagym.ui.components.auth.AuthPrimaryButton
import com.pws.primaragagym.ui.components.auth.AuthTextField
import com.pws.primaragagym.ui.theme.Dimens
import com.pws.primaragagym.ui.theme.Error
import com.pws.primaragagym.ui.theme.GreenPrimary
import com.pws.primaragagym.ui.theme.GreenPrimaryDark
import com.pws.primaragagym.ui.theme.Success
import com.pws.primaragagym.ui.theme.TextPrimaryDark
import com.pws.primaragagym.ui.theme.TextSecondaryDark
import com.pws.primaragagym.ui.theme.LightBackground
import com.pws.primaragagym.ui.viewmodel.ForgotPasswordEvent
import com.pws.primaragagym.ui.viewmodel.ForgotPasswordUiState
import com.pws.primaragagym.ui.viewmodel.ForgotPasswordViewModel
import kotlinx.coroutines.flow.collectLatest

enum class ForgotPasswordState {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    onBackToLoginClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isCompact = screenWidthDp < 600
    val isMedium = screenWidthDp in 600..839

    LaunchedEffect(Unit) {
        viewModel.resetState()
        viewModel.events.collectLatest { event ->
            when (event) {
                is ForgotPasswordEvent.EmailSent -> { /* handled by isSent state */ }
                is ForgotPasswordEvent.Error -> { /* handled by emailError state */ }
            }
        }
    }

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
                        Image(
                            painter = painterResource(id = R.drawable.logogym),
                            contentDescription = "Logo Primaraga Gym",
                            modifier = Modifier.size(Dimens.auth_logo_size_tablet),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacing_6))
                        Text(
                            text = "LUPA PASSWORD?",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimaryDark,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacing_2))
                        Text(
                            text = "Kami siap membantu Anda\nmengakses kembali akun Anda.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimaryDark.copy(alpha = 0.85f),
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
                    ForgotPasswordFormContent(
                        uiState = uiState,
                        onEmailChange = viewModel::onEmailChange,
                        onSendClick = viewModel::onSendResetLink,
                        onResendClick = viewModel::resendResetLink,
                        onEditEmailClick = viewModel::resetState,
                        onBackToLoginClick = onBackToLoginClick,
                        isCompact = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacing_8)
                            .imePadding()
                    )
                }
            }
        } else {
            @OptIn(ExperimentalMaterial3Api::class)
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Lupa Password",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = TextPrimaryDark
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = onBackToLoginClick,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_arrow_back),
                                    contentDescription = "Kembali ke Login",
                                    tint = TextPrimaryDark,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = LightBackground
                        )
                    )
                },
                containerColor = LightBackground,
                modifier = Modifier.fillMaxSize()
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Dimens.screen_padding_horizontal_compact)
                        .padding(top = Dimens.spacing_4)
                        .imePadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(Dimens.spacing_4))
                    AuthHeader(
                        logoSize = 120.dp,
                        showTagline = false
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing_8))
                    ForgotPasswordFormContent(
                        uiState = uiState,
                        onEmailChange = viewModel::onEmailChange,
                        onSendClick = viewModel::onSendResetLink,
                        onResendClick = viewModel::resendResetLink,
                        onEditEmailClick = viewModel::resetState,
                        onBackToLoginClick = onBackToLoginClick,
                        isCompact = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing_8))
                }
            }
        }
    }
}

@Composable
private fun ForgotPasswordFormContent(
    uiState: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onResendClick: () -> Unit,
    onEditEmailClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isCompact) {
            Text(
                text = "Lupa Password",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.spacing_2)
            )
            Text(
                text = "Masukkan email akun Anda. Kami akan mengirimkan tautan untuk mereset kata sandi.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryDark,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.spacing_8)
            )
        }

        if (uiState.emailError != null && !uiState.isSent) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.spacing_4),
                shape = RoundedCornerShape(Dimens.input_corner_radius),
                color = Error.copy(alpha = 0.1f)
            ) {
                Text(
                    text = uiState.emailError,
                    style = MaterialTheme.typography.bodySmall,
                    color = Error,
                    modifier = Modifier.padding(Dimens.spacing_4),
                    textAlign = TextAlign.Center
                )
            }
        }

        AnimatedVisibility(
            visible = !uiState.isSent,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AuthTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    label = "Email",
                    placeholder = "nama@email.com",
                    keyboardType = KeyboardType.Email,
                    isError = uiState.emailError != null,
                    errorMessage = uiState.emailError,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Dimens.spacing_8))

                AuthPrimaryButton(
                    text = "Kirim Link Reset",
                    onClick = onSendClick,
                    isLoading = uiState.isLoading,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.isSent,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            SuccessContent(
                email = uiState.email,
                isLoading = uiState.isLoading,
                onResendClick = onResendClick,
                onEditEmailClick = onEditEmailClick,
                onBackToLoginClick = onBackToLoginClick
            )
        }

        if (!isCompact && !uiState.isSent) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.spacing_6),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = onBackToLoginClick) {
                    Text(
                        text = "Kembali ke Login",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GreenPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SuccessContent(
    email: String,
    isLoading: Boolean,
    onResendClick: () -> Unit,
    onEditEmailClick: () -> Unit,
    onBackToLoginClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = Dimens.spacing_4)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = Success.copy(alpha = 0.15f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = null,
                tint = Success,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(Dimens.spacing_6))

        Text(
            text = "Link Terkirim!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryDark
        )

        Spacer(modifier = Modifier.height(Dimens.spacing_3))

        Text(
            text = "Kami telah mengirimkan tautan reset password ke email:",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimens.spacing_2))

        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = GreenPrimary
        )

        Spacer(modifier = Modifier.height(Dimens.spacing_3))

        Text(
            text = "Silakan periksa folder Kotak Masuk (Inbox) atau Spam email Anda.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimens.spacing_8))

        AuthPrimaryButton(
            text = "Kembali ke Login",
            onClick = onBackToLoginClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(Dimens.spacing_4))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onResendClick,
                enabled = !isLoading
            ) {
                Text(
                    text = "Kirim Ulang Link",
                    style = MaterialTheme.typography.bodySmall,
                    color = GreenPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "•",
                color = TextSecondaryDark,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            TextButton(
                onClick = onEditEmailClick,
                enabled = !isLoading
            ) {
                Text(
                    text = "Ganti Email",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
