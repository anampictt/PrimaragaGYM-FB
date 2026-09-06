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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Receipt
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.BackgroundColor
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.CardBackground
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.GreenAccent
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.GreenLight
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.StatusSuccess
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextMuted
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextPrimary
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextSecondary
import com.pws.primaragagym.ui.theme.Dimens

// ============================================================================
// MOCK DATA
// ============================================================================
data class InvoiceUiModel(
    val id: String,
    val memberName: String,
    val memberCode: String,
    val plan: String,
    val amount: String,
    val amountRaw: Long,
    val method: String,
    val status: InvoiceStatus,
    val date: String
)

enum class InvoiceStatus(val label: String) {
    PAID("Paid"),
    PENDING("Pending"),
    CANCELLED("Cancelled")
}

private val mockInvoices = listOf(
    InvoiceUiModel(
        id = "INV-20260906-001",
        memberName = "John Smith",
        memberCode = "MBR-001",
        plan = "Premium Monthly",
        amount = "Rp 350.000",
        amountRaw = 350_000,
        method = "QRIS",
        status = InvoiceStatus.PAID,
        date = "06 September 2026"
    ),
    InvoiceUiModel(
        id = "INV-20260905-002",
        memberName = "Sarah Connor",
        memberCode = "MBR-002",
        plan = "Monthly Basic",
        amount = "Rp 250.000",
        amountRaw = 250_000,
        method = "Cash",
        status = InvoiceStatus.PAID,
        date = "05 September 2026"
    ),
    InvoiceUiModel(
        id = "INV-20260904-003",
        memberName = "Michael Brown",
        memberCode = "MBR-003",
        plan = "Annual Premium",
        amount = "Rp 2.500.000",
        amountRaw = 2_500_000,
        method = "Transfer",
        status = InvoiceStatus.PAID,
        date = "04 September 2026"
    ),
    InvoiceUiModel(
        id = "INV-20260903-004",
        memberName = "Emma Wilson",
        memberCode = "MBR-005",
        plan = "Premium Monthly",
        amount = "Rp 350.000",
        amountRaw = 350_000,
        method = "QRIS",
        status = InvoiceStatus.PAID,
        date = "03 September 2026"
    )
)

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    onBackClick: () -> Unit = {},
    onInvoiceClick: (String) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Invoice / Kwitansi",
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
        if (mockInvoices.isEmpty()) {
            EmptyInvoiceState(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = if (isTablet) 32.dp else Dimens.screen_padding_horizontal,
                    vertical = Dimens.spacing_4
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
            ) {
                items(mockInvoices) { invoice ->
                    InvoiceCard(
                        invoice = invoice,
                        onClick = { onInvoiceClick(invoice.id) }
                    )
                }
            }
        }
    }
}

// ============================================================================
// INVOICE CARD
// ============================================================================
@Composable
private fun InvoiceCard(
    invoice: InvoiceUiModel,
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
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(GreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Receipt,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(Dimens.spacing_3))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invoice.id,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${invoice.memberName} • ${invoice.plan}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = invoice.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.width(Dimens.spacing_3))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = invoice.amount,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = GreenAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = invoice.method,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.width(Dimens.spacing_2))

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ============================================================================
// EMPTY STATE
// ============================================================================
@Composable
private fun EmptyInvoiceState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Receipt,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_4))
            Text(
                text = "Belum ada invoice",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_2))
            Text(
                text = "Invoice pembayaran akan muncul di sini.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}