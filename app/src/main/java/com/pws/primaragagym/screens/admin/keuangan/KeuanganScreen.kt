package com.pws.primaragagym.screens.admin.keuangan

import android.app.DatePickerDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.BackgroundColor
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.CardBackground
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.ExpenseBg
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.ExpenseRed
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.GreenAccent
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.GreenLight
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.ProfitBg
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.ProfitBlue
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.StatRevenueBg
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextMuted
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextPrimary
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextSecondary
import com.pws.primaragagym.ui.theme.Dimens
import com.pws.primaragagym.ui.viewmodel.AuthViewModel
import com.pws.primaragagym.ui.viewmodel.FinancialChartPoint
import com.pws.primaragagym.ui.viewmodel.KeuanganUiState
import com.pws.primaragagym.ui.viewmodel.KeuanganViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ============================================================================
// MENU ITEM MODEL & LIST
// ============================================================================
private data class MenuItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val badge: String? = null
)

private val menuItems = listOf(
    MenuItem(
        title = "Catatan Transaksi",
        description = "Catat transaksi pemasukan dan pengeluaran operasional gym.",
        icon = Icons.Filled.Receipt
    ),
    MenuItem(
        title = "Invoice / Kwitansi",
        description = "Lihat dan kelola bukti pembayaran.",
        icon = Icons.Filled.ReceiptLong,
        badge = "3"
    ),
    MenuItem(
        title = "Laporan Pemasukan",
        description = "Lihat ringkasan pemasukan berdasarkan periode.",
        icon = Icons.Filled.Assessment
    )
)

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeuanganScreen(
    viewModel: KeuanganViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onCatatPembayaranClick: () -> Unit = {},
    onInvoiceClick: () -> Unit = {},
    onLaporanClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    val authState by authViewModel.uiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(authState.currentUser) {
        val branchId = authState.currentUser?.branchId ?: ""
        viewModel.loadSummary(branchId)
    }

    val formatCurrency = { amount: Long ->
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0
        format.format(amount)
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Keuangan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Kelola pendapatan, pengeluaran & laba gym",
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
                    containerColor = GreenLight,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = if (isTablet) 32.dp else Dimens.screen_padding_horizontal,
                    vertical = Dimens.spacing_5
                )
        ) {
            // Header Title
            Text(
                text = "Ringkasan Hari Ini",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier.padding(bottom = Dimens.spacing_3)
            )

            // 3 Financial Cards: Pendapatan Hari Ini, Pengeluaran Hari Ini, Laba Bersih Hari Ini
            if (isTablet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
                ) {
                    SummaryMetricCard(
                        title = "Pendapatan Kotor",
                        subtitle = "Hari Ini",
                        value = formatCurrency(uiState.todayRevenue),
                        icon = Icons.Filled.TrendingUp,
                        iconBg = StatRevenueBg,
                        iconTint = GreenAccent,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        title = "Pengeluaran",
                        subtitle = "Hari Ini",
                        value = formatCurrency(uiState.todayExpense),
                        icon = Icons.Filled.TrendingDown,
                        iconBg = ExpenseBg,
                        iconTint = ExpenseRed,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        title = "Laba Bersih",
                        subtitle = "Hari Ini (Net)",
                        value = formatCurrency(uiState.todayNetProfit),
                        icon = Icons.Filled.AccountBalance,
                        iconBg = if (uiState.todayNetProfit >= 0) ProfitBg else ExpenseBg,
                        iconTint = if (uiState.todayNetProfit >= 0) ProfitBlue else ExpenseRed,
                        valueColor = if (uiState.todayNetProfit >= 0) ProfitBlue else ExpenseRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing_3)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
                    ) {
                        SummaryMetricCard(
                            title = "Pendapatan Kotor",
                            subtitle = "Hari Ini",
                            value = formatCurrency(uiState.todayRevenue),
                            icon = Icons.Filled.TrendingUp,
                            iconBg = StatRevenueBg,
                            iconTint = GreenAccent,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMetricCard(
                            title = "Pengeluaran",
                            subtitle = "Hari Ini",
                            value = formatCurrency(uiState.todayExpense),
                            icon = Icons.Filled.TrendingDown,
                            iconBg = ExpenseBg,
                            iconTint = ExpenseRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Laba Bersih Highlight Card
                    HighlightNetProfitCard(
                        netProfit = uiState.todayNetProfit,
                        formattedProfit = formatCurrency(uiState.todayNetProfit),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_6))

            // Interactive Chart Section
            FinancialChartCard(
                uiState = uiState,
                formatCurrency = formatCurrency,
                onFilterSelected = { filter, date ->
                    viewModel.setChartFilter(filter, date)
                }
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_8))

            // Menu Utama Section
            Text(
                text = "Menu Transaksi & Laporan",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPrimary,
                modifier = Modifier.padding(bottom = Dimens.spacing_4)
            )

            val onClicks = listOf(onCatatPembayaranClick, onInvoiceClick, onLaporanClick)
            menuItems.forEachIndexed { index, menuItem ->
                val onClick: () -> Unit = onClicks.getOrElse(index) { {} }
                KeuanganMenuCard(
                    menuItem = menuItem,
                    onClick = onClick
                )
                if (index < menuItems.lastIndex) {
                    Spacer(modifier = Modifier.height(Dimens.spacing_3))
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_6))
        }
    }
}

