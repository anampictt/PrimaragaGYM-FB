package com.pws.primaragagym.screens.superadmin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.FitnessCenter
import com.pws.primaragagym.commond.DateUtils
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder
import com.pws.primaragagym.domain.model.BirthdayMemberItem
import com.pws.primaragagym.domain.model.ActiveNeverCheckinMemberItem
import com.pws.primaragagym.domain.model.InactiveMemberItem
import com.pws.primaragagym.domain.model.MemberInsightType
import com.pws.primaragagym.domain.model.formatBirthdayCountdown
import com.pws.primaragagym.domain.model.ChatTemplateCategory
import com.pws.primaragagym.domain.model.formatChatTemplateMessage
import com.pws.primaragagym.ui.viewmodel.ChatTemplateViewModel
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.collectAsState
import com.pws.primaragagym.ui.components.shimmerEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.pws.primaragagym.ui.theme.GreenPrimary
import com.pws.primaragagym.ui.theme.GreenPrimaryLight
import com.pws.primaragagym.ui.viewmodel.SuperAdminDashboardViewModel
import com.pws.primaragagym.ui.viewmodel.AuthViewModel

// ============================================================================
// COLORS - Match design reference
// ============================================================================
private val BackgroundColor = Color(0xFFF5F7FA)
private val HeaderBackgroundColor = Color(0xFFE8F5E9) // Light green header
private val CardBackground = Color.White
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B6B6B)
private val TextMuted = Color(0xFF9E9E9E)
private val IconBackground = Color(0xFFE8F5E9) // Light green icon container
private val GreenAccent = Color(0xFF32A060) // Primaraga green
private val GreenStatus = Color(0xFF4CAF50)

// ============================================================================
// MOCK DATA - From design reference
// ============================================================================
private data class DashboardData(
    val greeting: String,
    val title: String,
    val subtitle: String,
    val summaryDate: String,
    val totalPengguna: Int,
    val totalCabang: Int,
    val totalMember: Int = 0,
    val pendapatan: String,
    val systemStatus: String,
    val lastLogin: String
)

private data class StatEntry(
    val value: String,
    val label: String,
    val icon: ImageVector
)

private val dashboardData = DashboardData(
    greeting = "Selamat datang kembali,",
    title = "Super Admin",
    subtitle = "Kelola gym Anda dengan mudah dan efisien.",
    summaryDate = "29 Mei 2025",
    totalPengguna = 128,
    totalCabang = 15,
    pendapatan = "2.6Jt",
    systemStatus = "Sistem aman dan terproteksi",
    lastLogin = "29 Mei 2025, 08:30 WIB"
)

// ============================================================================
// MENU DATA
// ============================================================================
data class MenuItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)

val allMenuItems = listOf(
    MenuItem(
        id = "manajemen_pengguna",
        title = "Manajemen Pengguna",
        description = "Kelola data pengguna sistem,\nseperti admin, staff, dan trainer.",
        icon = Icons.Filled.Person
    ),
    MenuItem(
        id = "manajemen_role",
        title = "Manajemen Role",
        description = "Atur peran dan hak akses\npengguna dalam sistem.",
        icon = Icons.Filled.Security
    ),
    MenuItem(
        id = "manajemen_member",
        title = "Member",
        description = "Kelola data member gym Anda.",
        icon = Icons.Filled.Groups
    ),
    MenuItem(
        id = "check_in_out",
        title = "Check In & Check Out",
        description = "Scan barcode member untuk\nproses check-in dan check-out.",
        icon = Icons.Filled.QrCodeScanner
    ),
    MenuItem(
        id = "riwayat_check_in_out",
        title = "Riwayat Check-in & Out",
        description = "Catatan riwayat jam masuk dan\nkeluar member gym.",
        icon = Icons.Filled.History
    ),
    MenuItem(
        id = "notifikasi",
        title = "Notifikasi",
        description = "Lihat informasi dan\npemberitahuan terbaru.",
        icon = Icons.Filled.Notifications
    )
)

// ============================================================================
// BOTTOM NAVIGATION DATA
// ============================================================================
enum class BottomNavItem(
    val label: String,
    val icon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Filled.Home),
    KEUANGAN("Keuangan", Icons.Filled.TrendingUp),
    PENGATURAN_AKUN("Profil", Icons.Filled.Person)
}

