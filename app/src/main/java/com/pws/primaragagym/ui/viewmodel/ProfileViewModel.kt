package com.pws.primaragagym.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pws.primaragagym.di.ServiceLocator
import com.pws.primaragagym.domain.model.User
import com.pws.primaragagym.domain.usecase.GetCurrentUserUseCase
import com.pws.primaragagym.domain.usecase.LogoutUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: User? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val showLogoutDialog: Boolean = false
)

sealed class ProfileEvent {
    data object LogoutSuccess : ProfileEvent()
    data class LogoutError(val message: String) : ProfileEvent()
}

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase = ServiceLocator.getCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase = ServiceLocator.logoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    val logoutSuccessVisible = MutableStateFlow(false)

    fun hideLogoutSuccess() {
        logoutSuccessVisible.value = false
    }

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val user = getCurrentUserUseCase()
                _uiState.value = _uiState.value.copy(
                    profile = user,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Gagal memuat profil."
                )
            }
        }
    }

    fun showLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = true)
    }

    fun hideLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = false)
    }

    fun onLogoutConfirm() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showLogoutDialog = false)
            try {
                logoutUseCase()
                logoutSuccessVisible.value = true
                _events.emit(ProfileEvent.LogoutSuccess)
            } catch (e: Exception) {
                _events.emit(ProfileEvent.LogoutError(e.message ?: "Logout gagal."))
            }
        }
    }
}
