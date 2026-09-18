package com.pws.primaragagym.screens.admin.notifikasi

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.BackgroundColor
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.CardBackground
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.ChipMemberBg
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.ChipMembershipBg
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.GreenAccent
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.GreenLight
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.StatusError
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.StatusWarning
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.TextMuted
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.TextPrimary
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.TextSecondary
import com.pws.primaragagym.ui.theme.Dimens

// ============================================================================
// NOTIFICATION DETAIL TYPES
// ============================================================================
private enum class NotificationType {
    MEMBERSHIP_EXPIRING,
    MEMBER_INACTIVE,
    BIRTHDAY
}

private data class NotificationDetailData(
    val type: NotificationType,
    val title: String,
    val memberName: String,
    val memberCode: String,
    val body: String,
    val date: String,
    val icon: ImageVector,
    val iconBackground: Color,
    val iconColor: Color,
    val extraInfo: String? = null,
    val showViewMember: Boolean = true
)

// ============================================================================
// MOCK DATA LOOKUP
// ============================================================================
private fun getNotificationData(notificationId: String): NotificationDetailData {
    return when (notificationId) {
        "NOTIF-001" -> NotificationDetailData(
            type = NotificationType.MEMBERSHIP_EXPIRING,
            title = "Membership Hampir Expired",
            memberName = "John Smith",
            memberCode = "MBR-001",
            body = "Membership Premium Monthly akan berakhir dalam 3 hari.\n\nSegera hubungi member untuk mengingatkan perpanjangan membership.",
            date = "06 September 2026",
            icon = Icons.Filled.Warning,
            iconBackground = ChipMembershipBg,
            iconColor = StatusWarning,
            extraInfo = "Berakhir: 30 September 2026"
        )
        "NOTIF-002" -> NotificationDetailData(
            type = NotificationType.MEMBER_INACTIVE,
            title = "Member Tidak Aktif",
            memberName = "Sarah Connor",
            memberCode = "MBR-002",
            body = "Sarah Connor tidak melakukan check-in selama 14 hari.\n\nPertimbangkan untuk menghubungi member dan menanyakan kondisinya.",
            date = "06 September 2026",
            icon = Icons.Filled.PersonOff,
            iconBackground = ChipMemberBg,
            iconColor = StatusError,
            extraInfo = "Terakhir check-in: 23 August 2026"
        )
        "NOTIF-003" -> NotificationDetailData(
            type = NotificationType.BIRTHDAY,
            title = "Ulang Tahun Member",
            memberName = "Michael Brown",
            memberCode = "MBR-003",
            body = "Michael Brown berulang tahun hari ini.\n\nKirimkan ucapan selamat kepada member melalui WhatsApp.",
            date = "06 September 2026",
            icon = Icons.Filled.Person,
            iconBackground = ChipMembershipBg,
            iconColor = StatusWarning,
            extraInfo = null,
            showViewMember = false
        )
        else -> NotificationDetailData(
            type = NotificationType.MEMBERSHIP_EXPIRING,
            title = "Membership Hampir Expired",
            memberName = "Emma Wilson",
            memberCode = "MBR-005",
            body = "Membership Premium Monthly akan berakhir dalam 7 hari.",
            date = "05 September 2026",
            icon = Icons.Filled.Warning,
            iconBackground = ChipMembershipBg,
            iconColor = StatusWarning,
            extraInfo = "Berakhir: 13 September 2026"
        )
    }
}

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    notificationId: String = "",
    onBackClick: () -> Unit = {},
    onViewMemberClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val data = remember { getNotificationData(notificationId.ifBlank { "NOTIF-001" }) }

    Scaffold(
        containerColor = BackgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Notifikasi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = if (isTablet) 32.dp else Dimens.screen_padding_horizontal,
                    vertical = Dimens.spacing_5
                )
        ) {
            // Notification Detail Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.card_corner_radius),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacing_5)
                ) {
                    // Icon & Title
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(data.iconBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = data.icon,
                                contentDescription = null,
                                tint = data.iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(Dimens.spacing_3))
                        Column {
                            Text(
                                text = data.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = data.date,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.spacing_5))

                    // Member Info
                    NotificationInfoRow(label = "Member", value = data.memberName)
                    Spacer(modifier = Modifier.height(Dimens.spacing_3))
                    NotificationInfoRow(label = "ID", value = data.memberCode)

                    if (data.extraInfo != null) {
                        Spacer(modifier = Modifier.height(Dimens.spacing_3))
                        NotificationInfoRow(label = "Info", value = data.extraInfo)
                    }

                    Spacer(modifier = Modifier.height(Dimens.spacing_5))

                    // Body
                    Text(
                        text = data.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_6))

            // Action Buttons
            if (data.showViewMember) {
                Button(
                    onClick = onViewMemberClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimens.button_corner_radius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenAccent
                    )
                ) {
                    Text(
                        text = "Lihat Member",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_4))
        }
    }
}

// ============================================================================
// HELPER
// ============================================================================
@Composable
private fun NotificationInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}
