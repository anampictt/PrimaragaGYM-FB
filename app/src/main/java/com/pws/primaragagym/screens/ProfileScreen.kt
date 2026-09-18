package com.pws.primaragagym.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccessTime
import com.pws.primaragagym.commond.DateUtils
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import com.pws.primaragagym.di.ServiceLocator
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pws.primaragagym.ui.viewmodel.ProfileEvent
import com.pws.primaragagym.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

// ============================================================================
// COLORS
// ============================================================================
private val BackgroundColor = Color(0xFFF5F7FA)
private val CardBackground = Color.White
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B6B6B)
private val GreenAccent = Color(0xFF32A060)
private val GreenLight = Color(0xFFE8F5E9)
private val RedAccent = Color(0xFFFF5252)
private val RedLight = Color(0xFFFFE8E8)

// ============================================================================
// PROFILE SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onLogoutSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    val showLogoutSuccess by viewModel.logoutSuccessVisible.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var photoErrorMessage by remember { mutableStateOf<String?>(null) }
    val storageDataSource = remember { ServiceLocator.firebaseStorageDataSource }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            isUploadingPhoto = true
            photoErrorMessage = null
            scope.launch {
                val uploadResult = storageDataSource.uploadUserPhoto(context, uri)
                uploadResult.onSuccess { downloadUrl ->
                    viewModel.updateProfilePhoto(downloadUrl) { success, err ->
                        isUploadingPhoto = false
                        if (!success) {
                            photoErrorMessage = err ?: "Gagal memperbarui profil pengguna"
                        }
                    }
                }.onFailure { error ->
                    isUploadingPhoto = false
                    photoErrorMessage = error.localizedMessage ?: "Gagal mengunggah foto profil"
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ProfileEvent.LogoutSuccess -> onLogoutSuccess()
                is ProfileEvent.LogoutError -> { /* handled by snackbar */ }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = BackgroundColor,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Profil",
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
                                tint = TextPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CardBackground
                    )
                )
            }
        ) { paddingValues ->
            if (uiState.isLoading && uiState.profile == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            } else {
                val profile = uiState.profile
                val roleName = when {
                    profile?.role == com.pws.primaragagym.domain.model.UserRole.SUPER_ADMIN ||
                    profile?.roleTitle?.replace("_", " ")?.equals("Super Admin", ignoreCase = true) == true -> "Super Admin"
                    profile?.role == com.pws.primaragagym.domain.model.UserRole.ADMIN ||
                    profile?.roleTitle?.equals("Admin", ignoreCase = true) == true -> "Admin"
                    profile?.role == com.pws.primaragagym.domain.model.UserRole.MEMBER ||
                    profile?.roleTitle?.equals("Member", ignoreCase = true) == true -> "Member"
                    else -> profile?.roleTitle?.ifBlank { null } ?: profile?.role?.displayName ?: ""
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(
                            horizontal = if (isTablet) 32.dp else 16.dp
                        )
                        .padding(vertical = 24.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileHeader(
                        name = profile?.name.orEmpty().ifBlank { "User" },
                        role = roleName,
                        photoUrl = profile?.photoUrl,
                        isUploading = isUploadingPhoto,
                        errorMessage = photoErrorMessage,
                        onEditPhotoClick = { imagePickerLauncher.launch("image/*") }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    SectionTitle(text = "Informasi Pribadi")
                    Spacer(modifier = Modifier.height(12.dp))

                    PersonalInfoCard(profile = profile)

                    Spacer(modifier = Modifier.height(24.dp))

                    SectionTitle(text = "Keamanan")
                    Spacer(modifier = Modifier.height(12.dp))

                    SecurityCard(onChangePasswordClick = onChangePasswordClick)

                    Spacer(modifier = Modifier.height(24.dp))

                    SectionTitle(text = "Aktivitas")
                    Spacer(modifier = Modifier.height(12.dp))

                    val formattedLastLogin = profile?.lastLogin?.ifBlank { null }?.let {
                        DateUtils.formatLastLogin(it, fallback = "-")
                    } ?: "-"
                    ActivityCard(lastLogin = formattedLastLogin)

                    Spacer(modifier = Modifier.height(32.dp))

                    LogoutButton(onClick = viewModel::showLogoutDialog)

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        if (uiState.showLogoutDialog) {
            LogoutConfirmationDialog(
                onDismiss = viewModel::hideLogoutDialog,
                onConfirm = {
                    viewModel.onLogoutConfirm(onSuccess = onLogoutSuccess)
                }
            )
        }

        // Logout Success Popup
        if (showLogoutSuccess) {
            LogoutSuccessPopup(
                onDismiss = { viewModel.hideLogoutSuccess() }
            )
        }
    }
}

// ============================================================================
// SECTION TITLE
// ============================================================================
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold
        ),
        color = TextPrimary,
        modifier = Modifier.fillMaxWidth()
    )
}

