package com.pws.primaragagym.screens.superadmin.manajemenpengguna

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import com.pws.primaragagym.ui.components.common.AnimatedStatusPopup
import com.pws.primaragagym.ui.components.common.StatusPopupType
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.pws.primaragagym.domain.model.FirestoreUser
import com.pws.primaragagym.ui.viewmodel.RoleListViewModel
import com.pws.primaragagym.ui.viewmodel.UserListViewModel
import com.pws.primaragagym.di.ServiceLocator
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

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
// UI STATE
// ============================================================================
data class AddUserUiState(
    val photoUri: Uri? = null,
    val fullName: String = "",
    val email: String = "",
    val address: String = "",
    val selectedRole: String? = null,
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val confirmPassword: String = "",
    val isConfirmPasswordVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val errors: Map<String, String> = emptyMap()
)

// ============================================================================
// ACCESS MENU MODEL
// ============================================================================
data class AccessMenuUiModel(
    val id: String,
    val name: String,
    val enabled: Boolean
)

// ============================================================================
// ROLES LIST
// ============================================================================
private val defaultRoles = listOf(
    "Super Admin",
    "Admin",
    "Staff",
    "Trainer"
)

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahPenggunaScreen(
    userId: String? = null,
    userViewModel: UserListViewModel = viewModel(),
    roleViewModel: RoleListViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    val isEditMode = userId != null
    val roleState by roleViewModel.uiState.collectAsState()
    val userState by userViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        roleViewModel.loadRoles()
        userViewModel.loadUsers()
    }

    val availableRoles = remember(roleState.roles) {
        val names = roleState.roles.map { it.name }.filter { it.isNotBlank() }
        if (names.isEmpty()) defaultRoles else names
    }

    val existingUser = remember(userId, userState.users) {
        if (userId != null) userViewModel.getUserById(userId) else null
    }

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var photoUploadError by remember { mutableStateOf<String?>(null) }
    val storageDataSource = remember { ServiceLocator.firebaseStorageDataSource }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successDialogTitle by remember { mutableStateOf("") }
    var successDialogMessage by remember { mutableStateOf("") }

    // Focus states
    var isFullNameFocused by remember { mutableStateOf(false) }
    var isEmailFocused by remember { mutableStateOf(false) }
    var isAddressFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }
    var isConfirmPasswordFocused by remember { mutableStateOf(false) }

    LaunchedEffect(existingUser) {
        existingUser?.let { user ->
            fullName = user.displayName
            email = user.email
            address = user.address
            selectedRole = user.resolvedRole
            if (!user.photoUrl.isNullOrBlank()) {
                photoUrl = user.photoUrl
            }
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri
            isUploadingPhoto = true
            photoUploadError = null
            coroutineScope.launch {
                val uploadResult = storageDataSource.uploadUserPhoto(context, uri)
                isUploadingPhoto = false
                uploadResult.onSuccess { downloadUrl ->
                    photoUrl = downloadUrl
                }.onFailure { error ->
                    photoUploadError = error.localizedMessage ?: "Gagal mengunggah foto profil"
                }
            }
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            AddUserTopBar(
                title = if (isEditMode) "Edit Pengguna" else "Tambah Pengguna",
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
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Photo Upload Section
                PhotoUploadSection(
                    photoUri = photoUri,
                    photoUrl = photoUrl,
                    isUploading = isUploadingPhoto,
                    uploadError = photoUploadError,
                    onUploadClick = { imagePickerLauncher.launch("image/*") }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Form Fields
                FormTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        errors = errors - "fullName"
                    },
                    label = "Nama Lengkap",
                    placeholder = "Masukkan nama lengkap",
                    isError = errors.containsKey("fullName"),
                    errorMessage = errors["fullName"],
                    isFocused = isFullNameFocused,
                    onFocusChange = { isFullNameFocused = it },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = InputIconLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                FormTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errors = errors - "email"
                    },
                    label = "Email",
                    placeholder = "Masukkan email",
                    keyboardType = KeyboardType.Email,
                    isError = errors.containsKey("email"),
                    errorMessage = errors["email"],
                    isFocused = isEmailFocused,
                    onFocusChange = { isEmailFocused = it },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = null,
                            tint = InputIconLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                FormTextField(
                    value = address,
                    onValueChange = {
                        address = it
                        errors = errors - "address"
                    },
                    label = "Alamat",
                    placeholder = "Masukkan alamat lengkap",
                    isError = errors.containsKey("address"),
                    errorMessage = errors["address"],
                    isFocused = isAddressFocused,
                    onFocusChange = { isAddressFocused = it },
                    singleLine = false,
                    minLines = 3,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = null,
                            tint = InputIconLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Role Dropdown
                RoleDropdown(
                    selectedRole = selectedRole,
                    roles = availableRoles,
                    onRoleSelected = { role ->
                        selectedRole = role
                        errors = errors - "role"
                    },
                    isExpanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = it },
                    isError = errors.containsKey("role"),
                    errorMessage = errors["role"]
                )

                Spacer(modifier = Modifier.height(16.dp))

                FormPasswordField(
                    value = password,
                    onValueChange = {
                        password = it
                        errors = errors - "password"
                    },
                    label = if (isEditMode) "Kata Sandi (Opsional)" else "Kata Sandi",
                    placeholder = if (isEditMode) "Kosongkan jika tidak ingin mengubah sandi" else "Masukkan kata sandi",
                    isPasswordVisible = passwordVisible,
                    onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                    isError = errors.containsKey("password"),
                    errorMessage = errors["password"],
                    isFocused = isPasswordFocused,
                    onFocusChange = { isPasswordFocused = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                FormPasswordField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errors = errors - "confirmPassword"
                    },
                    label = if (isEditMode) "Konfirmasi Kata Sandi (Opsional)" else "Konfirmasi Kata Sandi",
                    placeholder = if (isEditMode) "Ulangi kata sandi baru" else "Ulangi kata sandi",
                    isPasswordVisible = confirmPasswordVisible,
                    onPasswordVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                    isError = errors.containsKey("confirmPassword"),
                    errorMessage = errors["confirmPassword"],
                    isFocused = isConfirmPasswordFocused,
                    onFocusChange = { isConfirmPasswordFocused = it }
                )

                if (errors.containsKey("submit")) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errors["submit"] ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Submit Button
                Button(
                    onClick = {
                        // Validate
                        val newErrors = mutableMapOf<String, String>()

                        if (fullName.isBlank()) {
                            newErrors["fullName"] = "Nama lengkap wajib diisi"
                        }
                        if (email.isBlank()) {
                            newErrors["email"] = "Email wajib diisi"
                        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                            newErrors["email"] = "Format email tidak valid"
                        }
                        if (address.isBlank()) {
                            newErrors["address"] = "Alamat wajib diisi"
                        }
                        if (selectedRole == null) {
                            newErrors["role"] = "Silakan pilih role"
                        }
                        if (!isEditMode) {
                            if (password.isBlank()) {
                                newErrors["password"] = "Kata sandi wajib diisi"
                            } else if (password.length < 6) {
                                newErrors["password"] = "Kata sandi minimal 6 karakter"
                            }

                            if (confirmPassword.isBlank()) {
                                newErrors["confirmPassword"] = "Konfirmasi kata sandi wajib diisi"
                            } else if (password != confirmPassword) {
                                newErrors["confirmPassword"] = "Konfirmasi kata sandi tidak cocok"
                            }
                        } else {
                            if (password.isNotBlank()) {
                                if (password.length < 6) {
                                    newErrors["password"] = "Kata sandi minimal 6 karakter"
                                }
                                if (confirmPassword.isBlank()) {
                                    newErrors["confirmPassword"] = "Konfirmasi kata sandi wajib diisi jika mengubah sandi"
                                } else if (password != confirmPassword) {
                                    newErrors["confirmPassword"] = "Konfirmasi kata sandi tidak cocok"
                                }
                            } else if (confirmPassword.isNotBlank()) {
                                newErrors["password"] = "Silakan masukkan kata sandi baru"
                            }
                        }

                        if (newErrors.isNotEmpty()) {
                            errors = newErrors
                            return@Button
                        }

                        isSubmitting = true
                        errors = emptyMap()

                        coroutineScope.launch {
                            if (isEditMode && existingUser != null) {
                                val roleVal = selectedRole ?: existingUser.resolvedRole
                                val updatedUser = existingUser.copy(
                                    name = fullName.trim(),
                                    fullName = fullName.trim(),
                                    email = email.trim(),
                                    address = address.trim(),
                                    role = roleVal,
                                    roleId = roleVal,
                                    photoUrl = photoUrl ?: existingUser.photoUrl
                                )
                                userViewModel.updateUser(updatedUser) { success, errorMsg ->
                                    isSubmitting = false
                                    if (success) {
                                        successDialogTitle = "Perubahan Berhasil Disimpan"
                                        successDialogMessage = "Data pengguna \"${fullName.trim()}\" berhasil diperbarui."
                                        showSuccessDialog = true
                                    } else {
                                        errors = mapOf("submit" to (errorMsg ?: "Gagal memperbarui pengguna"))
                                    }
                                }
                            } else {
                                try {
                                    val appOptions = FirebaseApp.getInstance().options
                                    val secondaryApp = try {
                                        FirebaseApp.getInstance("SecondaryAuthApp")
                                    } catch (e: Exception) {
                                        FirebaseApp.initializeApp(context, appOptions, "SecondaryAuthApp")
                                    }
                                    val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)
                                    val authResult = secondaryAuth.createUserWithEmailAndPassword(email.trim(), password).await()
                                    val newUid = authResult.user?.uid ?: UUID.randomUUID().toString()
                                    secondaryAuth.signOut()

                                    val roleVal = selectedRole ?: "Staff"
                                    val newUser = FirestoreUser(
                                        uid = newUid,
                                        name = fullName.trim(),
                                        fullName = fullName.trim(),
                                        email = email.trim(),
                                        role = roleVal,
                                        roleId = roleVal,
                                        address = address.trim(),
                                        photoUrl = photoUrl,
                                        isActive = true,
                                        createdAt = java.util.Date()
                                    )

                                    userViewModel.createUser(newUser) { success, errorMsg ->
                                        isSubmitting = false
                                        if (success) {
                                            successDialogTitle = "Pengguna Berhasil Ditambahkan"
                                            successDialogMessage = "Pengguna \"${fullName.trim()}\" berhasil ditambahkan sebagai $roleVal."
                                            showSuccessDialog = true
                                        } else {
                                            errors = mapOf("submit" to (errorMsg ?: "Gagal menyimpan data pengguna ke Firestore"))
                                        }
                                    }
                                } catch (e: Exception) {
                                    isSubmitting = false
                                    val errorMsg = when {
                                        e.message?.contains("already in use", ignoreCase = true) == true -> "Email sudah digunakan oleh akun lain"
                                        e.message?.contains("weak password", ignoreCase = true) == true -> "Kata sandi terlalu lemah (minimal 6 karakter)"
                                        else -> e.localizedMessage ?: "Gagal mendaftarkan akun di Firebase Auth"
                                    }
                                    errors = mapOf("submit" to errorMsg)
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
                        text = if (isEditMode) "Simpan Perubahan" else "Tambah Pengguna",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    AnimatedStatusPopup(
        visible = showSuccessDialog,
        type = if (isEditMode) StatusPopupType.SUCCESS_EDIT else StatusPopupType.SUCCESS_ADD,
        title = successDialogTitle.ifBlank { if (isEditMode) "Perubahan Berhasil Disimpan" else "Pengguna Berhasil Ditambahkan" },
        message = successDialogMessage.ifBlank { "Data pengguna telah tersimpan di sistem." },
        confirmButtonText = "Selesai",
        onDismiss = {
            showSuccessDialog = false
            onSubmitSuccess()
        }
    )
}

// ============================================================================
// TOP APP BAR
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddUserTopBar(
    title: String = "Tambah Pengguna",
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
// PHOTO UPLOAD SECTION
// ============================================================================
@Composable
private fun PhotoUploadSection(
    photoUri: Uri?,
    photoUrl: String?,
    isUploading: Boolean,
    uploadError: String?,
    onUploadClick: () -> Unit
) {
    val previewModel: Any? = photoUri ?: photoUrl

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(GreenLight)
                .border(
                    width = 2.dp,
                    color = GreenAccent.copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .clickable { if (!isUploading) onUploadClick() },
            contentAlignment = Alignment.Center
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    color = GreenAccent,
                    strokeWidth = 3.dp
                )
            } else if (previewModel != null) {
                AsyncImage(
                    model = previewModel,
                    contentDescription = "Foto Profil",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.AddAPhoto,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onUploadClick,
            enabled = !isUploading,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenAccent.copy(alpha = 0.1f),
                contentColor = GreenAccent
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 8.dp
            )
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = GreenAccent,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Mengunggah...",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.AddAPhoto,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (previewModel != null) "Ubah Foto" else "Tambah Foto",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (uploadError != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = uploadError,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE53935),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
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
    keyboardType: KeyboardType = KeyboardType.Text,
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
                .onFocusChanged { onFocusChange(it.isFocused) }
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
                keyboardType = keyboardType,
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

// ============================================================================
// ROLE DROPDOWN
// ============================================================================
@Composable
private fun RoleDropdown(
    selectedRole: String?,
    roles: List<String> = defaultRoles,
    onRoleSelected: (String) -> Unit,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Role",
            style = MaterialTheme.typography.labelLarge,
            color = if (isError) MaterialTheme.colorScheme.error else TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isExpanded) InputBackgroundFocusedLight else InputBackgroundLight)
                .border(
                    width = 1.dp,
                    color = when {
                        isError -> MaterialTheme.colorScheme.error
                        isExpanded -> InputBorderFocusedLight
                        else -> InputBorderLight
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onExpandedChange(true) }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedRole ?: "Pilih role",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = if (selectedRole != null) InputTextLight else InputHintLight
                    )
                )
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = InputIconLight,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(CardBackground)
            ) {
                roles.forEach { role ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = role,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        },
                        onClick = {
                            onRoleSelected(role)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }

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
// FORM PASSWORD FIELD
// ============================================================================
@Composable
private fun FormPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isPasswordVisible: Boolean,
    onPasswordVisibilityToggle: () -> Unit,
    isError: Boolean = false,
    errorMessage: String? = null,
    isFocused: Boolean = false,
    onFocusChange: (Boolean) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
                .onFocusChanged { onFocusChange(it.isFocused) }
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            cursorBrush = SolidColor(InputCursorLight),
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = InputTextLight
            ),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    IconButton(
                        onClick = onPasswordVisibilityToggle,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = if (isPasswordVisible) {
                                androidx.compose.ui.res.painterResource(id = com.pws.primaragagym.R.drawable.ic_visibility)
                            } else {
                                androidx.compose.ui.res.painterResource(id = com.pws.primaragagym.R.drawable.ic_visibility_off)
                            },
                            contentDescription = if (isPasswordVisible) "Sembunyikan password" else "Tampilkan password",
                            tint = InputIconLight
                        )
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
