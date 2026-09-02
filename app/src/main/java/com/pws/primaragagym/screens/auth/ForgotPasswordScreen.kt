package com.pws.primaragagym.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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

enum class ForgotPasswordState {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val state: ForgotPasswordState = ForgotPasswordState.IDLE,
    val errorMessage: String? = null
)

@Composable
fun ForgotPasswordScreen(
    uiState: ForgotPasswordUiState = ForgotPasswordUiState(),
    onEmailChange: (String) -> Unit = {},
    onSendClick: () -> Unit = {},
    onBackToLoginClick: () -> Unit = {},
    isEmailFocused: Boolean = false,
    onEmailFocusChange: (Boolean) -> Unit = {}
) {
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
            // Tablet - Split layout
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Panel - Branding
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

                // Right Panel - Form
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .background(LightBackground),
                    contentAlignment = Alignment.Center
                ) {
                    ForgotPasswordFormContent(
                        uiState = uiState,
                        onEmailChange = onEmailChange,
                        onSendClick = onSendClick,
                        onBackToLoginClick = onBackToLoginClick,
                        isEmailFocused = isEmailFocused,
                        onEmailFocusChange = onEmailFocusChange,
                        isCompact = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacing_8)
                            .imePadding()
                    )
                }
            }
        } else {
            // Phone - Single column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.screen_padding_horizontal_compact)
                    .padding(top = Dimens.spacing_8)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimens.spacing_4),
                    contentAlignment = Alignment.CenterStart
                ) {
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
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_4))
                AuthHeader(
                    logoSize = 80.dp,
                    showTagline = false
                )
                Spacer(modifier = Modifier.height(Dimens.spacing_8))
                ForgotPasswordFormContent(
                    uiState = uiState,
                    onEmailChange = onEmailChange,
                    onSendClick = onSendClick,
                    onBackToLoginClick = onBackToLoginClick,
                    isEmailFocused = isEmailFocused,
                    onEmailFocusChange = onEmailFocusChange,
                    isCompact = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Dimens.spacing_8))
            }
        }
    }
}

@Composable
private fun ForgotPasswordFormContent(
    uiState: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
    isEmailFocused: Boolean,
    onEmailFocusChange: (Boolean) -> Unit,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
            text = "Masukkan email Anda. Kami akan mengirimkan\nlink untuk mereset password.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryDark,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.spacing_8)
        )

        // Error message
        if (uiState.state == ForgotPasswordState.ERROR && uiState.errorMessage != null) {
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

        // Email Field (only show when not in success state)
        AnimatedVisibility(
            visible = uiState.state != ForgotPasswordState.SUCCESS,
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
                    isFocused = isEmailFocused,
                    onFocusChange = onEmailFocusChange,
                    enabled = uiState.state != ForgotPasswordState.LOADING,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Dimens.spacing_8))

                AuthPrimaryButton(
                    text = "Kirim Link Reset",
                    onClick = onSendClick,
                    isLoading = uiState.state == ForgotPasswordState.LOADING,
                    enabled = uiState.state != ForgotPasswordState.LOADING,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Success State
        AnimatedVisibility(
            visible = uiState.state == ForgotPasswordState.SUCCESS,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            SuccessContent(
                email = uiState.email,
                onBackToLoginClick = onBackToLoginClick
            )
        }

        // Back to Login link (only show when not in success state and not compact)
        if (!isCompact) {
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
    onBackToLoginClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = Dimens.spacing_8)
    ) {
        // Success Icon Circle
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
            text = "Kami telah mengirimkan link reset password\nke email:",
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
            text = "Cek folder inbox atau spam email Anda.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimens.spacing_8))

        TextButton(onClick = onBackToLoginClick) {
            Text(
                text = "Kembali ke Login",
                style = MaterialTheme.typography.labelLarge,
                color = GreenPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
