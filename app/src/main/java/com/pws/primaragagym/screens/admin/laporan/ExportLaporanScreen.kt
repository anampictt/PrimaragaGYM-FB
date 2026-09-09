package com.pws.primaragagym.screens.admin.laporan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
// REPORT TYPES
// ============================================================================
private data class ExportReportType(val label: String, val icon: ImageVector, val iconBg: Color)
private data class ExportFormat(val label: String, val description: String, val icon: ImageVector, val iconBg: Color)
private data class ExportFilter(val label: String)

// ============================================================================
// MOCK DATA
// ============================================================================
private val reportTypes = listOf(
    ExportReportType("Laporan Member", Icons.Filled.Group, StatActiveBg),
    ExportReportType("Laporan Keuangan", Icons.Filled.Description, StatRevenueBg)
)

private val exportFormats = listOf(
    ExportFormat("PDF", "Dokumen siap dicetak atau dibagikan.", Icons.Filled.PictureAsPdf, GreenLight),
    ExportFormat("Excel", "Data dapat dianalisis kembali.", Icons.Filled.TableChart, StatActiveBg)
)

private val exportFilters = listOf(
    ExportFilter("September 2026"),
    ExportFilter("Custom")
)

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportLaporanScreen(
    onBackClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    var selectedReportType by remember { mutableIntStateOf(0) }
    var selectedFormat by remember { mutableIntStateOf(0) }
    var selectedFilter by remember { mutableIntStateOf(0) }
    var showExportSuccess by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Export Laporan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Pilih laporan dan format yang ingin diekspor.",
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
                    containerColor = Color.White,
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
            // ===== STEP 1: PILIH JENIS LAPORAN =====
            Text(
                text = "Jenis Laporan",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_3))

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing_3)) {
                reportTypes.forEachIndexed { index, type ->
                    ExportTypeCard(
                        type = type,
                        isSelected = selectedReportType == index,
                        onClick = { selectedReportType = index }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_6))

            // ===== STEP 2: PILIH PERIODE =====
            Text(
                text = "Periode",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_3))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_2)
            ) {
                exportFilters.forEachIndexed { index, filter ->
                    FilterChip(
                        selected = selectedFilter == index,
                        onClick = { selectedFilter = index },
                        label = {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenAccent,
                            selectedLabelColor = Color.White,
                            containerColor = CardBackground,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            if (selectedFilter == 1) {
                Spacer(modifier = Modifier.height(Dimens.spacing_3))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
                ) {
                    DateFieldReadOnly(
                        label = "Tanggal Mulai",
                        value = "01 September 2026",
                        modifier = Modifier.weight(1f)
                    )
                    DateFieldReadOnly(
                        label = "Tanggal Akhir",
                        value = "30 September 2026",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_6))

            // ===== STEP 3: PILIH FORMAT =====
            Text(
                text = "Format Export",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_3))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
            ) {
                exportFormats.forEachIndexed { index, format ->
                    ExportFormatCard(
                        format = format,
                        isSelected = selectedFormat == index,
                        onClick = { selectedFormat = index },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_8))

            // ===== STEP 4: PREVIEW =====
            ExportPreviewCard(
                reportType = reportTypes[selectedReportType].label,
                period = if (selectedFilter == 0) "September 2026" else "01 Sep 2026 - 30 Sep 2026",
                format = exportFormats[selectedFormat].label,
                memberCount = "128",
                activeCount = "112",
                newCount = "16",
                onExportClick = { showPreview = true }
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_6))
        }
    }

    // Preview Dialog
    if (showPreview) {
        ExportPreviewDialog(
            reportType = reportTypes[selectedReportType].label,
            period = if (selectedFilter == 0) "September 2026" else "01 September 2026 - 30 September 2026",
            format = exportFormats[selectedFormat].label,
            memberCount = "128",
            activeCount = "112",
            newCount = "16",
            onExportClick = {
                showPreview = false
                showExportSuccess = true
            },
            onDismiss = { showPreview = false }
        )
    }

    // Success Dialog
    if (showExportSuccess) {
        AlertDialog(
            onDismissRequest = { showExportSuccess = false },
            confirmButton = {
                TextButton(onClick = { showExportSuccess = false }) {
                    Text("Selesai", color = GreenAccent)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(Dimens.card_corner_radius),
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
                    text = "Export berhasil disiapkan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Fitur pembuatan file akan diintegrasikan pada tahap berikutnya.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        )
    }
}

// ============================================================================
// EXPORT TYPE CARD
// ============================================================================
@Composable
private fun ExportTypeCard(
    type: ExportReportType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Dimens.card_corner_radius),
        border = if (isSelected) BorderStroke(2.dp, GreenAccent) else BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GreenLight else CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) GreenAccent else type.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = type.icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else GreenAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = type.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(GreenAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color(0xFFB0B0B0), CircleShape)
                )
            }
        }
    }
}

