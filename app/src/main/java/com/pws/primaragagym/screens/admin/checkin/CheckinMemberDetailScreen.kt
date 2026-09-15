package com.pws.primaragagym.screens.admin.checkin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pws.primaragagym.ui.viewmodel.AuthViewModel
import com.pws.primaragagym.ui.viewmodel.CheckinFirestoreViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val BackgroundColor = Color(0xFFF5F7FA)
private val CardBackground = Color.White
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B6B6B)
private val IconBackground = Color(0xFFE8F5E9)
private val GreenAccent = Color(0xFF32A060)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckinMemberDetailScreen(
    memberId: String,
    viewModel: CheckinFirestoreViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val member = uiState.memberToProcess
    val membership = uiState.memberMembership
    val isLoading = uiState.isLoading
    val isSearching = isLoading || uiState.isSearchingMember || (member == null && uiState.error == null)

    val snackbarHostState = remember { SnackbarHostState() }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successDialogMessage by remember { mutableStateOf("") }

    DisposableEffect(memberId) {
        onDispose {
            viewModel.clearSearchState()
        }
    }

    LaunchedEffect(memberId) {
        val branchId = authState.currentUser?.branchId ?: ""
        viewModel.searchMemberByCode(memberCodeOrId = memberId, branchId = branchId)
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            successDialogMessage = uiState.successMessage ?: ""
            showSuccessDialog = true
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            snackbarHostState.showSnackbar(uiState.error ?: "")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detail Check-in Member",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundColor
    ) { paddingValues ->
        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenAccent)
            }
        } else if (member == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Member Tidak Ditemukan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = uiState.error ?: "Kode barcode / ID '$memberId' tidak terdaftar di database.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                    ) {
                        Text("Kembali ke Scanner")
                    }
                }
            }
        } else {
            val isCheckedIn = uiState.isCheckedIn || uiState.todayCheckins.any { it.memberId == member.memberId && it.status == "CHECKED_IN" }
            val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")) }

            val planTitle = member.planName.ifBlank { membership?.planName ?: "Membership Standar" }
            val startText = member.startDate.ifBlank { membership?.startDate?.let { dateFormat.format(it) } ?: "-" }
            val endText = member.expiredDate.ifBlank { membership?.endDate?.let { dateFormat.format(it) } ?: "-" }

            val isExpired = member.status.equals("EXPIRED", ignoreCase = true) || (uiState.error?.contains("kadaluarsa", ignoreCase = true) == true)
            val isSuspended = member.status.equals("SUSPENDED", ignoreCase = true)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Profile Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(IconBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = member.fullName.take(2).uppercase().ifBlank { "MB" },
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = GreenAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = member.fullName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = member.memberCode,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = GreenAccent
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val (statusBg, statusTextColor, statusLabel) = when {
                            isSuspended -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), "SUSPENDED")
                            isExpired -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), "EXPIRED")
                            else -> Triple(Color(0xFFE8F5E9), GreenAccent, "AKTIF")
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(statusBg)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = statusTextColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Membership Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Paket Membership",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = planTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GreenAccent
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Mulai Berlaku",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = startText,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = TextPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Berakhir Pada",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = endText,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = if (isExpired) Color(0xFFD32F2F) else TextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status Keberadaan Saat Ini
                if (isCheckedIn) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFFF57C00),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Member Sedang di Dalam Gym",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFE65100)
                                )
                                val inTime = uiState.activeCheckin?.checkInAt?.let {
                                    SimpleDateFormat("HH:mm 'WIB'", Locale("id", "ID")).apply {
                                        timeZone = TimeZone.getTimeZone("Asia/Jakarta")
                                    }.format(it)
                                } ?: "Hari ini"
                                Text(
                                    text = "Tercatat check-in pada: $inTime",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE65100).copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Login,
                                contentDescription = null,
                                tint = GreenAccent,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Member Belum Check-in",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    text = "Tekan tombol di bawah untuk mencatat kedatangan member",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF2E7D32).copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Button
                when {
                    isSuspended -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFD32F2F))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Member sedang di-suspend. Akses check-in dinonaktifkan.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFFD32F2F)
                                )
                            }
                        }
                    }
                    isExpired -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFD32F2F))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Membership sudah kadaluarsa.",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFD32F2F)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Harap lakukan perpanjangan paket membership terlebih dahulu sebelum check-in.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFD32F2F).copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                    else -> {
                        if (!isCheckedIn) {
                            Button(
                                onClick = { 
                                    val branchId = authState.currentUser?.branchId ?: ""
                                    viewModel.performCheckin(branchId) 
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                enabled = !isLoading
                            ) {
                                Icon(Icons.Filled.Login, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Konfirmasi Check-in Masuk",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        } else {
                            Button(
                                onClick = { viewModel.performCheckout(member.memberId, member.fullName) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                enabled = !isLoading
                            ) {
                                Icon(Icons.Filled.Logout, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Konfirmasi Check-out Keluar",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.clearMessages()
                onBackClick()
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(IconBackground),
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
                    text = "Aktivitas Berhasil Dicatat",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = successDialogMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.clearMessages()
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Selesai")
                }
            }
        )
    }
}
