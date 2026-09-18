package com.pws.primaragagym.screens.admin.keuangan

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.BackgroundColor
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.CardBackground
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.CashBg
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.ExpenseBg
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.ExpenseRed
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.GreenAccent
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.GreenLight
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.QrisBg
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextMuted
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextPrimary
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextSecondary
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TransferBg
import com.pws.primaragagym.ui.components.UploadBuktiPembayaranField
import com.pws.primaragagym.ui.theme.Dimens
import com.pws.primaragagym.ui.viewmodel.AuthViewModel
import com.pws.primaragagym.ui.viewmodel.KeuanganViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ============================================================================
// ENUMS
// ============================================================================
enum class TransactionType(val label: String, val code: String) {
    INCOME("Pemasukan", "INCOME"),
    EXPENSE("Pengeluaran", "EXPENSE")
}

enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    TRANSFER("Transfer"),
    QRIS("QRIS")
}

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatatPembayaranScreen(
    keuanganViewModel: KeuanganViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSuccess: () -> Unit = {},
    onViewInvoice: () -> Unit = {}
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val authState by authViewModel.uiState.collectAsState()
    val keuanganState by keuanganViewModel.uiState.collectAsState()

    LaunchedEffect(authState.currentUser) {
        val branchId = authState.currentUser?.branchId ?: ""
        keuanganViewModel.loadSummary(branchId)
    }

    // Form State
    var transactionType by remember { mutableStateOf(TransactionType.INCOME) }
    var transactionTitle by remember { mutableStateOf("") }
    var nominal by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(PaymentMethod.CASH) }
    var paymentProofUrl by remember { mutableStateOf("") }
    var paymentProofUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var paymentProofBase64 by remember { mutableStateOf<String?>(null) }
    var catatan by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }

    var showSuccessDialog by remember { mutableStateOf(false) }

    // Validation State
    var titleError by remember { mutableStateOf(false) }
    var nominalError by remember { mutableStateOf(false) }
    var methodError by remember { mutableStateOf(false) }

    val dateDisplayFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")) }

    val openDatePicker = {
        val cal = Calendar.getInstance().apply { time = selectedDate }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val pickedCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                selectedDate = pickedCal.time
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        containerColor = BackgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Catatan Transaksi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Catat pemasukan atau pengeluaran operasional gym",
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
                .padding(top = paddingValues.calculateTopPadding())
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = if (isTablet) 32.dp else Dimens.screen_padding_horizontal,
                    vertical = Dimens.spacing_5
                )
        ) {
            // 1. Transaction Type Toggle (Pemasukan vs Pengeluaran)
            Text(
                text = "Jenis Transaksi",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_2))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
            ) {
                // Income Button
                TransactionTypeTab(
                    title = "Pemasukan",
                    subtitle = "Uang Masuk",
                    icon = Icons.Filled.TrendingUp,
                    isSelected = transactionType == TransactionType.INCOME,
                    activeColor = GreenAccent,
                    activeBg = GreenLight,
                    onClick = {
                        transactionType = TransactionType.INCOME
                        titleError = false
                    },
                    modifier = Modifier.weight(1f)
                )

                // Expense Button
                TransactionTypeTab(
                    title = "Pengeluaran",
                    subtitle = "Uang Keluar",
                    icon = Icons.Filled.TrendingDown,
                    isSelected = transactionType == TransactionType.EXPENSE,
                    activeColor = ExpenseRed,
                    activeBg = ExpenseBg,
                    onClick = {
                        transactionType = TransactionType.EXPENSE
                        titleError = false
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_5))

            // 2. Judul / Keterangan Transaksi * (Wajib Diisi)
            Text(
                text = "Judul / Keterangan Transaksi *",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_2))

            OutlinedTextField(
                value = transactionTitle,
                onValueChange = {
                    transactionTitle = it
                    titleError = false
                },
                placeholder = {
                    Text(
                        text = if (transactionType == TransactionType.INCOME)
                            "Misal: Penjualan Pocari Sweat 2 botol, Tiket Masuk Visit, Sewa Loker..."
                        else
                            "Misal: Pembelian stok minuman 3 karton, Gaji karyawan/trainer, Listrik PLN...",
                        color = TextMuted
                    )
                },
                singleLine = true,
                isError = titleError,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (transactionType == TransactionType.INCOME) GreenAccent else ExpenseRed,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    errorBorderColor = ExpenseRed,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = if (transactionType == TransactionType.INCOME) GreenAccent else ExpenseRed,
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    errorContainerColor = CardBackground
                ),
                shape = RoundedCornerShape(Dimens.input_corner_radius)
            )
            if (titleError) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Judul / keterangan transaksi wajib diisi",
                    style = MaterialTheme.typography.labelSmall,
                    color = ExpenseRed
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_5))

            // 3. Tanggal Transaksi (Date Picker Interaktif)
            Text(
                text = "Tanggal Transaksi *",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_2))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = dateDisplayFormat.format(selectedDate),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = openDatePicker) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = "Pilih Tanggal",
                                tint = if (transactionType == TransactionType.INCOME) GreenAccent else ExpenseRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (transactionType == TransactionType.INCOME) GreenAccent else ExpenseRed,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground
                    ),
                    shape = RoundedCornerShape(Dimens.input_corner_radius)
                )

                // Klik di seluruh area field untuk membuka kalender
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(Dimens.input_corner_radius))
                        .clickable(onClick = openDatePicker)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Klik jika ingin mencatat transaksi kemarin / tanggal sebelumnya",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_5))

            // 4. Nominal Transaksi
            Text(
                text = "Nominal (Rp) *",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_2))

            OutlinedTextField(
                value = nominal,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() } && newValue.length <= 12) {
                        nominal = newValue
                        nominalError = false
                    }
                },
                placeholder = {
                    Text(text = "Rp ________", color = TextMuted)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = nominalError,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (transactionType == TransactionType.INCOME) GreenAccent else ExpenseRed,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = if (transactionType == TransactionType.INCOME) GreenAccent else ExpenseRed,
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground
                ),
                shape = RoundedCornerShape(Dimens.input_corner_radius)
            )
            if (nominalError) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Nominal wajib diisi dan harus lebih dari 0",
                    style = MaterialTheme.typography.labelSmall,
                    color = ExpenseRed
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_5))

            // 5. Metode Pembayaran
            Text(
                text = "Metode Pembayaran *",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_3))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
            ) {
                PaymentMethod.entries.forEach { method ->
                    PaymentMethodChip(
                        method = method,
                        isSelected = selectedMethod == method,
                        accentColor = if (transactionType == TransactionType.INCOME) GreenAccent else ExpenseRed,
                        onClick = {
                            selectedMethod = method
                            methodError = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (methodError) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Metode pembayaran wajib dipilih",
                    style = MaterialTheme.typography.labelSmall,
                    color = ExpenseRed
                )
            }

            // Bukti Pembayaran / Struk (Transfer or QRIS)
            if (selectedMethod == PaymentMethod.TRANSFER || selectedMethod == PaymentMethod.QRIS) {
                Spacer(modifier = Modifier.height(Dimens.spacing_4))
                UploadBuktiPembayaranField(
                    proofUri = paymentProofUri,
                    proofUrl = paymentProofUrl,
                    onImageSelected = { uri, base64 ->
                        paymentProofUri = uri
                        paymentProofBase64 = base64
                        if (!base64.isNullOrBlank()) {
                            paymentProofUrl = base64
                        }
                    },
                    onProofUrlChanged = {
                        paymentProofUrl = it
                    }
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_5))

            // 6. Catatan Tambahan
            Text(
                text = "Catatan Tambahan (Opsional)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_2))

            OutlinedTextField(
                value = catatan,
                onValueChange = { catatan = it },
                placeholder = {
                    Text(text = "Keterangan tambahan transaksi...", color = TextMuted)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (transactionType == TransactionType.INCOME) GreenAccent else ExpenseRed,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground
                ),
                shape = RoundedCornerShape(Dimens.input_corner_radius)
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_8))

            // 7. Submit Button
            val submitColor = if (transactionType == TransactionType.INCOME) GreenAccent else ExpenseRed
            val isSubmitting = keuanganState.isSubmitting

            Button(
                onClick = {
                    var hasError = false
                    if (transactionTitle.isBlank()) {
                        titleError = true
                        hasError = true
                    }
                    if (nominal.isBlank() || nominal.toLongOrNull() == null || nominal.toLong() <= 0) {
                        nominalError = true
                        hasError = true
                    }
                    if (selectedMethod == null) {
                        methodError = true
                        hasError = true
                    }

                    if (!hasError && !isSubmitting) {
                        val finalProofUrl = if (selectedMethod != PaymentMethod.CASH) {
                            paymentProofUrl.trim().ifBlank { paymentProofBase64 }
                        } else {
                            null
                        }

                        val cleanTitle = transactionTitle.trim()
                        val currentBranch = authState.currentUser?.branchId ?: ""
                        keuanganViewModel.recordTransaction(
                            type = transactionType.code,
                            category = cleanTitle,
                            amount = nominal.toLong(),
                            paymentMethod = selectedMethod!!.name,
                            notes = catatan.trim(),
                            title = cleanTitle,
                            memberId = "",
                            memberName = "",
                            proofUrl = finalProofUrl,
                            transactionDate = selectedDate,
                            branchId = currentBranch,
                            onSuccess = {
                                showSuccessDialog = true
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.button_height),
                enabled = !isSubmitting,
                shape = RoundedCornerShape(Dimens.button_corner_radius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = submitColor,
                    disabledContainerColor = submitColor.copy(alpha = 0.5f)
                )
            ) {
                if (isSubmitting) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Menyimpan...",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = if (transactionType == TransactionType.INCOME) "Simpan Pemasukan" else "Simpan Pengeluaran",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_6))
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        val currentMethod = selectedMethod?.label ?: "Cash"
        val currentTitle = transactionTitle.trim()
        val currentAmount = nominal.toLongOrNull() ?: 0L

        TransactionSuccessDialog(
            type = transactionType,
            title = currentTitle,
            nominal = currentAmount,
            method = currentMethod,
            transactionDate = dateDisplayFormat.format(selectedDate),
            onViewInvoice = {
                showSuccessDialog = false
                keuanganViewModel.clearMessages()
                onViewInvoice()
            },
            onDismiss = {
                showSuccessDialog = false
                keuanganViewModel.clearMessages()
                onSuccess()
            }
        )
    }
}

// ============================================================================
// TRANSACTION TYPE TAB
// ============================================================================
@Composable
private fun TransactionTypeTab(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    activeBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, activeColor, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) activeBg else CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) activeColor else BackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) activeColor else TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

// ============================================================================
// PAYMENT METHOD CHIP
// ============================================================================
@Composable
private fun PaymentMethodChip(
    method: PaymentMethod,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val methodBg = when (method) {
        PaymentMethod.CASH -> CashBg
        PaymentMethod.TRANSFER -> TransferBg
        PaymentMethod.QRIS -> QrisBg
    }

    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, accentColor, RoundedCornerShape(Dimens.input_corner_radius))
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(Dimens.input_corner_radius),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) methodBg else methodBg.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_3),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = method.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) accentColor else TextSecondary
            )
        }
    }
}

