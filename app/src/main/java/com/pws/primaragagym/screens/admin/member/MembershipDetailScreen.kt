package com.pws.primaragagym.screens.admin.member

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import com.pws.primaragagym.domain.model.FirestoreMember
import com.pws.primaragagym.ui.theme.Dimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipDetailScreen(
    memberId: String,
    onBackClick: () -> Unit = {},
    onPerpanjangClick: () -> Unit = {},
    onUpgradeClick: () -> Unit = {},
    onRiwayatClick: () -> Unit = {},
    onKartuMemberClick: (String) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val horizontalPadding = if (isTablet) 32.dp else Dimens.screen_padding_horizontal

    var member by remember { mutableStateOf<FirestoreMember?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val bgColor = MemberColors.BackgroundColor
    val cardBg = MemberColors.CardBackground
    val greenAccent = MemberColors.GreenAccent
    val greenLight = MemberColors.GreenLight
    val textMuted = MemberColors.TextMuted
    val textPrimary = MemberColors.TextPrimary
    val textSecondary = MemberColors.TextSecondary

    LaunchedEffect(memberId) {
        if (memberId.isNotBlank()) {
            isLoading = true
            errorMessage = null
            withContext(Dispatchers.IO) {
                try {
                    val memberRepo = com.pws.primaragagym.data.repository.MemberRepositoryImpl()
                    val membershipRepo = com.pws.primaragagym.data.repository.MembershipRepositoryImpl()
                    val result = memberRepo.getMemberById(memberId)
                    result.fold(
                        onSuccess = { m ->
                            var plan = m.planName
                            var expired = m.expiredDate
                            var start = m.startDate
                            var price = m.planPrice

                            if (plan.isBlank() || expired.isBlank()) {
                                val activeMs = membershipRepo.getActiveMembership(m.memberId).getOrNull()
                                if (activeMs != null) {
                                    if (plan.isBlank()) plan = activeMs.planName
                                    if (price == 0L) price = activeMs.price
                                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                    if (expired.isBlank()) expired = activeMs.endDate?.let { sdf.format(it) } ?: ""
                                    if (start.isBlank()) start = activeMs.startDate?.let { sdf.format(it) } ?: ""
                                }
                            }

                            val resolved = m.copy(
                                planName = plan,
                                expiredDate = expired,
                                startDate = start,
                                planPrice = price
                            )

                            withContext(Dispatchers.Main) {
                                member = resolved
                                isLoading = false
                            }
                        },
                        onFailure = { err ->
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
                                        planPrice = dummy.planPrice.filter { it.isDigit() }.toLongOrNull() ?: 0L
                                    )
                                } else {
                                    errorMessage = err.message ?: "Data member tidak ditemukan"
                                }
                                isLoading = false
                            }
                        }
                    )
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        errorMessage = e.message ?: "Gagal memuat detail member"
                        isLoading = false
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Membership",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cardBg
                )
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
                CircularProgressIndicator(color = greenAccent)
            }
        } else if (member == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "Data member tidak ditemukan",
                    style = MaterialTheme.typography.bodyLarge,
                    color = textPrimary
                )
            }
        } else {
            val currentMember = member!!
            val name = currentMember.fullName.ifBlank { "Member" }
            val initials = name.split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .joinToString("")
                .ifEmpty { "?" }
            val memberCode = currentMember.memberCode.ifBlank { currentMember.memberId }
            val planName = currentMember.planName.ifBlank { "Membership" }
            val planPriceStr = if (currentMember.planPrice > 0L) {
                "Rp " + java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")).format(currentMember.planPrice)
            } else {
                "-"
            }
            val startDateStr = currentMember.startDate.ifBlank { "-" }
            val expiredDateStr = currentMember.expiredDate.ifBlank { "-" }
            val statusEnum = resolveMemberStatus(currentMember.status, currentMember.expiredDate, currentMember.createdAt)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = Dimens.spacing_5)
            ) {
                // Profile Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                    shape = RoundedCornerShape(Dimens.card_corner_radius),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacing_6),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(greenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = greenAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(Dimens.spacing_3))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = textPrimary
                        )
                        Text(
                            text = memberCode,
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacing_3))
                        MemberStatusBadge(status = statusEnum)
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_5))

                // Paket Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                    shape = RoundedCornerShape(Dimens.card_corner_radius),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacing_5)
                    ) {
                        Text(
                            text = "Paket Membership",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = textPrimary,
                            modifier = Modifier.padding(bottom = Dimens.spacing_3)
                        )
                        Text(
                            text = planName,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = greenAccent
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacing_4))
                        InfoRow("Harga", planPriceStr, textMuted, textPrimary)
                        InfoRow("Tanggal Mulai", startDateStr, textMuted, textPrimary)
                        InfoRow("Tanggal Berakhir", formatExpiredDateDisplay(expiredDateStr, currentMember.createdAt), textMuted, textPrimary)
                        InfoRow("Status", statusEnum.displayName, textMuted, textPrimary)

                        Spacer(modifier = Modifier.height(Dimens.spacing_4))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onPerpanjangClick,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = greenAccent),
                                shape = RoundedCornerShape(Dimens.button_corner_radius)
                            ) {
                                Text("Perpanjang")
                            }
                            OutlinedButton(
                                onClick = onUpgradeClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(Dimens.button_corner_radius)
                            ) {
                                Text("Upgrade", color = greenAccent)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_5))

                // Riwayat Transaksi Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                    shape = RoundedCornerShape(Dimens.card_corner_radius),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    onClick = onRiwayatClick
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacing_4),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Riwayat Transaksi",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = null,
                            tint = greenAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_4))

                // Cetak Kartu Member Button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                    shape = RoundedCornerShape(Dimens.card_corner_radius),
                    colors = CardDefaults.cardColors(containerColor = greenAccent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    onClick = { onKartuMemberClick(currentMember.memberId.ifBlank { currentMember.id }) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacing_4),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cetak Kartu Member",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_8))
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, labelColor: Color, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = labelColor)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = valueColor)
    }
}
