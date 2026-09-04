package com.pws.primaragagym.screens.admin.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pws.primaragagym.ui.theme.DashboardPrimary
import com.pws.primaragagym.ui.theme.DashboardTextSecondary
import com.pws.primaragagym.ui.theme.Dimens
import com.pws.primaragagym.ui.theme.GreenPrimary
import com.pws.primaragagym.ui.theme.GreenPrimaryDark
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ============================================================================
// COLORS - Match Super Admin Dashboard design reference
// ============================================================================
private val BackgroundColor = Color(0xFFF5F7FA)
private val HeaderBackgroundColor = Color(0xFFE8F5E9)
private val CardBackground = Color.White
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B6B6B)
private val TextMuted = Color(0xFF9E9E9E)
private val IconBackground = Color(0xFFE8F5E9)
private val GreenAccent = Color(0xFF32A060)
private val GreenStatus = Color(0xFF4CAF50)
private val StatMembersBg = Color(0xFFE8F5E9)
private val StatRevenueBg = Color(0xFFFFF3E0)
private val StatNewMemberBg = Color(0xFFE3F2FD)

// ============================================================================
// MOCK DATA - Admin specific data
// ============================================================================
private data class AdminDashboardData(
    val greeting: String = "Selamat datang kembali,",
    val title: String = "Admin",
    val subtitle: String = "Kelola gym Anda dengan mudah dan efisien.",
    val summaryDate: String = "",
    val totalActiveMembers: Int = 128,
    val todayRevenue: String = "2.6 Jt",
    val newMembersToday: Int = 8,
    val dailyCheckIns: List<DailyCheckInData> = emptyList()
)

private data class DailyCheckInData(
    val day: String,
    val count: Int
)

private val adminDashboardData = AdminDashboardData(
    summaryDate = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy")),
    dailyCheckIns = listOf(
        DailyCheckInData("Sen", 18),
        DailyCheckInData("Sel", 24),
        DailyCheckInData("Rab", 15),
        DailyCheckInData("Kam", 27),
        DailyCheckInData("Jum", 31),
        DailyCheckInData("Sab", 22),
        DailyCheckInData("Min", 12)
    )
)

// ============================================================================
// MENU DATA - Admin specific menu items
// ============================================================================
private data class MenuItem(
    val title: String,
    val description: String,
    val icon: ImageVector
)

private val adminMenuItems = listOf(
    MenuItem(
        title = "Member",
        description = "Kelola data member gym Anda.",
        icon = Icons.Filled.Groups
    ),
    MenuItem(
        title = "Check In & Check Out",
        description = "Scan barcode member untuk\nproses check-in dan check-out.",
        icon = Icons.Filled.QrCodeScanner
    ),
    MenuItem(
        title = "Catatan Keuangan",
        description = "Catat dan kelola transaksi\nkeuangan gym.",
        icon = Icons.Filled.AccountBalanceWallet
    ),
    MenuItem(
        title = "Notifikasi",
        description = "Lihat informasi dan\npemberitahuan terbaru.",
        icon = Icons.Filled.Notifications
    ),
    MenuItem(
        title = "Laporan Keuangan",
        description = "Lihat laporan pemasukan dan\nkeuangan gym.",
        icon = Icons.Filled.Assessment
    )
)

// ============================================================================
// BOTTOM NAVIGATION DATA - Admin specific
// ============================================================================
private enum class AdminBottomNavItem(
    val label: String,
    val icon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Filled.Home),
    MEMBER("Member", Icons.Filled.Person),
    CHECK_IN("Check In", Icons.Filled.QrCodeScanner),
    KEUANGAN("Keuangan", Icons.Filled.AccountBalanceWallet),
    PROFIL("Profil", Icons.Filled.Settings)
}

// ============================================================================
// MAIN SCREEN
// ============================================================================
@Composable
fun AdminDashboardScreen(
    onMemberClick: () -> Unit = {},
    onCheckInOutClick: () -> Unit = {},
    onFinanceClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onReportClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    val data = remember { adminDashboardData }
    var selectedNavItem by remember { mutableStateOf(AdminBottomNavItem.DASHBOARD) }

    if (isTablet) {
        TabletAdminDashboardLayout(
            data = data,
            selectedNavItem = selectedNavItem,
            onNavItemSelected = { selectedNavItem = it },
            onMemberClick = onMemberClick,
            onCheckInOutClick = onCheckInOutClick,
            onFinanceClick = onFinanceClick,
            onNotificationClick = onNotificationClick,
            onReportClick = onReportClick,
            onProfileClick = onProfileClick,
            onDashboardNavClick = { selectedNavItem = AdminBottomNavItem.DASHBOARD }
        )
    } else {
        PhoneAdminDashboardLayout(
            data = data,
            selectedNavItem = selectedNavItem,
            onNavItemSelected = { selectedNavItem = it },
            onMemberClick = onMemberClick,
            onCheckInOutClick = onCheckInOutClick,
            onFinanceClick = onFinanceClick,
            onNotificationClick = onNotificationClick,
            onReportClick = onReportClick,
            onProfileClick = onProfileClick
        )
    }
}

