package com.pws.primaragagym.screens.superadmin.manajemenrole

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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pws.primaragagym.di.ServiceLocator
import com.pws.primaragagym.domain.model.FirestoreRole
import com.pws.primaragagym.ui.viewmodel.RoleListViewModel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================================
// COLORS - Match existing management screens
// ============================================================================
private val BackgroundColor = Color(0xFFF5F7FA)
private val CardBackground = Color.White
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B6B6B)
private val TextMuted = Color(0xFF9E9E9E)
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
// ACCESS MENU MODEL
// ============================================================================
data class AccessMenuUiModel(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val enabled: Boolean
)

// ============================================================================
// UI STATE
// ============================================================================
data class AddRoleUiState(
    val roleName: String = "",
    val accessMenus: List<AccessMenuUiModel> = emptyList(),
    val isSubmitting: Boolean = false,
    val errors: Map<String, String> = emptyMap()
)

// ============================================================================
// DEFAULT ACCESS MENUS
// ============================================================================
private fun getDefaultAccessMenus(): List<AccessMenuUiModel> = listOf(
    AccessMenuUiModel("dashboard", "Dashboard", Icons.Filled.Dashboard, false),
    AccessMenuUiModel("manajemen_pengguna", "Manajemen Pengguna", Icons.Filled.Group, false),
    AccessMenuUiModel("manajemen_role", "Manajemen Role", Icons.Filled.AdminPanelSettings, false),
    AccessMenuUiModel("manajemen_cabang", "Manajemen Cabang", Icons.Filled.Home, false),
    AccessMenuUiModel("manajemen_member", "Member", Icons.Filled.Person, false),
    AccessMenuUiModel("membership", "Membership", Icons.Filled.FitnessCenter, false),
    AccessMenuUiModel("check_in", "Check In", Icons.Filled.QrCodeScanner, false),
    AccessMenuUiModel("check_out", "Check Out", Icons.AutoMirrored.Filled.Logout, false),
    AccessMenuUiModel("keuangan", "Catatan Keuangan", Icons.Filled.AccountBalanceWallet, false),
    AccessMenuUiModel("notifikasi", "Notifikasi", Icons.Filled.Notifications, false),
    AccessMenuUiModel("laporan", "Laporan Keuangan", Icons.Filled.Description, false),
    AccessMenuUiModel("pengaturan", "Pengaturan Akun", Icons.Filled.AdminPanelSettings, false)
)

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahRoleScreen(
    roleId: String? = null,
    viewModel: RoleListViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    val roleState by viewModel.uiState.collectAsState()

    var roleName by remember { mutableStateOf("") }
    var roleDescription by remember { mutableStateOf("") }
    var accessMenus by remember { mutableStateOf(getDefaultAccessMenus()) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isRoleNameFocused by remember { mutableStateOf(false) }
    var isDescriptionFocused by remember { mutableStateOf(false) }

    val isEditMode = !roleId.isNullOrBlank()

    LaunchedEffect(roleId, roleState.roles) {
        if (isEditMode) {
            val cachedRole = roleState.roles.find { it.roleId == roleId }
            if (cachedRole != null) {
                roleName = cachedRole.name
                roleDescription = cachedRole.description
                accessMenus = getDefaultAccessMenus().map { menu ->
                    val isEnabled = cachedRole.permissions[menu.id] == true
                    menu.copy(enabled = isEnabled)
                }
            }

            if (roleId != null) {
                val freshRole = ServiceLocator.roleRepository.getRoleById(roleId).getOrNull()
                if (freshRole != null) {
                    roleName = freshRole.name
                    roleDescription = freshRole.description
                    accessMenus = getDefaultAccessMenus().map { menu ->
                        val isEnabled = freshRole.permissions[menu.id] == true
                        menu.copy(enabled = isEnabled)
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            AddRoleTopBar(
                title = if (isEditMode) "Edit Role" else "Tambah Role",
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
                // Role Name Field
                FormTextField(
                    value = roleName,
                    onValueChange = {
                        roleName = it
                        errors = errors - "roleName"
                    },
                    label = "Nama Role",
                    placeholder = "Masukkan nama role",
                    isError = errors.containsKey("roleName"),
                    errorMessage = errors["roleName"],
                    isFocused = isRoleNameFocused,
                    onFocusChange = { isRoleNameFocused = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description Field
                FormTextField(
                    value = roleDescription,
                    onValueChange = {
                        roleDescription = it
                    },
                    label = "Deskripsi",
                    placeholder = "Masukkan deskripsi role",
                    isFocused = isDescriptionFocused,
                    onFocusChange = { isDescriptionFocused = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Access Section Header
                Text(
                    text = "Hak Akses",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Pilih menu yang dapat diakses oleh role ini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Access Menu List
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = CardBackground
                    ),
                    elevation = androidx.compose.material3.CardDefaults.cardElevation(
                        defaultElevation = 1.dp
                    )
                ) {
                    Column {
                        accessMenus.forEachIndexed { index, menu ->
                            AccessMenuRow(
                                menu = menu,
                                onToggle = { enabled ->
                                    accessMenus = accessMenus.map {
                                        if (it.id == menu.id) it.copy(enabled = enabled) else it
                                    }
                                }
                            )

                            // Divider between items
                            if (index < accessMenus.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .height(1.dp)
                                        .background(Color(0xFFF0F0F0))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Submit Button
                Button(
                    onClick = {
                        val newErrors = mutableMapOf<String, String>()

                        if (roleName.isBlank()) {
                            newErrors["roleName"] = "Nama role wajib diisi"
                        }

                        if (newErrors.isNotEmpty()) {
                            errors = newErrors
                            return@Button
                        }

                        isSubmitting = true
                        errors = emptyMap()

                        val permissionsMap = accessMenus.associate { it.id to it.enabled }
                        val roleToSave = FirestoreRole(
                            roleId = roleId ?: "",
                            name = roleName.trim(),
                            description = roleDescription.trim(),
                            permissions = permissionsMap,
                            isActive = true
                        )

                        if (isEditMode) {
                            viewModel.updateRole(roleToSave) { success, errorMsg ->
                                isSubmitting = false
                                if (success) {
                                    onSubmitSuccess()
                                } else {
                                    errors = mapOf("submit" to (errorMsg ?: "Gagal memperbarui role"))
                                }
                            }
                        } else {
                            viewModel.createRole(roleToSave) { success, errorMsg ->
                                isSubmitting = false
                                if (success) {
                                    onSubmitSuccess()
                                } else {
                                    errors = mapOf("submit" to (errorMsg ?: "Gagal membuat role"))
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
                        text = if (isEditMode) "Simpan Perubahan" else "Tambah Role",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (errors.containsKey("submit")) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errors["submit"] ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
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
private fun AddRoleTopBar(
    title: String = "Tambah Role",
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
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
    isFocused: Boolean = false,
    onFocusChange: (Boolean) -> Unit = {}
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
                .height(56.dp)
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
                .padding(horizontal = 16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            cursorBrush = SolidColor(InputCursorLight),
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = InputTextLight
            ),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
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

// ============================================================================
// ACCESS MENU ROW
// ============================================================================
@Composable
private fun AccessMenuRow(
    menu: AccessMenuUiModel,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = menu.icon,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = menu.name,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }

        Switch(
            checked = menu.enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = GreenAccent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE0E0E0)
            )
        )
    }
}
