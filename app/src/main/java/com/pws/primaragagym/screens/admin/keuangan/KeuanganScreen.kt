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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.widget.Toast
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.text.style.TextOverflow
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
    ),
    MenuItem(
        title = "Laporan Pemasukan",
        description = "Lihat ringkasan pemasukan berdasarkan periode.",
        icon = Icons.Filled.Assessment
    ),
    MenuItem(
        title = "Export Laporan",
        description = "Unduh & bagikan laporan keuangan ke format PDF atau Excel.",
        icon = Icons.Filled.Download
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
    var showExportDialog by remember { mutableStateOf(false) }

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
                viewModel.setChartFilter("PILIH_TANGGAL", selected)
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

    val periodSubtitle = when (uiState.chartFilter) {
        "HARI_INI" -> "Hari Ini"
        "7_HARI" -> "7 Hari Terakhir"
        "BULAN_INI" -> "Bulan Ini"
        "PILIH_TANGGAL" -> selectedDateLabel ?: "Pilih Tanggal"
        else -> "Hari Ini"
    }

    LaunchedEffect(authState.currentUser) {
        val branchId = authState.currentUser?.branchId ?: ""
        viewModel.loadSummary(branchId)
    }

    val formatCurrency = { amount: Long ->
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0
        format.format(amount)
    }

    // 3 Financial Cards: Pendapatan, Pengeluaran, Laba Bersih (mengikuti filter yang dipilih)
    val displayIncome = if (uiState.chartFilter == "HARI_INI" && uiState.filteredTotalIncome == 0L && uiState.todayRevenue > 0L) {
        uiState.todayRevenue
    } else {
        uiState.filteredTotalIncome
    }
    val displayExpense = if (uiState.chartFilter == "HARI_INI" && uiState.filteredTotalExpense == 0L && uiState.todayExpense > 0L) {
        uiState.todayExpense
    } else {
        uiState.filteredTotalExpense
    }
    val displayProfit = if (uiState.chartFilter == "HARI_INI" && uiState.filteredTotalIncome == 0L && uiState.filteredTotalExpense == 0L && (uiState.todayRevenue > 0L || uiState.todayExpense > 0L)) {
        uiState.todayNetProfit
    } else {
        uiState.filteredNetProfit
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
            // Header Title & Active Period Badge & Export Button (Responsive)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.spacing_3),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ringkasan Keuangan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Periode: $periodSubtitle",
                        style = MaterialTheme.typography.labelSmall,
                        color = GreenAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = { showExportDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    border = BorderStroke(1.dp, GreenAccent.copy(alpha = 0.6f)),
                    modifier = Modifier.height(30.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GreenAccent
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = null,
                        tint = GreenAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Export",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GreenAccent
                    )
                }
            }

            // Filter Chips Scrollable Row (Diletakkan di Ringkasan)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = Dimens.spacing_4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = uiState.chartFilter == "HARI_INI",
                    onClick = { viewModel.setChartFilter("HARI_INI", null) },
                    label = { Text("Hari Ini", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenAccent,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = uiState.chartFilter == "7_HARI",
                    onClick = { viewModel.setChartFilter("7_HARI", null) },
                    label = { Text("7 Hari Terakhir", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenAccent,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = uiState.chartFilter == "BULAN_INI",
                    onClick = { viewModel.setChartFilter("BULAN_INI", null) },
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



            if (isTablet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
                ) {
                    SummaryMetricCard(
                        title = "Pendapatan Kotor",
                        subtitle = periodSubtitle,
                        value = formatCurrency(displayIncome),
                        icon = Icons.Filled.TrendingUp,
                        iconBg = StatRevenueBg,
                        iconTint = GreenAccent,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        title = "Pengeluaran",
                        subtitle = periodSubtitle,
                        value = formatCurrency(displayExpense),
                        icon = Icons.Filled.TrendingDown,
                        iconBg = ExpenseBg,
                        iconTint = ExpenseRed,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        title = "Laba Bersih",
                        subtitle = "$periodSubtitle (Net)",
                        value = formatCurrency(displayProfit),
                        icon = Icons.Filled.AccountBalance,
                        iconBg = if (displayProfit >= 0) ProfitBg else ExpenseBg,
                        iconTint = if (displayProfit >= 0) ProfitBlue else ExpenseRed,
                        valueColor = if (displayProfit >= 0) ProfitBlue else ExpenseRed,
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
                            subtitle = periodSubtitle,
                            value = formatCurrency(displayIncome),
                            icon = Icons.Filled.TrendingUp,
                            iconBg = StatRevenueBg,
                            iconTint = GreenAccent,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMetricCard(
                            title = "Pengeluaran",
                            subtitle = periodSubtitle,
                            value = formatCurrency(displayExpense),
                            icon = Icons.Filled.TrendingDown,
                            iconBg = ExpenseBg,
                            iconTint = ExpenseRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Laba Bersih Highlight Card
                    HighlightNetProfitCard(
                        netProfit = displayProfit,
                        formattedProfit = formatCurrency(displayProfit),
                        periodLabel = periodSubtitle,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_6))

            // Interactive Chart Section (mengikuti filter Ringkasan)
            FinancialChartCard(
                uiState = uiState,
                periodSubtitle = periodSubtitle
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

            val onClicks = listOf(
                onCatatPembayaranClick,
                onInvoiceClick,
                onLaporanClick,
                { showExportDialog = true }
            )
            if (isTablet) {
                // Tablet: 2x2 Responsive Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
                ) {
                    KeuanganMenuCard(
                        menuItem = menuItems[0],
                        onClick = onCatatPembayaranClick,
                        modifier = Modifier.weight(1f)
                    )
                    KeuanganMenuCard(
                        menuItem = menuItems[1],
                        onClick = onInvoiceClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.spacing_3))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
                ) {
                    KeuanganMenuCard(
                        menuItem = menuItems[2],
                        onClick = onLaporanClick,
                        modifier = Modifier.weight(1f)
                    )
                    KeuanganMenuCard(
                        menuItem = menuItems[3],
                        onClick = { showExportDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Phone: 1 Column List
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
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_6))
        }
    }

    if (showExportDialog) {
        ExportLaporanDialog(
            periodSubtitle = periodSubtitle,
            totalIncome = displayIncome,
            totalExpense = displayExpense,
            netProfit = displayProfit,
            transactions = uiState.filteredPayments,
            adminName = authState.currentUser?.name ?: "Admin Kasir",
            formatCurrency = formatCurrency,
            onDismiss = { showExportDialog = false }
        )
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
    periodLabel: String = "Hari Ini",
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
                        text = "Laba Bersih ($periodLabel)",
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
@Composable
private fun FinancialChartCard(
    uiState: KeuanganUiState,
    periodSubtitle: String,
    modifier: Modifier = Modifier
) {
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
            // Header with Chart Icon & Active Period Subtitle
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
                        text = "Tren perbandingan cash flow ($periodSubtitle)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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

// ============================================================================
// EXPORT LAPORAN DIALOG
// ============================================================================
@Composable
private fun ExportLaporanDialog(
    periodSubtitle: String,
    totalIncome: Long,
    totalExpense: Long,
    netProfit: Long,
    transactions: List<com.pws.primaragagym.domain.model.FirestorePayment>,
    adminName: String,
    formatCurrency: (Long) -> String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    var selectedFormat by remember { mutableStateOf("PDF") } // "PDF" or "EXCEL"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isTablet) 36.dp else 14.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth(if (isTablet) 0.55f else 1f)
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(if (isTablet) 24.dp else 16.dp)
                ) {
                    // Header Dialog
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Export Laporan Keuangan",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Unduh atau bagikan laporan arus kas",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Tutup",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Period & Summary Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BackgroundColor)
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Periode Laporan:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GreenLight)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = periodSubtitle,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = GreenAccent
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Pemasukan", fontSize = 10.sp, color = TextMuted)
                                    Text(
                                        text = formatCurrency(totalIncome),
                                        fontSize = if (isTablet) 13.sp else 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenAccent,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Pengeluaran", fontSize = 10.sp, color = TextMuted)
                                    Text(
                                        text = formatCurrency(totalExpense),
                                        fontSize = if (isTablet) 13.sp else 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ExpenseRed,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text("Laba Bersih", fontSize = 10.sp, color = TextMuted)
                                    Text(
                                        text = formatCurrency(netProfit),
                                        fontSize = if (isTablet) 13.sp else 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (netProfit >= 0) ProfitBlue else ExpenseRed,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${transactions.size} transaksi tercatat",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 10.5.sp
                            )
                            Text(
                                text = if (netProfit >= 0) "Status: Surplus (Untung)" else "Status: Defisit (Rugi)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (netProfit >= 0) ProfitBlue else ExpenseRed,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pilih Format Header
                Text(
                    text = "Pilih Format Dokumen:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Option 1: PDF Card
                val isPdf = selectedFormat == "PDF"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedFormat = "PDF" }
                        .border(
                            width = if (isPdf) 2.dp else 1.dp,
                            color = if (isPdf) GreenAccent else Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPdf) GreenLight.copy(alpha = 0.5f) else Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PictureAsPdf,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dokumen PDF (.pdf)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Format resmi A4, siap cetak & arsip",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        if (isPdf) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(GreenAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Option 2: Excel Card
                val isExcel = selectedFormat == "EXCEL"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedFormat = "EXCEL" }
                        .border(
                            width = if (isExcel) 2.dp else 1.dp,
                            color = if (isExcel) GreenAccent else Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExcel) GreenLight.copy(alpha = 0.5f) else Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.TableChart,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Spreadsheet Excel (.csv)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Buka di Excel, Google Sheets, & WPS",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        if (isExcel) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(GreenAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Export Actions Data
                val exportData = remember(periodSubtitle, totalIncome, totalExpense, netProfit, transactions, adminName) {
                    FinancialReportExportData(
                        gymName = "PRIMARAGA GYM",
                        title = "LAPORAN KEUANGAN & ARUS KAS",
                        periodLabel = periodSubtitle,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        netProfit = netProfit,
                        transactions = transactions,
                        adminName = adminName
                    )
                }

                // Fully Responsive Action Buttons
                if (isTablet) {
                    // TABLET LAYOUT: 1 Row with ample space
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    if (selectedFormat == "PDF") {
                                        val file = LaporanKeuanganExportHelper.generatePdfReport(context, exportData)
                                        LaporanKeuanganExportHelper.openFile(context, file, "application/pdf")
                                    } else {
                                        val file = LaporanKeuanganExportHelper.generateExcelReport(context, exportData)
                                        LaporanKeuanganExportHelper.openFile(context, file, "text/csv")
                                    }
                                    onDismiss()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Gagal membuka: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp),
                            border = BorderStroke(1.dp, GreenAccent)
                        ) {
                            Icon(imageVector = Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(16.dp), tint = GreenAccent)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buka File", fontSize = 13.sp, color = GreenAccent, fontWeight = FontWeight.SemiBold)
                        }

                        if (selectedFormat == "PDF") {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val file = LaporanKeuanganExportHelper.generatePdfReport(context, exportData)
                                        LaporanKeuanganExportHelper.printPdf(context, file, "Laporan_Keuangan_${periodSubtitle}")
                                        onDismiss()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal mencetak: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(44.dp),
                                border = BorderStroke(1.dp, Color(0xFF64748B))
                            ) {
                                Icon(imageVector = Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF64748B))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cetak PDF", fontSize = 13.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Button(
                            onClick = {
                                try {
                                    if (selectedFormat == "PDF") {
                                        val file = LaporanKeuanganExportHelper.generatePdfReport(context, exportData)
                                        LaporanKeuanganExportHelper.shareFile(
                                            context,
                                            file,
                                            "application/pdf",
                                            "Laporan Keuangan Primaraga Gym ($periodSubtitle)"
                                        )
                                    } else {
                                        val file = LaporanKeuanganExportHelper.generateExcelReport(context, exportData)
                                        LaporanKeuanganExportHelper.shareFile(
                                            context,
                                            file,
                                            "text/csv",
                                            "Laporan Keuangan Primaraga Gym ($periodSubtitle)"
                                        )
                                    }
                                    onDismiss()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Gagal membagikan: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.3f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                        ) {
                            Icon(imageVector = Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bagikan Laporan", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // PHONE LAYOUT: Primary Bagikan Button (Full Width) + 2 Equal Secondary Buttons Row
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    if (selectedFormat == "PDF") {
                                        val file = LaporanKeuanganExportHelper.generatePdfReport(context, exportData)
                                        LaporanKeuanganExportHelper.shareFile(
                                            context,
                                            file,
                                            "application/pdf",
                                            "Laporan Keuangan Primaraga Gym ($periodSubtitle)"
                                        )
                                    } else {
                                        val file = LaporanKeuanganExportHelper.generateExcelReport(context, exportData)
                                        LaporanKeuanganExportHelper.shareFile(
                                            context,
                                            file,
                                            "text/csv",
                                            "Laporan Keuangan Primaraga Gym ($periodSubtitle)"
                                        )
                                    }
                                    onDismiss()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Gagal membagikan: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                        ) {
                            Icon(imageVector = Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedFormat == "PDF") "Bagikan PDF (WhatsApp / File)" else "Bagikan File Excel",
                                fontSize = 13.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        if (selectedFormat == "PDF") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val file = LaporanKeuanganExportHelper.generatePdfReport(context, exportData)
                                            LaporanKeuanganExportHelper.openFile(context, file, "application/pdf")
                                            onDismiss()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Gagal membuka: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                    border = BorderStroke(1.dp, GreenAccent)
                                ) {
                                    Icon(imageVector = Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(15.dp), tint = GreenAccent)
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Buka File", fontSize = 12.sp, color = GreenAccent, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                }

                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val file = LaporanKeuanganExportHelper.generatePdfReport(context, exportData)
                                            LaporanKeuanganExportHelper.printPdf(context, file, "Laporan_Keuangan_${periodSubtitle}")
                                            onDismiss()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Gagal mencetak: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                    border = BorderStroke(1.dp, Color(0xFF64748B))
                                ) {
                                    Icon(imageVector = Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF64748B))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Cetak PDF", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold, maxLines = 1)
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val file = LaporanKeuanganExportHelper.generateExcelReport(context, exportData)
                                        LaporanKeuanganExportHelper.openFile(context, file, "text/csv")
                                        onDismiss()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal membuka: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                border = BorderStroke(1.dp, GreenAccent)
                            ) {
                                Icon(imageVector = Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(16.dp), tint = GreenAccent)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buka di Excel / Google Sheets", fontSize = 12.sp, color = GreenAccent, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
}