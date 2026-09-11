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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pws.primaragagym.ui.theme.Dimens

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

    val member = remember(memberId) {
        dummyMembers.find { it.id == memberId } ?: dummyMembers.first()
    }

    val bgColor = MemberColors.BackgroundColor
    val cardBg = MemberColors.CardBackground
    val greenAccent = MemberColors.GreenAccent
    val greenLight = MemberColors.GreenLight
    val textMuted = MemberColors.TextMuted
    val textPrimary = MemberColors.TextPrimary
    val textSecondary = MemberColors.TextSecondary

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(vertical = Dimens.spacing_5)
        ) {
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
                            text = member.avatarInitial,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = greenAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.spacing_3))
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = textPrimary
                    )
                    Text(
                        text = member.memberCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing_3))
                    MemberStatusBadge(status = member.status)
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_5))

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
                        text = "Paket",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = textPrimary,
                        modifier = Modifier.padding(bottom = Dimens.spacing_3)
                    )
                    Text(
                        text = member.planName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = greenAccent
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing_4))
                    InfoRow("Harga", member.planPrice, textMuted, textPrimary)
                    InfoRow("Tanggal Mulai", member.startDate, textMuted, textPrimary)
                    InfoRow("Tanggal Berakhir", member.expiredDate, textMuted, textPrimary)
                    InfoRow("Status", member.status.displayName, textMuted, textPrimary)

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

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                shape = RoundedCornerShape(Dimens.card_corner_radius),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                        modifier = Modifier
                            .size(20.dp)
                            .padding(0.dp)
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
                onClick = { onKartuMemberClick(member.id) }
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
                        tint = cardBg,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cetak Kartu Member",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = cardBg
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_8))
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
