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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun RiwayatTransaksiScreen(
    memberId: String,
    onBackClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val horizontalPadding = if (isTablet) 32.dp else Dimens.screen_padding_horizontal

    val member = dummyMembers.find { it.id == memberId } ?: dummyMembers.first()

    Scaffold(
        containerColor = BackgroundColor,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Member Info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .padding(top = Dimens.spacing_5),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.avatarInitial,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = GreenAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = member.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Text(
                            text = member.memberCode,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = Dimens.spacing_4,
                    bottom = Dimens.spacing_5
                )
            ) {
                items(dummyTransactions) { transaction ->
                    TransactionCard(transaction = transaction)
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(transaction: TransactionUiModel) {
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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.type.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = transaction.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = transaction.amount,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                TransactionStatusBadge(status = transaction.status)
            }
        }
    }
}

@Composable
private fun TransactionStatusBadge(status: TransactionStatus) {
    val (bgColor, textColor) = when (status) {
        TransactionStatus.PAID -> Pair(Color(0xFFE8F5E9), Color(0xFF4CAF50))
        TransactionStatus.PENDING -> Pair(Color(0xFFFFF3E0), Color(0xFFFF9800))
        TransactionStatus.CANCELLED -> Pair(Color(0xFFFFEBEE), Color(0xFFF44336))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.displayName,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = textColor
        )
    }
}