// ============================================================================
// MAIN SCREEN
// ============================================================================
@Composable
fun SuperAdminDashboardContent(
    viewModel: SuperAdminDashboardViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onUserManagementClick: () -> Unit = {},
    onRoleManagementClick: () -> Unit = {},
    onMemberClick: () -> Unit = {},
    onMemberDetailClick: (String) -> Unit = {},
    onCheckInOutClick: () -> Unit = {},
    onRiwayatCheckInOutClick: () -> Unit = {},
    onCatatanKeuanganClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onReportClick: () -> Unit = {},
    onAccountSettingsClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    
    val authState by authViewModel.uiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var activeInsightDialog by remember { mutableStateOf<MemberInsightType?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    val displayName = authState.currentUser?.name?.ifBlank { null }
        ?: authState.currentUser?.roleTitle?.ifBlank { null }
        ?: authState.currentUser?.role?.displayName
        ?: "Pengguna"

    val data = DashboardData(
        greeting = "Selamat datang kembali,",
        title = displayName,
        subtitle = "Kelola gym Anda dengan mudah dan efisien.",
        summaryDate = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy")),
        totalPengguna = uiState.totalUsers,
        totalCabang = uiState.totalBranches,
        totalMember = uiState.totalMembers,
        pendapatan = uiState.todayRevenue.ifBlank { "Rp 0" },
        systemStatus = "Sistem aman dan terproteksi",
        lastLogin = authState.currentUser?.lastLogin?.ifBlank { null }?.let {
            DateUtils.formatLastLogin(it, fallback = "Baru saja")
        } ?: "Baru saja"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = if (isTablet) 32.dp else 0.dp)
            .padding(top = if (isTablet) 32.dp else 0.dp)
    ) {
        item {
            DashboardHeader(
                data = data,
                isTablet = isTablet,
                onNotificationClick = onNotificationClick,
                onAccountSettingsClick = onAccountSettingsClick
            )
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isTablet) 0.dp else 24.dp)
                    .padding(top = 24.dp)
            ) {
                SummaryCard(
                    data = data,
                    authViewModel = authViewModel,
                    isLoading = uiState.isLoading,
                    isTablet = isTablet
                )
                Spacer(modifier = Modifier.height(24.dp))
                MemberInsightsSection(
                    birthdayMembers = uiState.birthdayMembers,
                    activeNeverCheckinMembers = uiState.activeNeverCheckinMembers,
                    inactiveMembers = uiState.inactiveMembers,
                    isLoading = uiState.isLoading,
                    isTablet = isTablet,
                    onInsightClick = { insightType ->
                        activeInsightDialog = insightType
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                if (!isTablet) {
                    val allowedMenuItems = remember(authState.permissions, authState.currentUser) {
                        allMenuItems.filter { authViewModel.hasPermission(it.id) }
                    }
                    if (allowedMenuItems.isNotEmpty()) {
                        MenuUtamaSection(
                            menuItems = allowedMenuItems,
                            onUserManagementClick = onUserManagementClick,
                            onRoleManagementClick = onRoleManagementClick,
                            onMemberClick = onMemberClick,
                            onCheckInOutClick = onCheckInOutClick,
                            onRiwayatCheckInOutClick = onRiwayatCheckInOutClick,
                            onCatatanKeuanganClick = onCatatanKeuanganClick,
                            onNotificationClick = onNotificationClick,
                            onReportClick = onReportClick,
                            isTablet = isTablet
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                InformasiSistemSection(data = data, isTablet = isTablet)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (activeInsightDialog != null) {
        MemberInsightDetailDialog(
            type = activeInsightDialog!!,
            birthdayMembers = uiState.birthdayMembers,
            activeNeverCheckinMembers = uiState.activeNeverCheckinMembers,
            inactiveMembers = uiState.inactiveMembers,
            onDismiss = { activeInsightDialog = null },
            onMemberDetailClick = { memberId ->
                activeInsightDialog = null
                onMemberDetailClick(memberId)
            }
        )
    }
}


// ============================================================================
// DASHBOARD HEADER
// ============================================================================
@Composable
private fun DashboardHeader(
    data: DashboardData,
    isTablet: Boolean = false,
    onNotificationClick: () -> Unit = {},
    onAccountSettingsClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderBackgroundColor)
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 16.dp,
                bottom = 24.dp
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left side - Greeting
            Column {
                Text(
                    text = data.greeting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (data.title.isBlank()) {
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shimmerEffect()
                    )
                } else {
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (isTablet) 32.sp else 28.sp
                        ),
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = data.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Right side - Notification & Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Notification
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onNotificationClick() },
                    contentAlignment = Alignment.Center
                ) {
                    BadgedBox(
                        badge = {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd),
                                containerColor = GreenAccent,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = "3",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// SUMMARY CARD
// ============================================================================
@Composable
private fun SummaryCard(
    data: DashboardData,
    authViewModel: AuthViewModel,
    isLoading: Boolean = false,
    isTablet: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isTablet) 20.dp else 16.dp)
        ) {
            // Header with title and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ringkasan Hari Ini",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = data.summaryDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            AnimatedContent(
                targetState = isLoading,
                transitionSpec = {
                    fadeIn(animationSpec = tween(350)) togetherWith fadeOut(animationSpec = tween(350))
                },
                label = "SummaryCardContentAnimation"
            ) { loading ->
                if (loading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        repeat(3) {
                            StatisticItemShimmer(
                                isTablet = isTablet,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    // Statistics Row: Total Member, Total Pengguna, Pendapatan
                    val statList = listOf(
                        StatEntry("${data.totalMember}", "Total Member", Icons.Filled.Person),
                        StatEntry("${data.totalPengguna}", "Total Pengguna", Icons.Filled.Group),
                        StatEntry(data.pendapatan, "Pendapatan", Icons.Filled.Payments)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        statList.forEach { stat ->
                            StatisticItem(
                                value = stat.value,
                                label = stat.label,
                                icon = stat.icon,
                                isTablet = isTablet,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticItemShimmer(
    isTablet: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 2.dp)
    ) {
        // Shimmer Circular Icon Container
        Box(
            modifier = Modifier
                .size(if (isTablet) 50.dp else 44.dp)
                .clip(CircleShape)
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Shimmer Value Box
        Box(
            modifier = Modifier
                .width(if (isTablet) 64.dp else 56.dp)
                .height(if (isTablet) 22.dp else 18.dp)
                .clip(RoundedCornerShape(6.dp))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Shimmer Label Box
        Box(
            modifier = Modifier
                .width(if (isTablet) 76.dp else 64.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
    }
}

@Composable
private fun StatisticItem(
    value: String,
    label: String,
    icon: ImageVector = Icons.Filled.Person,
    isTablet: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 2.dp)
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(if (isTablet) 50.dp else 44.dp)
                .clip(CircleShape)
                .background(IconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GreenAccent,
                modifier = Modifier.size(if (isTablet) 24.dp else 22.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dynamic font sizing based on length to prevent wrapping on narrow screens
        val valueFontSize = when {
            isTablet -> if (value.length > 10) 18.sp else 22.sp
            value.length > 13 -> 11.5.sp
            value.length > 10 -> 13.sp
            value.length > 7 -> 14.5.sp
            value.length > 4 -> 16.sp
            else -> 20.sp
        }

        // Value
        Text(
            text = value,
            style = TextStyle(
                fontSize = valueFontSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            color = TextPrimary,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = if (isTablet) 12.sp else 10.5.sp,
                textAlign = TextAlign.Center
            ),
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ============================================================================
// MEMBER INSIGHTS SECTION
// ============================================================================
@Composable
private fun MemberInsightsSection(
    birthdayMembers: List<BirthdayMemberItem>,
    activeNeverCheckinMembers: List<ActiveNeverCheckinMemberItem>,
    inactiveMembers: List<InactiveMemberItem>,
    isLoading: Boolean = false,
    isTablet: Boolean = false,
    onInsightClick: (MemberInsightType) -> Unit
) {
    Column {
        Text(
            text = "Informasi Member",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (isTablet) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MemberInsightCard(
                    title = "Akan Ulang Tahun",
                    subtitle = if (birthdayMembers.isEmpty()) "Tidak ada dalam 7 hari" else "${birthdayMembers.size} member (7 hari ke depan)",
                    count = birthdayMembers.size,
                    icon = Icons.Filled.Cake,
                    accentColor = Color(0xFFE91E63),
                    containerColor = Color(0xFFFCE4EC),
                    isTablet = true,
                    onClick = { onInsightClick(MemberInsightType.BIRTHDAY) },
                    modifier = Modifier.weight(1f)
                )

                MemberInsightCard(
                    title = "Aktif Belum Check-in",
                    subtitle = if (activeNeverCheckinMembers.isEmpty()) "Semua sudah pernah datang" else "${activeNeverCheckinMembers.size} member belum pernah datang",
                    count = activeNeverCheckinMembers.size,
                    icon = Icons.Filled.FitnessCenter,
                    accentColor = Color(0xFFEF6C00),
                    containerColor = Color(0xFFFFF3E0),
                    isTablet = true,
                    onClick = { onInsightClick(MemberInsightType.NEVER_CHECKIN) },
                    modifier = Modifier.weight(1f)
                )

                MemberInsightCard(
                    title = "Belum Perpanjang",
                    subtitle = if (inactiveMembers.isEmpty()) "Tidak ada member expired" else "${inactiveMembers.size} member perlu win-back",
                    count = inactiveMembers.size,
                    icon = Icons.Filled.History,
                    accentColor = Color(0xFFD32F2F),
                    containerColor = Color(0xFFFFEBEE),
                    isTablet = true,
                    onClick = { onInsightClick(MemberInsightType.INACTIVE) },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MemberInsightCard(
                    title = "Member Akan Ulang Tahun",
                    subtitle = if (birthdayMembers.isEmpty()) "Tidak ada dalam 7 hari ke depan" else "${birthdayMembers.size} member dalam 7 hari ke depan",
                    count = birthdayMembers.size,
                    icon = Icons.Filled.Cake,
                    accentColor = Color(0xFFE91E63),
                    containerColor = Color(0xFFFCE4EC),
                    isTablet = false,
                    onClick = { onInsightClick(MemberInsightType.BIRTHDAY) }
                )

                MemberInsightCard(
                    title = "Aktif Tapi Belum Check-in",
                    subtitle = if (activeNeverCheckinMembers.isEmpty()) "Semua member aktif sudah check-in" else "${activeNeverCheckinMembers.size} member belum pernah latihan",
                    count = activeNeverCheckinMembers.size,
                    icon = Icons.Filled.FitnessCenter,
                    accentColor = Color(0xFFEF6C00),
                    containerColor = Color(0xFFFFF3E0),
                    isTablet = false,
                    onClick = { onInsightClick(MemberInsightType.NEVER_CHECKIN) }
                )

                MemberInsightCard(
                    title = "Tidak Aktif / Belum Perpanjang",
                    subtitle = if (inactiveMembers.isEmpty()) "Tidak ada member expired" else "${inactiveMembers.size} member expired belum perpanjang",
                    count = inactiveMembers.size,
                    icon = Icons.Filled.History,
                    accentColor = Color(0xFFD32F2F),
                    containerColor = Color(0xFFFFEBEE),
                    isTablet = false,
                    onClick = { onInsightClick(MemberInsightType.INACTIVE) }
                )
            }
        }
    }
}

@Composable
private fun MemberInsightCard(
    title: String,
    subtitle: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isTablet: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        if (isTablet) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(containerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(containerColor)
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$count Member",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            ),
                            color = accentColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = TextSecondary,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.heightIn(min = 34.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Lihat rincian",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        ),
                        color = accentColor
                    )
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(containerColor)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = accentColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

private data class InsightThemeConfig(
    val title: String,
    val icon: ImageVector,
    val accentColor: Color,
    val containerColor: Color
)

@Composable
private fun MemberInsightDetailDialog(
    type: MemberInsightType,
    birthdayMembers: List<BirthdayMemberItem>,
    activeNeverCheckinMembers: List<ActiveNeverCheckinMemberItem>,
    inactiveMembers: List<InactiveMemberItem>,
    onDismiss: () -> Unit,
    onMemberDetailClick: (String) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    var searchQuery by remember { mutableStateOf("") }

    val templateViewModel: ChatTemplateViewModel = viewModel()
    val templateState by templateViewModel.uiState.collectAsState()

    val birthdayTemplateMsg = templateState.templates.find { it.categoryEnum == ChatTemplateCategory.BIRTHDAY && it.isDefault }?.message
        ?: templateState.templates.find { it.categoryEnum == ChatTemplateCategory.BIRTHDAY }?.message
        ?: "Halo Kak {nama}, segenap keluarga {gym} mengucapkan Selamat Ulang Tahun! 🎂🎉 Semoga sehat selalu, panjang umur, dan semakin bersemangat berolahraga bersama {gym}! 💪"

    val neverCheckinTemplateMsg = templateState.templates.find { it.categoryEnum == ChatTemplateCategory.NEVER_CHECKIN && it.isDefault }?.message
        ?: templateState.templates.find { it.categoryEnum == ChatTemplateCategory.NEVER_CHECKIN }?.message
        ?: "Halo Kak {nama}, kami dari tim {gym} melihat Kakak sudah aktif terdaftar tapi belum sempat datang latihan nih. Ada yang bisa kami bantu atau jadwalkan pengenalan alat gym? Kami tunggu kedatangannya ya Kak! 💪🔥"

    val inactiveTemplateMsg = templateState.templates.find { it.categoryEnum == ChatTemplateCategory.INACTIVE && it.isDefault }?.message
        ?: templateState.templates.find { it.categoryEnum == ChatTemplateCategory.INACTIVE }?.message
        ?: "Halo Kak {nama}, kami kangen latihan bareng Kakak di {gym}! Yuk aktifkan kembali membership Kakak dan dapatkan promo perpanjangan menarik hari ini. Ditunggu kedatangannya ya Kak! 🏋️‍♂️"

    val config = when (type) {
        MemberInsightType.BIRTHDAY -> InsightThemeConfig(
            title = "Member Akan Ulang Tahun",
            icon = Icons.Filled.Cake,
            accentColor = Color(0xFFE91E63),
            containerColor = Color(0xFFFCE4EC)
        )
        MemberInsightType.NEVER_CHECKIN -> InsightThemeConfig(
            title = "Aktif Belum Pernah Check-in",
            icon = Icons.Filled.FitnessCenter,
            accentColor = Color(0xFFEF6C00),
            containerColor = Color(0xFFFFF3E0)
        )
        MemberInsightType.INACTIVE -> InsightThemeConfig(
            title = "Belum Perpanjang Membership",
            icon = Icons.Filled.History,
            accentColor = Color(0xFFD32F2F),
            containerColor = Color(0xFFFFEBEE)
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 320.dp, max = if (isTablet) 560.dp else 420.dp)
                .fillMaxWidth(if (isTablet) 0.85f else 0.94f)
                .padding(vertical = if (isTablet) 24.dp else 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isTablet) 24.dp else 18.dp)
            ) {
                // Header Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(config.containerColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = config.icon,
                                contentDescription = null,
                                tint = config.accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = config.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isTablet) 17.sp else 15.sp
                                ),
                                color = TextPrimary
                            )
                            val count = when (type) {
                                MemberInsightType.BIRTHDAY -> birthdayMembers.size
                                MemberInsightType.NEVER_CHECKIN -> activeNeverCheckinMembers.size
                                MemberInsightType.INACTIVE -> inactiveMembers.size
                            }
                            Text(
                                text = "$count member terdata",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Tutup",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nama atau kode member...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Hapus",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenAccent,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // List Data sesuai tipe
                when (type) {
                    MemberInsightType.BIRTHDAY -> {
                        val filtered = birthdayMembers.filter {
                            it.member.fullName.contains(searchQuery, ignoreCase = true) ||
                            it.member.memberCode.contains(searchQuery, ignoreCase = true)
                        }
                        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                        val paginated = com.pws.primaragagym.ui.components.common.rememberPaginatedList(
                            items = filtered,
                            pageSize = 8,
                            resetKey = searchQuery
                        )
                        com.pws.primaragagym.ui.components.common.BindPagination(listState, paginated)

                        if (filtered.isEmpty()) {
                            EmptyInsightState(
                                message = if (searchQuery.isNotEmpty()) "Tidak ada member yang cocok dengan pencarian." else "Tidak ada member yang berulang tahun dalam 7 hari ke depan 🎉"
                            )
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = if (isTablet) 460.dp else 400.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(paginated.visibleItems, key = { it.member.memberId.ifBlank { it.member.memberCode } }) { item ->
                                    val bdayCountdown = formatBirthdayCountdown(item.daysRemaining)
                                    val isToday = item.daysRemaining == 0
                                    val bdayMsg = formatChatTemplateMessage(
                                        template = birthdayTemplateMsg,
                                        memberName = item.member.fullName,
                                        memberCode = item.member.memberCode,
                                        age = item.ageThisYear,
                                        daysRemaining = item.daysRemaining
                                    )

                                    InsightMemberCardItem(
                                        name = item.member.fullName,
                                        code = item.member.memberCode,
                                        badgeText = bdayCountdown,
                                        badgeBgColor = if (isToday) Color(0xFFFCE4EC) else Color(0xFFF3E5F5),
                                        badgeTextColor = if (isToday) Color(0xFFC2185B) else Color(0xFF7B1FA2),
                                        detailInfo = "Tanggal: ${item.formattedBirthday}" + if (item.ageThisYear != null) " • Usia: ${item.ageThisYear} thn" else "",
                                        phone = item.member.phoneNumber,
                                        onWhatsAppClick = {
                                            sendInsightWhatsApp(context, item.member.phoneNumber, bdayMsg)
                                        },
                                        onDetailClick = {
                                            onMemberDetailClick(item.member.memberId)
                                        },
                                        actionLabel = "Kirim Ucapan"
                                    )
                                }

                                if (paginated.isLoadingMore) {
                                    item(key = "pagination_loading_bday") {
                                        com.pws.primaragagym.ui.components.common.PaginationLoadingItem()
                                    }
                                } else if (!paginated.hasMore && paginated.totalCount > 8) {
                                    item(key = "pagination_end_bday") {
                                        com.pws.primaragagym.ui.components.common.PaginationEndOfListItem(totalCount = paginated.totalCount)
                                    }
                                }
                            }
                        }
                    }

                    MemberInsightType.NEVER_CHECKIN -> {
                        val filtered = activeNeverCheckinMembers.filter {
                            it.member.fullName.contains(searchQuery, ignoreCase = true) ||
                            it.member.memberCode.contains(searchQuery, ignoreCase = true)
                        }
                        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                        val paginated = com.pws.primaragagym.ui.components.common.rememberPaginatedList(
                            items = filtered,
                            pageSize = 8,
                            resetKey = searchQuery
                        )
                        com.pws.primaragagym.ui.components.common.BindPagination(listState, paginated)

                        if (filtered.isEmpty()) {
                            EmptyInsightState(
                                message = if (searchQuery.isNotEmpty()) "Tidak ada member yang cocok dengan pencarian." else "Hebat! Semua member aktif sudah pernah check-in latihan 💪"
                            )
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = if (isTablet) 460.dp else 400.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(paginated.visibleItems, key = { it.member.memberId.ifBlank { it.member.memberCode } }) { item ->
                                    val checkinMsg = formatChatTemplateMessage(
                                        template = neverCheckinTemplateMsg,
                                        memberName = item.member.fullName,
                                        memberCode = item.member.memberCode,
                                        daysRemaining = item.daysSinceJoined.toInt()
                                    )

                                    InsightMemberCardItem(
                                        name = item.member.fullName,
                                        code = item.member.memberCode,
                                        badgeText = "0x Check-in",
                                        badgeBgColor = Color(0xFFFFF3E0),
                                        badgeTextColor = Color(0xFFE65100),
                                        detailInfo = "Bergabung: ${item.formattedJoinDate} (${item.daysSinceJoined} hari lalu)",
                                        phone = item.member.phoneNumber,
                                        onWhatsAppClick = {
                                            sendInsightWhatsApp(context, item.member.phoneNumber, checkinMsg)
                                        },
                                        onDetailClick = {
                                            onMemberDetailClick(item.member.memberId)
                                        },
                                        actionLabel = "Sapa Member"
                                    )
                                }

                                if (paginated.isLoadingMore) {
                                    item(key = "pagination_loading_never") {
                                        com.pws.primaragagym.ui.components.common.PaginationLoadingItem()
                                    }
                                } else if (!paginated.hasMore && paginated.totalCount > 8) {
                                    item(key = "pagination_end_never") {
                                        com.pws.primaragagym.ui.components.common.PaginationEndOfListItem(totalCount = paginated.totalCount)
                                    }
                                }
                            }
                        }
                    }

                    MemberInsightType.INACTIVE -> {
                        val filtered = inactiveMembers.filter {
                            it.member.fullName.contains(searchQuery, ignoreCase = true) ||
                            it.member.memberCode.contains(searchQuery, ignoreCase = true)
                        }
                        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                        val paginated = com.pws.primaragagym.ui.components.common.rememberPaginatedList(
                            items = filtered,
                            pageSize = 8,
                            resetKey = searchQuery
                        )
                        com.pws.primaragagym.ui.components.common.BindPagination(listState, paginated)

                        if (filtered.isEmpty()) {
                            EmptyInsightState(
                                message = if (searchQuery.isNotEmpty()) "Tidak ada member yang cocok dengan pencarian." else "Tidak ada member yang tidak aktif ✨"
                            )
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = if (isTablet) 460.dp else 400.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(paginated.visibleItems, key = { it.member.memberId.ifBlank { it.member.memberCode } }) { item ->
                                    val winbackMsg = formatChatTemplateMessage(
                                        template = inactiveTemplateMsg,
                                        memberName = item.member.fullName,
                                        memberCode = item.member.memberCode,
                                        planName = item.member.planName,
                                        expiredDate = item.formattedExpiredDisplay
                                    )

                                    InsightMemberCardItem(
                                        name = item.member.fullName,
                                        code = item.member.memberCode,
                                        badgeText = item.formattedExpiredTimeAgo,
                                        badgeBgColor = Color(0xFFFFEBEE),
                                        badgeTextColor = Color(0xFFC62828),
                                        detailInfo = "Expired: ${item.formattedExpiredDisplay}" + if (item.member.planName.isNotBlank()) " • Paket: ${item.member.planName}" else "",
                                        phone = item.member.phoneNumber,
                                        onWhatsAppClick = {
                                            sendInsightWhatsApp(context, item.member.phoneNumber, winbackMsg)
                                        },
                                        onDetailClick = {
                                            onMemberDetailClick(item.member.memberId)
                                        },
                                        actionLabel = "Tawarkan Promo"
                                    )
                                }

                                if (paginated.isLoadingMore) {
                                    item(key = "pagination_loading_inactive") {
                                        com.pws.primaragagym.ui.components.common.PaginationLoadingItem()
                                    }
                                } else if (!paginated.hasMore && paginated.totalCount > 8) {
                                    item(key = "pagination_end_inactive") {
                                        com.pws.primaragagym.ui.components.common.PaginationEndOfListItem(totalCount = paginated.totalCount)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightMemberCardItem(
    name: String,
    code: String,
    badgeText: String,
    badgeBgColor: Color,
    badgeTextColor: Color,
    detailInfo: String,
    phone: String = "",
    onWhatsAppClick: () -> Unit,
    onDetailClick: () -> Unit,
    actionLabel: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar initial
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GreenPrimaryLight.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.firstOrNull()?.uppercase() ?: "M",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GreenAccent
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp
                            ),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (code.isNotBlank()) {
                            Text(
                                text = "($code)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = detailInfo,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBgColor)
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        ),
                        color = badgeTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Row: Phone info on left, balanced action buttons on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (phone.isNotBlank() && phone != "-") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = TextSecondary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDetailClick,
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0D5DD)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.OpenInNew,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Detail",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.5.sp
                            ),
                            color = TextPrimary
                        )
                    }

                    Button(
                        onClick = onWhatsAppClick,
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366) // WhatsApp Green
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Chat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyInsightState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

private fun sendInsightWhatsApp(context: Context, rawPhone: String, message: String) {
    val digits = rawPhone.filter { it.isDigit() }
    if (digits.isBlank()) {
        Toast.makeText(context, "Nomor WhatsApp member belum terisi", Toast.LENGTH_SHORT).show()
        return
    }

    val formattedPhone = when {
        digits.startsWith("0") -> "62" + digits.substring(1)
        digits.startsWith("62") -> digits
        else -> digits
    }

    try {
        val encoded = URLEncoder.encode(message, "UTF-8")
        val uri = Uri.parse("https://wa.me/$formattedPhone?text=$encoded")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Tidak dapat membuka WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

// ============================================================================
// MENU UTAMA SECTION
// ============================================================================
@Composable
private fun MenuUtamaSection(
    menuItems: List<MenuItem>,
    onUserManagementClick: () -> Unit,
    onRoleManagementClick: () -> Unit,
    onMemberClick: () -> Unit,
    onCheckInOutClick: () -> Unit,
    onRiwayatCheckInOutClick: () -> Unit = {},
    onCatatanKeuanganClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onReportClick: () -> Unit,
    isTablet: Boolean = false
) {
    if (menuItems.isEmpty()) return

    Column {
        // Section Title
        Text(
            text = "Menu Utama",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        menuItems.forEachIndexed { index, menuItem ->
            val onClick = when (menuItem.id) {
                "manajemen_pengguna" -> onUserManagementClick
                "manajemen_role" -> onRoleManagementClick
                "manajemen_member" -> onMemberClick
                "check_in_out" -> onCheckInOutClick
                "riwayat_check_in_out" -> onRiwayatCheckInOutClick
                "notifikasi" -> onNotificationClick
                "laporan" -> onReportClick
                else -> ({})
            }
            MenuCard(
                menuItem = menuItem,
                onClick = onClick
            )
            if (index < menuItems.lastIndex) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun MenuCard(
    menuItem: MenuItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(IconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = menuItem.icon,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = menuItem.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = menuItem.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Arrow
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = GreenAccent,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ============================================================================
// INFORMASI SISTEM SECTION
// ============================================================================
@Composable
private fun InformasiSistemSection(
    data: DashboardData,
    isTablet: Boolean = false
) {
    Column {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Informasi Sistem",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPrimary
            )

            Text(
                text = "Lihat Semua >",
                style = MaterialTheme.typography.bodySmall,
                color = GreenAccent,
                fontWeight = FontWeight.SemiBold
            )
        }

        // System Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = GreenPrimaryLight.copy(alpha = 0.15f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GreenPrimaryLight.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = data.systemStatus,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Terakhir login: ${data.lastLogin}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Green status dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(GreenStatus)
                )
            }
        }
    }
}

// ============================================================================
// BOTTOM NAVIGATION BAR
// ============================================================================
@Composable
fun BottomNavigationBar(
    selectedItem: BottomNavItem?,
    items: List<BottomNavItem> = BottomNavItem.values().toList(),
    onItemSelected: (BottomNavItem) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = selectedItem == item
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) GreenAccent else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isSelected) GreenAccent else TextMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = GreenAccent.copy(alpha = 0.1f)
                )
            )
        }
    }
}

// ============================================================================
// TABLET SIDEBAR
// ============================================================================
@Composable
fun TabletSidebar(
    selectedItem: BottomNavItem?,
    selectedMenuIndex: Int? = null,
    selectedMenuKey: String? = null,
    authViewModel: AuthViewModel = viewModel(),
    onItemSelected: (BottomNavItem) -> Unit,
    onUserManagementClick: () -> Unit,
    onRoleManagementClick: () -> Unit,
    onMemberClick: () -> Unit,
    onCheckInOutClick: () -> Unit,
    onRiwayatCheckInOutClick: () -> Unit = {},
    onCatatanKeuanganClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onReportClick: () -> Unit
) {
    val authState by authViewModel.uiState.collectAsState()
    val allowedMenuItems = remember(authState.permissions, authState.currentUser) {
        allMenuItems.filter { authViewModel.hasPermission(it.id) }
    }

    Column(
        modifier = Modifier
            .width(240.dp)
            .fillMaxSize()
            .background(Color.White)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PRIMARAGA",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = GreenAccent
            )
            Text(
                text = "GYM",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Navigation Items (Scrollable)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            val canAccessKeuangan = authViewModel.hasPermission("keuangan") || authViewModel.hasPermission("laporan")
            val topNavItems = remember(canAccessKeuangan) {
                buildList {
                    add(BottomNavItem.DASHBOARD)
                    if (canAccessKeuangan) {
                        add(BottomNavItem.KEUANGAN)
                    }
                }
            }
            topNavItems.forEachIndexed { index, item ->
                val isSelected = selectedItem == item
                TabletNavItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onItemSelected(item) }
                )
                if (index < topNavItems.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (allowedMenuItems.isNotEmpty()) {
                Text(
                    text = "Menu Utama",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                )

                allowedMenuItems.forEachIndexed { index, menuItem ->
                    val click = when (menuItem.id) {
                        "manajemen_pengguna" -> onUserManagementClick
                        "manajemen_role" -> onRoleManagementClick
                        "manajemen_member" -> onMemberClick
                        "check_in_out" -> onCheckInOutClick
                        "riwayat_check_in_out" -> onRiwayatCheckInOutClick
                        "notifikasi" -> onNotificationClick
                        "laporan" -> onReportClick
                        else -> ({})
                    }

                    val isSelected = if (selectedMenuKey != null) {
                        selectedMenuKey == menuItem.id
                    } else {
                        selectedMenuIndex == index
                    }

                    TabletSidebarMenuItem(
                        menuItem = menuItem,
                        isSelected = isSelected,
                        onClick = click
                    )
                    if (index < allowedMenuItems.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Bottom Fixed Navigation Item: Pengaturan Akun
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            val accountSettingsItem = BottomNavItem.PENGATURAN_AKUN
            TabletNavItem(
                item = accountSettingsItem,
                isSelected = selectedItem == accountSettingsItem,
                onClick = { onItemSelected(accountSettingsItem) }
            )
        }
    }
}

@Composable
private fun TabletSidebarMenuItem(
    menuItem: MenuItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        GreenAccent.copy(alpha = 0.1f)
    } else {
        Color.Transparent
    }

    val contentColor = if (isSelected) {
        GreenAccent
    } else {
        TextSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = menuItem.icon,
            contentDescription = menuItem.title,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = menuItem.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor
        )
    }
}

@Composable
private fun TabletNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        GreenAccent.copy(alpha = 0.1f)
    } else {
        Color.Transparent
    }

    val contentColor = if (isSelected) {
        GreenAccent
    } else {
        TextSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor
        )
    }
}
