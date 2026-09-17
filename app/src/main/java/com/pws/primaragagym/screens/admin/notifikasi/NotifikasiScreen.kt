package com.pws.primaragagym.screens.admin.notifikasi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pws.primaragagym.domain.model.FirestoreNotification
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.BackgroundColor
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.CardBackground
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.GreenAccent
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.GreenLight
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.TextMuted
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.TextPrimary
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.TextSecondary
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.UnreadBackground
import com.pws.primaragagym.ui.theme.Dimens
import com.pws.primaragagym.ui.viewmodel.NotificationDisplayMapper
import com.pws.primaragagym.ui.viewmodel.NotifikasiViewModel

// ============================================================================
// FILTER TYPES
// ============================================================================
private enum class NotifikasiFilter(val label: String) {
    ALL("Semua"),
    MEMBERSHIP("Membership"),
    MEMBER("Member"),
    SISTEM("Sistem")
}

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifikasiScreen(
    onBackClick: () -> Unit = {},
    onNotificationClick: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    branchId: String? = null,
    viewModel: NotifikasiViewModel = viewModel()
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf(NotifikasiFilter.ALL) }
    var showWhatsAppDialog by remember { mutableStateOf(false) }
    var whatsAppMemberName by remember { mutableStateOf("") }

    // Mulai observasi notifikasi dari Firestore
    LaunchedEffect(branchId) {
        viewModel.observeNotifications(branchId = branchId)
    }

    val filteredNotifications = uiState.notifications.filter { notif ->
        when (selectedFilter) {
            NotifikasiFilter.ALL -> true
            NotifikasiFilter.MEMBERSHIP -> notif.type == "MEMBERSHIP_EXPIRING"
            NotifikasiFilter.MEMBER -> notif.type == "MEMBER_INACTIVE" || notif.type == "MEMBER_BIRTHDAY"
            NotifikasiFilter.SISTEM -> notif.type == "SYSTEM" || notif.type.isBlank()
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Notifikasi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Pantau aktivitas dan kondisi member.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    if (uiState.unreadCount > 0) {
                        TextButton(
                            onClick = { viewModel.markAllAsRead(branchId) }
                        ) {
                            Text(
                                text = "Tandai semua dibaca",
                                style = MaterialTheme.typography.labelMedium,
                                color = GreenAccent
                            )
                        }
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Pengaturan",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenLight,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = if (isTablet) 32.dp else Dimens.screen_padding_horizontal,
                vertical = Dimens.spacing_4
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
        ) {
            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_2)
                ) {
                    NotifikasiFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = filter.label,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenAccent,
                                selectedLabelColor = Color.White,
                                containerColor = CardBackground,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }

            // Loading state
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacing_8),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GreenAccent)
                    }
                }
            } else if (filteredNotifications.isEmpty()) {
                item {
                    EmptyNotificationState()
                }
            } else {
                items(filteredNotifications, key = { it.notificationId }) { notification ->
                    RealNotificationCard(
                        notification = notification,
                        onClick = {
                            // Mark as read
                            if (!notification.isRead) {
                                viewModel.markAsRead(notification.notificationId)
                            }
                            // Birthday → tampilkan dialog
                            if (notification.type == "MEMBER_BIRTHDAY") {
                                whatsAppMemberName = notification.message
                                    .substringBefore(" berulang")
                                    .ifBlank { "Member" }
                                showWhatsAppDialog = true
                            } else {
                                onNotificationClick(notification.notificationId)
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(Dimens.spacing_4))
            }
        }
    }

    // Birthday Dialog
    if (showWhatsAppDialog) {
        AlertDialog(
            onDismissRequest = { showWhatsAppDialog = false },
            confirmButton = {
                TextButton(onClick = { showWhatsAppDialog = false }) {
                    Text("OK", color = GreenAccent)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(Dimens.card_corner_radius),
            title = {
                Text(
                    text = "Kirim Ucapan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Fitur pengiriman ucapan WhatsApp akan segera tersedia.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        )
    }
}

// ============================================================================
// REAL NOTIFICATION CARD (dari Firestore)
// ============================================================================
@Composable
private fun RealNotificationCard(
    notification: FirestoreNotification,
    onClick: () -> Unit
) {
    val (icon, iconBg, iconColor) = NotificationDisplayMapper.getIconAndColors(notification.type)
    val timeLabel = NotificationDisplayMapper.formatTime(notification.createdAt)
    val isUnread = !notification.isRead

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Dimens.card_corner_radius),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) UnreadBackground else CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnread) 2.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_4),
            verticalAlignment = Alignment.Top
        ) {
            // Unread indicator dot
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(GreenAccent)
                        .align(Alignment.Top)
                )
                Spacer(modifier = Modifier.width(Dimens.spacing_2))
            }

            // Icon circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(Dimens.spacing_3))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                // Birthday action button
                if (notification.type == "MEMBER_BIRTHDAY") {
                    Spacer(modifier = Modifier.height(Dimens.spacing_3))
                    Card(
                        shape = RoundedCornerShape(Dimens.button_corner_radius),
                        colors = CardDefaults.cardColors(containerColor = GreenAccent)
                    ) {
                        Text(
                            text = "Kirim Ucapan",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(Dimens.spacing_2))

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ============================================================================
// EMPTY STATE
// ============================================================================
@Composable
private fun EmptyNotificationState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.spacing_8),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_4))
            Text(
                text = "Belum ada notifikasi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_2))
            Text(
                text = "Notifikasi akan muncul di sini.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