// ============================================================================
// TRANSACTION SUCCESS DIALOG
// ============================================================================
@Composable
private fun TransactionSuccessDialog(
    type: TransactionType,
    title: String,
    nominal: Long,
    method: String,
    transactionDate: String,
    onViewInvoice: () -> Unit,
    onDismiss: () -> Unit
) {
    val formattedNominal = "Rp ${String.format("%,d", nominal).replace(",", ".")}"
    val isIncome = type == TransactionType.INCOME
    val accentColor = if (isIncome) GreenAccent else ExpenseRed
    val lightBg = if (isIncome) GreenLight else ExpenseBg

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Dimens.button_corner_radius),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor)
                ) {
                    Text("Selesai")
                }
                if (isIncome) {
                    Button(
                        onClick = onViewInvoice,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(Dimens.button_corner_radius),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                    ) {
                        Text("Lihat Invoice")
                    }
                }
            }
        },
        containerColor = CardBackground,
        shape = RoundedCornerShape(Dimens.card_corner_radius),
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(lightBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = if (isIncome) "Pemasukan Berhasil Dicatat" else "Pengeluaran Berhasil Dicatat",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(Dimens.spacing_3))
                Text(
                    text = formattedNominal,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Spacer(modifier = Modifier.height(Dimens.spacing_2))
                Text(
                    text = "$method • $transactionDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    )
}
