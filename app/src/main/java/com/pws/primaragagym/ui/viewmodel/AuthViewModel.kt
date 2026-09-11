package com.pws.primaragagym.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pws.primaragagym.di.ServiceLocator
import com.pws.primaragagym.domain.model.User
import com.pws.primaragagym.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val currentUser: User? = null,
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false
)

class AuthViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase = ServiceLocator.getCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    fun checkAuthState() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val user = getCurrentUserUseCase()
                _uiState.value = _uiState.value.copy(
                    currentUser = user,
                    isAuthenticated = user != null,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    currentUser = null,
                    isAuthenticated = false,
                    isLoading = false
                )
            }
        }
    }

    fun setUser(user: User) {
        _uiState.value = _uiState.value.copy(
            currentUser = user,
            isAuthenticated = true
        )
    }

    fun clearUser() {
        _uiState.value = _uiState.value.copy(
            currentUser = null,
            isAuthenticated = false
        )
    }
}
