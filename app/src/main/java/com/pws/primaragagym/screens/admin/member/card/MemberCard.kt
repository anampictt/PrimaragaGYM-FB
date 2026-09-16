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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextOverflow

private val CardGreen = Color(0xFF32A060)
private val CardDarkGreen = Color(0xFF236B47)
private val CardLightGreen = Color(0xFFE8F5E9)
private val CardBackground = Color.White
private val CardTextPrimary = Color(0xFF1A1A1A)
private val CardTextSecondary = Color(0xFF6B6B6B)
private val CardTextMuted = Color(0xFF9E9E9E)

/**
 * Format string tanggal untuk tampilan ringkas kartu member (contoh: "16 Sep 2026").
 * Menghapus jam/WIB jika ada dan memendekkan bulan agar muat sempurna di layar HP.
 */
fun formatCardDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank() || dateStr == "-") return "-"

    var clean = dateStr.trim()
    if (clean.contains(",")) {
        clean = clean.substringBefore(",").trim()
    } else if (clean.contains("T")) {
        clean = clean.substringBefore("T").trim()
    }

    val patterns = listOf(
        "yyyy-MM-dd",
        "dd MMMM yyyy",
        "d MMMM yyyy",
        "dd MMM yyyy",
        "d MMM yyyy",
        "dd-MM-yyyy",
        "dd/MM/yyyy",
        "yyyy/MM/dd"
    )
    val outSdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id", "ID"))

    for (pattern in patterns) {
        try {
            val sdf = java.text.SimpleDateFormat(pattern, java.util.Locale("id", "ID"))
            sdf.isLenient = false
            val parsed = sdf.parse(clean)
            if (parsed != null) {
                return outSdf.format(parsed)
            }
        } catch (_: Exception) {}

        try {
            val sdfEn = java.text.SimpleDateFormat(pattern, java.util.Locale.ENGLISH)
            sdfEn.isLenient = false
            val parsed = sdfEn.parse(clean)
            if (parsed != null) {
                return outSdf.format(parsed)
            }
        } catch (_: Exception) {}
    }

    return clean
}

@Composable
fun MemberCard(
    data: MemberCardData,
    modifier: Modifier = Modifier,
    width: Dp? = 340.dp,
    cornerRadius: Dp = 20.dp
) {
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(data.qrContent) {
        qrBitmap = QrCodeGenerator.generateQrBitmap(data.qrContent)
    }

    val cardModifier = if (width != null) {
        modifier.width(width)
    } else {
        modifier
            .fillMaxWidth()
            .widthIn(max = 360.dp)
    }

    Card(
        modifier = cardModifier,
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
                // Avatar with visible fallback and border
                val initialText = if (data.avatarInitial.isNotBlank()) {
                    data.avatarInitial
                } else {
                    data.name.firstOrNull()?.uppercaseChar()?.toString() ?: "M"
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(CardLightGreen)
                        .border(2.dp, CardGreen.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Selalu render inisial sebagai fallback di background
                    Text(
                        text = initialText,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = CardGreen
                    )

                    // Jika ada photoUrl, tampilkan AsyncImage di atas inisial
                    if (!data.photoUrl.isNullOrBlank()) {
                        coil.compose.AsyncImage(
                            model = com.pws.primaragagym.ui.components.normalizeImageUrl(data.photoUrl),
                            contentDescription = "Foto ${data.name}",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
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
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
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

            // Dates Section - Box rapi dengan 2 kolom terbobot seimbang
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF8F9FA))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Baris 1: Mulai Aktif & Berakhir
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Mulai Aktif",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp
                                ),
                                color = CardTextMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatCardDate(data.startDate),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = CardTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Garis pemisah vertikal
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .height(24.dp)
                                .width(1.dp)
                                .background(Color(0xFFE0E0E0))
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Berakhir",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp
                                ),
                                color = CardTextMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatCardDate(data.expiredDate),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = CardTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Baris 2: Tanggal Lahir (jika ada)
                    if (data.dateOfBirth.isNotBlank() && data.dateOfBirth != "-") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFEEEEEE))
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tgl Lahir",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp
                                ),
                                color = CardTextMuted
                            )
                            Text(
                                text = formatCardDate(data.dateOfBirth),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = CardTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // QR Code (Persegi dengan border rapi)
            qrBitmap?.let { bitmap ->
                Box(
                    modifier = Modifier
                        .size(124.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(12.dp)),
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
