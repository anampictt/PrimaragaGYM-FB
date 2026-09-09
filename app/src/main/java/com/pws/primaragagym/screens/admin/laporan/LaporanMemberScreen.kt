package com.pws.primaragagym.screens.admin.laporan

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.BackgroundColor
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.CardBackground
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.ChartBarCash
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.ChartBarDefault
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.ChartBarQris
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.ChartBarTransfer
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.GreenAccent
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.GreenLight
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.StatActiveBg
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.StatExpiredBg
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.StatNewBg
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.StatRevenueBg
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.StatSuspendedBg
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.StatTotalBg
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.StatusActive
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.StatusExpiring
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.StatusExpired
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.StatusSuspended
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.TextMuted
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.TextPrimary
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.TextSecondary
import com.pws.primaragagym.ui.theme.Dimens

// ============================================================================
// FILTER TYPES
// ============================================================================
private enum class MemberFilter(val label: String) {
    TODAY("Hari Ini"),
    WEEK("Minggu Ini"),
    MONTH("Bulan Ini"),
    CUSTOM("Custom")
}

// ============================================================================
// MOCK DATA
// ============================================================================
private data class MemberSummaryStats(
    val total: Int = 128,
    val active: Int = 112,
    val newMembers: Int = 16,
    val expired: Int = 8,
    val suspended: Int = 4
)

private data class NewMemberItem(
    val name: String,
    val code: String,
    val joinDate: String,
    val plan: String
)

private data class MemberActivityStats(
    val totalCheckIns: Int = 1245,
    val avgPerDay: Int = 41,
    val mostActiveMember: String = "John Smith"
)

private data class MembershipStatusItem(
    val label: String,
    val count: Int,
    val color: Color
)

private val stats = MemberSummaryStats()
private val newMembers = listOf(
    NewMemberItem("John Smith", "MBR-001", "06 September 2026", "Premium Monthly"),
    NewMemberItem("Sarah Connor", "MBR-002", "05 September 2026", "Basic Monthly"),
    NewMemberItem("Michael Brown", "MBR-003", "04 September 2026", "Annual Premium"),
    NewMemberItem("Emma Wilson", "MBR-005", "03 September 2026", "Premium Monthly"),
    NewMemberItem("David Miller", "MBR-004", "02 September 2026", "Basic Monthly")
)
private val activityStats = MemberActivityStats()
private val membershipStatuses = listOf(
    MembershipStatusItem("Active", 112, StatusActive),
    MembershipStatusItem("Expiring Soon", 8, StatusExpiring),
    MembershipStatusItem("Expired", 6, StatusExpired),
    MembershipStatusItem("Suspended", 2, StatusSuspended)
)

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanMemberScreen(
    onBackClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600
    var selectedFilter by remember { mutableStateOf(MemberFilter.MONTH) }
    var showCustomDate by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Laporan Member",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Ringkasan member berdasarkan periode.",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
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
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing_4)
        ) {
            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_2)
                ) {
                    MemberFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = {
                                selectedFilter = filter
                                showCustomDate = filter == MemberFilter.CUSTOM
                            },
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

            // Custom Date Pickers
            if (showCustomDate) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing_3)) {
                        DateFieldRow(
                            label = "Tanggal Mulai",
                            value = "01 September 2026",
                            onClick = { showStartPicker = !showStartPicker }
                        )
                        DateFieldRow(
                            label = "Tanggal Akhir",
                            value = "30 September 2026",
                            onClick = { showEndPicker = !showEndPicker }
                        )
                    }
                }
            }

            // Summary Stats
            item {
                if (isTablet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
                    ) {
                        StatCard(
                            title = "Total Member",
                            value = "${stats.total}",
                            icon = Icons.Filled.Groups,
                            iconBackground = StatTotalBg,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Member Aktif",
                            value = "${stats.active}",
                            icon = Icons.Filled.TrendingUp,
                            iconBackground = StatActiveBg,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Member Baru",
                            value = "${stats.newMembers}",
                            icon = Icons.Filled.PersonAdd,
                            iconBackground = StatNewBg,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Member Expired",
                            value = "${stats.expired}",
                            icon = Icons.Filled.LocalActivity,
                            iconBackground = StatExpiredBg,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing_3)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
                        ) {
                            StatCard(
                                title = "Total Member",
                                value = "${stats.total}",
                                icon = Icons.Filled.Groups,
                                iconBackground = StatTotalBg,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Member Aktif",
                                value = "${stats.active}",
                                icon = Icons.Filled.TrendingUp,
                                iconBackground = StatActiveBg,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
                        ) {
                            StatCard(
                                title = "Member Baru",
                                value = "${stats.newMembers}",
                                icon = Icons.Filled.PersonAdd,
                                iconBackground = StatNewBg,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Member Expired",
                                value = "${stats.expired}",
                                icon = Icons.Filled.LocalActivity,
                                iconBackground = StatExpiredBg,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Membership Status Breakdown
            item {
                MembershipStatusSection(isTablet = isTablet)
            }

            // Activity Stats
            item {
                ActivityStatsCard(isTablet = isTablet)
            }

            // New Members
            item {
                Text(
                    text = "Member Baru",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            items(newMembers) { member ->
                NewMemberCard(member = member)
            }

            item {
                Spacer(modifier = Modifier.height(Dimens.spacing_4))
            }
        }
    }
}

// ============================================================================
// DATE FIELD ROW
// ============================================================================
@Composable
private fun DateFieldRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            enabled = false,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = TextSecondary
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = TextPrimary,
                disabledBorderColor = Color(0xFFE0E0E0),
                disabledContainerColor = CardBackground,
                disabledTrailingIconColor = TextSecondary
            ),
            shape = RoundedCornerShape(Dimens.input_corner_radius)
        )
    }
}

