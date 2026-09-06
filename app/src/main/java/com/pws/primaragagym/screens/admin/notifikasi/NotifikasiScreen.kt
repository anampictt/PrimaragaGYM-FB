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
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.BackgroundColor
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.CardBackground
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.ChipMemberBg
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.ChipMembershipBg
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.ChipSistemBg
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.GreenAccent
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.GreenLight
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.StatusError
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.StatusInfo
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.StatusSuccess
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.StatusWarning
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.TextMuted
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.TextPrimary
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.TextSecondary
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.UnreadBackground
import com.pws.primaragagym.ui.theme.Dimens

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
// NOTIFICATION TYPES
// ============================================================================
private enum class NotificationCategory {
    MEMBERSHIP, MEMBER, SISTEM, BIRTHDAY
}

private data class NotificationItem(
    val id: String,
    val category: NotificationCategory,
    val title: String,
    val memberName: String,
    val body: String,
    val time: String,
    val isUnread: Boolean,
    val icon: ImageVector,
    val iconBackground: Color,
    val iconColor: Color,
    val extraInfo: String? = null
)

// ============================================================================
// MOCK DATA
// ============================================================================
private fun buildMockNotifications(): List<NotificationItem> = listOf(
    NotificationItem(
        id = "NOTIF-001",
        category = NotificationCategory.MEMBERSHIP,
        title = "Membership Hampir Expired",
        memberName = "John Smith",
        body = "Membership Premium Monthly akan berakhir dalam 3 hari.",
        time = "2 jam lalu",
        isUnread = true,
        icon = Icons.Filled.Warning,
        iconBackground = ChipMembershipBg,
        iconColor = StatusWarning,
        extraInfo = "30 September 2026"
    ),
    NotificationItem(
        id = "NOTIF-002",
        category = NotificationCategory.MEMBER,
        title = "Member Tidak Aktif",
        memberName = "Sarah Connor",
        body = "Member tidak melakukan check-in selama 14 hari.",
        time = "5 jam lalu",
        isUnread = true,
        icon = Icons.Filled.PersonOff,
        iconBackground = ChipMemberBg,
        iconColor = StatusError,
        extraInfo = "Terakhir check-in: 23 August 2026"
    ),
    NotificationItem(
        id = "NOTIF-003",
        category = NotificationCategory.BIRTHDAY,
        title = "Ulang Tahun Member",
        memberName = "Michael Brown",
        body = "Michael Brown berulang tahun hari ini. Kirim ucapan kepada member.",
        time = "Hari ini",
        isUnread = true,
        icon = Icons.Filled.Cake,
        iconBackground = ChipSistemBg,
        iconColor = StatusError,
        extraInfo = null
    ),
    NotificationItem(
        id = "NOTIF-004",
        category = NotificationCategory.MEMBERSHIP,
        title = "Membership Hampir Expired",
        memberName = "Emma Wilson",
        body = "Membership Premium Monthly akan berakhir dalam 7 hari.",
        time = "1 hari lalu",
        isUnread = false,
        icon = Icons.Filled.Warning,
        iconBackground = ChipMembershipBg,
        iconColor = StatusWarning,
        extraInfo = "13 September 2026"
    ),
    NotificationItem(
        id = "NOTIF-005",
        category = NotificationCategory.MEMBER,
        title = "Member Tidak Aktif",
        memberName = "David Miller",
        body = "Member tidak melakukan check-in selama 21 hari.",
        time = "2 hari lalu",
        isUnread = false,
        icon = Icons.Filled.PersonOff,
        iconBackground = ChipMemberBg,
        iconColor = StatusError,
        extraInfo = "Terakhir check-in: 16 August 2026"
    )
)

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifikasiScreen(
    onBackClick: () -> Unit = {},
    onNotificationClick: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    var selectedFilter by remember { mutableStateOf(NotifikasiFilter.ALL) }
    val notifications = remember { mutableStateListOf<NotificationItem>().apply { addAll(buildMockNotifications()) } }
    var showWhatsAppDialog by remember { mutableStateOf(false) }
    var whatsAppMemberName by remember { mutableStateOf("") }

    val filteredNotifications = when (selectedFilter) {
        NotifikasiFilter.ALL -> notifications
        NotifikasiFilter.MEMBERSHIP -> notifications.filter { it.category == NotificationCategory.MEMBERSHIP }
        NotifikasiFilter.MEMBER -> notifications.filter { it.category == NotificationCategory.MEMBER || it.category == NotificationCategory.BIRTHDAY }
        NotifikasiFilter.SISTEM -> notifications.filter { it.category == NotificationCategory.BIRTHDAY }
    }

    val unreadCount = notifications.count { it.isUnread }

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
                    if (unreadCount > 0) {
                        TextButton(
                            onClick = {
                                notifications.forEachIndexed { index, _ ->
                                    notifications[index] = notifications[index].copy(isUnread = false)
                                }
                            }
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

            // Notifications
            if (filteredNotifications.isEmpty()) {
                item {
                    EmptyNotificationState()
                }
            } else {
                items(filteredNotifications, key = { it.id }) { notification ->
                    NotificationCard(
                        notification = notification,
                        onClick = {
                            // Mark as read
                            val index = notifications.indexOfFirst { it.id == notification.id }
                            if (index >= 0) {
                                notifications[index] = notification.copy(isUnread = false)
                            }

                            // Handle birthday WhatsApp
                            if (notification.category == NotificationCategory.BIRTHDAY) {
                                whatsAppMemberName = notification.memberName
                                showWhatsAppDialog = true
                            } else {
                                onNotificationClick(notification.id)
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

    // WhatsApp Dialog
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
                    text = "Fitur pengiriman WhatsApp akan tersedia pada tahap berikutnya.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        )
    }
}

// ============================================================================
// NOTIFICATION CARD
// ============================================================================
@Composable
private fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Dimens.card_corner_radius),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isUnread) UnreadBackground else CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isUnread) 2.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_4),
            verticalAlignment = Alignment.Top
        ) {
            // Unread indicator
            if (notification.isUnread) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(GreenAccent)
                        .align(Alignment.Top)
                )
                Spacer(modifier = Modifier.width(Dimens.spacing_2))
            }

            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(notification.iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = notification.icon,
                    contentDescription = null,
                    tint = notification.iconColor,
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
                        fontWeight = if (notification.isUnread) FontWeight.Bold else FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = notification.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.memberName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenAccent
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (notification.extraInfo != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = notification.extraInfo,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                if (notification.category == NotificationCategory.BIRTHDAY) {
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