package com.pws.primaragagym.ui.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pws.primaragagym.ui.theme.GreenPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Tipe status feedback untuk popup beranimasi
 */
enum class StatusPopupType(
    val defaultTitle: String,
    val defaultMessage: String,
    val icon: ImageVector,
    val isSuccess: Boolean
) {
    SUCCESS(
        defaultTitle = "Berhasil!",
        defaultMessage = "Operasi telah berhasil diselesaikan.",
        icon = Icons.Filled.Check,
        isSuccess = true
    ),
    SUCCESS_ADD(
        defaultTitle = "Data Berhasil Ditambahkan!",
        defaultMessage = "Data baru telah tersimpan dengan aman di sistem.",
        icon = Icons.Filled.Check,
        isSuccess = true
    ),
    SUCCESS_EDIT(
        defaultTitle = "Perubahan Berhasil Disimpan!",
        defaultMessage = "Data telah berhasil diperbarui.",
        icon = Icons.Filled.Check,
        isSuccess = true
    ),
    SUCCESS_DELETE(
        defaultTitle = "Data Berhasil Dihapus!",
        defaultMessage = "Data yang dipilih telah dihapus dari sistem.",
        icon = Icons.Filled.Delete,
        isSuccess = true
    ),
    SUCCESS_LOGIN(
        defaultTitle = "Login Berhasil!",
        defaultMessage = "Selamat datang kembali di Primaraga Gym.",
        icon = Icons.Filled.Shield,
        isSuccess = true
    ),
    SUCCESS_LOGOUT(
        defaultTitle = "Logout Berhasil!",
        defaultMessage = "Sampai jumpa kembali! Sesi Anda telah diakhiri.",
        icon = Icons.AutoMirrored.Filled.ExitToApp,
        isSuccess = true
    ),
    ERROR(
        defaultTitle = "Terjadi Kesalahan",
        defaultMessage = "Gagal memproses permintaan. Silakan coba lagi.",
        icon = Icons.Filled.Close,
        isSuccess = false
    )
}

/**
 * Komponen Dialog Status Beranimasi (Popup Sukses / Gagal).
 * Memiliki animasi bounce/spring saat muncul, efek ikon membal,
 * latar belakang gelap dengan fade-in, serta mendukung auto-dismiss atau tombol konfirmasi.
 */
@Composable
fun AnimatedStatusPopup(
    visible: Boolean,
    type: StatusPopupType = StatusPopupType.SUCCESS,
    title: String? = null,
    message: String? = null,
    confirmButtonText: String? = "Selesai",
    autoDismissMs: Long? = null,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val isSuccess = type.isSuccess
    val primaryColor = if (isSuccess) GreenPrimary else Color(0xFFE53935)
    val lightBgColor = if (isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    val displayTitle = title ?: type.defaultTitle
    val displayMessage = message ?: type.defaultMessage

    // Animasi skala ikon (bounce pop-in)
    val iconScale = remember { Animatable(0.2f) }
    val cardScale = remember { Animatable(0.85f) }
    val cardAlpha = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            launch {
                cardAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                )
            }
            launch {
                cardScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
            launch {
                delay(100)
                iconScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioHighBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }

            // Auto dismiss jika diset
            if (autoDismissMs != null && autoDismissMs > 0L) {
                delay(autoDismissMs)
                onDismiss()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f * cardAlpha.value))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        // Jika tidak ada auto dismiss, klik di luar juga bisa menutup jika tombol ada
                        if (confirmButtonText != null) {
                            onDismiss()
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .widthIn(max = 380.dp)
                    .scale(cardScale.value)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* Mencegah klik menembus ke backdrop */ }
                    ),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Badge Ikon Beranimasi
                    Box(
                        modifier = Modifier
                            .scale(iconScale.value)
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(lightBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = type.icon,
                            contentDescription = if (isSuccess) "Sukses" else "Gagal",
                            tint = primaryColor,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Judul Popup
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color(0xFF1E293B),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Deskripsi Pesan
                    Text(
                        text = displayMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )

                    // Tombol Konfirmasi (Opsional)
                    if (!confirmButtonText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 4.dp
                            )
                        ) {
                            Text(
                                text = confirmButtonText,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
