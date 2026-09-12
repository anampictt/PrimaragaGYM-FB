package com.pws.primaragagym.screens.superadmin.manajemancabang

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pws.primaragagym.domain.model.FirestoreBranch
import com.pws.primaragagym.ui.viewmodel.BranchListViewModel

// ============================================================================
// COLORS - Match existing management screens
// ============================================================================
private val BackgroundColor = Color(0xFFF5F7FA)
private val CardBackground = Color.White
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B6B6B)
private val GreenAccent = Color(0xFF32A060)
private val GreenLight = Color(0xFFE8F5E9)
private val InputBackgroundLight = Color(0xFFF8F8F8)
private val InputBackgroundFocusedLight = Color(0xFFFFFFFF)
private val InputBorderLight = Color(0xFFE0E0E0)
private val InputBorderFocusedLight = Color(0xFF32A060)
private val InputCursorLight = Color(0xFF32A060)
private val InputHintLight = Color(0xFF9E9E9E)
private val InputTextLight = Color(0xFF1A1A1A)
private val InputIconLight = Color(0xFF757575)

// ============================================================================
// UI STATE
// ============================================================================
data class AddBranchUiState(
    val name: String = "",
    val address: String = "",
    val isSubmitting: Boolean = false,
    val errors: Map<String, String> = emptyMap()
)

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahCabangScreen(
    branchId: String? = null,
    viewModel: BranchListViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    val branchState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var existingBranch by remember { mutableStateOf<FirestoreBranch?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var isNameFocused by remember { mutableStateOf(false) }
    var isAddressFocused by remember { mutableStateOf(false) }

    val isEditMode = !branchId.isNullOrBlank()

    LaunchedEffect(branchId, branchState.branches) {
        if (isEditMode && branchId != null) {
            val cached = branchState.branches.find { it.branchId == branchId || it.id == branchId }
            if (cached != null) {
                existingBranch = cached
                name = cached.name
                address = cached.address
            }
            val freshResult = viewModel.getBranchById(branchId)
            val fresh = freshResult.getOrNull()
            if (fresh != null) {
                existingBranch = fresh
                name = fresh.name
                address = fresh.address
            }
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            AddBranchTopBar(
                isEditMode = isEditMode,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (isTablet) 32.dp else 16.dp)
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Branch Icon
                BranchIconSection()

                Spacer(modifier = Modifier.height(24.dp))

                // General Error Message
                if (generalError != null) {
                    Text(
                        text = generalError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Branch Name Field
                FormTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errors = errors - "name"
                        generalError = null
                    },
                    label = "Nama Cabang",
                    placeholder = "Masukkan nama cabang",
                    isError = errors.containsKey("name"),
                    errorMessage = errors["name"],
                    isFocused = isNameFocused,
                    onFocusChange = { isNameFocused = it },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Business,
                            contentDescription = null,
                            tint = InputIconLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Address Field
                FormTextField(
                    value = address,
                    onValueChange = {
                        address = it
                        errors = errors - "address"
                        generalError = null
                    },
                    label = "Alamat",
                    placeholder = "Masukkan alamat cabang",
                    isError = errors.containsKey("address"),
                    errorMessage = errors["address"],
                    isFocused = isAddressFocused,
                    onFocusChange = { isAddressFocused = it },
                    singleLine = false,
                    minLines = 3,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Submit Button
                Button(
                    onClick = {
                        // Validate
                        val newErrors = mutableMapOf<String, String>()

                        if (name.isBlank()) {
                            newErrors["name"] = "Nama cabang wajib diisi"
                        }
                        if (address.isBlank()) {
                            newErrors["address"] = "Alamat wajib diisi"
                        }

                        if (newErrors.isNotEmpty()) {
                            errors = newErrors
                            return@Button
                        }

                        isSubmitting = true
                        errors = emptyMap()
                        generalError = null

                        if (isEditMode) {
                            val targetId = branchId ?: existingBranch?.branchId ?: ""
                            val updatedBranch = (existingBranch ?: FirestoreBranch(branchId = targetId)).copy(
                                branchId = targetId,
                                name = name.trim(),
                                address = address.trim()
                            )
                            viewModel.updateBranch(updatedBranch) { success, errMsg ->
                                isSubmitting = false
                                if (success) {
                                    onSubmitSuccess()
                                } else {
                                    generalError = errMsg ?: "Gagal memperbarui data cabang"
                                }
                            }
                        } else {
                            val newBranch = FirestoreBranch(
                                name = name.trim(),
                                address = address.trim(),
                                isActive = true
                            )
                            viewModel.createBranch(newBranch) { success, errMsg ->
                                isSubmitting = false
                                if (success) {
                                    onSubmitSuccess()
                                } else {
                                    generalError = errMsg ?: "Gagal membuat cabang baru"
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenAccent,
                        contentColor = Color.White,
                        disabledContainerColor = GreenAccent.copy(alpha = 0.4f),
                        disabledContentColor = Color.White.copy(alpha = 0.6f)
                    )
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
                        text = if (isEditMode) "Simpan Perubahan" else "Tambah Cabang",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ============================================================================
// TOP APP BAR
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBranchTopBar(
    isEditMode: Boolean = false,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = if (isEditMode) "Edit Cabang" else "Tambah Cabang",
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

// ============================================================================
// BRANCH ICON SECTION
// ============================================================================
@Composable
private fun BranchIconSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(GreenLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Business,
                contentDescription = null,
                tint = GreenAccent,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

// ============================================================================
// FORM TEXT FIELD
// ============================================================================
@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    isFocused: Boolean = false,
    onFocusChange: (Boolean) -> Unit = {},
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isError) MaterialTheme.colorScheme.error else TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (singleLine) 56.dp else (56 + (minLines - 1) * 24).dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isFocused) InputBackgroundFocusedLight else InputBackgroundLight)
                .border(
                    width = 1.dp,
                    color = when {
                        isError -> MaterialTheme.colorScheme.error
                        isFocused -> InputBorderFocusedLight
                        else -> InputBorderLight
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = if (singleLine) 0.dp else 12.dp),
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = if (singleLine) ImeAction.Next else ImeAction.Default
            ),
            cursorBrush = SolidColor(InputCursorLight),
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = InputTextLight
            ),
            decorationBox = { innerTextField ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center
                ) {
                    if (leadingIcon != null && singleLine) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            leadingIcon()
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (value.isEmpty()) {
                                    Text(
                                        text = placeholder,
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            color = InputHintLight
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    } else {
                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = InputHintLight
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            }
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}
