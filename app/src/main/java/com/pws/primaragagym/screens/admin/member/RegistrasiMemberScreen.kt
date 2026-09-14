package com.pws.primaragagym.screens.admin.member

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pws.primaragagym.ui.viewmodel.PlanListViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pws.primaragagym.screens.admin.member.MemberColors.BackgroundColor
import com.pws.primaragagym.screens.admin.member.MemberColors.CardBackground
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenAccent
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenLight
import com.pws.primaragagym.screens.admin.member.MemberColors.TextMuted
import com.pws.primaragagym.screens.admin.member.MemberColors.TextPrimary
import com.pws.primaragagym.screens.admin.member.MemberColors.TextSecondary
import com.pws.primaragagym.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ============================================================================
// PAYMENT METHOD
// ============================================================================
private enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    TRANSFER("Transfer"),
    QRIS("QRIS")
}

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrasiMemberScreen(
    onBackClick: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {},
    planViewModel: PlanListViewModel = viewModel()
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    val planState by planViewModel.uiState.collectAsState()
    val availablePlans = if (planState.plans.isNotEmpty()) planState.plans else dummyMembershipPlans

    val horizontalPadding = if (isTablet) 32.dp else Dimens.screen_padding_horizontal

    // Form state
    var name by remember { mutableStateOf("") }
    var memberId by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedPlan by remember { mutableStateOf<MembershipPlanUiModel?>(null) }
    var paymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    val todayFormatted = remember {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        sdf.format(Date())
    }
    var startDate by remember { mutableStateOf(todayFormatted) }

    // Validation errors
    var nameError by remember { mutableStateOf<String?>(null) }
    var memberIdError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var planError by remember { mutableStateOf<String?>(null) }
    var paymentError by remember { mutableStateOf<String?>(null) }

    // Dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showPlanPicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Registrasi Member Baru",
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
            // Photo Upload
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, GreenAccent.copy(alpha = 0.5f), CircleShape)
                    .background(GreenLight)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.AddAPhoto,
                        contentDescription = "Upload Foto",
                        tint = GreenAccent,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Upload Foto",
                        style = MaterialTheme.typography.labelSmall,
                        color = GreenAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_5))

            // Form Fields
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
            ) {
                // Name
                FormTextField(
                    label = "Nama Lengkap",
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    error = nameError,
                    placeholder = "Masukkan nama lengkap"
                )

                Spacer(modifier = Modifier.height(Dimens.spacing_4))

                // Member ID
                FormTextField(
                    label = "ID Member",
                    value = memberId,
                    onValueChange = {
                        memberId = it
                        memberIdError = null
                    },
                    error = memberIdError,
                    placeholder = "Contoh: MBR-001"
                )

                Spacer(modifier = Modifier.height(Dimens.spacing_4))

                // Phone
                FormTextField(
                    label = "Nomor Telepon",
                    value = phone,
                    onValueChange = {
                        phone = it
                        phoneError = null
                    },
                    error = phoneError,
                    placeholder = "Contoh: 08123456789",
                    keyboardType = KeyboardType.Phone
                )

                Spacer(modifier = Modifier.height(Dimens.spacing_4))

                // Email
                FormTextField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Contoh: email@email.com",
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(Dimens.spacing_4))

                // Address
                FormTextField(
                    label = "Alamat",
                    value = address,
                    onValueChange = { address = it },
                    placeholder = "Masukkan alamat lengkap",
                    singleLine = false,
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(Dimens.spacing_4))

                // Membership Plan Picker
                Text(
                    text = "Membership Plan",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                PlanPickerCard(
                    selectedPlan = selectedPlan,
                    onClick = { showPlanPicker = true }
                )
                if (planError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = planError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE53935)
                    )
                }

                if (selectedPlan != null) {
                    Spacer(modifier = Modifier.height(Dimens.spacing_4))

                    // Plan Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = GreenLight
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(Dimens.spacing_4)
                        ) {
                            Text(
                                text = selectedPlan!!.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = GreenAccent
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Harga: ${selectedPlan!!.price}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Durasi: ${selectedPlan!!.duration}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tanggal Mulai: $startDate",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            val endDate = calculateEndDate(startDate, selectedPlan!!)
                            Text(
                                text = "Tanggal Berakhir: $endDate",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = GreenAccent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_4))

                // Tanggal Mulai
                DatePickerField(
                    label = "Tanggal Mulai",
                    value = startDate,
                    onDateSelected = { startDate = it }
                )

                Spacer(modifier = Modifier.height(Dimens.spacing_4))

                // Payment Method
                Text(
                    text = "Metode Pembayaran",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PaymentMethod.entries.forEach { method ->
                        PaymentChip(
                            method = method,
                            isSelected = paymentMethod == method,
                            onClick = {
                                paymentMethod = method
                                paymentError = null
                            }
                        )
                    }
                }
                if (paymentError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = paymentError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE53935)
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_6))

                // Submit Button
                Button(
                    onClick = {
                        var hasError = false

                        if (name.isBlank()) {
                            nameError = "Nama wajib diisi"
                            hasError = true
                        }
                        if (memberId.isBlank()) {
                            memberIdError = "ID Member wajib diisi"
                            hasError = true
                        }
                        if (phone.isBlank()) {
                            phoneError = "Nomor telepon wajib diisi"
                            hasError = true
                        }
                        if (selectedPlan == null) {
                            planError = "Paket wajib dipilih"
                            hasError = true
                        }
                        if (paymentMethod == null) {
                            paymentError = "Metode pembayaran wajib dipilih"
                            hasError = true
                        }

                        if (!hasError) {
                            showSuccessDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.button_height),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenAccent
                    ),
                    shape = RoundedCornerShape(Dimens.button_corner_radius)
                ) {
                    Text(
                        text = "Simpan Member",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_8))
            }
        }
    }

    // Plan Picker Dialog
    if (showPlanPicker) {
        PlanPickerDialog(
            plans = availablePlans,
            onPlanSelected = {
                selectedPlan = it
                planError = null
                showPlanPicker = false
            },
            onDismiss = { showPlanPicker = false }
        )
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    onSubmitSuccess()
                }) {
                    Text("Tutup", color = GreenAccent)
                }
            },
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
                    text = "Member berhasil didaftarkan",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            text = {
                Column {
                    Text("Apa yang ingin Anda lakukan selanjutnya?")
                    Spacer(modifier = Modifier.height(Dimens.spacing_4))
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cetak Kartu")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Kirim via WhatsApp")
                    }
                }
            }
        )
    }
}

