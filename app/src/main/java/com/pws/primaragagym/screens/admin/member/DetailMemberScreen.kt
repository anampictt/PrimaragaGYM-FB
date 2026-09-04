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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
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
import com.pws.primaragagym.screens.admin.member.MemberColors.BackgroundColor
import com.pws.primaragagym.screens.admin.member.MemberColors.CardBackground
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenAccent
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenLight
import com.pws.primaragagym.screens.admin.member.MemberColors.TextMuted
import com.pws.primaragagym.screens.admin.member.MemberColors.TextPrimary
import com.pws.primaragagym.screens.admin.member.MemberColors.TextSecondary
import com.pws.primaragagym.ui.theme.Dimens

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailMemberScreen(
    memberId: String,
    onBackClick: () -> Unit = {},
    onPerpanjangClick: () -> Unit = {},
    onUpgradeClick: () -> Unit = {},
    onRiwayatClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    // Find member from dummy data
    val member = remember(memberId) {
        dummyMembers.find { it.id == memberId } ?: dummyMembers.first()
    }

    val horizontalPadding = if (isTablet) 32.dp else Dimens.screen_padding_horizontal

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Member",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextPrimary
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
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBackground
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
            // Profile Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                shape = RoundedCornerShape(Dimens.card_corner_radius),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacing_6),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(GreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.avatarInitial,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = GreenAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(Dimens.spacing_4))

                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = member.memberCode,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(Dimens.spacing_3))

                    MemberStatusBadge(status = member.status)
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_5))

            // Membership Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                shape = RoundedCornerShape(Dimens.card_corner_radius),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacing_5)
                ) {
                    Text(
                        text = "Membership",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(Dimens.spacing_4))

                    Text(
                        text = member.planName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = GreenAccent
                    )

                    Spacer(modifier = Modifier.height(Dimens.spacing_3))

                    Row {
                        Text(
                            text = "${member.startDate} - ${member.expiredDate}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(Dimens.spacing_4))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onPerpanjangClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenAccent
                            ),
                            shape = RoundedCornerShape(Dimens.button_corner_radius)
                        ) {
                            Text("Perpanjang")
                        }
                        OutlinedButton(
                            onClick = onUpgradeClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(Dimens.button_corner_radius)
                        ) {
                            Text(
                                text = "Upgrade",
                                color = GreenAccent
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_5))

            // Personal Information Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                shape = RoundedCornerShape(Dimens.card_corner_radius),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacing_5)
                ) {
                    Text(
                        text = "Informasi Pribadi",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(Dimens.spacing_4))

                    InfoRow(label = "Nama Lengkap", value = member.name)
                    InfoRow(label = "ID Member", value = member.memberCode)
                    InfoRow(label = "Nomor Telepon", value = member.phone)
                    InfoRow(label = "Email", value = member.email)
                    InfoRow(label = "Alamat", value = member.address)
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_5))

            // Riwayat Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                shape = RoundedCornerShape(Dimens.card_corner_radius),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    RiwayatMenuItem(
                        title = "Check-in & Check-out",
                        onClick = { }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MemberColors.DividerColor)
                            .padding(horizontal = Dimens.spacing_5)
                    )
                    RiwayatMenuItem(
                        title = "Riwayat Transaksi",
                        onClick = onRiwayatClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_8))
        }
    }
}

// ============================================================================
// INFO ROW
// ============================================================================
@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}

// ============================================================================
// RIWAYAT MENU ITEM
// ============================================================================
@Composable
private fun RiwayatMenuItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.spacing_4)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Transparent)
            .padding(Dimens.spacing_2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = GreenAccent,
            modifier = Modifier.size(20.dp)
        )
    }
}

// Dummy for import reference