// ============================================================================
// EXPORT FORMAT CARD
// ============================================================================
@Composable
private fun ExportFormatCard(
    format: ExportFormat,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(Dimens.card_corner_radius),
        border = if (isSelected) BorderStroke(2.dp, GreenAccent) else BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GreenLight else CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) GreenAccent else format.iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = format.icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else GreenAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(GreenAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color(0xFFB0B0B0), CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = format.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = format.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ============================================================================
// DATE FIELD READ ONLY
// ============================================================================
@Composable
private fun DateFieldReadOnly(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = TextPrimary,
                disabledBorderColor = Color(0xFFE0E0E0),
                disabledContainerColor = CardBackground
            ),
            shape = RoundedCornerShape(Dimens.input_corner_radius)
        )
    }
}

// ============================================================================
// EXPORT PREVIEW CARD
// ============================================================================
@Composable
private fun ExportPreviewCard(
    reportType: String,
    period: String,
    format: String,
    memberCount: String,
    activeCount: String,
    newCount: String,
    onExportClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.card_corner_radius),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_5)
        ) {
            Text(
                text = "Preview",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = Dimens.spacing_4)
            )

            PreviewRow(label = "Laporan", value = reportType)
            PreviewRow(label = "Periode", value = period)
            PreviewRow(label = "Format", value = format)
            PreviewRow(label = "Jumlah Member", value = memberCount)
            PreviewRow(label = "Member Aktif", value = activeCount)
            PreviewRow(label = "Member Baru", value = newCount)

            Spacer(modifier = Modifier.height(Dimens.spacing_5))

            Button(
                onClick = onExportClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.button_height),
                shape = RoundedCornerShape(Dimens.button_corner_radius),
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
            ) {
                Text(
                    text = "Export Laporan",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ============================================================================
// PREVIEW ROW
// ============================================================================
@Composable
private fun PreviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

// ============================================================================
// EXPORT PREVIEW DIALOG
// ============================================================================
@Composable
private fun ExportPreviewDialog(
    reportType: String,
    period: String,
    format: String,
    memberCount: String,
    activeCount: String,
    newCount: String,
    onExportClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(
                modifier = Modifier.padding(Dimens.spacing_3),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(Dimens.button_corner_radius),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenAccent)
                ) {
                    Text("Batal")
                }
                Button(
                    onClick = onExportClick,
                    shape = RoundedCornerShape(Dimens.button_corner_radius),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                ) {
                    Text("Export")
                }
            }
        },
        containerColor = CardBackground,
        shape = RoundedCornerShape(Dimens.card_corner_radius),
        title = {
            Text(
                text = "Konfirmasi Export",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Laporan:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Text(
                    text = reportType,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Periode:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Text(
                    text = period,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Format:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Text(
                    text = format,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenAccent
                )
                Spacer(modifier = Modifier.height(Dimens.spacing_4))
                PreviewRow(label = "Jumlah Member", value = memberCount)
                PreviewRow(label = "Member Aktif", value = activeCount)
                PreviewRow(label = "Member Baru", value = newCount)
            }
        }
    )
}
