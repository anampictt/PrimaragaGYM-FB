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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
        lastLogin = authState.currentUser?.lastLogin?.ifBlank { null } ?: "Baru saja"
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
        tonalElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
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
