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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

// ============================================================================
// FILTER TYPES
// ============================================================================
private enum class LaporanFilter(val label: String) {
    TODAY("Hari Ini"),
    WEEK("Minggu Ini"),
    MONTH("Bulan Ini"),
    CUSTOM("Custom")
}

// ============================================================================
// METHOD DATA
// ============================================================================
private data class MethodSummary(
    val method: String,
    val icon: ImageVector,
    val amount: String,
    val amountRaw: Long,
    val backgroundColor: Color,
    val iconColor: Color
)

private data class TransactionItem(
    val memberName: String,
    val plan: String,
    val amount: String,
    val method: String,
    val date: String
)

// ============================================================================
// MOCK DATA
// ============================================================================
private val methodSummaries = listOf(
    MethodSummary("Cash", Icons.Filled.Wallet, "Rp 10.500.000", 10_500_000, CashBg, Color(0xFFFF9800)),
    MethodSummary("Transfer", Icons.Filled.AccountBalance, "Rp 15.000.000", 15_000_000, TransferBg, Color(0xFF2196F3)),
    MethodSummary("QRIS", Icons.Filled.QrCode2, "Rp 13.000.000", 13_000_000, QrisBg, GreenAccent)
)

private val recentTransactions = listOf(
    TransactionItem("John Smith", "Premium Monthly", "Rp 350.000", "QRIS", "06 Sep 2026"),
    TransactionItem("Sarah Connor", "Monthly Basic", "Rp 250.000", "Cash", "06 Sep 2026"),
    TransactionItem("Emma Wilson", "Premium Monthly", "Rp 350.000", "QRIS", "05 Sep 2026"),
    TransactionItem("Michael Brown", "Annual Premium", "Rp 2.500.000", "Transfer", "04 Sep 2026"),
    TransactionItem("David Miller", "Basic Monthly", "Rp 200.000", "Cash", "03 Sep 2026"),
    TransactionItem("Emma Wilson", "Premium Monthly", "Rp 350.000", "QRIS", "02 Sep 2026")
)

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanPemasukanScreen(
    onBackClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    var selectedFilter by remember { mutableStateOf(LaporanFilter.MONTH) }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Laporan Pemasukan",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = if (isTablet) 32.dp else Dimens.screen_padding_horizontal,
                vertical = Dimens.spacing_5
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing_4)
        ) {
            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_2)
                ) {
                    LaporanFilter.entries.forEach { filter ->
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
                        LaporanSummaryCard(
                            title = "Total Pemasukan",
                            value = "Rp 38.500.000",
                            icon = Icons.Filled.AccountBalanceWallet,
                            iconBackgroundColor = StatMonthBg,
                            modifier = Modifier.weight(1f)
                        )
                        LaporanSummaryCard(
                            title = "Jumlah Transaksi",
                            value = "124",
                            icon = Icons.Filled.Receipt,
                            iconBackgroundColor = StatTransactionBg,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing_3)) {
                        LaporanSummaryCard(
                            title = "Total Pemasukan",
                            value = "Rp 38.500.000",
                            icon = Icons.Filled.AccountBalanceWallet,
                            iconBackgroundColor = StatMonthBg
                        )
                        LaporanSummaryCard(
                            title = "Jumlah Transaksi",
                            value = "124",
                            icon = Icons.Filled.Receipt,
                            iconBackgroundColor = StatTransactionBg
                        )
                    }
                }
            }

            // Section: Berdasarkan Metode
            item {
                Text(
                    text = "Berdasarkan Metode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            item {
                if (isTablet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
                    ) {
                        methodSummaries.forEach { method ->
                            MethodSummaryCard(
                                method = method,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing_3)) {
                        methodSummaries.forEach { method ->
                            MethodSummaryCard(method = method)
                        }
                    }
                }
            }

            // Section: Transaksi Terbaru
            item {
                Spacer(modifier = Modifier.height(Dimens.spacing_2))
                Text(
                    text = "Transaksi Terbaru",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            // Transaction List
            if (recentTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Dimens.card_corner_radius),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.spacing_8),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada transaksi",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }
                }
            } else {
                items(recentTransactions) { transaction ->
                    TransactionItemCard(transaction = transaction)
                }
            }

            item {
                Spacer(modifier = Modifier.height(Dimens.spacing_4))
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
    icon: ImageVector,
    iconBackgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    .background(iconBackgroundColor),
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
// METHOD SUMMARY CARD
// ============================================================================
@Composable
private fun MethodSummaryCard(
    method: MethodSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    .background(method.backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = method.icon,
                    contentDescription = null,
                    tint = method.iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(Dimens.spacing_3))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = method.method,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    text = method.amount,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
    }
}

// ============================================================================
// TRANSACTION ITEM CARD
// ============================================================================
@Composable
private fun TransactionItemCard(
    transaction: TransactionItem
) {
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
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GreenLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transaction.memberName.take(2).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = GreenAccent,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(Dimens.spacing_3))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.memberName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${transaction.plan} • ${transaction.method}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = transaction.amount,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenAccent
                )
                Text(
                    text = transaction.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}