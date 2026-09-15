package com.pws.primaragagym.screens.admin.member.card

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pws.primaragagym.screens.admin.member.MemberColors

private val CardGreen = Color(0xFF32A060)
private val CardDarkGreen = Color(0xFF236B47)
private val CardLightGreen = Color(0xFFE8F5E9)
private val CardBackground = Color.White
private val CardTextPrimary = Color(0xFF1A1A1A)
private val CardTextSecondary = Color(0xFF6B6B6B)
private val CardTextMuted = Color(0xFF9E9E9E)

@Composable
fun MemberCard(
    data: MemberCardData,
    modifier: Modifier = Modifier,
    width: Dp = 340.dp,
    cornerRadius: Dp = 20.dp
) {
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(data.qrContent) {
        qrBitmap = QrCodeGenerator.generateQrBitmap(data.qrContent)
    }

    Card(
        modifier = modifier
            .width(width),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "PRIMARAGA GYM",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = CardGreen
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Profile + Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(CardLightGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = data.avatarInitial,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = CardGreen
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = data.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = CardTextPrimary,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = data.memberCode,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = CardGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusBadgeCard(status = data.status)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Plan
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardLightGreen)
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                Text(
                    text = data.planName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = CardGreen,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Aktif",
                        style = MaterialTheme.typography.labelSmall,
                        color = CardTextMuted
                    )
                    Text(
                        text = data.startDate,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = CardTextPrimary
                    )
                }
                if (data.dateOfBirth.isNotBlank() && data.dateOfBirth != "-") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Tgl Lahir",
                            style = MaterialTheme.typography.labelSmall,
                            color = CardTextMuted
                        )
                        Text(
                            text = data.dateOfBirth,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = CardTextPrimary
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Berakhir",
                        style = MaterialTheme.typography.labelSmall,
                        color = CardTextMuted
                    )
                    Text(
                        text = data.expiredDate,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = CardTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // QR Code (Persegi)
            qrBitmap?.let { bitmap ->
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "QR Code Member",
                        modifier = Modifier.size(112.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "PRIMARAGA GYM",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                ),
                color = CardTextMuted
            )
        }
    }
}

@Composable
private fun StatusBadgeCard(status: String) {
    val bgColor = when (status.lowercase()) {
        "active" -> Color(0xFFE8F5E9)
        "expiring soon" -> Color(0xFFFFF3E0)
        "expired" -> Color(0xFFFFEBEE)
        else -> Color(0xFFF5F5F5)
    }
    val textColor = when (status.lowercase()) {
        "active" -> Color(0xFF4CAF50)
        "expiring soon" -> Color(0xFFFF9800)
        "expired" -> Color(0xFFF44336)
        else -> Color(0xFF9E9E9E)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = textColor
        )
    }
}