// ============================================================================
// FORM TEXT FIELD
// ============================================================================
@Composable
private fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = TextMuted
                )
            },
            isError = error != null,
            supportingText = if (error != null) {
                { Text(error, color = Color(0xFFE53935)) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = keyboardType
            ),
            shape = RoundedCornerShape(Dimens.input_corner_radius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenAccent,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground
            )
        )
    }
}

// ============================================================================
// DATE PICKER FIELD
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = androidx.compose.material3.rememberDatePickerState()

    if (showDatePicker) {
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale("id", "ID"))
                        onDateSelected(formatter.format(java.util.Date(millis)))
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = GreenAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                placeholder = {
                    Text(
                        text = "Pilih Tanggal",
                        color = TextMuted
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "Pilih Tanggal",
                        tint = TextSecondary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.input_corner_radius),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenAccent,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    disabledContainerColor = CardBackground,
                    disabledBorderColor = Color(0xFFE0E0E0),
                    disabledTextColor = TextPrimary,
                    disabledTrailingIconColor = TextSecondary
                )
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(Dimens.input_corner_radius))
                    .clickable { showDatePicker = true }
            )
        }
    }
}

// ============================================================================
// PLAN PICKER CARD
// ============================================================================
@Composable
private fun PlanPickerCard(
    selectedPlan: MembershipPlanUiModel?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Dimens.input_corner_radius),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_4),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedPlan?.name ?: "Pilih Paket",
                style = MaterialTheme.typography.bodyMedium,
                color = if (selectedPlan != null) TextPrimary else TextMuted,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}

// ============================================================================
// PLAN PICKER DIALOG
// ============================================================================
@Composable
private fun PlanPickerDialog(
    plans: List<MembershipPlanUiModel>,
    onPlanSelected: (MembershipPlanUiModel) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pilih Paket Membership",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (plans.isEmpty()) {
                    Text(
                        text = "Belum ada paket membership tersedia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    plans.forEach { plan ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onPlanSelected(plan) },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = CardBackground
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = plan.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${plan.price} - ${plan.duration}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                val (badgeBg, badgeText) = when (plan.type) {
                                    PlanType.DAILY -> Pair(Color(0xFFE3F2FD), Color(0xFF1976D2))
                                    PlanType.MONTHLY -> Pair(Color(0xFFE8F5E9), GreenAccent)
                                    PlanType.YEARLY -> Pair(Color(0xFFFFF3E0), Color(0xFFF57C00))
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(badgeBg)
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = plan.type.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = badgeText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSecondary)
            }
        }
    )
}

// ============================================================================
// PAYMENT CHIP
// ============================================================================
@Composable
private fun PaymentChip(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) GreenAccent else CardBackground
    val textColor = if (isSelected) Color.White else TextPrimary
    val borderColor = if (isSelected) GreenAccent else Color(0xFFE0E0E0)

    Card(
        modifier = Modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color.White,
                    unselectedColor = TextMuted
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = method.displayName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = textColor
            )
        }
    }
}

// ============================================================================
// HELPER
// ============================================================================
private fun calculateEndDate(startDateStr: String, plan: MembershipPlanUiModel): String {
    val supportedFormats = listOf(
        SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")),
        SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID")),
        SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID")),
        SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID")),
        SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
    )

    var parsedDate: Date? = null
    for (sdf in supportedFormats) {
        try {
            parsedDate = sdf.parse(startDateStr.trim())
            if (parsedDate != null) break
        } catch (_: Exception) {}
    }

    val cal = Calendar.getInstance().apply {
        time = parsedDate ?: Date()
    }

    val durationLower = plan.duration.lowercase().trim()
    val digitsOnly = plan.duration.filter { it.isDigit() }
    val extractedNumber = digitsOnly.toIntOrNull()

    when {
        // e.g. "30 Hari" or "15 Hari"
        durationLower.contains("hari") || durationLower.contains("day") -> {
            val days = extractedNumber ?: 1
            cal.add(Calendar.DAY_OF_YEAR, days)
        }
        // e.g. "1 Bulan" or "3 Bulan" or "6 Bulan"
        durationLower.contains("bulan") || durationLower.contains("month") -> {
            val months = extractedNumber ?: 1
            cal.add(Calendar.MONTH, months)
        }
        // e.g. "1 Tahun" or "Year"
        durationLower.contains("tahun") || durationLower.contains("year") -> {
            val years = extractedNumber ?: 1
            cal.add(Calendar.YEAR, years)
        }
        // Fallback by PlanType
        plan.type == PlanType.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
        plan.type == PlanType.YEARLY -> cal.add(Calendar.YEAR, 1)
        else -> cal.add(Calendar.MONTH, 1) // default monthly
    }

    val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    return outputFormat.format(cal.time)
}