// ============================================================================
// PROFILE HEADER
// ============================================================================
@Composable
private fun ProfileHeader(
    name: String,
    role: String,
    photoUrl: String?,
    isUploading: Boolean = false,
    errorMessage: String? = null,
    onEditPhotoClick: () -> Unit = {}
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(108.dp)
                .clickable { if (!isUploading) onEditPhotoClick() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(GreenLight)
                    .border(2.dp, GreenAccent.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = GreenAccent,
                        strokeWidth = 3.dp
                    )
                } else if (!photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Foto profil",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Foto profil",
                        tint = GreenAccent,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // Camera edit badge at bottom right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(GreenAccent)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AddAPhoto,
                    contentDescription = "Ganti Foto Profil",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE53935),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        if (role.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(GreenLight)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = role,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = GreenAccent
                )
            }
        }
    }
}

// ============================================================================
// PERSONAL INFO CARD
// ============================================================================
@Composable
private fun PersonalInfoCard(profile: com.pws.primaragagym.domain.model.User?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ProfileInfoRow(icon = Icons.Filled.Person, label = "Nama Lengkap", value = profile?.name ?: "-")
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFEEEEEE), thickness = 1.dp)
            ProfileInfoRow(icon = Icons.Filled.Email, label = "Email", value = profile?.email ?: "-")
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFEEEEEE), thickness = 1.dp)
            ProfileInfoRow(icon = Icons.Filled.Phone, label = "Nomor Telepon", value = profile?.phone?.takeIf { it.isNotBlank() } ?: "-")
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFEEEEEE), thickness = 1.dp)
            ProfileInfoRow(icon = Icons.Filled.Home, label = "Alamat", value = profile?.address?.takeIf { it.isNotBlank() } ?: "-", isLast = true)
        }
    }
}

// ============================================================================
// PROFILE INFO ROW
// ============================================================================
@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GreenLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GreenAccent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ============================================================================
// SECURITY CARD
// ============================================================================
@Composable
private fun SecurityCard(onChangePasswordClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onChangePasswordClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ubah Kata Sandi",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Perbarui kata sandi akun Anda",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Ubah kata sandi",
                tint = GreenAccent,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ============================================================================
// ACTIVITY CARD
// ============================================================================
@Composable
private fun ActivityCard(lastLogin: String) {
    val displayLastLogin = remember(lastLogin) {
        if (lastLogin.isBlank() || lastLogin == "-") "-" else DateUtils.formatLastLogin(lastLogin, fallback = "-")
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AccessTime,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Terakhir Login", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = displayLastLogin,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}

// ============================================================================
// LOGOUT BUTTON
// ============================================================================
@Composable
private fun LogoutButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            containerColor = RedLight,
            contentColor = RedAccent
        )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Keluar",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ============================================================================
// LOGOUT CONFIRMATION DIALOG
// ============================================================================
@Composable
private fun LogoutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "Keluar dari akun?",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = "Apakah Anda yakin ingin keluar dari akun?",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Keluar",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = RedAccent
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Batal",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }
    )
}

// ============================================================================
// LOGOUT SUCCESS POPUP
// ============================================================================
@Composable
private fun LogoutSuccessPopup(onDismiss: () -> Unit) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1500)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(Color.White, shape = RoundedCornerShape(24.dp))
                .padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(GreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Success",
                    tint = GreenAccent,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Logout Berhasil!",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sampai jumpa kembali",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}