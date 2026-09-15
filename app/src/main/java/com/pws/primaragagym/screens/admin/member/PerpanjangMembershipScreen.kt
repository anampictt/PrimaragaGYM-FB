package com.pws.primaragagym.screens.admin.member

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.pws.primaragagym.screens.admin.member.MemberColors.BackgroundColor
import com.pws.primaragagym.screens.admin.member.MemberColors.CardBackground
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenAccent
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenLight
import com.pws.primaragagym.screens.admin.member.MemberColors.TextMuted
import com.pws.primaragagym.screens.admin.member.MemberColors.TextPrimary
import com.pws.primaragagym.screens.admin.member.MemberColors.TextSecondary
import com.pws.primaragagym.ui.theme.Dimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private enum class RenewalPayment(val displayName: String) {
    CASH("Cash"),
    TRANSFER("Transfer"),
    QRIS("QRIS")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerpanjangMembershipScreen(
    memberId: String,
    onBackClick: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val horizontalPadding = if (isTablet) 32.dp else Dimens.screen_padding_horizontal

    val scope = rememberCoroutineScope()
    var member by remember { mutableStateOf<FirestoreMember?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(memberId) {
        if (memberId.isNotBlank()) {
            isLoading = true
            withContext(Dispatchers.IO) {
                try {
                    val memberRepo = com.pws.primaragagym.data.repository.MemberRepositoryImpl()
                    val planRepo = com.pws.primaragagym.data.repository.MembershipPlanRepositoryImpl()
                    val result = memberRepo.getMemberById(memberId)
                    result.fold(
                        onSuccess = { m ->
                            var resolvedMember = m
                            // If planPrice is 0 or duration is blank, try finding matching plan from repository
                            if (resolvedMember.planPrice == 0L || resolvedMember.duration.isBlank() || resolvedMember.planType.isBlank()) {
                                val plansResult = planRepo.getMembershipPlans()
                                val allPlans = plansResult.getOrNull() ?: emptyList()
                                val matchingPlan = allPlans.find {
                                    (resolvedMember.planId.isNotBlank() && it.planId == resolvedMember.planId) ||
                                    (resolvedMember.planName.isNotBlank() && it.name.equals(resolvedMember.planName, ignoreCase = true))
                                }
                                if (matchingPlan != null) {
                                    resolvedMember = resolvedMember.copy(
                                        planPrice = if (resolvedMember.planPrice == 0L) matchingPlan.price else resolvedMember.planPrice,
                                        duration = if (resolvedMember.duration.isBlank()) matchingPlan.duration else resolvedMember.duration,
                                        planType = if (resolvedMember.planType.isBlank()) matchingPlan.type else resolvedMember.planType
                                    )
                                }
                            }
                            withContext(Dispatchers.Main) {
                                member = resolvedMember
                                isLoading = false
                            }
                        },
                        onFailure = {
                            withContext(Dispatchers.Main) {
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
                                        planPrice = dummy.planPrice.filter { it.isDigit() }.toLongOrNull() ?: 350000L,
                                        duration = "1 Bulan",
                                        planType = "MONTHLY"
                                    )
                                }
                                isLoading = false
                            }
                        }
                    )
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isLoading = false
                    }
                }
            }
        }
    }

    var selectedPayment by remember { mutableStateOf<RenewalPayment?>(null) }
    var paymentError by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val currentMember = member
    val planName = currentMember?.planName?.ifBlank { "Membership" } ?: "Membership"
    val durationStr = currentMember?.duration?.ifBlank {
        when (currentMember.planType.uppercase()) {
            "DAILY" -> "1 Hari"
            "YEARLY" -> "1 Tahun"
            else -> "1 Bulan"
        }
    } ?: "1 Bulan"
    val currentExpiredDate = currentMember?.expiredDate?.ifBlank { "-" } ?: "-"
    val calculatedNewEndDate = calculateRenewalEndDate(
        currentEndDate = currentExpiredDate,
        durationStr = durationStr,
        planType = currentMember?.planType ?: ""
    )
    val priceValue = currentMember?.planPrice ?: 0L
    val priceFormatted = if (priceValue > 0L) formatRupiah(priceValue) else "Rp 350.000"

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Perpanjang Membership",
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
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenAccent)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = Dimens.spacing_5)
            ) {
                // 1. Member Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
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
                        val memberName = currentMember?.fullName?.ifBlank { "Member" } ?: "Member"
                        val avatarInitial = memberName.split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                            .joinToString("")
                            .ifEmpty { "?" }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(GreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = avatarInitial,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = GreenAccent
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = memberName,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Text(
                                text = planName,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "Expired: ${formatExpiredDateDisplay(currentExpiredDate, currentMember?.createdAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_5))

                // 2. Detail Paket Perpanjangan (Sesuai Paket Member)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                    shape = RoundedCornerShape(Dimens.card_corner_radius),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacing_5)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Paket Membership",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GreenLight)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = null,
                                        tint = GreenAccent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Sesuai Paket Awal",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = GreenAccent
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Dimens.spacing_3))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BackgroundColor)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = planName,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Durasi Paket: $durationStr",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = priceFormatted,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = GreenAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(Dimens.spacing_3))

                        Text(
                            text = "Masa aktif akan diperpanjang secara otomatis sesuai dengan paket yang dipilih saat registrasi awal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_5))

                // 3. Metode Pembayaran Card (Equal-width chips with border)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                    shape = RoundedCornerShape(Dimens.card_corner_radius),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacing_5)
                    ) {
                        Text(
                            text = "Metode Pembayaran",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacing_4))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RenewalPayment.entries.forEach { payment ->
                                PaymentOptionChip(
                                    modifier = Modifier.weight(1f),
                                    payment = payment,
                                    isSelected = selectedPayment == payment,
                                    onClick = {
                                        selectedPayment = payment
                                        paymentError = null
                                    }
                                )
                            }
                        }
                        if (paymentError != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = paymentError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE53935)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_5))

                // 4. Ringkasan Perpanjangan Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                    shape = RoundedCornerShape(Dimens.card_corner_radius),
                    colors = CardDefaults.cardColors(containerColor = GreenLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacing_5)
                    ) {
                        Text(
                            text = "Ringkasan Perpanjangan",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacing_3))

                        SummaryRow("Paket", planName)
                        SummaryRow("Durasi Perpanjangan", durationStr)
                        SummaryRow("Biaya Perpanjangan", priceFormatted)
                        SummaryRow("Tanggal Berakhir Saat Ini", formatExpiredDateDisplay(currentExpiredDate, currentMember?.createdAt))
                        SummaryRow("Tanggal Berakhir Baru", calculatedNewEndDate, isHighlight = true)

                        Spacer(modifier = Modifier.height(Dimens.spacing_4))

                        Button(
                            onClick = {
                                if (selectedPayment == null) {
                                    paymentError = "Metode pembayaran wajib dipilih"
                                } else if (currentMember != null && !isSubmitting) {
                                    isSubmitting = true
                                    paymentError = null
                                    val amountLong = if (currentMember.planPrice > 0L) currentMember.planPrice else 350000L
                                    scope.launch {
                                        withContext(Dispatchers.IO) {
                                            try {
                                                val memberRepo = com.pws.primaragagym.data.repository.MemberRepositoryImpl()
                                                val paymentRepo = com.pws.primaragagym.data.repository.PaymentRepositoryImpl()

                                                memberRepo.updateMember(
                                                    currentMember.copy(
                                                        expiredDate = calculatedNewEndDate,
                                                        status = "ACTIVE"
                                                    )
                                                )

                                                paymentRepo.createPayment(
                                                    memberId = currentMember.memberId,
                                                    memberName = currentMember.fullName,
                                                    membershipId = null,
                                                    branchId = currentMember.branchId,
                                                    amount = amountLong,
                                                    paymentMethod = selectedPayment!!.displayName,
                                                    paymentType = "RENEWAL",
                                                    planName = "Perpanjang ${currentMember.planName} ($durationStr)"
                                                )

                                                withContext(Dispatchers.Main) {
                                                    isSubmitting = false
                                                    showSuccessDialog = true
                                                }
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) {
                                                    isSubmitting = false
                                                    paymentError = "Gagal memperpanjang: ${e.message}"
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.button_height),
                            enabled = !isSubmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                            shape = RoundedCornerShape(Dimens.button_corner_radius)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Konfirmasi Perpanjangan")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_8))
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    onSubmitSuccess()
                }) {
                    Text("Selesai", color = GreenAccent)
                }
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(GreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = GreenAccent,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Membership Berhasil Diperpanjang",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            },
            text = {
                val memberName = member?.fullName ?: "Member"
                Text("Membership untuk $memberName telah berhasil diperpanjang hingga $calculatedNewEndDate.")
            }
        )
    }
}

@Composable
private fun PaymentOptionChip(
    modifier: Modifier = Modifier,
    payment: RenewalPayment,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) GreenAccent else CardBackground
    val textColor = if (isSelected) Color.White else TextPrimary
    val borderColor = if (isSelected) GreenAccent else Color(0xFFE0E0E0)

    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                modifier = Modifier.size(20.dp),
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color.White,
                    unselectedColor = TextMuted
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = payment.displayName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = textColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isHighlight) GreenAccent else TextPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.3f)
        )
    }
}

