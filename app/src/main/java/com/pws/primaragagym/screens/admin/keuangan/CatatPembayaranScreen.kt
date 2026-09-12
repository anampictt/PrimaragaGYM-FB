package com.pws.primaragagym.screens.admin.keuangan

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.pws.primaragagym.ui.viewmodel.KeuanganViewModel
import com.pws.primaragagym.ui.viewmodel.MemberListViewModel
import com.pws.primaragagym.ui.viewmodel.AuthViewModel
import com.pws.primaragagym.screens.admin.member.MemberUiModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.BackgroundColor
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.CardBackground
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.CashBg
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.GreenAccent
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.GreenLight
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.QrisBg
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.StatTransactionBg
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextMuted
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextPrimary
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextSecondary
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TransferBg
import com.pws.primaragagym.ui.theme.Dimens
// ============================================================================
// MOCK DATA
// ============================================================================
data class PaymentType(
    val value: String,
    val label: String
)

private val paymentTypes = listOf(
    PaymentType("new", "Membership Baru"),
    PaymentType("extend", "Perpanjang Membership"),
    PaymentType("upgrade", "Upgrade Membership"),
    PaymentType("downgrade", "Downgrade Membership"),
    PaymentType("other", "Lainnya")
)

enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    TRANSFER("Transfer"),
    QRIS("QRIS")
}

private enum class PaymentFilter {
    ALL, MEMBERSHIP, MEMBER, SYSTEM
}

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatatPembayaranScreen(
    memberListViewModel: MemberListViewModel = viewModel(),
    keuanganViewModel: KeuanganViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSuccess: () -> Unit = {},
    onViewInvoice: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val authState by authViewModel.uiState.collectAsState()
    val memberListState by memberListViewModel.uiState.collectAsState()
    val keuanganState by keuanganViewModel.uiState.collectAsState()

    LaunchedEffect(authState.currentUser) {
        val branchId = authState.currentUser?.branchId
        if (branchId != null) {
            memberListViewModel.loadMembers(branchId, reset = true)
            keuanganViewModel.loadSummary(branchId) // also sets branchId
        }
    }

    LaunchedEffect(keuanganState.successMessage) {
        if (keuanganState.successMessage != null) {
            // we handle dialog inside the view
        }
    }

    var selectedMember by remember { mutableStateOf<MemberUiModel?>(null) }
    var selectedPaymentType by remember { mutableStateOf<PaymentType?>(null) }
    var nominal by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var catatan by remember { mutableStateOf("") }
    var showMemberSearch by remember { mutableStateOf(false) }
    var showPaymentTypeDropdown by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Validation
    var memberError by remember { mutableStateOf(false) }
    var paymentTypeError by remember { mutableStateOf(false) }
    var nominalError by remember { mutableStateOf(false) }
    var methodError by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()
    
    val todayDate = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Catat Pembayaran",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = if (isTablet) 32.dp else Dimens.screen_padding_horizontal,
                    vertical = Dimens.spacing_5
                )
        ) {
            // Member Selection
            Text(
                text = "Cari Member",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_2))

            if (selectedMember != null) {
                SelectedMemberCard(
                    member = selectedMember!!,
                    onClear = { selectedMember = null }
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMemberSearch = true },
                    shape = RoundedCornerShape(Dimens.input_corner_radius),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacing_4),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimens.spacing_3))
                        Text(
                            text = "Cari nama / ID member...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            }
            if (memberError) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Member wajib dipilih",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_5))

            // Payment Type
            Text(
                text = "Jenis Pembayaran",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_2))

            Box {
                OutlinedTextField(
                    value = selectedPaymentType?.label ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = {
                        Text(
                            text = "Pilih jenis pembayaran",
                            color = TextMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPaymentTypeDropdown = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = TextPrimary,
                        disabledBorderColor = if (paymentTypeError) Color(0xFFF44336) else Color(0xFFE0E0E0),
                        disabledContainerColor = CardBackground,
                        disabledPlaceholderColor = TextMuted
                    ),
                    shape = RoundedCornerShape(Dimens.input_corner_radius)
                )
                DropdownMenu(
                    expanded = showPaymentTypeDropdown,
                    onDismissRequest = { showPaymentTypeDropdown = false }
                ) {
                    paymentTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.label) },
                            onClick = {
                                selectedPaymentType = type
                                showPaymentTypeDropdown = false
                                paymentTypeError = false
                            }
                        )
                    }
                }
            }
            if (paymentTypeError) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Jenis pembayaran wajib dipilih",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_5))

            // Nominal
            Text(
                text = "Nominal",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
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
                    focusedBorderColor = GreenAccent,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = GreenAccent,
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
                    color = Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_5))

            // Payment Method
            Text(
                text = "Metode Pembayaran",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
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
                    color = Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_5))

            // Tanggal
            Text(
                text = "Tanggal Pembayaran",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_2))

            OutlinedTextField(
                value = todayDate,
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

            Spacer(modifier = Modifier.height(Dimens.spacing_5))

            // Catatan
            Text(
                text = "Catatan",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_2))

            OutlinedTextField(
                value = catatan,
                onValueChange = { catatan = it },
                placeholder = {
                    Text(text = "Opsional", color = TextMuted)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenAccent,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = GreenAccent,
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground
                ),
                shape = RoundedCornerShape(Dimens.input_corner_radius)
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_8))

            // Submit Button
            Button(
                onClick = {
                    var hasError = false
                    if (selectedMember == null) {
                        memberError = true
                        hasError = true
                    }
                    if (selectedPaymentType == null) {
                        paymentTypeError = true
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
                    if (!hasError) {
                        val member = selectedMember
                        if (member != null && selectedMethod != null && selectedPaymentType != null) {
                            keuanganViewModel.recordPayment(
                                memberId = member.id,
                                memberName = member.name,
                                membershipId = null, // Can map this if needed
                                amount = nominal.toLong(),
                                paymentMethod = selectedMethod!!.name,
                                paymentType = selectedPaymentType!!.value,
                                planName = member.planName
                            )
                            showSuccessDialog = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.button_height),
                enabled = !keuanganState.isLoading,
                shape = RoundedCornerShape(Dimens.button_corner_radius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenAccent
                )
            ) {
                Text(
                    text = "Simpan Pembayaran",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_6))
        }
    }

    // Member Search Bottom Sheet
    if (showMemberSearch) {
        ModalBottomSheet(
            onDismissRequest = { showMemberSearch = false },
            sheetState = sheetState,
            containerColor = CardBackground
        ) {
            MemberSearchBottomSheet(
                members = memberListState.members,
                onMemberSelected = { member ->
                    selectedMember = member
                    showMemberSearch = false
                    memberError = false
                }
            )
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        val member = selectedMember
        val pType = selectedPaymentType
        val pMethod = selectedMethod

        if (member != null && pType != null && pMethod != null) {
            PaymentSuccessDialog(
                member = member,
                paymentType = pType.label,
                nominal = nominal.toLong(),
                method = pMethod.label,
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
}

// ============================================================================
// SELECTED MEMBER CARD
// ============================================================================
@Composable
private fun SelectedMemberCard(
    member: MemberUiModel,
    onClear: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(Dimens.card_corner_radius),
        colors = CardDefaults.cardColors(containerColor = GreenLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_4),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GreenAccent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name.take(2).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(Dimens.spacing_3))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "${member.memberCode} • ${member.planName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            TextButton(onClick = onClear) {
                Text(
                    text = "Ganti",
                    color = GreenAccent,
                    style = MaterialTheme.typography.labelMedium
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val methodColor = when (method) {
        PaymentMethod.CASH -> CashBg
        PaymentMethod.TRANSFER -> TransferBg
        PaymentMethod.QRIS -> QrisBg
    }

    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, GreenAccent, RoundedCornerShape(Dimens.input_corner_radius))
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(Dimens.input_corner_radius),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) methodColor else methodColor.copy(alpha = 0.5f)
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
                    tint = GreenAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = method.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) GreenAccent else TextSecondary
            )
        }
    }
}

