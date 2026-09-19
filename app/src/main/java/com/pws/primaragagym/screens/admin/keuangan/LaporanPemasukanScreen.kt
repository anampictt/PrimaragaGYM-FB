package com.pws.primaragagym.screens.admin.keuangan

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pws.primaragagym.domain.model.FirestorePayment
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.BackgroundColor
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.CardBackground
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.CashBg
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.GreenAccent
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.GreenLight
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.QrisBg
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.StatMonthBg
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.StatTransactionBg
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextMuted
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextPrimary
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextSecondary
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TransferBg
import com.pws.primaragagym.ui.theme.Dimens
import com.pws.primaragagym.ui.viewmodel.AuthViewModel
import com.pws.primaragagym.ui.viewmodel.LaporanFilterPeriod
import com.pws.primaragagym.ui.viewmodel.LaporanPemasukanViewModel
import com.pws.primaragagym.ui.viewmodel.MethodBreakdownItem
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanPemasukanScreen(
    onBackClick: () -> Unit = {},
    viewModel: LaporanPemasukanViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    var showCustomDateDialog by remember { mutableStateOf(false) }

    // Load data based on current user's branch
    LaunchedEffect(authState.currentUser) {
        val branchId = authState.currentUser?.branchId ?: ""
        viewModel.loadPayments(branchId)
    }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val paginatedTransactions = com.pws.primaragagym.ui.components.common.rememberPaginatedList(
        items = uiState.displayedTransactions,
        pageSize = 12,
        resetKey = Triple(uiState.selectedPeriod, uiState.selectedTypeFilter, uiState.searchQuery)
    )
    com.pws.primaragagym.ui.components.common.BindPagination(listState, paginatedTransactions)

    Scaffold(
        containerColor = BackgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Laporan Pemasukan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Rekap pendaftaran member & metode pembayaran",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextPrimary
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
        if (uiState.isLoading && uiState.allPayments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
                ) {
                    CircularProgressIndicator(color = GreenAccent)
                    Text(
                        text = "Memuat data laporan pemasukan...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
                contentPadding = PaddingValues(
                    horizontal = if (isTablet) 32.dp else Dimens.screen_padding_horizontal_compact,
                    vertical = Dimens.spacing_4
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing_4)
            ) {
                // ============================================================
                // 1. FILTER PERIODE CHIPS
                // ============================================================
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing_2)) {
                        Text(
                            text = "Periode Laporan",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_2)
                        ) {
                            items(LaporanFilterPeriod.entries.toTypedArray()) { period ->
                                val isSelected = uiState.selectedPeriod == period
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (period == LaporanFilterPeriod.CUSTOM) {
                                            showCustomDateDialog = true
                                        } else {
                                            viewModel.setPeriodFilter(period)
                                        }
                                    },
                                    leadingIcon = if (period == LaporanFilterPeriod.CUSTOM) {
                                        {
                                            Icon(
                                                imageVector = Icons.Filled.CalendarToday,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = if (isSelected) Color.White else GreenAccent
                                            )
                                        }
                                    } else null,
                                    label = {
                                        Text(
                                            text = period.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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

                        // Banner info custom date yang sedang aktif
                        AnimatedVisibility(visible = uiState.selectedPeriod == LaporanFilterPeriod.CUSTOM) {
                            ActiveCustomDateBanner(
                                startDate = uiState.customStartDate,
                                endDate = uiState.customEndDate,
                                onEditClick = { showCustomDateDialog = true }
                            )
                        }
                    }
                }

                // ============================================================
                // 2. KARTU RINGKASAN PEMASUKAN
                // ============================================================
                item {
                    if (isTablet) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
                        ) {
                            LaporanSummaryCard(
                                title = "Total Pemasukan",
                                value = formatRupiah(uiState.totalIncome),
                                subtitle = "${uiState.totalTransactions} Total Transaksi",
                                icon = Icons.Filled.AccountBalanceWallet,
                                iconBg = StatMonthBg,
                                iconTint = GreenAccent,
                                modifier = Modifier.weight(1f),
                                isPrimary = false
                            )
                            LaporanSummaryCard(
                                title = "Pendaftaran Member",
                                value = formatRupiah(uiState.registrationIncome),
                                subtitle = "${uiState.registrationCount} Member Baru",
                                icon = Icons.Filled.PersonAdd,
                                iconBg = Color(0xFFE0F2FE),
                                iconTint = Color(0xFF0284C7),
                                modifier = Modifier.weight(1f),
                                isPrimary = false
                            )
                            LaporanSummaryCard(
                                title = "Perpanjangan",
                                value = formatRupiah(uiState.renewalIncome),
                                subtitle = "${uiState.renewalCount} Transaksi",
                                icon = Icons.Filled.Refresh,
                                iconBg = Color(0xFFEDE7F6),
                                iconTint = Color(0xFF7C3AED),
                                modifier = Modifier.weight(1f),
                                isPrimary = false
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing_3)) {
                            // Highlight card Total Pemasukan (Hero card)
                            LaporanSummaryCard(
                                title = "Total Pemasukan",
                                value = formatRupiah(uiState.totalIncome),
                                subtitle = "${uiState.totalTransactions} Transaksi pada periode ini",
                                icon = Icons.Filled.AccountBalanceWallet,
                                iconBg = StatMonthBg,
                                iconTint = GreenAccent,
                                isPrimary = true
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_2)
                            ) {
                                LaporanSummaryCard(
                                    title = "Pendaftaran Baru",
                                    value = formatRupiah(uiState.registrationIncome),
                                    subtitle = "${uiState.registrationCount} Member",
                                    icon = Icons.Filled.PersonAdd,
                                    iconBg = Color(0xFFE0F2FE),
                                    iconTint = Color(0xFF0284C7),
                                    modifier = Modifier.weight(1f),
                                    isPrimary = false
                                )
                                LaporanSummaryCard(
                                    title = "Perpanjangan",
                                    value = formatRupiah(uiState.renewalIncome),
                                    subtitle = "${uiState.renewalCount} Transaksi",
                                    icon = Icons.Filled.Refresh,
                                    iconBg = Color(0xFFEDE7F6),
                                    iconTint = Color(0xFF7C3AED),
                                    modifier = Modifier.weight(1f),
                                    isPrimary = false
                                )
                            }
                        }
                    }
                }

                // ============================================================
                // 3. PENDAPATAN BERDASARKAN METODE PEMBAYARAN
                // ============================================================
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing_2)) {
                        Text(
                            text = "Pendapatan Berdasarkan Metode",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Distribusi nominal dan persentase pembayaran yang diterima",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                item {
                    if (isTablet) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
                        ) {
                            uiState.methodSummaries.forEach { item ->
                                MethodBreakdownCard(
                                    item = item,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing_3)) {
                            uiState.methodSummaries.forEach { item ->
                                MethodBreakdownCard(item = item)
                            }
                        }
                    }
                }

                // ============================================================
                // 4. DAFTAR TRANSAKSI & SEARCH
                // ============================================================
                item {
                    Spacer(modifier = Modifier.height(Dimens.spacing_2))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daftar Transaksi Pemasukan",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Menampilkan ${uiState.displayedTransactions.size} transaksi",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Search Box & Type Filter
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing_2)) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "Cari nama member, invoice, paket...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotBlank()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(
                                            imageVector = Icons.Filled.Clear,
                                            contentDescription = "Hapus pencarian",
                                            tint = TextSecondary
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CardBackground,
                                unfocusedContainerColor = CardBackground,
                                focusedBorderColor = GreenAccent,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )

                        // Secondary Type Filter Chips (Semua, Pendaftaran Baru, Perpanjangan)
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_2)
                        ) {
                            val typeOptions = listOf(
                                "ALL" to "Semua Transaksi",
                                "REGISTRATION" to "Pendaftaran Member",
                                "RENEWAL" to "Perpanjangan"
                            )
                            items(typeOptions, key = { it.first }) { (key, label) ->
                                val selected = uiState.selectedTypeFilter == key
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.setTypeFilter(key) },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GreenLight,
                                        selectedLabelColor = GreenAccent,
                                        containerColor = CardBackground,
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }
                }

                // ============================================================
                // 5. TRANSACTION LIST
                // ============================================================
                if (uiState.displayedTransactions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(Dimens.card_corner_radius),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.spacing_8),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Dimens.spacing_2)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(GreenLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Receipt,
                                        contentDescription = null,
                                        tint = GreenAccent,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Text(
                                    text = "Belum Ada Data Transaksi",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (uiState.searchQuery.isNotBlank())
                                        "Tidak ada transaksi yang cocok dengan kata kunci '${uiState.searchQuery}'"
                                    else
                                        "Tidak ada pemasukan pada periode yang dipilih.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = paginatedTransactions.visibleItems,
                        key = { it.paymentId.ifBlank { it.invoiceNumber } }
                    ) { payment ->
                        PaymentTransactionCard(payment = payment)
                    }

                    if (paginatedTransactions.isLoadingMore) {
                        item(key = "pagination_loading") {
                            com.pws.primaragagym.ui.components.common.PaginationLoadingItem()
                        }
                    } else if (!paginatedTransactions.hasMore && paginatedTransactions.totalCount > 12) {
                        item(key = "pagination_end") {
                            com.pws.primaragagym.ui.components.common.PaginationEndOfListItem(totalCount = paginatedTransactions.totalCount)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(Dimens.spacing_5))
                }
            }
        }
    }

    // ========================================================================
    // CUSTOM DATE RANGE DIALOG
    // ========================================================================
    if (showCustomDateDialog) {
        CustomDateRangeDialog(
            initialStartDate = uiState.customStartDate ?: Date(),
            initialEndDate = uiState.customEndDate ?: Date(),
            onDismiss = { showCustomDateDialog = false },
            onApply = { start, end ->
                viewModel.setPeriodFilter(LaporanFilterPeriod.CUSTOM, start, end)
                showCustomDateDialog = false
            }
        )
    }
}

