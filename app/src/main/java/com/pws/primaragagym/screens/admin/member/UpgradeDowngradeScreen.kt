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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SwapVert
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
import androidx.compose.ui.unit.dp
import com.pws.primaragagym.domain.model.FirestoreMember
import com.pws.primaragagym.domain.model.FirestoreMembershipPlan
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

private enum class UpgradePayment(val displayName: String) {
    CASH("Cash"),
    TRANSFER("Transfer"),
    QRIS("QRIS")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeDowngradeScreen(
    memberId: String,
    onBackClick: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val horizontalPadding = if (isTablet) 32.dp else Dimens.screen_padding_horizontal

    val scope = rememberCoroutineScope()
    var member by remember { mutableStateOf<FirestoreMember?>(null) }
    var availablePlans by remember { mutableStateOf<List<FirestoreMembershipPlan>>(emptyList()) }
    var selectedPlan by remember { mutableStateOf<FirestoreMembershipPlan?>(null) }
    var selectedPayment by remember { mutableStateOf<UpgradePayment?>(UpgradePayment.CASH) }
    var paymentError by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(memberId) {
        if (memberId.isNotBlank()) {
            isLoading = true
            withContext(Dispatchers.IO) {
                try {
                    val memberRepo = com.pws.primaragagym.data.repository.MemberRepositoryImpl()
                    val planRepo = com.pws.primaragagym.data.repository.MembershipPlanRepositoryImpl()

                    val memberRes = memberRepo.getMemberById(memberId)
                    val plansRes = planRepo.getMembershipPlans(isActive = true)

                    val loadedMember = memberRes.getOrNull()
                    val loadedPlans = plansRes.getOrNull()

                    val finalPlans = if (!loadedPlans.isNullOrEmpty()) {
                        loadedPlans
                    } else {
                        dummyMembershipPlans.map {
                            FirestoreMembershipPlan(
                                planId = it.id,
                                name = it.name,
                                type = it.type.name,
                                duration = it.duration,
                                price = it.price.filter { c -> c.isDigit() }.toLongOrNull() ?: 0L,
                                isActive = true
                            )
                        }
                    }

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
                                    planPrice = dummy.planPrice.filter { it.isDigit() }.toLongOrNull() ?: 350000L,
                                    duration = "1 Bulan",
                                    planType = "MONTHLY"
                                )
                            }
                        }
                        availablePlans = finalPlans
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
    val currentPlanName = currentMember?.planName?.ifBlank { "Membership" } ?: "Membership"
    val currentPrice = currentMember?.planPrice ?: 0L
    val currentDuration = currentMember?.duration?.ifBlank {
        when (currentMember.planType.uppercase()) {
            "DAILY" -> "1 Hari"
            "YEARLY" -> "1 Tahun"
            else -> "30 Hari"
        }
    } ?: "30 Hari"
    val currentExpiredDate = currentMember?.expiredDate?.ifBlank { "-" } ?: "-"

    val selected = selectedPlan
    val isDifferentPlan = selected != null &&
        selected.planId != currentMember?.planId &&
        !selected.name.equals(currentMember?.planName, ignoreCase = true)

    val currentTier = run {
        val t = (currentMember?.planType ?: "").lowercase().trim()
        val d = currentDuration.lowercase().trim()
        when {
            t == "yearly" || d.contains("tahun") || d.contains("year") || d.contains("365") -> 3
            t == "monthly" || d.contains("bulan") || d.contains("month") || d.contains("30") -> 2
            t == "daily" || d.contains("hari") || d.contains("day") -> 1
            else -> 2
        }
    }

    val selectedTier = if (selected != null) {
        val t = selected.type.lowercase().trim()
        val d = selected.duration.lowercase().trim()
        when {
            t == "yearly" || d.contains("tahun") || d.contains("year") || d.contains("365") -> 3
            t == "monthly" || d.contains("bulan") || d.contains("month") || d.contains("30") -> 2
            t == "daily" || d.contains("hari") || d.contains("day") -> 1
            else -> 2
        }
    } else currentTier

    val priceDiff = if (selected != null) selected.price - currentPrice else 0L

    val isDowngrade = if (selected != null && isDifferentPlan) {
        when {
            selectedTier < currentTier -> true
            selectedTier > currentTier -> false
            else -> priceDiff < 0L
        }
    } else false

    val actionTitle = if (isDowngrade) "Downgrade Membership" else "Upgrade Membership"
    val actionVerb = if (isDowngrade) "di-downgrade" else "di-upgrade"
    val actionPlanPrefix = if (isDowngrade) "Downgrade ke" else "Upgrade ke"
    val paymentType = if (isDowngrade) "DOWNGRADE" else "UPGRADE"

    val selisihText = when {
        isDowngrade -> "Hemat ${formatRupiah(-priceDiff)} (Downgrade)"
        priceDiff > 0L -> "+ ${formatRupiah(priceDiff)} (Biaya Upgrade)"
        priceDiff < 0L -> "Hemat ${formatRupiah(-priceDiff)} (Downgrade)"
        else -> "Rp 0 (Harga Sama)"
    }

    val todayStr = remember {
        SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Calendar.getInstance().time)
    }

    val calculatedNewEndDate = if (selected != null) {
        calculateUpgradeEndDate(selected)
    } else {
        currentExpiredDate
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isDifferentPlan) actionTitle else "Upgrade / Downgrade",
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
                // 1. Member Profile Card
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
                                text = "Kode: ${currentMember?.memberCode?.ifBlank { "-" } ?: "-"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "Expired Saat Ini: ${formatExpiredDateDisplay(currentExpiredDate, currentMember?.createdAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_4))

                // 2. Paket Saat Ini Card
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
                            .padding(Dimens.spacing_4)
                    ) {
                        Text(
                            text = "Paket Saat Ini",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = currentPlanName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Durasi: $currentDuration",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = if (currentPrice > 0L) formatRupiah(currentPrice) else "-",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                    }
                }

                // Arrow Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.spacing_2),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDownward,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // 3. Pilih Paket Baru Card
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
                                text = "Pilih Paket Baru",
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
                                        imageVector = Icons.Filled.SwapVert,
                                        contentDescription = null,
                                        tint = GreenAccent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Paket Tersedia",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = GreenAccent
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Pilih paket yang tersedia untuk mengganti atau meng-upgrade membership member.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp, bottom = Dimens.spacing_4)
                        )

                        if (availablePlans.isEmpty()) {
                            Text(
                                text = "Belum ada paket membership lain yang tersedia.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                availablePlans.forEach { plan ->
                                    val isCurrent = (plan.planId.isNotBlank() && plan.planId == currentMember?.planId) ||
                                            plan.name.equals(currentMember?.planName, ignoreCase = true)
                                    val isSelected = selectedPlan?.planId == plan.planId ||
                                            (selectedPlan == null && isCurrent)

                                    PlanSelectionCard(
                                        plan = plan,
                                        isSelected = isSelected && !isCurrent,
                                        isCurrentPlan = isCurrent,
                                        onClick = {
                                            if (!isCurrent) {
                                                selectedPlan = plan
                                                paymentError = null
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Ringkasan Upgrade & Pembayaran (Hanya jika memilih paket baru yang berbeda)
                if (isDifferentPlan && selected != null) {
                    Spacer(modifier = Modifier.height(Dimens.spacing_5))

                    // Metode Pembayaran Card
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
                                UpgradePayment.entries.forEach { payment ->
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

                    // Ringkasan Perubahan Paket
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
                                text = "Rincian $actionTitle",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing_3))

                            SummaryRow("Paket Lama", currentPlanName)
                            SummaryRow("Paket Baru", selected.name)
                            SummaryRow("Durasi Paket Baru", getPlanDurationDisplay(selected))
                            SummaryRow("Harga Paket Baru", formatRupiah(selected.price))
                            SummaryRow("Selisih Biaya", selisihText)
                            SummaryRow("Tanggal Mulai Baru", todayStr)
                            SummaryRow("Tanggal Berakhir Baru", calculatedNewEndDate, isHighlight = true)

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Masa aktif paket baru dihitung mulai hari ini ($todayStr).",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )

                            Spacer(modifier = Modifier.height(Dimens.spacing_4))

                            Button(
                                onClick = {
                                    if (selectedPayment == null) {
                                        paymentError = "Metode pembayaran wajib dipilih"
                                    } else if (currentMember != null && !isSubmitting) {
                                        isSubmitting = true
                                        paymentError = null

                                        val amountToPay = if (!isDowngrade && priceDiff > 0L) priceDiff else selected.price
                                        val durationFormatted = getPlanDurationDisplay(selected)

                                        scope.launch {
                                            withContext(Dispatchers.IO) {
                                                try {
                                                    val memberRepo = com.pws.primaragagym.data.repository.MemberRepositoryImpl()
                                                    val paymentRepo = com.pws.primaragagym.data.repository.PaymentRepositoryImpl()

                                                    // Update member di Firestore
                                                    memberRepo.updateMember(
                                                        currentMember.copy(
                                                            planId = selected.planId.ifBlank { selected.id },
                                                            planName = selected.name,
                                                            planPrice = selected.price,
                                                            planType = selected.type,
                                                            duration = durationFormatted,
                                                            startDate = todayStr,
                                                            expiredDate = calculatedNewEndDate,
                                                            status = "ACTIVE"
                                                        )
                                                    )

                                                    // Catat transaksi pembayaran upgrade/downgrade
                                                    paymentRepo.createPayment(
                                                        memberId = currentMember.memberId,
                                                        memberName = currentMember.fullName,
                                                        membershipId = null,
                                                        branchId = currentMember.branchId,
                                                        amount = amountToPay,
                                                        paymentMethod = selectedPayment!!.displayName,
                                                        paymentType = paymentType,
                                                        planName = "$actionPlanPrefix ${selected.name} ($durationFormatted)"
                                                    )

                                                    withContext(Dispatchers.Main) {
                                                        isSubmitting = false
                                                        showSuccessDialog = true
                                                    }
                                                } catch (e: Exception) {
                                                    withContext(Dispatchers.Main) {
                                                        isSubmitting = false
                                                        paymentError = "Gagal memproses $actionTitle: ${e.message}"
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
                                    Text("Konfirmasi $actionTitle")
                                }
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
                    text = "$actionTitle Berhasil",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            },
            text = {
                val memberName = member?.fullName ?: "Member"
                val newPlan = selectedPlan?.name ?: "Paket Baru"
                Text("Membership untuk $memberName telah berhasil $actionVerb ke paket $newPlan hingga $calculatedNewEndDate.")
            }
        )
    }
}

@Composable
private fun PlanSelectionCard(
    plan: FirestoreMembershipPlan,
    isSelected: Boolean,
    isCurrentPlan: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isSelected -> GreenAccent
        isCurrentPlan -> Color(0xFFBDBDBD)
        else -> Color(0xFFE0E0E0)
    }
    val containerBg = when {
        isSelected -> GreenLight.copy(alpha = 0.5f)
        isCurrentPlan -> Color(0xFFF9F9F9)
        else -> CardBackground
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isCurrentPlan, onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = if (!isCurrentPlan) onClick else null,
                enabled = !isCurrentPlan,
                modifier = Modifier.size(20.dp),
                colors = RadioButtonDefaults.colors(
                    selectedColor = GreenAccent,
                    unselectedColor = TextMuted
                )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        color = if (isCurrentPlan) TextMuted else TextPrimary
                    )
                    if (isCurrentPlan) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE0E0E0))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Paket Saat Ini",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = TextSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GreenLight)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = getPlanDurationDisplay(plan),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = GreenAccent
                        )
                    }
                    if (plan.maxMembers != null && plan.maxMembers > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFEDE7F6))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Maks ${plan.maxMembers} Member",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFF673AB7)
                            )
                        }
                    }
                    if (plan.description.isNotBlank()) {
                        Text(
                            text = plan.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatRupiah(plan.price),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isCurrentPlan) TextMuted else GreenAccent
            )
        }
    }
}

