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
import androidx.compose.material.icons.filled.ArrowDownward
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pws.primaragagym.screens.admin.member.MemberColors.BackgroundColor
import com.pws.primaragagym.screens.admin.member.MemberColors.CardBackground
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenAccent
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenLight
import com.pws.primaragagym.screens.admin.member.MemberColors.TextMuted
import com.pws.primaragagym.screens.admin.member.MemberColors.TextPrimary
import com.pws.primaragagym.screens.admin.member.MemberColors.TextSecondary
import com.pws.primaragagym.ui.theme.Dimens

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

    val member = remember(memberId) {
        dummyMembers.find { it.id == memberId } ?: dummyMembers.first()
    }

    var selectedPlan by remember { mutableStateOf(dummyMembershipPlans.find { it.name == member.planName }) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val currentPlan = dummyMembershipPlans.find { it.name == member.planName }
    val selisih = if (selectedPlan != null && currentPlan != null) {
        val selected = extractPrice(selectedPlan!!.price)
        val current = extractPrice(currentPlan.price)
        val diff = selected - current
        if (diff >= 0) "Rp ${String.format("%,.0f", diff.toDouble()).replace(",", ".")}" else "Hemat Rp ${String.format("%,.0f", (-diff).toDouble()).replace(",", ".")}"
    } else ""

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Upgrade / Downgrade",
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
            // Current Plan
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
                        .padding(Dimens.spacing_5),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Paket Saat Ini",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentPlan?.name ?: member.planName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GreenAccent
                    )
                    Text(
                        text = currentPlan?.price ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // Arrow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.spacing_3),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowDownward,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(32.dp)
                )
            }

            // New Plan Selection
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
                        text = "Paket Baru",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing_4))

                    dummyMembershipPlans.filter { it.type == PlanType.MONTHLY }.forEach { plan ->
                        PlanSelectionItem(
                            plan = plan,
                            isSelected = selectedPlan?.id == plan.id,
                            onClick = { selectedPlan = plan }
                        )
                    }
                }
            }

            if (selectedPlan != null && selectedPlan!!.name != member.planName) {
                Spacer(modifier = Modifier.height(Dimens.spacing_5))

                // Selisih
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                    shape = RoundedCornerShape(Dimens.card_corner_radius),
                    colors = CardDefaults.cardColors(containerColor = GreenLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacing_5),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Selisih",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Text(
                            text = selisih,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GreenAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_5))

                Button(
                    onClick = { showSuccessDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
                        .height(Dimens.button_height),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                    shape = RoundedCornerShape(Dimens.button_corner_radius)
                ) {
                    Text("Konfirmasi Perubahan")
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
                    text = "Paket berhasil diubah",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            },
            text = {
                Text("${member.name} telah berhasil diubah ke paket ${selectedPlan?.name}.")
            }
        )
    }
}

@Composable
private fun PlanSelectionItem(
    plan: MembershipPlanUiModel,
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = plan.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )
            Text(
                text = "${plan.price} - ${plan.duration}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

private fun extractPrice(priceStr: String): Int {
    return priceStr
        .replace("Rp ", "")
        .replace(".", "")
        .replace(",", "")
        .toIntOrNull() ?: 0
}
