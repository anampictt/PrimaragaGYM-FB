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
import kotlinx.coroutines.tasks.await

data class ProfileUiState(
    val profile: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showLogoutDialog: Boolean = false
)

sealed class ProfileEvent {
    data object LogoutSuccess : ProfileEvent()
    data class LogoutError(val message: String) : ProfileEvent()
}

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase = ServiceLocator.getCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase = ServiceLocator.logoutUseCase,
    initialUser: User? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            profile = initialUser,
            isLoading = initialUser == null
        )
    )
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

    fun setInitialUser(user: User?) {
        if (_uiState.value.profile == null && user != null) {
            _uiState.value = _uiState.value.copy(
                profile = user,
                isLoading = false
            )
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            if (_uiState.value.profile == null) {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }
            try {
                val user = getCurrentUserUseCase()
                if (user != null) {
                    _uiState.value = _uiState.value.copy(
                        profile = user,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                }
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

    val isUploadingPhoto = MutableStateFlow(false)

    fun updateProfilePhoto(downloadUrl: String, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                isUploadingPhoto.value = true
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                val uid = currentUser?.uid
                if (uid != null) {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    db.collection("users").document(uid).update(
                        mapOf(
                            "photoUrl" to downloadUrl,
                            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                        )
                    ).await()

                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setPhotoUri(android.net.Uri.parse(downloadUrl))
                        .build()
                    currentUser.updateProfile(profileUpdates).await()
                }

                _uiState.value = _uiState.value.copy(
                    profile = _uiState.value.profile?.copy(photoUrl = downloadUrl)
                )
                isUploadingPhoto.value = false
                onComplete?.invoke(true, null)
            } catch (e: Exception) {
                isUploadingPhoto.value = false
                onComplete?.invoke(false, e.localizedMessage ?: "Gagal memperbarui foto profil.")
            }
        }
    }

    fun onLogoutConfirm(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showLogoutDialog = false)
            try {
                logoutUseCase()
                logoutSuccessVisible.value = true
                _events.emit(ProfileEvent.LogoutSuccess)
                onSuccess?.invoke()
            } catch (e: Exception) {
                _events.emit(ProfileEvent.LogoutError(e.message ?: "Logout gagal."))
            }
        }
    }

    companion object {
        fun provideFactory(
            initialUser: User? = null
        ): androidx.lifecycle.ViewModelProvider.Factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(
                    initialUser = initialUser
                ) as T
            }
        }
    }
}
