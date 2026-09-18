package com.pws.primaragagym.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pws.primaragagym.di.ServiceLocator
import com.pws.primaragagym.domain.usecase.ForgotPasswordUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val isSent: Boolean = false
)

sealed class ForgotPasswordEvent {
    data object EmailSent : ForgotPasswordEvent()
    data class Error(val message: String) : ForgotPasswordEvent()
}

class ForgotPasswordViewModel(
    private val forgotPasswordUseCase: ForgotPasswordUseCase = ServiceLocator.forgotPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ForgotPasswordEvent>()
    val events: SharedFlow<ForgotPasswordEvent> = _events.asSharedFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, emailError = null)
    }

    fun onSendResetLink() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(emailError = "Email wajib diisi.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, emailError = null)
            val result = forgotPasswordUseCase(email)
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSent = true)
                    _events.emit(ForgotPasswordEvent.EmailSent)
                },
                onFailure = { exception ->
                    val errorMsg = exception.message ?: "Gagal mengirim link reset."
                    _uiState.value = _uiState.value.copy(emailError = errorMsg)
                    _events.emit(ForgotPasswordEvent.Error(errorMsg))
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = ForgotPasswordUiState()
    }

    fun resendResetLink() {
        _uiState.value = _uiState.value.copy(isSent = false)
        onSendResetLink()
    }
}
