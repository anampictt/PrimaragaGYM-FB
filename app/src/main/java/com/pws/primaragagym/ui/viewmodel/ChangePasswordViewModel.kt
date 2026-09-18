package com.pws.primaragagym.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pws.primaragagym.di.ServiceLocator
import com.pws.primaragagym.domain.usecase.ChangePasswordUseCase
import com.pws.primaragagym.domain.usecase.ForgotPasswordUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isSendingResetEmail: Boolean = false,
    val resetEmailSuccessMessage: String? = null
)

sealed class ChangePasswordEvent {
    data object Success : ChangePasswordEvent()
    data class Error(val message: String) : ChangePasswordEvent()
    data class ResetEmailSent(val email: String) : ChangePasswordEvent()
}

class ChangePasswordViewModel(
    private val changePasswordUseCase: ChangePasswordUseCase = ServiceLocator.changePasswordUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase = ServiceLocator.forgotPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ChangePasswordEvent>()
    val events: SharedFlow<ChangePasswordEvent> = _events.asSharedFlow()

    fun onCurrentPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            currentPassword = password,
            currentPasswordError = null,
            generalError = null
        )
    }

    fun onNewPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            newPassword = password,
            newPasswordError = null,
            generalError = null
        )
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = password,
            confirmPasswordError = null,
            generalError = null
        )
    }

    fun onChangePassword() {
        val current = _uiState.value.currentPassword
        val new = _uiState.value.newPassword
        val confirm = _uiState.value.confirmPassword

        var hasError = false
        var currentError: String? = null
        var newError: String? = null
        var confirmError: String? = null

        if (current.isBlank()) {
            currentError = "Kata sandi saat ini wajib diisi."
            hasError = true
        }

        if (new.isBlank()) {
            newError = "Kata sandi baru wajib diisi."
            hasError = true
        } else if (new.length < 6) {
            newError = "Kata sandi baru minimal 6 karakter."
            hasError = true
        } else if (new == current) {
            newError = "Kata sandi baru tidak boleh sama dengan kata sandi saat ini."
            hasError = true
        }

        if (confirm.isBlank()) {
            confirmError = "Konfirmasi kata sandi wajib diisi."
            hasError = true
        } else if (confirm != new) {
            confirmError = "Konfirmasi kata sandi tidak cocok."
            hasError = true
        }

        if (hasError) {
            _uiState.value = _uiState.value.copy(
                currentPasswordError = currentError,
                newPasswordError = newError,
                confirmPasswordError = confirmError
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                currentPasswordError = null,
                newPasswordError = null,
                confirmPasswordError = null,
                generalError = null
            )

            val result = changePasswordUseCase(current, new)
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSuccess = true)
                    _events.emit(ChangePasswordEvent.Success)
                },
                onFailure = { exception ->
                    val errorMsg = exception.message ?: "Gagal mengubah kata sandi."
                    if (errorMsg.contains("saat ini", ignoreCase = true)) {
                        _uiState.value = _uiState.value.copy(currentPasswordError = errorMsg)
                    } else {
                        _uiState.value = _uiState.value.copy(generalError = errorMsg)
                    }
                    _events.emit(ChangePasswordEvent.Error(errorMsg))
                }
            )
        }
    }

    fun onSendResetEmail(email: String) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) {
            _uiState.value = _uiState.value.copy(
                generalError = "Email pengguna tidak valid untuk pengiriman link reset."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSendingResetEmail = true,
                generalError = null,
                resetEmailSuccessMessage = null
            )

            val result = forgotPasswordUseCase(cleanEmail)
            _uiState.value = _uiState.value.copy(isSendingResetEmail = false)

            result.fold(
                onSuccess = {
                    val msg = "Link reset kata sandi telah dikirim ke $cleanEmail. Silakan periksa inbox/spam Anda."
                    _uiState.value = _uiState.value.copy(resetEmailSuccessMessage = msg)
                    _events.emit(ChangePasswordEvent.ResetEmailSent(cleanEmail))
                },
                onFailure = { exception ->
                    val errorMsg = exception.message ?: "Gagal mengirim link reset password."
                    _uiState.value = _uiState.value.copy(generalError = errorMsg)
                    _events.emit(ChangePasswordEvent.Error(errorMsg))
                }
            )
        }
    }

    fun clearResetEmailNotice() {
        _uiState.value = _uiState.value.copy(resetEmailSuccessMessage = null)
    }

    fun resetState() {
        _uiState.value = ChangePasswordUiState()
    }
}
