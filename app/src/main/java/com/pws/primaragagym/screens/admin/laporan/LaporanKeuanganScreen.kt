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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.BackgroundColor
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.CardBackground
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.CashBg
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.ChartBarCash
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.ChartBarDefault
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.ChartBarQris
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.ChartBarTransfer
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.GreenAccent
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.QrisBg
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.StatRevenueBg
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.StatTransactionBg
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.TextMuted
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.TextPrimary
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.TextSecondary
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.TransferBg
import com.pws.primaragagym.ui.theme.Dimens

// ============================================================================
// FILTER TYPES
// ============================================================================
private enum class LaporanKeuanganFilter(val label: String) {
    TODAY("Hari Ini"),
    WEEK("Minggu Ini"),
    MONTH("Bulan Ini"),
    CUSTOM("Custom")
}

// ============================================================================
// MOCK DATA
// ============================================================================
private data class MethodSummary(
    val method: String,
    val icon: ImageVector,
    val amount: String,
    val amountRaw: Long,
    val backgroundColor: Color,
    val barColor: Color
)

private data class DailyIncomeItem(
    val day: String,
    val date: Int,
    val amount: String,
    val amountRaw: Long
)

private data class PaymentTypeSummary(
    val type: String,
    val amount: String,
    val amountRaw: Long
)

private val methodSummaries = listOf(
    MethodSummary("Cash", Icons.Filled.Wallet, "Rp 10.500.000", 10_500_000, CashBg, ChartBarCash),
    MethodSummary("Transfer", Icons.Filled.AccountBalance, "Rp 15.000.000", 15_000_000, TransferBg, ChartBarTransfer),
    MethodSummary("QRIS", Icons.Filled.QrCode2, "Rp 13.000.000", 13_000_000, QrisBg, ChartBarQris)
)

private val dailyIncomes = listOf(
    DailyIncomeItem("Sen", 1, "Rp 1.200.000", 1_200_000),
    DailyIncomeItem("Sel", 2, "Rp 950.000", 950_000),
    DailyIncomeItem("Rab", 3, "Rp 1.750.000", 1_750_000),
    DailyIncomeItem("Kam", 4, "Rp 1.400.000", 1_400_000),
    DailyIncomeItem("Jum", 5, "Rp 2.100.000", 2_100_000),
    DailyIncomeItem("Sab", 6, "Rp 1.800.000", 1_800_000),
    DailyIncomeItem("Min", 7, "Rp 1.100.000", 1_100_000)
)

private val paymentTypeSummaries = listOf(
    PaymentTypeSummary("Membership Baru", "Rp 15.000.000", 15_000_000),
    PaymentTypeSummary("Perpanjang Membership", "Rp 18.500.000", 18_500_000),
    PaymentTypeSummary("Upgrade Membership", "Rp 5.000.000", 5_000_000),
    PaymentTypeSummary("Lainnya", "Rp 0", 0)
)

private val months = listOf(
    "Januari 2026", "Februari 2026", "Maret 2026",
    "April 2026", "Mei 2026", "Juni 2026",
    "Juli 2026", "Agustus 2026", "September 2026",
    "Oktober 2026", "November 2026", "Desember 2026"
)

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanKeuanganScreen(
    onBackClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    var selectedFilter by remember { mutableStateOf(LaporanKeuanganFilter.MONTH) }
    var selectedMonthIndex by remember { mutableIntStateOf(8) } // September 2026

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Laporan Keuangan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Ringkasan pemasukan berdasarkan periode.",
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
            // Month Selector
            item {
                MonthSelector(
                    currentMonth = months[selectedMonthIndex],
                    onPrevMonth = { if (selectedMonthIndex > 0) selectedMonthIndex-- },
                    onNextMonth = { if (selectedMonthIndex < months.lastIndex) selectedMonthIndex++ },
                    onMonthSelected = { index -> selectedMonthIndex = index }
                )
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_2)
                ) {
                    LaporanKeuanganFilter.entries.forEach { filter ->
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

            // Summary Cards
            item {
                if (isTablet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
                    ) {
                        LaporanStatCard(
                            title = "Total Pemasukan",
                            value = "Rp 38.500.000",
                            icon = Icons.Filled.AccountBalanceWallet,
                            iconBackground = StatRevenueBg,
                            modifier = Modifier.weight(1f)
                        )
                        LaporanStatCard(
                            title = "Total Transaksi",
                            value = "124",
                            icon = Icons.Filled.Receipt,
                            iconBackground = StatTransactionBg,
                            modifier = Modifier.weight(1f)
                        )
                        LaporanStatCard(
                            title = "Rata-rata Transaksi",
                            value = "Rp310.484",
                            icon = Icons.Filled.TrendingUp,
                            iconBackground = CashBg,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing_3)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
                        ) {
                            LaporanStatCard(
                                title = "Total Pemasukan",
                                value = "Rp 38.500.000",
                                icon = Icons.Filled.AccountBalanceWallet,
                                iconBackground = StatRevenueBg,
                                modifier = Modifier.weight(1f)
                            )
                            LaporanStatCard(
                                title = "Total Transaksi",
                                value = "124",
                                icon = Icons.Filled.Receipt,
                                iconBackground = StatTransactionBg,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        LaporanStatCard(
                            title = "Rata-rata Transaksi",
                            value = "Rp310.484",
                            icon = Icons.Filled.TrendingUp,
                            iconBackground = CashBg,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Section: Pemasukan Berdasarkan Metode
            item {
                Text(
                    text = "Pemasukan Berdasarkan Metode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            item {
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
                        // Horizontal bar chart
                        SimpleHorizontalBarChart(
                            items = methodSummaries,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacing_4))
                        if (isTablet) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                methodSummaries.forEach { method ->
                                    MethodLegendItem(method = method)
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing_3)) {
                                methodSummaries.forEach { method ->
                                    MethodLegendItem(method = method)
                                }
                            }
                        }
                    }
                }
            }

            // Section: Ringkasan Jenis Pembayaran
            item {
                Text(
                    text = "Ringkasan Jenis Pembayaran",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = Dimens.spacing_2)
                )
            }

            item {
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
                        val maxAmount = paymentTypeSummaries.maxOfOrNull { it.amountRaw } ?: 1L
                        paymentTypeSummaries.forEachIndexed { index, item ->
                            PaymentTypeBar(
                                type = item.type,
                                amount = item.amount,
                                percentage = if (maxAmount > 0) item.amountRaw.toFloat() / maxAmount else 0f,
                                color = when (index) {
                                    0 -> ChartBarTransfer
                                    1 -> GreenAccent
                                    2 -> ChartBarCash
                                    else -> ChartBarDefault
                                }
                            )
                            if (index < paymentTypeSummaries.lastIndex) {
                                Spacer(modifier = Modifier.height(Dimens.spacing_3))
                            }
                        }
                    }
                }
            }

            // Section: Pemasukan Harian
            item {
                Text(
                    text = "Pemasukan Harian",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = Dimens.spacing_2)
                )
            }

            item {
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
                        // Simple bar chart
                        SimpleBarChart(
                            items = dailyIncomes,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacing_3))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            dailyIncomes.forEach { item ->
                                Text(
                                    text = "${item.date}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(Dimens.spacing_4))
            }
        }
    }
}