private fun formatRupiah(amount: Long): String {
    val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp ${nf.format(amount)}"
}

private fun calculateRenewalEndDate(
    currentEndDate: String,
    durationStr: String,
    planType: String
): String {
    val jakartaTz = TimeZone.getTimeZone("Asia/Jakarta")
    val supportedFormats = listOf(
        SimpleDateFormat("dd MMMM yyyy, HH:mm 'WIB'", Locale("id", "ID")),
        SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale("id", "ID")),
        SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")),
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")),
        SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")),
        SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")),
        SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")),
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    ).onEach { it.timeZone = jakartaTz }

    val now = Calendar.getInstance(jakartaTz)
    var parsedDate: Date? = null

    for (sdf in supportedFormats) {
        try {
            val d = sdf.parse(currentEndDate.trim())
            if (d != null) {
                parsedDate = d
                break
            }
        } catch (_: Exception) {}
    }

    val cal = Calendar.getInstance(jakartaTz)
    // Jika tanggal kadaluarsa saat ini masih di masa depan, perpanjang dari tanggal kadaluarsa tersebut
    // Jika sudah lewat (expired) atau kosong, perpanjang mulai dari hari ini
    if (parsedDate != null && parsedDate.after(now.time)) {
        cal.time = parsedDate
    } else {
        cal.time = now.time
    }

    val dLower = durationStr.lowercase().trim()
    val digits = durationStr.filter { it.isDigit() }
    val number = digits.toIntOrNull()

    when {
        // Custom Plan (misal: "10 Hari", "45 Hari", atau type CUSTOM)
        planType.equals("CUSTOM", ignoreCase = true) -> {
            val days = number ?: 30
            cal.add(Calendar.DAY_OF_YEAR, days)
        }
        // Tahunan / Yearly
        dLower.contains("tahun") || dLower.contains("year") || planType.equals("YEARLY", ignoreCase = true) -> {
            val years = if (number != null && !dLower.contains("hari") && !dLower.contains("bulan")) number else 1
            cal.add(Calendar.YEAR, years)
        }
        // Harian / Daily
        (dLower.contains("hari") && !dLower.contains("bulan") && !dLower.contains("30")) ||
        dLower.contains("day") ||
        planType.equals("DAILY", ignoreCase = true) -> {
            val days = number ?: 1
            cal.add(Calendar.DAY_OF_YEAR, days)
        }
        // Bulanan / Monthly (misal: "30 Hari", "1 Bulan", "bulanan")
        else -> {
            val months = if (number != null && (dLower.contains("bulan") || dLower.contains("month")) && !dLower.contains("30")) number else 1
            cal.add(Calendar.MONTH, months)
        }
    }

    val outputSdf = SimpleDateFormat("dd MMMM yyyy, HH:mm 'WIB'", Locale("id", "ID")).apply {
        timeZone = jakartaTz
    }
    return outputSdf.format(cal.time)
}
