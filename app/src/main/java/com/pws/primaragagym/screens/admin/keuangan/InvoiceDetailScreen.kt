package com.pws.primaragagym.screens.admin.keuangan

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import coil.compose.AsyncImage
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pws.primaragagym.domain.model.FirestorePayment
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.BackgroundColor
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.CardBackground
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.GreenAccent
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.GreenLight
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.StatusSuccess
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextMuted
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextPrimary
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextSecondary
import com.pws.primaragagym.screens.admin.member.InvoiceReceiptData
import com.pws.primaragagym.screens.admin.member.InvoiceReceiptHelper
import com.pws.primaragagym.screens.admin.member.card.QrCodeGenerator
import com.pws.primaragagym.ui.theme.Dimens
import com.pws.primaragagym.ui.viewmodel.InvoiceDetailViewModel
import java.net.URLEncoder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

// ============================================================================
// MAIN SCREEN - DETAIL INVOICE & NOTA DIGITAL
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoiceId: String = "",
    viewModel: InvoiceDetailViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val uiState by viewModel.uiState.collectAsState()

    var showPrintDialog by remember { mutableStateOf(false) }

    LaunchedEffect(invoiceId) {
        if (invoiceId.isNotBlank()) {
            viewModel.loadDetail(invoiceId)
        }
    }

    val formatCurrency = { amount: Long ->
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0
        format.format(amount)
    }

    val formatDate = { date: java.util.Date? ->
        try {
            val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm 'WIB'", Locale("id", "ID")).apply {
                timeZone = java.util.TimeZone.getTimeZone("Asia/Jakarta")
            }
            date?.let { dateFormat.format(it) } ?: "-"
        } catch (_: Exception) { "-" }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Detail Nota & Kwitansi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Bukti resmi pembayaran Primaraga Gym",
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenAccent)
            }
        } else if (uiState.error != null || (uiState.invoice == null && !uiState.isLoading)) {
            // Error State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Receipt,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Invoice Tidak Ditemukan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.error ?: "Tidak dapat menemukan data transaksi untuk ID: $invoiceId",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.loadDetail(invoiceId) },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                    ) {
                        Text("Coba Lagi")
                    }
                }
            }
        } else {
            val payment = uiState.invoice!!
            val displayInvoiceNo = payment.invoiceNumber.ifBlank { payment.paymentId }
            val displayTitle = payment.planName.ifBlank { payment.category.ifBlank { payment.paymentType } }

            val displayMemberCode = uiState.memberCode.ifBlank {
                payment.memberCode.ifBlank {
                    if (payment.memberId.isNotBlank()) {
                        if (payment.memberId.startsWith("PRMG-")) {
                            payment.memberId
                        } else if (payment.memberId.length > 8) {
                            "PRMG-" + payment.memberId.takeLast(4).uppercase()
                        } else {
                            payment.memberId
                        }
                    } else {
                        ""
                    }
                }
            }

            // Generate large QR Code bitmap for verification
            val qrBitmap: Bitmap? = remember(displayInvoiceNo) {
                try {
                    QrCodeGenerator.generateQrBitmap(displayInvoiceNo, size = 360)
                } catch (_: Exception) {
                    null
                }
            }

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
                // ==================== NOTA RESMI PRIMARAGA GYM ====================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimens.card_corner_radius),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacing_6)
                    ) {
                        // Header Gym & Lunas Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(GreenLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Receipt,
                                        contentDescription = null,
                                        tint = GreenAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(Dimens.spacing_3))
                                Column {
                                    Text(
                                        text = "PRIMARAGA GYM",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GreenAccent
                                    )
                                    Text(
                                        text = "Kwitansi & Nota Resmi",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            // Status LUNAS Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GreenLight
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = GreenAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "LUNAS",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = GreenAccent
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Dimens.spacing_5))
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(Dimens.spacing_5))

                        // ==================== QR CODE NOTA ====================
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(170.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (qrBitmap != null) {
                                    Image(
                                        bitmap = qrBitmap.asImageBitmap(),
                                        contentDescription = "QR Code Invoice",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Receipt,
                                        contentDescription = null,
                                        tint = GreenAccent,
                                        modifier = Modifier.size(64.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = displayInvoiceNo,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = GreenAccent
                            )

                            Text(
                                text = "Scan QR Code untuk verifikasi kwitansi pembayaran",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(Dimens.spacing_5))
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(Dimens.spacing_5))

                        // ==================== RINCIAN TRANSAKSI ====================
                        Text(
                            text = "Rincian Transaksi",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacing_3))

                        InvoiceDetailRow(label = "Tanggal & Waktu", value = formatDate(payment.paidAt ?: payment.createdAt))
                        Spacer(modifier = Modifier.height(Dimens.spacing_2))
                        InvoiceDetailRow(label = "Metode Pembayaran", value = payment.paymentMethod)
                        Spacer(modifier = Modifier.height(Dimens.spacing_2))
                        InvoiceDetailRow(label = "Cabang Gym", value = payment.branchId.ifBlank { "Primaraga Gym" })

                        Spacer(modifier = Modifier.height(Dimens.spacing_4))
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(Dimens.spacing_4))

                        // ==================== DATA MEMBER ====================
                        Text(
                            text = "Data Pelanggan",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacing_3))

                        InvoiceDetailRow(label = "Nama", value = payment.memberName.ifBlank { "Umum (Non-Member)" })
                        if (displayMemberCode.isNotBlank()) {
                            Spacer(modifier = Modifier.height(Dimens.spacing_2))
                            InvoiceDetailRow(label = "ID Member", value = displayMemberCode)
                        }

                        Spacer(modifier = Modifier.height(Dimens.spacing_4))
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(Dimens.spacing_4))

                        // ==================== ITEM / PAKET ====================
                        Text(
                            text = "Item Pembayaran",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacing_3))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalOffer,
                                contentDescription = null,
                                tint = GreenAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = displayTitle,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatCurrency(payment.amount),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }

                        if (payment.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(Dimens.spacing_2))
                            Text(
                                text = "Catatan: ${payment.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(Dimens.spacing_5))
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.5.dp)
                        Spacer(modifier = Modifier.height(Dimens.spacing_4))

                        // ==================== TOTAL BAYAR ====================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Pembayaran",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = formatCurrency(payment.amount),
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = GreenAccent
                            )
                        }
                    }
                }

                if (!payment.proofUrl.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(Dimens.spacing_5))
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
                                text = "Bukti Pembayaran (${payment.paymentMethod})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing_3))
                            AsyncImage(
                                model = com.pws.primaragagym.ui.components.normalizeImageUrl(payment.proofUrl),
                                contentDescription = "Bukti Transfer / QRIS",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF5F5F5)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_6))

                // ==================== ACTION BUTTONS ====================
                // 1. WhatsApp Share Button
                Button(
                    onClick = {
                        val memberDisplay = payment.memberName.ifBlank { "Umum" }
                        val nominalDisplay = formatCurrency(payment.amount)
                        val dateDisplay = formatDate(payment.paidAt ?: payment.createdAt)

                        val message = """
                            *KWITANSI PEMBAYARAN PRIMARAGA GYM*
                            ===============================
                            *No. Invoice* : $displayInvoiceNo
                            *Tanggal*     : $dateDisplay
                            *Member*      : $memberDisplay
                            *Item*        : $displayTitle
                            *Metode*      : ${payment.paymentMethod}
                            *Status*      : LUNAS (PAID)
                            -------------------------------
                            *TOTAL*       : $nominalDisplay
                            ===============================
                            Terima kasih telah melakukan pembayaran di Primaraga Gym!
                            Simpan pesan ini sebagai bukti transaksi yang sah.
                        """.trimIndent()

                        try {
                            val url = "https://api.whatsapp.com/send?text=${URLEncoder.encode(message, "UTF-8")}"
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse(url)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Aplikasi WhatsApp tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.button_height),
                    shape = RoundedCornerShape(Dimens.button_corner_radius),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bagikan Kwitansi ke WhatsApp",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_3))

                // 2. Print / Screenshot Notice Button
                OutlinedButton(
                    onClick = { showPrintDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.button_height),
                    shape = RoundedCornerShape(Dimens.button_corner_radius),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenAccent)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Print,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cetak / Simpan Nota",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_8))
            }
        }
    }

    // Print Options Dialog
    if (showPrintDialog && uiState.invoice != null) {
        val payment = uiState.invoice!!
        val displayInvoiceNo = payment.invoiceNumber.ifBlank { payment.paymentId }
        val displayTitle = payment.planName.ifBlank { payment.category.ifBlank { payment.paymentType } }
        val dateStr = formatDate(payment.paidAt ?: payment.createdAt)
        val receiptData = InvoiceReceiptData(
            invoiceNumber = displayInvoiceNo,
            memberId = payment.memberId,
            memberName = payment.memberName.ifBlank { "Umum / Member" },
            memberCode = if (uiState.memberCode.isNotBlank()) uiState.memberCode else (payment.memberCode.ifBlank { "-" }),
            phoneNumber = "",
            planName = displayTitle,
            duration = if (payment.category == "MEMBERSHIP") "Membership" else "1 Transaksi",
            startDate = dateStr,
            expiredDate = dateStr,
            amount = payment.amount,
            paymentMethod = payment.paymentMethod,
            adminName = "Admin Kasir",
            dateStr = dateStr
        )

        AlertDialog(
            onDismissRequest = { showPrintDialog = false },
            confirmButton = {
                TextButton(onClick = { showPrintDialog = false }) {
                    Text("Tutup", color = TextSecondary)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(Dimens.card_corner_radius),
            icon = {
                Icon(
                    imageVector = Icons.Filled.Print,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Opsi Cetak Dokumen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Pilih format cetak. Dokumen dilengkapi QR Code untuk verifikasi scan di aplikasi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 1. Cetak Nota Kasir (Thermal)
                    Button(
                        onClick = {
                            showPrintDialog = false
                            InvoiceReceiptHelper.printReceipt(context, receiptData)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                    ) {
                        Icon(Icons.Filled.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cetak Nota Kasir (Thermal)", style = MaterialTheme.typography.labelMedium)
                    }

                    // 2. Cetak Invoice Resmi (A4)
                    OutlinedButton(
                        onClick = {
                            showPrintDialog = false
                            InvoiceReceiptHelper.printInvoice(context, receiptData)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenAccent)
                    ) {
                        Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cetak Invoice Resmi (A4)", style = MaterialTheme.typography.labelMedium)
                    }

                    // 3. Kirim PDF ke WhatsApp
                    OutlinedButton(
                        onClick = {
                            showPrintDialog = false
                            InvoiceReceiptHelper.sendInvoiceToWhatsApp(context, receiptData)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kirim Dokumen PDF ke WA", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        )
    }
}

// ============================================================================
// HELPER COMPOSABLE
// ============================================================================
@Composable
private fun InvoiceDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = TextPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.58f)
        )
    }
}