// ============================================================================
// PHONE LAYOUT
// ============================================================================
@Composable
private fun PhoneAdminDashboardLayout(
    data: AdminDashboardData,
    selectedNavItem: AdminBottomNavItem,
    onNavItemSelected: (AdminBottomNavItem) -> Unit,
    onMemberClick: () -> Unit,
    onCheckInOutClick: () -> Unit,
    onFinanceClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onReportClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundColor,
        bottomBar = {
            AdminBottomNavigationBar(
                selectedItem = selectedNavItem,
                onItemSelected = onNavItemSelected,
                onNavigate = { item ->
                    when (item) {
                        AdminBottomNavItem.MEMBER -> onMemberClick()
                        AdminBottomNavItem.CHECK_IN -> onCheckInOutClick()
                        AdminBottomNavItem.KEUANGAN -> onFinanceClick()
                        AdminBottomNavItem.PROFIL -> onProfileClick()
                        else -> {}
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Section
            item {
                AdminDashboardHeader(data = data)
            }

            // Content Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.screen_padding_horizontal)
                        .padding(top = Dimens.screen_padding_vertical)
                ) {
                    // Summary Card
                    AdminSummaryCard(data = data)

                    Spacer(modifier = Modifier.height(Dimens.spacing_6))

                    // Check-in Chart Section
                    CheckInChartSection(data = data)

                    Spacer(modifier = Modifier.height(Dimens.spacing_6))

                    // Menu Utama Section
                    AdminMenuUtamaSection(
                        menuItems = adminMenuItems,
                        onMemberClick = onMemberClick,
                        onCheckInOutClick = onCheckInOutClick,
                        onFinanceClick = onFinanceClick,
                        onNotificationClick = onNotificationClick,
                        onReportClick = onReportClick
                    )

                    Spacer(modifier = Modifier.height(Dimens.spacing_6))
                }
            }
        }
    }
}

// ============================================================================
// TABLET LAYOUT
// ============================================================================
@Composable
private fun TabletAdminDashboardLayout(
    data: AdminDashboardData,
    selectedNavItem: AdminBottomNavItem,
    onNavItemSelected: (AdminBottomNavItem) -> Unit,
    onMemberClick: () -> Unit,
    onCheckInOutClick: () -> Unit,
    onFinanceClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onReportClick: () -> Unit,
    onProfileClick: () -> Unit,
    onDashboardNavClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // Sidebar Navigation
        AdminTabletSidebar(
            selectedItem = selectedNavItem,
            onItemSelected = onNavItemSelected,
            onMemberClick = onMemberClick,
            onCheckInOutClick = onCheckInOutClick,
            onFinanceClick = onFinanceClick,
            onProfileClick = onProfileClick,
            onDashboardClick = onDashboardNavClick
        )

        // Main Content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(horizontal = Dimens.spacing_8)
                .padding(top = Dimens.spacing_8)
        ) {
            // Header Section
            item {
                AdminDashboardHeader(
                    data = data,
                    isTablet = true
                )
            }

            // Content Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.spacing_6)
                ) {
                    // Summary Card
                    AdminSummaryCard(
                        data = data,
                        isTablet = true
                    )

                    Spacer(modifier = Modifier.height(Dimens.spacing_6))

                    // Check-in Chart Section
                    CheckInChartSection(
                        data = data,
                        isTablet = true
                    )

                    Spacer(modifier = Modifier.height(Dimens.spacing_6))

                    // Menu Utama Section
                    AdminMenuUtamaSection(
                        menuItems = adminMenuItems,
                        onMemberClick = onMemberClick,
                        onCheckInOutClick = onCheckInOutClick,
                        onFinanceClick = onFinanceClick,
                        onNotificationClick = onNotificationClick,
                        onReportClick = onReportClick,
                        isTablet = true
                    )

                    Spacer(modifier = Modifier.height(Dimens.spacing_6))
                }
            }
        }
    }
}