@Composable
private fun PaymentOptionChip(
    modifier: Modifier = Modifier,
    payment: UpgradePayment,
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
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isHighlight) GreenAccent else TextPrimary
        )
    }
}

private fun formatRupiah(amount: Long): String {
    val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp ${nf.format(amount)}"
}

private fun getPlanDurationDisplay(plan: FirestoreMembershipPlan): String {
    val d = plan.duration.trim()
    val dLower = d.lowercase()
    val tLower = plan.type.lowercase().trim()

    return when {
        tLower == "custom" || plan.type.equals("CUSTOM", ignoreCase = true) -> if (d.isNotBlank()) "Custom ($d)" else "Custom"
        dLower.contains("365") || dLower.contains("tahun") || dLower.contains("year") || tLower == "yearly" -> "Tahunan (1 Tahun)"
        dLower.contains("30") || dLower.contains("bulan") || dLower.contains("month") || tLower == "monthly" -> "Bulanan (30 Hari)"
        dLower.contains("hari") || dLower.contains("day") || tLower == "daily" -> "Harian (1 Hari)"
        d.isNotBlank() -> d
        else -> when (plan.type.uppercase()) {
            "DAILY" -> "Harian (1 Hari)"
            "YEARLY" -> "Tahunan (1 Tahun)"
            "CUSTOM" -> "Custom"
            else -> "Bulanan (30 Hari)"
        }
    }
}

