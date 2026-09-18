package com.pws.primaragagym.screens.superadmin.manajemenpengguna

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.pws.primaragagym.domain.model.FirestoreUser
import com.pws.primaragagym.ui.components.common.AnimatedStatusPopup
import com.pws.primaragagym.ui.components.common.StatusPopupType
import com.pws.primaragagym.ui.viewmodel.UserListViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val BackgroundColor = Color(0xFFF5F7FA)
private val CardBackground = Color.White
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B6B6B)
private val TextMuted = Color(0xFF9E9E9E)
private val GreenAccent = Color(0xFF32A060)
private val GreenLight = Color(0xFFE8F5E9)
private val DividerColor = Color(0xFFF0F0F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPenggunaScreen(
    userId: String,
    viewModel: UserListViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onDeleteSuccess: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val userState by viewModel.uiState.collectAsState()
    val user: FirestoreUser? = remember(userState.users, userId) {
        userState.users.find { it.uid == userId } ?: viewModel.getUserById(userId)
    }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteSuccessDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userState.users.isEmpty()) {
            viewModel.loadUsers()
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && user != null) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteConfirmDialog = false },
            title = { Text("Hapus Pengguna", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus pengguna \"${user.displayName}\"? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleting = true
                        viewModel.deleteUser(user.uid) { success, _ ->
                            isDeleting = false
                            showDeleteConfirmDialog = false
                            if (success) {
                                showDeleteSuccessDialog = true
                            }
                        }
                    },
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.error,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false },
                    enabled = !isDeleting
                ) {
                    Text("Batal")
                }
            }
        )
    }

    // Delete Success Dialog
    AnimatedStatusPopup(
        visible = showDeleteSuccessDialog,
        type = StatusPopupType.SUCCESS_DELETE,
        title = "Pengguna Berhasil Dihapus",
        message = "Data pengguna \"${user?.displayName ?: ""}\" telah berhasil dihapus dari sistem.",
        confirmButtonText = "Selesai",
        onDismiss = {
            showDeleteSuccessDialog = false
            onDeleteSuccess()
        }
    )

    Scaffold(
        containerColor = BackgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Pengguna",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
        if (userState.isLoading && user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenAccent)
            }
        } else if (user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Pengguna Tidak Ditemukan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Data pengguna dengan ID tersebut tidak ditemukan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Kembali")
                    }
                }
            }
        } else {
            val dateFormat = remember {
                SimpleDateFormat("dd MMMM yyyy, HH:mm 'WIB'", Locale("id", "ID")).apply {
                    timeZone = TimeZone.getTimeZone("Asia/Jakarta")
                }
            }
            val formattedCreatedAt = remember(user.createdAt) {
                user.createdAt?.let { dateFormat.format(it) } ?: "Belum tercatat"
            }

            val initials = remember(user.displayName) {
                user.displayName.split(" ")
                    .take(2)
                    .mapNotNull { part -> part.firstOrNull()?.uppercaseChar() }
                    .joinToString("")
                    .ifEmpty { "U" }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = if (isTablet) 32.dp else 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar Photo
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(GreenLight)
                                .border(2.dp, GreenAccent.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!user.photoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = user.photoUrl,
                                    contentDescription = "Foto Profil",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = initials,
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GreenAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Full Name
                        Text(
                            text = user.displayName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Email
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Badges
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Role Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GreenLight)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = user.resolvedRole,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = GreenAccent
                                )
                            }

                            // Status Badge
                            val statusBg = if (user.isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            val statusColor = if (user.isActive) GreenAccent else Color(0xFFD32F2F)
                            val statusText = if (user.isActive) "Aktif" else "Nonaktif"

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(statusBg)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = statusColor
                                )
                            }
                        }
                    }
                }

                // Section: Informasi Akun (Termasuk Tanggal Dibuat)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Informasi Akun",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tanggal Akun Dibuat
                        DetailItemRow(
                            icon = Icons.Filled.CalendarToday,
                            label = "Tanggal Akun Dibuat",
                            value = formattedCreatedAt,
                            highlightValue = true
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = DividerColor
                        )

                        // Role Pengguna
                        DetailItemRow(
                            icon = Icons.Filled.Badge,
                            label = "Hak Akses / Role",
                            value = user.resolvedRole
                        )
                    }
                }

                // Section: Kontak & Lokasi
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Kontak & Alamat",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        DetailItemRow(
                            icon = Icons.Filled.Email,
                            label = "Email Akun",
                            value = user.email
                        )

                        val phone = user.phoneNumber.ifBlank { user.phone.ifBlank { "-" } }
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = DividerColor
                        )


                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = DividerColor
                        )

                        DetailItemRow(
                            icon = Icons.Filled.Home,
                            label = "Alamat",
                            value = user.address.ifBlank { "-" }
                        )
                    }
                }

                // Action Buttons
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onEditClick(user.uid) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                ) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Data Pengguna", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFD32F2F).copy(alpha = 0.5f))
                    )
                ) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hapus Pengguna", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DetailItemRow(
    icon: ImageVector,
    label: String,
    value: String,
    highlightValue: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (highlightValue) GreenLight else Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (highlightValue) GreenAccent else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (highlightValue) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (highlightValue) GreenAccent else TextPrimary,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