// ============================================================================
// DASHBOARD HEADER - Matches Super Admin Dashboard header
// ============================================================================
@Composable
private fun AdminDashboardHeader(
    data: AdminDashboardData,
    isTablet: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderBackgroundColor)
            .padding(
                start = Dimens.screen_padding_horizontal,
                end = Dimens.screen_padding_horizontal,
                top = Dimens.spacing_4,
                bottom = Dimens.spacing_6
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
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (isTablet) 32.sp else 28.sp
                    ),
                    color = TextPrimary
                )
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
                        .background(Color.White),
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

                Spacer(modifier = Modifier.width(12.dp))

            }
        }
    }
}

// ============================================================================
// SUMMARY CARD - Admin specific statistics
// ============================================================================
@Composable
private fun AdminSummaryCard(
    data: AdminDashboardData,
    isTablet: Boolean = false
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isNarrowScreen = screenWidth < 360

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.card_corner_radius),
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
                .padding(Dimens.spacing_5)
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

            Spacer(modifier = Modifier.height(Dimens.spacing_5))

            // Statistics — vertical on very narrow screens, horizontal otherwise
            if (isNarrowScreen) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing_4)
                ) {
                    AdminStatisticItem(
                        value = "${data.totalActiveMembers}",
                        label = "Total Member Aktif",
                        icon = Icons.Filled.Groups,
                        iconBackgroundColor = StatMembersBg
                    )
                    AdminStatisticItem(
                        value = "Rp ${data.todayRevenue}",
                        label = "Pendapatan Hari Ini",
                        icon = Icons.Filled.Payments,
                        iconBackgroundColor = StatRevenueBg
                    )
                    AdminStatisticItem(
                        value = "${data.newMembersToday}",
                        label = "Member Baru Hari Ini",
                        icon = Icons.Filled.PersonAdd,
                        iconBackgroundColor = StatNewMemberBg
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AdminStatisticItem(
                        value = "${data.totalActiveMembers}",
                        label = "Total Member Aktif",
                        icon = Icons.Filled.Groups,
                        iconBackgroundColor = StatMembersBg
                    )
                    AdminStatisticItem(
                        value = "Rp ${data.todayRevenue}",
                        label = "Pendapatan Hari Ini",
                        icon = Icons.Filled.Payments,
                        iconBackgroundColor = StatRevenueBg
                    )
                    AdminStatisticItem(
                        value = "${data.newMembersToday}",
                        label = "Member Baru Hari Ini",
                        icon = Icons.Filled.PersonAdd,
                        iconBackgroundColor = StatNewMemberBg
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminStatisticItem(
    value: String,
    label: String,
    icon: ImageVector = Icons.Filled.Person,
    iconBackgroundColor: Color = IconBackground
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GreenAccent,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Value
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = TextPrimary,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ============================================================================
// CHECK-IN CHART SECTION
// ============================================================================
@Composable
private fun CheckInChartSection(
    data: AdminDashboardData,
    isTablet: Boolean = false
) {
    Column {
        // Section Title
        Text(
            text = "Check-in Harian",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "Jumlah check-in member dalam 7 hari terakhir",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = Dimens.spacing_4)
        )

        // Chart Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimens.card_corner_radius),
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
                    .padding(Dimens.spacing_5)
            ) {
                // Simple Line Chart
                CheckInLineChart(
                    data = data.dailyCheckIns,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )

                Spacer(modifier = Modifier.height(Dimens.spacing_4))

                // Day Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    data.dailyCheckIns.forEach { checkInData ->
                        Text(
                            text = checkInData.day,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// SIMPLE LINE CHART - Green styled
// ============================================================================
@Composable
private fun CheckInLineChart(
    data: List<DailyCheckInData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOfOrNull { it.count }?.toFloat() ?: 1f

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 20f

        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        // Draw grid lines
        val gridColor = Color(0xFFE0E0E0)
        for (i in 0..4) {
            val y = padding + (chartHeight / 4) * i
            drawLine(
                color = gridColor,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1f
            )
        }

        // Calculate points
        val points = data.mapIndexed { index, item ->
            val x = padding + (chartWidth / (data.size - 1)) * index
            val y = padding + chartHeight - (item.count / maxValue) * chartHeight
            Offset(x, y)
        }

        // Draw line with smooth curve
        val path = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    val midX = (prev.x + curr.x) / 2
                    quadraticTo(prev.x, prev.y, midX, (prev.y + curr.y) / 2)
                    if (i == points.size - 1) {
                        quadraticTo(curr.x, curr.y, curr.x, curr.y)
                    }
                }
            }
        }

        drawPath(
            path = path,
            color = GreenAccent,
            style = Stroke(
                width = 3f,
                cap = StrokeCap.Round
            )
        )

        // Draw points
        points.forEach { point ->
            // Outer circle
            drawCircle(
                color = GreenAccent.copy(alpha = 0.3f),
                radius = 12f,
                center = point
            )
            // Inner circle
            drawCircle(
                color = GreenAccent,
                radius = 6f,
                center = point
            )
            // White center
            drawCircle(
                color = Color.White,
                radius = 3f,
                center = point
            )
        }
    }
}

// ============================================================================
// MENU UTAMA SECTION - Admin specific menu
// ============================================================================
@Composable
private fun AdminMenuUtamaSection(
    menuItems: List<MenuItem>,
    onMemberClick: () -> Unit,
    onCheckInOutClick: () -> Unit,
    onFinanceClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onReportClick: () -> Unit,
    isTablet: Boolean = false
) {
    Column {
        // Section Title
        Text(
            text = "Menu Utama",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = TextPrimary,
            modifier = Modifier.padding(bottom = Dimens.spacing_4)
        )

        // Menu Items
        val onClicks = listOf(
            onMemberClick,
            onCheckInOutClick,
            onFinanceClick,
            onNotificationClick,
            onReportClick
        )

        menuItems.forEachIndexed { index, menuItem ->
            AdminMenuCard(
                menuItem = menuItem,
                onClick = onClicks[index]
            )
            if (index < menuItems.lastIndex) {
                Spacer(modifier = Modifier.height(Dimens.spacing_3))
            }
        }
    }
}

@Composable
private fun AdminMenuCard(
    menuItem: MenuItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Dimens.card_corner_radius),
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
                .padding(Dimens.spacing_4),
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

            Spacer(modifier = Modifier.width(Dimens.spacing_4))

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

            Spacer(modifier = Modifier.width(Dimens.spacing_2))

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
// BOTTOM NAVIGATION BAR
// ============================================================================
@Composable
private fun AdminBottomNavigationBar(
    selectedItem: AdminBottomNavItem,
    onItemSelected: (AdminBottomNavItem) -> Unit,
    onNavigate: (AdminBottomNavItem) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        AdminBottomNavItem.entries.forEach { item ->
            val isSelected = selectedItem == item
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    onItemSelected(item)
                    onNavigate(item)
                },
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
private fun AdminTabletSidebar(
    selectedItem: AdminBottomNavItem,
    onItemSelected: (AdminBottomNavItem) -> Unit,
    onMemberClick: () -> Unit,
    onCheckInOutClick: () -> Unit,
    onFinanceClick: () -> Unit,
    onProfileClick: () -> Unit,
    onDashboardClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(Color.White)
            .padding(vertical = Dimens.spacing_6),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Column(
            modifier = Modifier.padding(horizontal = Dimens.spacing_6),
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

        Spacer(modifier = Modifier.height(Dimens.spacing_8))

        // Navigation Items
        Column(
            modifier = Modifier.padding(horizontal = Dimens.spacing_4)
        ) {
            AdminBottomNavItem.entries.forEach { item ->
                val isSelected = selectedItem == item
                val onNavigate: () -> Unit = when (item) {
                    AdminBottomNavItem.DASHBOARD -> onDashboardClick
                    AdminBottomNavItem.MEMBER -> onMemberClick
                    AdminBottomNavItem.CHECK_IN -> onCheckInOutClick
                    AdminBottomNavItem.KEUANGAN -> onFinanceClick
                    AdminBottomNavItem.PROFIL -> onProfileClick
                }
                AdminTabletNavItem(
                    item = item,
                    isSelected = isSelected,
                    onSelect = { onItemSelected(item) },
                    onNavigate = onNavigate
                )
                if (item != AdminBottomNavItem.entries.last()) {
                    Spacer(modifier = Modifier.height(Dimens.spacing_2))
                }
            }
        }
    }
}

@Composable
private fun AdminTabletNavItem(
    item: AdminBottomNavItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onNavigate: () -> Unit
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
            .clickable {
                onSelect()
                onNavigate()
            }
            .padding(horizontal = Dimens.spacing_4, vertical = Dimens.spacing_3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(Dimens.spacing_3))
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor
        )
    }
}