private fun calculateUpgradeEndDate(newPlan: FirestoreMembershipPlan): String {
    val jakartaTz = TimeZone.getTimeZone("Asia/Jakarta")
    val cal = Calendar.getInstance(jakartaTz) // Selalu dihitung mulai dari hari dan jam saat ini di Asia/Jakarta

    val dLower = newPlan.duration.lowercase().trim()
    val tLower = newPlan.type.lowercase().trim()
    val dtLower = newPlan.durationType.lowercase().trim()
    val digits = newPlan.duration.filter { it.isDigit() }
    val number = digits.toIntOrNull()

    when {
        // Custom Plan (misal: "10 Hari", "45 Hari", type CUSTOM)
        tLower == "custom" || dtLower == "custom" -> {
            val days = when {
                newPlan.durationValue != null && newPlan.durationValue > 0 -> newPlan.durationValue
                number != null && number > 0 -> number
                else -> 30
            }
            cal.add(Calendar.DAY_OF_YEAR, days)
        }
        // Tahunan / Yearly (365 hari, 1 tahun, YEARLY, YEAR)
        dLower.contains("tahun") || dLower.contains("year") || dLower.contains("365") ||
        tLower == "yearly" || dtLower == "year" -> {
            val years = if (number != null && !dLower.contains("hari") && !dLower.contains("bulan") && !dLower.contains("365")) number else 1
            cal.add(Calendar.YEAR, years)
        }
        // Harian / Daily (1 hari, DAILY, DAY)
        (dLower.contains("hari") && !dLower.contains("bulan") && !dLower.contains("30") && !dLower.contains("365")) ||
        dLower.contains("day") ||
        tLower == "daily" || dtLower == "day" -> {
            val days = if (number != null && number < 30) number else 1
            cal.add(Calendar.DAY_OF_YEAR, days)
        }
        // Bulanan / Monthly (30 hari, 1 bulan, MONTHLY, MONTH)
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
