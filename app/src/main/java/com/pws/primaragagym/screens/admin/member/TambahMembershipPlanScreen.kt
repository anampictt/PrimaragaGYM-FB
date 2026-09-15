package com.pws.primaragagym.screens.admin.member

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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pws.primaragagym.domain.model.FirestoreMembershipPlan
import com.pws.primaragagym.screens.admin.member.MemberColors.BackgroundColor
import com.pws.primaragagym.screens.admin.member.MemberColors.CardBackground
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenAccent
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenLight
import com.pws.primaragagym.screens.admin.member.MemberColors.TextMuted
import com.pws.primaragagym.screens.admin.member.MemberColors.TextPrimary
import com.pws.primaragagym.screens.admin.member.MemberColors.TextSecondary
import com.pws.primaragagym.ui.theme.Dimens
import com.pws.primaragagym.ui.viewmodel.PlanListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahMembershipPlanScreen(
    planId: String? = null,
    viewModel: PlanListViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val horizontalPadding = if (isTablet) 32.dp else Dimens.screen_padding_horizontal

    val isEditMode = !planId.isNullOrBlank()
    val planState by viewModel.uiState.collectAsState()

    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")) }
    val today = remember { Calendar.getInstance().time }
    val defaultEndDate = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.time
    }

    var existingPlan by remember { mutableStateOf<FirestoreMembershipPlan?>(null) }
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(PlanType.MONTHLY) }
    var customStartDate by remember { mutableStateOf(dateFormat.format(today)) }
    var customEndDate by remember { mutableStateOf(dateFormat.format(defaultEndDate)) }
    var customDurationDays by remember { mutableIntStateOf(7) }
    var customDurationError by remember { mutableStateOf<String?>(null) }
    var price by remember { mutableStateOf("") }
    var maxMembers by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }

    var isSubmitting by remember { mutableStateOf(false) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }

    var showSuccessDialog by remember { mutableStateOf(false) }

    fun updateCustomDuration(start: String, end: String) {
        customStartDate = start
        customEndDate = end
        val days = calculateDaysBetween(start, end)
        customDurationDays = days
        if (days <= 0) {
            customDurationError = "Tanggal selesai harus setelah tanggal mulai (minimal 1 hari)"
        } else {
            customDurationError = null
        }
    }

    LaunchedEffect(planId, planState.rawPlans) {
        if (isEditMode && planId != null) {
            val cached = planState.rawPlans.find { it.planId == planId || it.id == planId }
            if (cached != null) {
                existingPlan = cached
                name = cached.name
                selectedType = when {
                    cached.type.equals("DAILY", ignoreCase = true) || (cached.durationType.equals("DAY", ignoreCase = true) && cached.durationValue == 1) -> PlanType.DAILY
                    cached.type.equals("YEARLY", ignoreCase = true) || cached.durationType.equals("YEAR", ignoreCase = true) -> PlanType.YEARLY
                    cached.type.equals("CUSTOM", ignoreCase = true) -> PlanType.CUSTOM
                    else -> PlanType.MONTHLY
                }
                if (cached.type.equals("CUSTOM", ignoreCase = true)) {
                    val days = cached.durationValue ?: cached.duration.filter { it.isDigit() }.toIntOrNull() ?: 7
                    customDurationDays = days
                    val cal = Calendar.getInstance()
                    val sStr = dateFormat.format(cal.time)
                    cal.add(Calendar.DAY_OF_YEAR, days)
                    val eStr = dateFormat.format(cal.time)
                    customStartDate = sStr
                    customEndDate = eStr
                }
                price = cached.price.toString()
                maxMembers = if (cached.maxMembers != null && cached.maxMembers > 0) cached.maxMembers.toString() else ""
                isActive = cached.isActive
            }

            val freshResult = viewModel.getPlanById(planId)
            val fresh = freshResult.getOrNull()
            if (fresh != null) {
                existingPlan = fresh
                name = fresh.name
                selectedType = when {
                    fresh.type.equals("DAILY", ignoreCase = true) || (fresh.durationType.equals("DAY", ignoreCase = true) && fresh.durationValue == 1) -> PlanType.DAILY
                    fresh.type.equals("YEARLY", ignoreCase = true) || fresh.durationType.equals("YEAR", ignoreCase = true) -> PlanType.YEARLY
                    fresh.type.equals("CUSTOM", ignoreCase = true) -> PlanType.CUSTOM
                    else -> PlanType.MONTHLY
                }
                if (fresh.type.equals("CUSTOM", ignoreCase = true)) {
                    val days = fresh.durationValue ?: fresh.duration.filter { it.isDigit() }.toIntOrNull() ?: 7
                    customDurationDays = days
                    val cal = Calendar.getInstance()
                    val sStr = dateFormat.format(cal.time)
                    cal.add(Calendar.DAY_OF_YEAR, days)
                    val eStr = dateFormat.format(cal.time)
                    customStartDate = sStr
                    customEndDate = eStr
                }
                price = fresh.price.toString()
                maxMembers = if (fresh.maxMembers != null && fresh.maxMembers > 0) fresh.maxMembers.toString() else ""
                isActive = fresh.isActive
            }
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit Paket" else "Tambah Paket",
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
                .verticalScroll(rememberScrollState())
                .padding(vertical = Dimens.spacing_5)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
            ) {
                // General error
                if (generalError != null) {
                    Text(
                        text = generalError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Nama Paket
                FormField(
                    label = "Nama Paket",
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                        generalError = null
                    },
                    placeholder = "Contoh: Premium Monthly",
                    error = nameError
                )

                Spacer(modifier = Modifier.height(Dimens.spacing_4))

                // Jenis Paket
                Text(
                    text = "Jenis Paket",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                PlanTypeDropdown(
                    selectedType = selectedType,
                    customDurationDays = customDurationDays,
                    onTypeSelected = { newType ->
                        selectedType = newType
                        generalError = null
                        if (newType == PlanType.CUSTOM && customDurationDays <= 0) {
                            updateCustomDuration(customStartDate, customEndDate)
                        }
                    }
                )

                if (selectedType == PlanType.CUSTOM) {
                    Spacer(modifier = Modifier.height(Dimens.spacing_4))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Dimens.card_corner_radius),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCE93D8).copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.spacing_4)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF3E5F5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DateRange,
                                        contentDescription = null,
                                        tint = Color(0xFF7B1FA2),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Atur Durasi Custom",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Pilih rentang tanggal untuk menghitung durasi paket secara otomatis",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Tanggal Mulai Perhitungan
                            PlanDatePickerField(
                                label = "Tanggal Mulai Perhitungan",
                                value = customStartDate,
                                onDateSelected = { newStart ->
                                    updateCustomDuration(newStart, customEndDate)
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Tanggal Selesai
                            PlanDatePickerField(
                                label = "Tanggal Selesai",
                                value = customEndDate,
                                onDateSelected = { newEnd ->
                                    updateCustomDuration(customStartDate, newEnd)
                                }
                            )

                            if (customDurationError != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = customDurationError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Calculated duration badge card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F5FB))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Durasi Paket Terhitung",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = "$customDurationDays Hari",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF7B1FA2)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFEDE7F6))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Masa Aktif Member",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                            color = Color(0xFF512DA8)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Member baru yang didaftarkan dengan paket ini akan otomatis memiliki masa aktif $customDurationDays hari sejak tanggal pendaftaran.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_4))

                // Harga
                FormField(
                    label = "Harga (Rp)",
                    value = price,
                    onValueChange = {
                        price = it.filter { c -> c.isDigit() }
                        priceError = null
                        generalError = null
                    },
                    placeholder = "Contoh: 350000",
                    error = priceError,
                    keyboardType = KeyboardType.Number,
                    prefix = "Rp "
                )

                Spacer(modifier = Modifier.height(Dimens.spacing_4))

                // Maksimal Member
                FormField(
                    label = "Maksimal Member",
                    value = maxMembers,
                    onValueChange = {
                        maxMembers = it.filter { c -> c.isDigit() }
                        generalError = null
                    },
                    placeholder = "Contoh: 50 (Kosongkan jika tanpa batas)",
                    keyboardType = KeyboardType.Number,
                    suffix = "Member"
                )

                Spacer(modifier = Modifier.height(Dimens.spacing_4))

                // Status Aktif
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimens.input_corner_radius),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacing_4),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Status Aktif",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = TextPrimary
                            )
                            Text(
                                text = if (isActive) "Paket dapat dipilih saat registrasi" else "Paket tidak tersedia",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GreenAccent
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_6))

                // Submit Button
                Button(
                    onClick = {
                        var hasError = false

                        if (name.isBlank()) {
                            nameError = "Nama paket wajib diisi"
                            hasError = true
                        }
                        if (selectedType == PlanType.CUSTOM && customDurationDays <= 0) {
                            customDurationError = "Durasi paket minimal 1 hari. Silakan pilih rentang tanggal yang valid."
                            hasError = true
                        }
                        if (price.isBlank()) {
                            priceError = "Harga wajib diisi"
                            hasError = true
                        }

                        if (hasError) return@Button

                        isSubmitting = true
                        generalError = null

                        val parsedPrice = price.filter { it.isDigit() }.toLongOrNull() ?: 0L
                        val parsedMax = maxMembers.filter { it.isDigit() }.toIntOrNull()
                        val durationStr = when (selectedType) {
                            PlanType.DAILY -> "1 Hari"
                            PlanType.MONTHLY -> "30 Hari"
                            PlanType.YEARLY -> "1 Tahun"
                            PlanType.CUSTOM -> "$customDurationDays Hari"
                        }
                        val durType = when (selectedType) {
                            PlanType.DAILY -> "DAY"
                            PlanType.MONTHLY -> "MONTH"
                            PlanType.YEARLY -> "YEAR"
                            PlanType.CUSTOM -> "DAY"
                        }
                        val durVal = when (selectedType) {
                            PlanType.DAILY -> 1
                            PlanType.MONTHLY -> 1
                            PlanType.YEARLY -> 1
                            PlanType.CUSTOM -> customDurationDays
                        }
                        val planTypeStr = when (selectedType) {
                            PlanType.DAILY -> "DAILY"
                            PlanType.MONTHLY -> "MONTHLY"
                            PlanType.YEARLY -> "YEARLY"
                            PlanType.CUSTOM -> "CUSTOM"
                        }

                        val targetId = planId ?: existingPlan?.planId ?: ""
                        val planObj = (existingPlan ?: FirestoreMembershipPlan(planId = targetId)).copy(
                            planId = targetId,
                            name = name.trim(),
                            type = planTypeStr,
                            durationType = durType,
                            durationValue = durVal,
                            duration = durationStr,
                            price = parsedPrice,
                            maxMembers = if (parsedMax != null && parsedMax > 0) parsedMax else null,
                            isActive = isActive
                        )

                        if (isEditMode) {
                            viewModel.updatePlan(planObj) { success, errMsg ->
                                isSubmitting = false
                                if (success) {
                                    showSuccessDialog = true
                                } else {
                                    generalError = errMsg ?: "Gagal memperbarui paket membership"
                                }
                            }
                        } else {
                            viewModel.createPlan(planObj) { success, errMsg ->
                                isSubmitting = false
                                if (success) {
                                    showSuccessDialog = true
                                } else {
                                    generalError = errMsg ?: "Gagal membuat paket membership baru"
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.button_height),
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                    shape = RoundedCornerShape(Dimens.button_corner_radius)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (isEditMode) "Simpan Perubahan" else "Simpan Paket",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_8))
            }
        }
    }

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
                    text = if (isEditMode) "Paket berhasil disimpan" else "Paket berhasil ditambahkan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            },
            text = {
                Text(
                    text = if (isEditMode) "Perubahan paket $name telah disimpan." else "Paket $name telah ditambahkan ke daftar membership plan.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    prefix: String = "",
    suffix: String = ""
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(text = placeholder, color = TextMuted)
            },
            isError = error != null,
            supportingText = if (error != null) {
                { Text(error, color = Color(0xFFE53935)) }
            } else null,
            prefix = if (prefix.isNotEmpty()) {
                { Text(prefix, color = TextMuted) }
            } else null,
            suffix = if (suffix.isNotEmpty()) {
                { Text(suffix, color = TextMuted) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
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

private data class PlanTypeOption(
    val type: PlanType,
    val durationLabel: String
)

private val defaultPlanTypeOptions = listOf(
    PlanTypeOption(PlanType.DAILY, "(1 Hari)"),
    PlanTypeOption(PlanType.MONTHLY, "(30 Hari)"),
    PlanTypeOption(PlanType.YEARLY, "(1 Tahun)"),
    PlanTypeOption(PlanType.CUSTOM, "(Custom Durasi)")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanTypeDropdown(
    selectedType: PlanType,
    customDurationDays: Int = 0,
    onTypeSelected: (PlanType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val currentOption = defaultPlanTypeOptions.find { it.type == selectedType } ?: defaultPlanTypeOptions[1]
    val displayDurationLabel = if (selectedType == PlanType.CUSTOM && customDurationDays > 0) {
        "($customDurationDays Hari)"
    } else {
        currentOption.durationLabel
    }

    val (selectedBadgeBg, selectedBadgeText) = when (selectedType) {
        PlanType.DAILY -> Pair(Color(0xFFE3F2FD), Color(0xFF1976D2))
        PlanType.MONTHLY -> Pair(Color(0xFFE8F5E9), GreenAccent)
        PlanType.YEARLY -> Pair(Color(0xFFFFF3E0), Color(0xFFF57C00))
        PlanType.CUSTOM -> Pair(Color(0xFFF3E5F5), Color(0xFF7B1FA2))
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = displayDurationLabel,
            onValueChange = {},
            readOnly = true,
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(selectedBadgeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = selectedType.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = selectedBadgeText
                    )
                }
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(Dimens.input_corner_radius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenAccent,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CardBackground)
        ) {
            defaultPlanTypeOptions.forEach { option ->
                val (badgeBg, badgeText) = when (option.type) {
                    PlanType.DAILY -> Pair(Color(0xFFE3F2FD), Color(0xFF1976D2))
                    PlanType.MONTHLY -> Pair(Color(0xFFE8F5E9), GreenAccent)
                    PlanType.YEARLY -> Pair(Color(0xFFFFF3E0), Color(0xFFF57C00))
                    PlanType.CUSTOM -> Pair(Color(0xFFF3E5F5), Color(0xFF7B1FA2))
                }

                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = option.type.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = badgeText
                                )
                            }
                            Text(
                                text = option.durationLabel,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = TextPrimary
                            )
                        }
                    },
                    onClick = {
                        onTypeSelected(option.type)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun calculateDaysBetween(startStr: String, endStr: String): Int {
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    return try {
        val start = sdf.parse(startStr.trim()) ?: return 0
        val end = sdf.parse(endStr.trim()) ?: return 0
        val diffMs = end.time - start.time
        val days = (diffMs / (1000 * 60 * 60 * 24)).toInt()
        if (days > 0) days else 0
    } catch (_: Exception) {
        0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanDatePickerField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                        onDateSelected(formatter.format(Date(millis)))
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
            DatePicker(state = datePickerState)
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
