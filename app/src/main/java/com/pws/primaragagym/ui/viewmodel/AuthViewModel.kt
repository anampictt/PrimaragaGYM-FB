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
    val permissions: Map<String, Boolean> = emptyMap(),
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
                if (user != null) {
                    loadPermissionsForUser(user)
                } else {
                    _uiState.value = _uiState.value.copy(
                        currentUser = null,
                        permissions = emptyMap(),
                        isAuthenticated = false,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    currentUser = null,
                    permissions = emptyMap(),
                    isAuthenticated = false,
                    isLoading = false
                )
            }
        }
    }

    private suspend fun loadPermissionsForUser(user: User) {
        val isSuperAdmin = user.role.name.equals("SUPER_ADMIN", ignoreCase = true)
        if (isSuperAdmin) {
            val allPerms = mapOf(
                "dashboard" to true,
                "manajemen_pengguna" to true,
                "manajemen_role" to true,
                "manajemen_cabang" to true,
                "manajemen_member" to true,
                "check_in_out" to true,
                "notifikasi" to true,
                "laporan" to true
            )
            _uiState.value = _uiState.value.copy(
                currentUser = user,
                permissions = allPerms,
                isAuthenticated = true,
                isLoading = false
            )
        } else {
            // Load permissions from Firestore roles collection
            val roleResult = ServiceLocator.roleRepository.getRoleByName(user.role.displayName)
            val perms = roleResult.getOrNull()?.permissions ?: mapOf(
                "dashboard" to true,
                "manajemen_pengguna" to false,
                "manajemen_role" to false,
                "manajemen_cabang" to false,
                "manajemen_member" to true,
                "check_in_out" to true,
                "notifikasi" to true,
                "laporan" to true
            )
            _uiState.value = _uiState.value.copy(
                currentUser = user,
                permissions = perms,
                isAuthenticated = true,
                isLoading = false
            )
        }
    }

    fun hasPermission(permissionKey: String): Boolean {
        val user = _uiState.value.currentUser ?: return false
        val roleName = user.role.name.replace(" ", "_")
        if (roleName.equals("SUPER_ADMIN", ignoreCase = true) || user.role.displayName.equals("Super Admin", ignoreCase = true)) {
            return true
        }
        val perms = _uiState.value.permissions
        if (perms[permissionKey] == true) return true
        return when (permissionKey) {
            "check_in_out" -> perms["check_in_out"] == true || perms["check_in"] == true || perms["check_out"] == true
            "manajemen_member", "member" -> perms["manajemen_member"] == true || perms["member"] == true
            "laporan", "laporan_keuangan" -> perms["laporan"] == true || perms["laporan_keuangan"] == true
            else -> false
        }
    }

    fun setUser(user: User) {
        viewModelScope.launch {
            loadPermissionsForUser(user)
        }
    }

    fun clearUser() {
        _uiState.value = _uiState.value.copy(
            currentUser = null,
            permissions = emptyMap(),
            isAuthenticated = false,
            isLoading = false
        )
    }
}