// ============================================================================
// MEMBER SEARCH BOTTOM SHEET
// ============================================================================
@Composable
private fun MemberSearchBottomSheet(
    members: List<MemberUiModel>,
    onMemberSelected: (MemberUiModel) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = members.filter {
        searchQuery.isBlank() ||
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.memberCode.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.screen_padding_horizontal)
            .padding(bottom = Dimens.spacing_8)
    ) {
        Text(
            text = "Pilih Member",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(Dimens.spacing_4))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari nama / ID member...", color = TextMuted) },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenAccent,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = GreenAccent,
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground
            ),
            shape = RoundedCornerShape(Dimens.input_corner_radius)
        )

        Spacer(modifier = Modifier.height(Dimens.spacing_4))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.spacing_8),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Member tidak ditemukan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        } else {
            members.forEach { member ->
                MemberSearchItem(
                    member = member,
                    onClick = { onMemberSelected(member) }
                )
                Spacer(modifier = Modifier.height(Dimens.spacing_2))
            }
        }
    }
}

@Composable
private fun MemberSearchItem(
    member: MemberUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Dimens.input_corner_radius),
        colors = CardDefaults.cardColors(containerColor = BackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_3),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GreenAccent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name.take(2).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = GreenAccent,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(Dimens.spacing_3))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = "${member.memberCode} • ${member.planName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

// ============================================================================
// PAYMENT SUCCESS DIALOG
// ============================================================================
@Composable
private fun PaymentSuccessDialog(
    member: MemberUiModel,
    paymentType: String,
    nominal: Long,
    method: String,
    onViewInvoice: () -> Unit,
    onDismiss: () -> Unit
) {
    val formattedNominal = "Rp ${String.format("%,d", nominal).replace(",", ".")}"

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
            ) {
                androidx.compose.material3.OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Dimens.button_corner_radius),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GreenAccent
                    )
                ) {
                    Text("Selesai")
                }
                Button(
                    onClick = onViewInvoice,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Dimens.button_corner_radius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenAccent
                    )
                ) {
                    Text("Lihat Invoice")
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
                text = "Pembayaran berhasil dicatat",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = paymentType,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(Dimens.spacing_3))
                Text(
                    text = formattedNominal,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GreenAccent
                )
                Spacer(modifier = Modifier.height(Dimens.spacing_2))
                Text(
                    text = "$method • 06 September 2026",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    )
}