// ============================================================================
// MONTH SELECTOR
// ============================================================================
@Composable
private fun MonthSelector(
    currentMonth: String,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthSelected: (Int) -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }
    val selectedIndex = months.indexOf(currentMonth).coerceAtLeast(0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.card_corner_radius),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrevMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Bulan sebelumnya",
                    tint = GreenAccent
                )
            }

            Box {
                OutlinedTextField(
                    value = currentMonth,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.clickableDropdown(showDropdown) { showDropdown = !showDropdown },
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
                        disabledBorderColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        disabledTrailingIconColor = TextSecondary
                    ),
                    textStyle = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false }
                ) {
                    months.forEachIndexed { index, month ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = month,
                                    fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                                    color = if (index == selectedIndex) GreenAccent else TextPrimary
                                )
                            },
                            onClick = {
                                onMonthSelected(index)
                                showDropdown = false
                            }
                        )
                    }
                }
            }

            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Bulan berikutnya",
                    tint = GreenAccent
                )
            }
        }
    }
}

// Helper for clickable dropdown
@Composable
private fun Modifier.clickableDropdown(
    showDropdown: Boolean,
    onToggle: () -> Unit
): Modifier = this.then(
    Modifier.clickable(onClick = onToggle)
)

// ============================================================================
// LAPORAN STAT CARD
// ============================================================================
@Composable
private fun LaporanStatCard(
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_4),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(Dimens.spacing_3))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
    }
}

// ============================================================================
// SIMPLE HORIZONTAL BAR CHART
// ============================================================================
@Composable
private fun SimpleHorizontalBarChart(
    items: List<MethodSummary>,
    modifier: Modifier = Modifier
) {
    val total = items.sumOf { it.amountRaw }.toFloat()
    if (total == 0f) return

    Canvas(modifier = modifier) {
        var xOffset = 0f
        items.forEach { item ->
            val barWidth = (item.amountRaw / total) * size.width
            drawRect(
                color = item.barColor,
                topLeft = Offset(xOffset, 0f),
                size = Size(barWidth, size.height)
            )
            xOffset += barWidth
        }
    }
}

// ============================================================================
// METHOD LEGEND ITEM
// ============================================================================
@Composable
private fun MethodLegendItem(method: MethodSummary) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(method.barColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = method.method,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text = method.amount,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

// ============================================================================
// PAYMENT TYPE BAR
// ============================================================================
@Composable
private fun PaymentTypeBar(
    type: String,
    amount: String,
    percentage: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = type,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(ChartBarDefault.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

// ============================================================================
// SIMPLE BAR CHART
// ============================================================================
@Composable
private fun SimpleBarChart(
    items: List<DailyIncomeItem>,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    val maxValue = items.maxOfOrNull { it.amountRaw }?.toFloat() ?: 1f

    Canvas(modifier = modifier) {
        val barWidth = (size.width / items.size) * 0.6f
        val spacing = (size.width / items.size) * 0.4f

        items.forEachIndexed { index, item ->
            val barHeight = (item.amountRaw / maxValue) * size.height
            val x = index * (barWidth + spacing) + spacing / 2
            val y = size.height - barHeight

            // Bar
            drawRoundRect(
                color = GreenAccent,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
        }
    }
}