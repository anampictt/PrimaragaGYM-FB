package com.pws.primaragagym.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Warning
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pws.primaragagym.data.repository.NotificationRepositoryImpl
import com.pws.primaragagym.domain.model.FirestoreNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ============================================================================
// UI State
// ============================================================================
data class NotifikasiUiState(
    val notifications: List<FirestoreNotification> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val unreadCount: Int get() = notifications.count { !it.isRead }
}

// ============================================================================
// ViewModel
// ============================================================================
class NotifikasiViewModel : ViewModel() {

    private val repository = NotificationRepositoryImpl()

    private val _uiState = MutableStateFlow(NotifikasiUiState())
    val uiState: StateFlow<NotifikasiUiState> = _uiState.asStateFlow()

    /**
     * Mulai observasi notifikasi real-time dari Firestore.
     * [branchId] filter opsional, null = tampilkan semua.
     */
    fun observeNotifications(branchId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.observeNotifications(branchId = branchId)
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message)
                    }
                }
                .collect { list ->
                    _uiState.update {
                        it.copy(notifications = list, isLoading = false, error = null)
                    }
                }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            // Optimistic update agar UI langsung responsif
            _uiState.update { current ->
                current.copy(
                    notifications = current.notifications.map {
                        if (it.notificationId == notificationId) it.copy(isRead = true) else it
                    }
                )
            }
            repository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead(branchId: String? = null) {
        viewModelScope.launch {
            val unreadList = _uiState.value.notifications.filter { !it.isRead }
            val unreadIds = unreadList.map { it.notificationId }.filter { it.isNotBlank() }

            // 1. Optimistic UI update: langsung ubah semua notifikasi di layar menjadi dibaca
            _uiState.update { current ->
                current.copy(
                    notifications = current.notifications.map {
                        if (!it.isRead) it.copy(isRead = true) else it
                    }
                )
            }

            // 2. Simpan ke Firestore via batch by IDs
            if (unreadIds.isNotEmpty()) {
                repository.markMultipleAsRead(unreadIds)
            }

            // 3. Query Firestore untuk notifikasi lain yang belum termuat
            repository.markAllAsRead(branchId = branchId?.ifBlank { null })
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            repository.deleteNotification(notificationId)
        }
    }
}

// ============================================================================
// Helper: map FirestoreNotification type to display metadata
// ============================================================================
object NotificationDisplayMapper {

    fun getIconAndColors(type: String): Triple<
            androidx.compose.ui.graphics.vector.ImageVector,
            Color,  // iconBackground
            Color   // iconColor
            > {
        return when (type) {
            "MEMBER_BIRTHDAY" -> Triple(
                Icons.Filled.Cake,
                Color(0xFFFCE4EC),
                Color(0xFFF44336)
            )
            "MEMBER_INACTIVE" -> Triple(
                Icons.Filled.PersonOff,
                Color(0xFFE3F2FD),
                Color(0xFFF44336)
            )
            "MEMBERSHIP_EXPIRING" -> Triple(
                Icons.Filled.Warning,
                Color(0xFFFFF3E0),
                Color(0xFFFF9800)
            )
            else -> Triple(
                Icons.Filled.Notifications,
                Color(0xFFE8F5E9),
                Color(0xFF32A060)
            )
        }
    }

    fun getCategoryLabel(type: String): String = when (type) {
        "MEMBER_BIRTHDAY" -> "Member"
        "MEMBER_INACTIVE" -> "Member"
        "MEMBERSHIP_EXPIRING" -> "Membership"
        else -> "Sistem"
    }

    fun formatTime(createdAt: java.util.Date?): String {
        if (createdAt == null) return ""
        val now = System.currentTimeMillis()
        val diff = now - createdAt.time
        val minutes = diff / (1000 * 60)
        val hours = diff / (1000 * 60 * 60)
        val days = diff / (1000 * 60 * 60 * 24)
        return when {
            minutes < 1 -> "Baru saja"
            minutes < 60 -> "$minutes menit lalu"
            hours < 24 -> "$hours jam lalu"
            days == 1L -> "Kemarin"
            else -> "$days hari lalu"
        }
    }
}
