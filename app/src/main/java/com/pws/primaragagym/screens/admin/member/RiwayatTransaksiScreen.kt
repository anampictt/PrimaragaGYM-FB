package com.pws.primaragagym.screens.admin.member

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pws.primaragagym.domain.model.FirestoreMember
import com.pws.primaragagym.domain.model.FirestorePayment
import com.pws.primaragagym.screens.admin.member.MemberColors.BackgroundColor
import com.pws.primaragagym.screens.admin.member.MemberColors.CardBackground
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenAccent
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenLight
import com.pws.primaragagym.screens.admin.member.MemberColors.TextMuted
import com.pws.primaragagym.screens.admin.member.MemberColors.TextPrimary
import com.pws.primaragagym.screens.admin.member.MemberColors.TextSecondary
import com.pws.primaragagym.ui.theme.Dimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatTransaksiScreen(
    memberId: String,
    onBackClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val horizontalPadding = if (isTablet) 32.dp else Dimens.screen_padding_horizontal

    var member by remember { mutableStateOf<FirestoreMember?>(null) }
    var transactions by remember { mutableStateOf<List<FirestorePayment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(memberId) {
        if (memberId.isNotBlank()) {
            isLoading = true
            withContext(Dispatchers.IO) {
                try {
                    val memberRepo = com.pws.primaragagym.data.repository.MemberRepositoryImpl()
                    val paymentRepo = com.pws.primaragagym.data.repository.PaymentRepositoryImpl()

                    val memberResult = memberRepo.getMemberById(memberId)
                    val loadedMember = memberResult.getOrNull()

                    val paymentResult = paymentRepo.getPaymentsByMember(memberId)
                    val loadedPayments = paymentResult.getOrNull() ?: emptyList()

                    withContext(Dispatchers.Main) {
                        if (loadedMember != null) {
                            member = loadedMember
                        } else {
                            val dummy = dummyMembers.find { it.id == memberId }
                            if (dummy != null) {
                                member = FirestoreMember(
                                    memberId = dummy.id,
                                    memberCode = dummy.memberCode,
                                    fullName = dummy.name,
                                    planName = dummy.planName,
                                    startDate = dummy.startDate,
                                    expiredDate = dummy.expiredDate,
                                    status = dummy.status.name,
                                    planPrice = dummy.planPrice.filter { it.isDigit() }.toLongOrNull() ?: 350000L
                                )
                            }
                        }

                        // Jika koleksi payments belum memiliki data tetapi member memiliki plan & startDate,
                        // buat catatan transaksi registrasi awal agar riwayat tidak kosong
                        if (loadedPayments.isEmpty() && member != null && (member!!.planPrice > 0L || member!!.planName.isNotBlank())) {
                            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                            val parsedDate = try {
                                if (member!!.startDate.isNotBlank()) sdf.parse(member!!.startDate) else Date()
                            } catch (_: Exception) {
                                Date()
                            }

                            transactions = listOf(
                                FirestorePayment(
                                    paymentId = "initial_${member!!.memberId}",
                                    memberId = member!!.memberId,
                                    memberName = member!!.fullName,
                                    amount = member!!.planPrice,
                                    paymentMethod = member!!.paymentMethod.ifBlank { "Cash" },
                                    paymentType = "REGISTRASI",
                                    planName = "Registrasi ${member!!.planName}",
                                    status = "PAID",
                                    paidAt = parsedDate,
                                    createdAt = parsedDate
                                )
                            )
                        } else {
                            transactions = loadedPayments
                        }
                        isLoading = false
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isLoading = false
                    }
                }
            }
        }
    }

    val currentMember = member
    val memberName = currentMember?.fullName?.ifBlank { "Member" } ?: "Member"
    val avatarInitial = memberName.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifEmpty { "?" }

    Scaffold(
        containerColor = BackgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Riwayat Transaksi",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBackground)
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenAccent)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
            ) {
                // Member Profile Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
                        .padding(top = Dimens.spacing_4),
                    shape = RoundedCornerShape(12.dp),
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
                            Text(
                                text = avatarInitial,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = GreenAccent
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = memberName,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "ID: ${currentMember?.memberCode?.ifBlank { "-" } ?: "-"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "Paket: ${currentMember?.planName?.ifBlank { "-" } ?: "-"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_4))

                // Daftar Transaksi
                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontalPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(GreenLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = GreenAccent,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Belum Ada Transaksi",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Riwayat pembayaran pendaftaran, perpanjangan, atau upgrade akan tercatat di sini.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                    val paginatedTransactions = com.pws.primaragagym.ui.components.common.rememberPaginatedList(
                        items = transactions,
                        pageSize = 10,
                        resetKey = memberId to transactions.size
                    )
                    com.pws.primaragagym.ui.components.common.BindPagination(listState, paginatedTransactions)

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(
                            start = horizontalPadding,
                            end = horizontalPadding,
                            top = 4.dp,
                            bottom = Dimens.spacing_5
                        )
                    ) {
                        items(paginatedTransactions.visibleItems) { payment ->
                            PaymentTransactionCard(payment = payment)
                        }

                        if (paginatedTransactions.isLoadingMore) {
                            item(key = "pagination_loading") {
                                com.pws.primaragagym.ui.components.common.PaginationLoadingItem()
                            }
                        } else if (!paginatedTransactions.hasMore && paginatedTransactions.totalCount > 10) {
                            item(key = "pagination_end") {
                                com.pws.primaragagym.ui.components.common.PaginationEndOfListItem(totalCount = paginatedTransactions.totalCount)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentTransactionCard(payment: FirestorePayment) {
    val isDowngradeType = payment.paymentType.equals("DOWNGRADE", ignoreCase = true) ||
            payment.planName.startsWith("Downgrade", ignoreCase = true) ||
            (payment.paymentType.equals("UPGRADE", ignoreCase = true) && payment.planName.contains("Harian", ignoreCase = true))

    val typeFormatted = when {
        isDowngradeType -> "Downgrade Membership"
        payment.paymentType.equals("UPGRADE", ignoreCase = true) -> "Upgrade Membership"
        payment.paymentType.equals("RENEWAL", ignoreCase = true) || payment.paymentType.equals("PERPANJANGAN", ignoreCase = true) -> "Perpanjang Membership"
        payment.paymentType.equals("NEW_MEMBERSHIP", ignoreCase = true) || payment.paymentType.equals("REGISTRASI", ignoreCase = true) -> "Registrasi Member"
        else -> payment.paymentType.replace("_", " ").lowercase()
            .replaceFirstChar { it.uppercase() }
    }

    val typeIcon = when {
        isDowngradeType -> Icons.Filled.ArrowDownward
        payment.paymentType.equals("UPGRADE", ignoreCase = true) -> Icons.Filled.ArrowUpward
        payment.paymentType.equals("RENEWAL", ignoreCase = true) || payment.paymentType.equals("PERPANJANGAN", ignoreCase = true) -> Icons.Filled.Refresh
        payment.paymentType.equals("NEW_MEMBERSHIP", ignoreCase = true) || payment.paymentType.equals("REGISTRASI", ignoreCase = true) -> Icons.Filled.PersonAdd
        else -> Icons.Filled.CreditCard
    }

    val dateFormatted = remember(payment.paidAt, payment.createdAt) {
        val dateToFormat = payment.paidAt ?: payment.createdAt ?: Date()
        val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm 'WIB'", Locale("id", "ID")).apply {
            timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        }
        sdf.format(dateToFormat)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = typeFormatted,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                if (payment.planName.isNotBlank()) {
                    Text(
                        text = payment.planName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    if (payment.paymentMethod.isNotBlank()) {
                        Text(
                            text = " • ${payment.paymentMethod}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatRupiah(payment.amount),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                PaymentStatusBadge(status = payment.status)
            }
        }
    }
}

@Composable
private fun PaymentStatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status.uppercase()) {
        "PAID", "SUCCESS" -> Triple(Color(0xFFE8F5E9), Color(0xFF4CAF50), "Paid")
        "PENDING" -> Triple(Color(0xFFFFF3E0), Color(0xFFFF9800), "Pending")
        "CANCELLED", "FAILED" -> Triple(Color(0xFFFFEBEE), Color(0xFFF44336), "Cancelled")
        else -> Triple(Color(0xFFF5F5F5), Color(0xFF757575), status)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor
        )
    }
}

private fun formatRupiah(amount: Long): String {
    val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp ${nf.format(amount)}"
}