// ============================================================================
// METRIC CARD COMPONENT
// ============================================================================
@Composable
private fun SummaryMetricCard(
    title: String,
    subtitle: String,
    value: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    valueColor: Color = TextPrimary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextSecondary,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = valueColor,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

// ============================================================================
// HIGHLIGHT NET PROFIT CARD (PHONE VIEW)
// ============================================================================
@Composable
private fun HighlightNetProfitCard(
    netProfit: Long,
    formattedProfit: String,
    modifier: Modifier = Modifier
) {
    val isPositive = netProfit >= 0
    val cardBg = if (isPositive) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
    val accentColor = if (isPositive) Color(0xFF16A34A) else Color(0xFFDC2626)
    val iconBg = if (isPositive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Laba Bersih Hari Ini",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = TextSecondary
                    )
                    Text(
                        text = if (isPositive) "Surplus (Untung)" else "Defisit (Rugi)",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = formattedProfit,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = accentColor
            )
        }
    }
}

// ============================================================================
// FINANCIAL CHART CARD
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FinancialChartCard(
    uiState: KeuanganUiState,
    formatCurrency: (Long) -> String,
    onFilterSelected: (String, Date?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selected = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }.time
                onFilterSelected("PILIH_TANGGAL", selected)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val selectedDateLabel = remember(uiState.selectedCustomDate) {
        uiState.selectedCustomDate?.let {
            SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(it)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with Chart Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShowChart,
                        contentDescription = null,
                        tint = GreenAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Grafik Pendapatan & Pengeluaran",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Tren perbandingan cash flow menurut periode",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips Scrollable Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = uiState.chartFilter == "HARI_INI",
                    onClick = { onFilterSelected("HARI_INI", null) },
                    label = { Text("Hari Ini", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenAccent,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = uiState.chartFilter == "7_HARI",
                    onClick = { onFilterSelected("7_HARI", null) },
                    label = { Text("7 Hari Terakhir", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenAccent,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = uiState.chartFilter == "BULAN_INI",
                    onClick = { onFilterSelected("BULAN_INI", null) },
                    label = { Text("Bulan Ini", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenAccent,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = uiState.chartFilter == "PILIH_TANGGAL",
                    onClick = { datePickerDialog.show() },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.chartFilter == "PILIH_TANGGAL" && selectedDateLabel != null)
                                    selectedDateLabel else "Pilih Tanggal",
                                fontSize = 12.sp
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenAccent,
                        selectedLabelColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Period Summary Stats Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BackgroundColor)
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Total Pemasukan", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = formatCurrency(uiState.filteredTotalIncome),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenAccent
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Pengeluaran", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = formatCurrency(uiState.filteredTotalExpense),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Laba Bersih", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = formatCurrency(uiState.filteredNetProfit),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.filteredNetProfit >= 0) ProfitBlue else ExpenseRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Visual Line Chart
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenAccent, modifier = Modifier.size(32.dp))
                }
            } else if (uiState.chartPoints.isEmpty() || (uiState.filteredTotalIncome == 0L && uiState.filteredTotalExpense == 0L)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada catatan transaksi pada periode ini",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            } else {
                LineChartCanvas(points = uiState.chartPoints)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(GreenAccent)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pemasukan", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)

                Spacer(modifier = Modifier.width(24.dp))

                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(ExpenseRed)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pengeluaran", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ============================================================================
// LINE CHART COMPONENT
// ============================================================================
@Composable
private fun LineChartCanvas(
    points: List<FinancialChartPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val maxAmount = remember(points) {
        val maxVal = points.maxOfOrNull { maxOf(it.income, it.expense) } ?: 0L
        if (maxVal > 0) maxVal else 1L
    }

    val chartHeight = 140.dp

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .padding(horizontal = 8.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val topPadding = 14.dp.toPx()
            val bottomPadding = 14.dp.toPx()
            val sidePadding = 16.dp.toPx()
            val availableWidth = canvasWidth - (sidePadding * 2f)
            val availableHeight = canvasHeight - topPadding - bottomPadding
            val n = points.size

            fun getX(index: Int): Float {
                return if (n > 1) {
                    sidePadding + index * (availableWidth / (n - 1).toFloat())
                } else {
                    canvasWidth / 2f
                }
            }

            fun getY(amount: Long): Float {
                val ratio = (amount.toFloat() / maxAmount).coerceIn(0f, 1f)
                return topPadding + availableHeight * (1f - ratio)
            }

            // 3 Horizontal Light Grid Lines
            val gridColor = Color(0xFFEEEEEE)
            drawLine(
                color = gridColor,
                start = Offset(sidePadding, topPadding),
                end = Offset(canvasWidth - sidePadding, topPadding),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = gridColor,
                start = Offset(sidePadding, topPadding + availableHeight / 2f),
                end = Offset(canvasWidth - sidePadding, topPadding + availableHeight / 2f),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = gridColor,
                start = Offset(sidePadding, topPadding + availableHeight),
                end = Offset(canvasWidth - sidePadding, topPadding + availableHeight),
                strokeWidth = 1.dp.toPx()
            )

            // Draw subtle gradient area under Income curve
            if (n > 1) {
                val incomeAreaPath = Path().apply {
                    moveTo(getX(0), topPadding + availableHeight)
                    for (i in 0 until n) {
                        lineTo(getX(i), getY(points[i].income))
                    }
                    lineTo(getX(n - 1), topPadding + availableHeight)
                    close()
                }
                drawPath(
                    path = incomeAreaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GreenAccent.copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        startY = topPadding,
                        endY = topPadding + availableHeight
                    )
                )
            }

            // Income Line (Green)
            val incomePath = Path()
            for (i in 0 until n) {
                val x = getX(i)
                val y = getY(points[i].income)
                if (i == 0) incomePath.moveTo(x, y) else incomePath.lineTo(x, y)
            }
            drawPath(
                path = incomePath,
                color = GreenAccent,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Expense Line (Red)
            val expensePath = Path()
            for (i in 0 until n) {
                val x = getX(i)
                val y = getY(points[i].expense)
                if (i == 0) expensePath.moveTo(x, y) else expensePath.lineTo(x, y)
            }
            drawPath(
                path = expensePath,
                color = ExpenseRed,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Circular Dots on Data Points
            for (i in 0 until n) {
                val x = getX(i)
                val incY = getY(points[i].income)
                val expY = getY(points[i].expense)

                // Income Dot (White outer ring + Green center)
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(x, incY)
                )
                drawCircle(
                    color = GreenAccent,
                    radius = 3.5.dp.toPx(),
                    center = Offset(x, incY)
                )

                // Expense Dot (White outer ring + Red center)
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(x, expY)
                )
                drawCircle(
                    color = ExpenseRed,
                    radius = 3.5.dp.toPx(),
                    center = Offset(x, expY)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // X-Axis Labels Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { point ->
                Text(
                    text = point.label,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ============================================================================
// MENU CARD
// ============================================================================
@Composable
private fun KeuanganMenuCard(
    menuItem: MenuItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(GreenLight),
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

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = menuItem.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextPrimary
                    )
                    if (menuItem.badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(GreenAccent)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = menuItem.badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = menuItem.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(Dimens.spacing_2))

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = GreenAccent,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}