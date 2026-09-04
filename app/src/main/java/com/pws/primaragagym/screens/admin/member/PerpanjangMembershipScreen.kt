package com.pws.primaragagym.screens.admin.member

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pws.primaragagym.screens.admin.member.MemberColors.BackgroundColor
import com.pws.primaragagym.screens.admin.member.MemberColors.CardBackground
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenAccent
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenLight
import com.pws.primaragagym.screens.admin.member.MemberColors.TextMuted
import com.pws.primaragagym.screens.admin.member.MemberColors.TextPrimary
import com.pws.primaragagym.screens.admin.member.MemberColors.TextSecondary
import com.pws.primaragagym.ui.theme.Dimens

private enum class RenewalDuration(val displayName: String, val months: Int) {
    ONE_MONTH("1 Bulan", 1),
    THREE_MONTHS("3 Bulan", 3),
    SIX_MONTHS("6 Bulan", 6),
    TWELVE_MONTHS("12 Bulan", 12)
}

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

    val member = remember(memberId) {
        dummyMembers.find { it.id == memberId } ?: dummyMembers.first()
    }

    var selectedDuration by remember { mutableStateOf(RenewalDuration.ONE_MONTH) }
    var selectedPayment by remember { mutableStateOf<RenewalPayment?>(null) }
    var paymentError by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val price = when (selectedDuration) {
        RenewalDuration.ONE_MONTH -> "Rp 350.000"
        RenewalDuration.THREE_MONTHS -> "Rp 950.000"
        RenewalDuration.SIX_MONTHS -> "Rp 1.800.000"
        RenewalDuration.TWELVE_MONTHS -> "Rp 3.000.000"
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(vertical = Dimens.spacing_5)
        ) {
            // Member Info Card
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
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(GreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.avatarInitial,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = GreenAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = member.name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Text(
                            text = member.planName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "Expired: ${member.expiredDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_5))

            // Duration Selection
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
                        text = "Pilih Durasi",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing_4))
                    RenewalDuration.entries.forEach { duration ->
                        DurationOption(
                            duration = duration,
                            isSelected = selectedDuration == duration,
                            onClick = { selectedDuration = duration }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_5))

            // Payment Method
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
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RenewalPayment.entries.forEach { payment ->
                            PaymentOptionChip(
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
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = paymentError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE53935)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_5))

            // Summary
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
                        text = "Ringkasan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing_3))
                    SummaryRow("Paket", member.planName)
                    SummaryRow("Durasi", selectedDuration.displayName)
                    SummaryRow("Harga", price)
                    SummaryRow("Tanggal Mulai", member.expiredDate)
                    SummaryRow("Tanggal Berakhir", calculateNewEndDate(member.expiredDate, selectedDuration.months))
                    Spacer(modifier = Modifier.height(Dimens.spacing_4))
                    Button(
                        onClick = {
                            if (selectedPayment == null) {
                                paymentError = "Metode pembayaran wajib dipilih"
                            } else {
                                showSuccessDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.button_height),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                        shape = RoundedCornerShape(Dimens.button_corner_radius)
                    ) {
                        Text("Konfirmasi Perpanjangan")
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_8))
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
                    Text("Tutup", color = GreenAccent)
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
                    text = "Membership berhasil diperpanjang",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            },
            text = {
                Text("Membership ${member.name} telah berhasil diperpanjang selama ${selectedDuration.displayName}.")
            }
        )
    }
}

@Composable
private fun DurationOption(
    duration: RenewalDuration,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = GreenAccent)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = duration.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}

@Composable
private fun PaymentOptionChip(
    payment: RenewalPayment,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) GreenAccent else CardBackground
    val textColor = if (isSelected) Color.White else TextPrimary

    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color.White,
                    unselectedColor = TextMuted
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = payment.displayName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = textColor
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}

private fun calculateNewEndDate(currentEndDate: String, months: Int): String {
    return when (months) {
        1 -> "30 Oktober 2026"
        3 -> "30 November 2026"
        6 -> "30 Maret 2027"
        12 -> "30 September 2027"
        else -> currentEndDate
    }
}