// ============================================================================
// ACTIVE CUSTOM DATE BANNER
// ============================================================================
@Composable
private fun ActiveCustomDateBanner(
    startDate: Date?,
    endDate: Date?,
    onEditClick: () -> Unit
) {
    val dateDisplay = remember(startDate, endDate) {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val s = startDate?.let { sdf.format(it) } ?: "-"
        val e = endDate?.let { sdf.format(it) } ?: s
        if (s == e) s else "$s - $e"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = GreenLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacing_3, vertical = Dimens.spacing_2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_2)
            ) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Periode Kustom: $dateDisplay",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenAccent
                )
            }
            TextButton(
                onClick = onEditClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        tint = GreenAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Ubah",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GreenAccent
                    )
                }
            }
        }
    }
}

// ============================================================================
// SUMMARY CARD
// ============================================================================
@Composable
private fun LaporanSummaryCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.card_corner_radius),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPrimary) 2.dp else 1.dp)
    ) {
        if (isPrimary) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.spacing_4),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(Dimens.spacing_3))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = GreenAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (value.length > 13) 14.sp else 16.sp
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ============================================================================
// METHOD BREAKDOWN CARD
// ============================================================================
@Composable
private fun MethodBreakdownCard(
    item: MethodBreakdownItem,
    modifier: Modifier = Modifier
) {
    val (icon, bg, tint) = when (item.key) {
        "CASH" -> Triple(Icons.Filled.Wallet, CashBg, Color(0xFFFF9800))
        "TRANSFER" -> Triple(Icons.Filled.AccountBalance, TransferBg, Color(0xFF2196F3))
        "QRIS" -> Triple(Icons.Filled.QrCode2, QrisBg, GreenAccent)
        else -> Triple(Icons.Filled.CreditCard, StatTransactionBg, Color(0xFF7C3AED))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.card_corner_radius),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_4),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing_2)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(bg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(Dimens.spacing_3))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${item.transactionCount} Transaksi",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatRupiah(item.amount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = String.format(Locale("id", "ID"), "%.1f%%", item.percentage),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = tint
                    )
                }
            }

            // Visual Progress Bar
            LinearProgressIndicator(
                progress = { (item.percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = tint,
                trackColor = bg
            )
        }
    }
}

