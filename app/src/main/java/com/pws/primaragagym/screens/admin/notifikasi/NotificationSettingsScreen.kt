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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.BackgroundColor
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.CardBackground
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.ChipMemberBg
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.ChipMembershipBg
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.ChipSistemBg
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.GreenAccent
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.TextMuted
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.TextPrimary
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.TextSecondary
import com.pws.primaragagym.ui.theme.Dimens

// ============================================================================
// SETTINGS ITEMS
// ============================================================================
private data class NotificationSettingItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconBackground: Color,
    val iconColor: Color
)

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBackClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    var pushNotificationEnabled by remember { mutableStateOf(true) }
    var membershipExpiringEnabled by remember { mutableStateOf(true) }
    var memberInactiveEnabled by remember { mutableStateOf(true) }
    var birthdayEnabled by remember { mutableStateOf(true) }
    var whatsappEnabled by remember { mutableStateOf(false) }

    val settingsItems = listOf(
        NotificationSettingItem(
            title = "Push Notification",
            description = "Notifikasi aplikasi",
            icon = Icons.Filled.Campaign,
            iconBackground = ChipSistemBg,
            iconColor = GreenAccent
        ),
        NotificationSettingItem(
            title = "Membership Hampir Expired",
            description = "Notifikasi saat membership akan berakhir",
            icon = Icons.Filled.Warning,
            iconBackground = ChipMembershipBg,
            iconColor = Color(0xFFFF9800)
        ),
        NotificationSettingItem(
            title = "Member Tidak Aktif",
            description = "Notifikasi saat member tidak check-in",
            icon = Icons.Filled.PersonOff,
            iconBackground = ChipMemberBg,
            iconColor = Color(0xFFF44336)
        ),
        NotificationSettingItem(
            title = "Ulang Tahun Member",
            description = "Notifikasi saat member berulang tahun",
            icon = Icons.Filled.Notifications,
            iconBackground = ChipSistemBg,
            iconColor = GreenAccent
        )
    )

    Scaffold(
        containerColor = BackgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pengaturan Notifikasi",
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
            // Info Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.card_corner_radius),
                colors = CardDefaults.cardColors(containerColor = ChipMembershipBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacing_4),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Dimens.spacing_3))
                    Text(
                        text = "Pengaturan notifikasi akan disimpan secara lokal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_6))

            // Push Notification Section
            Text(
                text = "Push Notification",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_3))

            SettingToggleCard(
                title = "Notifikasi Aplikasi",
                description = "Aktifkan notifikasi push",
                icon = Icons.Filled.Campaign,
                iconBackground = ChipSistemBg,
                iconColor = GreenAccent,
                isEnabled = pushNotificationEnabled,
                onToggle = { pushNotificationEnabled = it }
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_6))

            // Notification Categories
            Text(
                text = "Kategori Notifikasi",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_3))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.card_corner_radius),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    NotificationToggleItem(
                        title = "Membership Hampir Expired",
                        description = "Notifikasi saat membership akan berakhir",
                        icon = Icons.Filled.Warning,
                        iconBackground = ChipMembershipBg,
                        iconColor = Color(0xFFFF9800),
                        isEnabled = membershipExpiringEnabled,
                        onToggle = { membershipExpiringEnabled = it },
                        showDivider = true
                    )
                    NotificationToggleItem(
                        title = "Member Tidak Aktif",
                        description = "Notifikasi saat member tidak check-in",
                        icon = Icons.Filled.PersonOff,
                        iconBackground = ChipMemberBg,
                        iconColor = Color(0xFFF44336),
                        isEnabled = memberInactiveEnabled,
                        onToggle = { memberInactiveEnabled = it },
                        showDivider = true
                    )
                    NotificationToggleItem(
                        title = "Ulang Tahun Member",
                        description = "Notifikasi saat member berulang tahun",
                        icon = Icons.Filled.Notifications,
                        iconBackground = ChipSistemBg,
                        iconColor = GreenAccent,
                        isEnabled = birthdayEnabled,
                        onToggle = { birthdayEnabled = it },
                        showDivider = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_6))

            // WhatsApp Section
            Text(
                text = "WhatsApp",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_3))

            SettingToggleCard(
                title = "Integrasi WhatsApp",
                description = "Kirim notifikasi via WhatsApp",
                icon = Icons.Filled.Mail,
                iconBackground = Color(0xFF25D366).copy(alpha = 0.15f),
                iconColor = Color(0xFF25D366),
                isEnabled = whatsappEnabled,
                onToggle = { whatsappEnabled = it }
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_6))
        }
    }
}

// ============================================================================
// SETTING TOGGLE CARD
// ============================================================================
@Composable
private fun SettingToggleCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconBackground: Color,
    iconColor: Color,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.card_corner_radius),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_4),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(Dimens.spacing_3))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(Dimens.spacing_3))
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = GreenAccent,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFE0E0E0)
                )
            )
        }
    }
}

// ============================================================================
// NOTIFICATION TOGGLE ITEM
// ============================================================================
@Composable
private fun NotificationToggleItem(
    title: String,
    description: String,
    icon: ImageVector,
    iconBackground: Color,
    iconColor: Color,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_4),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(Dimens.spacing_3))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(Dimens.spacing_3))
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = GreenAccent,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFE0E0E0)
                )
            )
        }
        if (showDivider) {
            HorizontalDivider(
                color = Color(0xFFE0E0E0),
                modifier = Modifier.padding(horizontal = Dimens.spacing_4)
            )
        }
    }
}