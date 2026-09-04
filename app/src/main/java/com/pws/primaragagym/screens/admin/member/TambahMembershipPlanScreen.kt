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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahMembershipPlanScreen(
    planId: String? = null,
    onBackClick: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val horizontalPadding = if (isTablet) 32.dp else Dimens.screen_padding_horizontal

    val isEditMode = planId != null
    val existingPlan = planId?.let { id -> dummyMembershipPlans.find { it.id == id } }

    var name by remember { mutableStateOf(existingPlan?.name ?: "") }
    var selectedType by remember { mutableStateOf(existingPlan?.type ?: PlanType.MONTHLY) }
    var price by remember { mutableStateOf(existingPlan?.price?.replace("Rp ", "")?.replace(".", "") ?: "") }
    var duration by remember { mutableStateOf(existingPlan?.duration ?: "30 Hari") }
    var maxMembers by remember { mutableStateOf(existingPlan?.maxMembers?.toString() ?: "50") }
    var isActive by remember { mutableStateOf(existingPlan?.isActive ?: true) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var durationError by remember { mutableStateOf<String?>(null) }
    var maxMembersError by remember { mutableStateOf<String?>(null) }

    var showSuccessDialog by remember { mutableStateOf(false) }

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
                // Nama Paket
                FormField(
                    label = "Nama Paket",
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
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
                    onTypeSelected = { selectedType = it }
                )

                Spacer(modifier = Modifier.height(Dimens.spacing_4))

                // Harga
                FormField(
                    label = "Harga (Rp)",
                    value = price,
                    onValueChange = {
                        price = it.filter { c -> c.isDigit() }
                        priceError = null
                    },
                    placeholder = "Contoh: 350000",
                    error = priceError,
                    keyboardType = KeyboardType.Number,
                    prefix = "Rp "
                )

                Spacer(modifier = Modifier.height(Dimens.spacing_4))

                // Durasi
                FormField(
                    label = "Durasi",
                    value = duration,
                    onValueChange = {
                        duration = it
                        durationError = null
                    },
                    placeholder = "Contoh: 30 Hari",
                    error = durationError
                )

                Spacer(modifier = Modifier.height(Dimens.spacing_4))

                // Batas Maks Member
                FormField(
                    label = "Batas Maks Member",
                    value = maxMembers,
                    onValueChange = {
                        maxMembers = it.filter { c -> c.isDigit() }
                        maxMembersError = null
                    },
                    placeholder = "Contoh: 50",
                    error = maxMembersError,
                    keyboardType = KeyboardType.Number
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
                        if (price.isBlank()) {
                            priceError = "Harga wajib diisi"
                            hasError = true
                        }
                        if (duration.isBlank()) {
                            durationError = "Durasi wajib diisi"
                            hasError = true
                        }
                        if (maxMembers.isBlank()) {
                            maxMembersError = "Batas member wajib diisi"
                            hasError = true
                        }

                        if (!hasError) {
                            showSuccessDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.button_height),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                    shape = RoundedCornerShape(Dimens.button_corner_radius)
                ) {
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
    prefix: String = ""
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanTypeDropdown(
    selectedType: PlanType,
    onTypeSelected: (PlanType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedType.displayName,
            onValueChange = {},
            readOnly = true,
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
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CardBackground)
        ) {
            PlanType.entries.forEach { type ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = type.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}