// ============================================================================
// PAYMENT TRANSACTION CARD
// ============================================================================
@Composable
private fun PaymentTransactionCard(
    payment: FirestorePayment
) {
    val dateFormatted = remember(payment.paidAt, payment.createdAt) {
        val d = payment.paidAt ?: payment.createdAt ?: Date()
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale("id", "ID")).apply {
            timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        }
        sdf.format(d)
    }

    val isRegistration = remember(payment.paymentType, payment.notes, payment.planName) {
        val type = payment.paymentType.uppercase()
        val notes = payment.notes.lowercase()
        val plan = payment.planName.lowercase()
        type == "REGISTRASI" || type == "NEW_MEMBERSHIP" || notes.contains("registrasi") || plan.contains("registrasi")
    }

    val isRenewal = remember(payment.paymentType, payment.notes) {
        val type = payment.paymentType.uppercase()
        val notes = payment.notes.lowercase()
        type == "RENEWAL" || notes.contains("perpanjang")
    }

    val (methodBg, methodTint) = when (payment.paymentMethod.uppercase()) {
        "CASH" -> Pair(CashBg, Color(0xFFFF9800))
        "TRANSFER" -> Pair(TransferBg, Color(0xFF2196F3))
        "QRIS" -> Pair(QrisBg, GreenAccent)
        else -> Pair(StatTransactionBg, Color(0xFF7C3AED))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.input_corner_radius),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_3),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing_2)
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
                        .background(GreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = payment.memberName.take(2).uppercase().ifBlank { "MB" },
                        style = MaterialTheme.typography.labelMedium,
                        color = GreenAccent,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(Dimens.spacing_3))

                // Member Name & Details
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_1)
                    ) {
                        Text(
                            text = payment.memberName.ifBlank { "Member" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (payment.memberCode.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = payment.memberCode,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = payment.planName.ifBlank {
                            if (isRegistration) "Pendaftaran Member Baru"
                            else if (isRenewal) "Perpanjang Membership"
                            else "Pembayaran Member"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Amount
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "+ ${formatRupiah(payment.amount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = GreenAccent
                    )
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

            // Footer row: Invoice number, Type badge, Method badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = payment.invoiceNumber.ifBlank { "INV-${payment.paymentId.takeLast(6)}" },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_1)) {
                    // Type Badge
                    val typeLabel = when {
                        isRegistration -> "Pendaftaran Baru"
                        isRenewal -> "Perpanjangan"
                        else -> payment.paymentType
                    }
                    val typeBg = if (isRegistration) GreenLight else Color(0xFFE0F2FE)
                    val typeText = if (isRegistration) GreenAccent else Color(0xFF0284C7)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(typeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = typeText,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        )
                    }

                    // Method Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(methodBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = payment.paymentMethod.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = methodTint,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// CUSTOM DATE RANGE DIALOG COMPONENT
// ============================================================================
@Composable
private fun CustomDateRangeDialog(
    initialStartDate: Date,
    initialEndDate: Date,
    onDismiss: () -> Unit,
    onApply: (Date, Date) -> Unit
) {
    val context = LocalContext.current
    var startDate by remember { mutableStateOf(initialStartDate) }
    var endDate by remember { mutableStateOf(initialEndDate) }

    val sdf = remember { SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")) }

    val openStartDatePicker = {
        val cal = Calendar.getInstance().apply { time = startDate }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newStart = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                startDate = newStart
                if (newStart.after(endDate)) {
                    endDate = newStart
                }
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val openEndDatePicker = {
        val cal = Calendar.getInstance().apply { time = endDate }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newEnd = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.time
                endDate = newEnd
                if (newEnd.before(startDate)) {
                    startDate = newEnd
                }
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_2)
            ) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = null,
                    tint = GreenAccent
                )
                Text(
                    text = "Pilih Rentang Tanggal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
            ) {
                Text(
                    text = "Tentukan periode awal dan akhir transaksi yang ingin Anda tampilkan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                // Shortcut buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_1)
                ) {
                    ShortcutDateChip(
                        label = "Hari Ini",
                        onClick = {
                            val today = Date()
                            startDate = today
                            endDate = today
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ShortcutDateChip(
                        label = "7 Hari Lalu",
                        onClick = {
                            val now = Date()
                            val s = Calendar.getInstance().apply {
                                time = now
                                add(Calendar.DAY_OF_YEAR, -6)
                            }.time
                            startDate = s
                            endDate = now
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ShortcutDateChip(
                        label = "30 Hari Lalu",
                        onClick = {
                            val now = Date()
                            val s = Calendar.getInstance().apply {
                                time = now
                                add(Calendar.DAY_OF_YEAR, -29)
                            }.time
                            startDate = s
                            endDate = now
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Start Date Field
                DateSelectorBox(
                    label = "Dari Tanggal (Mulai)",
                    formattedDate = sdf.format(startDate),
                    onClick = openStartDatePicker
                )

                // End Date Field
                DateSelectorBox(
                    label = "Sampai Tanggal (Selesai)",
                    formattedDate = sdf.format(endDate),
                    onClick = openEndDatePicker
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(startDate, endDate) },
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Terapkan Filter",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Batal", color = TextSecondary)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun ShortcutDateChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(GreenLight)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = GreenAccent,
            maxLines = 1,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun DateSelectorBox(
    label: String,
    formattedDate: String,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = Dimens.spacing_3, vertical = Dimens.spacing_3)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ============================================================================
// HELPER: CURRENCY FORMATTER
// ============================================================================
private fun formatRupiah(amount: Long): String {
    val numberFormat = NumberFormat.getNumberInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    return "Rp ${numberFormat.format(amount)}"
}