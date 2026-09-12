package com.pws.primaragagym.screens.admin.keuangan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.BackgroundColor
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.CardBackground
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.GreenAccent
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.GreenLight
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.StatMonthBg
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.StatRevenueBg
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.StatTransactionBg
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextMuted
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextPrimary
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextSecondary
import com.pws.primaragagym.ui.theme.Dimens
import java.text.NumberFormat
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.pws.primaragagym.ui.viewmodel.KeuanganViewModel
import com.pws.primaragagym.ui.viewmodel.AuthViewModel

// ============================================================================
// MOCK DATA
// ============================================================================
private data class KeuanganSummaryData(
    val todayRevenue: String = "Rp 2.450.000",
    val todayRevenueRaw: Long = 2_450_000,
    val monthRevenue: String = "Rp 38.500.000",
    val monthRevenueRaw: Long = 38_500_000,
    val todayTransactions: Int = 24
)

private data class MenuItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val badge: String? = null
)

private val summaryData = KeuanganSummaryData()

private val menuItems = listOf(
    MenuItem(
        title = "Catat Pembayaran",
        description = "Catat pembayaran member dan transaksi gym.",
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
        val branchId = authState.currentUser?.branchId
        if (branchId != null) {
            viewModel.loadSummary(branchId)
        }
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
                            text = "Kelola pembayaran dan pemasukan gym.",
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
            // Summary Cards
            if (isTablet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_4)
                ) {
                    SummaryCard(
                        title = "Pendapatan Hari Ini",
                        value = formatCurrency(uiState.todayRevenue),
                        icon = Icons.Filled.Payments,
                        iconBackgroundColor = StatRevenueBg,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Pendapatan Bulan Ini",
                        value = formatCurrency(uiState.monthRevenue),
                        icon = Icons.Filled.AccountBalanceWallet,
                        iconBackgroundColor = StatMonthBg,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Transaksi Hari Ini",
                        value = "${uiState.todayTransactions}",
                        icon = Icons.Filled.Receipt,
                        iconBackgroundColor = StatTransactionBg,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing_3)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
                    ) {
                        SummaryCard(
                            title = "Pendapatan Hari Ini",
                            value = formatCurrency(uiState.todayRevenue),
                            icon = Icons.Filled.Payments,
                            iconBackgroundColor = StatRevenueBg,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "Pendapatan Bulan Ini",
                            value = formatCurrency(uiState.monthRevenue),
                            icon = Icons.Filled.AccountBalanceWallet,
                            iconBackgroundColor = StatMonthBg,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
                    ) {
                        SummaryCard(
                            title = "Transaksi Hari Ini",
                            value = "${uiState.todayTransactions}",
                            icon = Icons.Filled.Receipt,
                            iconBackgroundColor = StatTransactionBg,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_8))

            // Menu Utama Section
            Text(
                text = "Menu Utama",
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
// SUMMARY CARD
// ============================================================================
@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
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
            Spacer(modifier = Modifier.height(Dimens.spacing_2))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                maxLines = 1
            )
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