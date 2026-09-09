package com.pws.primaragagym.screens.admin.laporan

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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PictureAsPdf
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.BackgroundColor
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.CardBackground
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.GreenAccent
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.GreenLight
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.StatActiveBg
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.StatRevenueBg
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.TextMuted
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.TextPrimary
import com.pws.primaragagym.screens.admin.laporan.LaporanColors.TextSecondary
import com.pws.primaragagym.ui.theme.Dimens

// ============================================================================
// MENU DATA
// ============================================================================
private data class LaporanMenuItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconBackground: Color
)

private val laporanMenuItems = listOf(
    LaporanMenuItem(
        title = "Laporan Member",
        description = "Lihat data dan aktivitas member berdasarkan periode.",
        icon = Icons.Filled.Group,
        iconBackground = StatActiveBg
    ),
    LaporanMenuItem(
        title = "Laporan Keuangan",
        description = "Lihat ringkasan pemasukan dan transaksi bulanan.",
        icon = Icons.Filled.Assessment,
        iconBackground = StatRevenueBg
    ),
    LaporanMenuItem(
        title = "Export Laporan",
        description = "Export laporan ke format PDF atau Excel.",
        icon = Icons.Filled.PictureAsPdf,
        iconBackground = GreenLight
    )
)

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanScreen(
    onBackClick: () -> Unit = {},
    onLaporanMemberClick: () -> Unit = {},
    onLaporanKeuanganClick: () -> Unit = {},
    onExportClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Laporan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Pantau dan analisis aktivitas gym berdasarkan periode.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenLight,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = if (isTablet) 32.dp else Dimens.screen_padding_horizontal,
                    vertical = Dimens.spacing_5
                )
        ) {
            Text(
                text = "Menu Laporan",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPrimary,
                modifier = Modifier.padding(bottom = Dimens.spacing_4)
            )

            val onClicks = listOf(onLaporanMemberClick, onLaporanKeuanganClick, onExportClick)

            laporanMenuItems.forEachIndexed { index, menuItem ->
                LaporanMenuCard(
                    menuItem = menuItem,
                    onClick = onClicks.getOrElse(index) { {} }
                )
                if (index < laporanMenuItems.lastIndex) {
                    Spacer(modifier = Modifier.height(Dimens.spacing_3))
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_6))
        }
    }
}

// ============================================================================
// MENU CARD
// ============================================================================
@Composable
private fun LaporanMenuCard(
    menuItem: LaporanMenuItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    .background(menuItem.iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = menuItem.icon,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(Dimens.spacing_4))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = menuItem.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = menuItem.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(Dimens.spacing_2))

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = GreenAccent,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}