// ============================================================================
// STAT CARD
// ============================================================================
@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconBackground: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.card_corner_radius),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_4),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    tint = GreenAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(Dimens.spacing_2))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ============================================================================
// MEMBERSHIP STATUS SECTION
// ============================================================================
@Composable
private fun MembershipStatusSection(isTablet: Boolean) {
    Column {
        Text(
            text = "Status Membership",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = Dimens.spacing_3)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimens.card_corner_radius),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.spacing_4)
            ) {
                // Simple horizontal bar
                val total = membershipStatuses.sumOf { it.count }.toFloat()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    membershipStatuses.forEach { status ->
                        val weight = (status.count / total)
                        Box(
                            modifier = Modifier
                                .weight(weight.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(status.color)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_4))

                if (isTablet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        membershipStatuses.forEach { status ->
                            StatusLegendItem(status = status)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing_2)) {
                        membershipStatuses.forEach { status ->
                            StatusLegendItem(status = status)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// STATUS LEGEND ITEM
// ============================================================================
@Composable
private fun StatusLegendItem(status: MembershipStatusItem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(status.color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "${status.label}: ${status.count}",
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary
        )
    }
}

// ============================================================================
// ACTIVITY STATS CARD
// ============================================================================
@Composable
private fun ActivityStatsCard(isTablet: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.card_corner_radius),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_4)
        ) {
            Text(
                text = "Aktivitas Member",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = Dimens.spacing_4)
            )

            if (isTablet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ActivityStatItem(
                        label = "Total Check-in",
                        value = "${activityStats.totalCheckIns}",
                        modifier = Modifier.weight(1f)
                    )
                    ActivityStatItem(
                        label = "Rata-rata / Hari",
                        value = "${activityStats.avgPerDay}",
                        modifier = Modifier.weight(1f)
                    )
                    ActivityStatItem(
                        label = "Member Paling Aktif",
                        value = activityStats.mostActiveMember,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ActivityStatItem(
                        label = "Total Check-in",
                        value = "${activityStats.totalCheckIns}"
                    )
                    ActivityStatItem(
                        label = "Rata-rata / Hari",
                        value = "${activityStats.avgPerDay}"
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.spacing_3))
                ActivityStatItem(
                    label = "Member Paling Aktif",
                    value = activityStats.mostActiveMember
                )
            }
        }
    }
}

// ============================================================================
// ACTIVITY STAT ITEM
// ============================================================================
@Composable
private fun ActivityStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = GreenAccent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ============================================================================
// NEW MEMBER CARD
// ============================================================================
@Composable
private fun NewMemberCard(member: NewMemberItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.input_corner_radius),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_3),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GreenLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name.take(2).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = GreenAccent,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(Dimens.spacing_3))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = member.code,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = member.plan,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = GreenAccent
                )
                Text(
                    text = member.joinDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}