package com.pws.primaragagym.screens.admin.member

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.pws.primaragagym.ui.viewmodel.MemberListViewModel
import com.pws.primaragagym.ui.viewmodel.AuthViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.pws.primaragagym.screens.admin.member.card.MemberCardData
import com.pws.primaragagym.screens.admin.member.card.MemberCardImageGenerator
import com.pws.primaragagym.screens.admin.member.card.ShareHelper
import com.pws.primaragagym.screens.admin.member.MemberColors.BackgroundColor
import com.pws.primaragagym.screens.admin.member.MemberColors.CardBackground
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenAccent
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenLight
import com.pws.primaragagym.screens.admin.member.MemberColors.TextMuted
import com.pws.primaragagym.screens.admin.member.MemberColors.TextPrimary
import com.pws.primaragagym.screens.admin.member.MemberColors.TextSecondary
import com.pws.primaragagym.screens.admin.member.MemberColors.DividerColor
import com.pws.primaragagym.ui.theme.Dimens
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer

// ============================================================================
// FILTER TYPES
// ============================================================================
private enum class MemberFilter(val displayName: String) {
    ALL("Semua"),
    ACTIVE("Active"),
    EXPIRING("Expiring Soon"),
    EXPIRED("Expired"),
    SUSPENDED("Suspended")
}

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberManagementScreen(
    viewModel: MemberListViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onAddMemberClick: () -> Unit = {},
    onEditMemberClick: (String) -> Unit = {},
    onPreviewCardClick: (String) -> Unit = {},
    onMemberClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    val authState by authViewModel.uiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var memberToDelete by remember { mutableStateOf<MemberUiModel?>(null) }
    var showQrScannerDialog by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val branchId = authState.currentUser?.branchId ?: ""
                viewModel.loadMembers(branchId, reset = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(authState.currentUser) {
        val branchId = authState.currentUser?.branchId ?: ""
        viewModel.loadMembers(branchId, reset = true)
    }

    val filteredMembers = uiState.filteredMembers
    val searchQuery = uiState.searchQuery
    val selectedFilter = uiState.selectedFilter

    val horizontalPadding = if (isTablet) 32.dp else Dimens.screen_padding_horizontal

    fun shareMemberCard(member: MemberUiModel) {
        scope.launch {
            try {
                val cardData = MemberCardData(
                    memberCode = member.memberCode,
                    name = member.name,
                    planName = member.planName.ifEmpty { "Membership" },
                    startDate = member.startDate.ifEmpty { "-" },
                    expiredDate = member.expiredDate.ifEmpty { "-" },
                    status = member.status.displayName,
                    avatarInitial = member.avatarInitial,
                    qrContent = "PRIMARAGA_MEMBER:${member.memberCode}",
                    dateOfBirth = member.dateOfBirth.ifEmpty { "-" }
                )
                val bitmap = withContext(Dispatchers.Default) {
                    MemberCardImageGenerator.generateCardBitmap(context, cardData)
                }
                val fileName = "PrimaragaGYM_${member.memberCode}.png"
                val uri = MemberCardImageGenerator.getShareUri(context, bitmap, fileName)
                if (uri != null) {
                    ShareHelper.shareImage(context, uri, member.name)
                } else {
                    snackbarHostState.showSnackbar("Gagal membagikan kartu member")
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Gagal membagikan kartu member: ${e.message}")
            }
        }
    }

    fun saveMemberCard(member: MemberUiModel) {
        scope.launch {
            try {
                val cardData = MemberCardData(
                    memberCode = member.memberCode,
                    name = member.name,
                    planName = member.planName.ifEmpty { "Membership" },
                    startDate = member.startDate.ifEmpty { "-" },
                    expiredDate = member.expiredDate.ifEmpty { "-" },
                    status = member.status.displayName,
                    avatarInitial = member.avatarInitial,
                    qrContent = "PRIMARAGA_MEMBER:${member.memberCode}",
                    dateOfBirth = member.dateOfBirth.ifEmpty { "-" }
                )
                val bitmap = withContext(Dispatchers.Default) {
                    MemberCardImageGenerator.generateCardBitmap(context, cardData)
                }
                val fileName = "PrimaragaGYM_${member.memberCode}.png"
                val result = withContext(Dispatchers.IO) {
                    MemberCardImageGenerator.saveBitmapToFile(context, bitmap, fileName)
                }
                snackbarHostState.showSnackbar(
                    if (result.isSuccess) "Kartu member ${member.name} berhasil disimpan ke galeri" else "Gagal menyimpan kartu member"
                )
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Gagal menyimpan kartu member: ${e.message}")
            }
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            MemberManagementTopBar(onBackClick = onBackClick)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMemberClick,
                containerColor = GreenAccent,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Registrasi Member Baru"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Field
            MemberSearchField(
                query = searchQuery,
                onQueryChange = { viewModel.searchMembers(it) },
                onScanQrClick = { showQrScannerDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = horizontalPadding,
                        vertical = Dimens.spacing_4
                    )
            )

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(
                    horizontal = horizontalPadding
                ),
                modifier = Modifier.padding(bottom = Dimens.spacing_4)
            ) {
                items(MemberFilter.entries) { filter ->
                    val isSelected = when (filter) {
                        MemberFilter.ALL -> selectedFilter == null
                        MemberFilter.ACTIVE -> selectedFilter == MemberStatus.ACTIVE
                        MemberFilter.EXPIRING -> selectedFilter == MemberStatus.EXPIRING_SOON
                        MemberFilter.EXPIRED -> selectedFilter == MemberStatus.EXPIRED
                        MemberFilter.SUSPENDED -> selectedFilter == MemberStatus.SUSPENDED
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { 
                            when (filter) {
                                MemberFilter.ALL -> viewModel.filterByStatus(null)
                                MemberFilter.ACTIVE -> viewModel.filterByStatus(MemberStatus.ACTIVE)
                                MemberFilter.EXPIRING -> viewModel.filterByStatus(MemberStatus.EXPIRING_SOON)
                                MemberFilter.EXPIRED -> viewModel.filterByStatus(MemberStatus.EXPIRED)
                                MemberFilter.SUSPENDED -> viewModel.filterByStatus(MemberStatus.SUSPENDED)
                            }
                        },
                        label = {
                            Text(
                                text = filter.displayName,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenAccent,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Member Count
            Text(
                text = "${filteredMembers.size} member",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(
                    horizontal = horizontalPadding,
                    vertical = Dimens.spacing_2
                )
            )

            // Member List
            if (filteredMembers.isEmpty()) {
                MemberEmptyState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        bottom = 80.dp
                    )
                ) {
                    items(
                        items = filteredMembers,
                        key = { it.id }
                    ) { member ->
                        MemberManagementCard(
                            member = member,
                            onClick = { onMemberClick(member.id) },
                            onPreviewCardClick = { onPreviewCardClick(member.id) },
                            onShareCardClick = { shareMemberCard(member) },
                            onSaveCardClick = { saveMemberCard(member) },
                            onEditClick = { onEditMemberClick(member.id) },
                            onDeleteClick = { memberToDelete = member }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (memberToDelete != null) {
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            title = { Text("Hapus Member", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus member \"${memberToDelete?.name}\"? Data member akan dihapus permanen.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val member = memberToDelete
                        memberToDelete = null
                        if (member != null) {
                            viewModel.deleteMember(member.id) { success, errMsg ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (success) "Member ${member.name} berhasil dihapus" else (errMsg ?: "Gagal menghapus member")
                                    )
                                }
                            }
                        }
                    }
                ) {
                    Text("Hapus", color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showQrScannerDialog) {
        MemberQrScannerDialog(
            onDismiss = { showQrScannerDialog = false },
            onScanResult = { scannedCode ->
                viewModel.searchMembers(scannedCode)
                showQrScannerDialog = false
            }
        )
    }
}

// ============================================================================
// TOP APP BAR
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberManagementTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Manajemen Member",
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
// SEARCH FIELD
// ============================================================================
@Composable
private fun MemberSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onScanQrClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(Dimens.button_corner_radius))
            .border(
                width = 1.dp,
                color = DividerColor,
                shape = RoundedCornerShape(Dimens.button_corner_radius)
            )
            .background(CardBackground)
            .padding(start = 16.dp, end = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Cari nama / ID member...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = TextPrimary
                    ),
                    cursorBrush = SolidColor(GreenAccent),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Search
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Hapus Pencarian",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            IconButton(
                onClick = onScanQrClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = "Scan QR Code Kartu Member",
                    tint = GreenAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ============================================================================
// QR CODE SCANNER DIALOG
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberQrScannerDialog(
    onDismiss: () -> Unit,
    onScanResult: (String) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var isFlashOn by remember { mutableStateOf(false) }
    var cameraRef by remember { mutableStateOf<Camera?>(null) }
    var isScanned by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }

    // Manual input fallback dialog
    var showManualInputDialog by remember { mutableStateOf(false) }
    var manualCodeInput by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_anim"
    )

    fun handleScan(rawContent: String) {
        if (isScanned) return
        isScanned = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val cleanCode = rawContent.trim().removePrefix("PRIMARAGA_MEMBER:")
        onScanResult(cleanCode)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
        ) {
            if (hasCameraPermission && cameraError == null) {
                // Live camera preview
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val surfaceView = SurfaceView(ctx)
                        val holder = surfaceView.holder

                        val callback = object : SurfaceHolder.Callback {
                            var camera: Camera? = null
                            var isProcessing = false
                            var lastProcessTime = 0L

                            override fun surfaceCreated(sh: SurfaceHolder) {
                                try {
                                    camera = Camera.open()
                                    cameraRef = camera
                                    camera?.setDisplayOrientation(90)
                                    camera?.setPreviewDisplay(sh)

                                    val params = camera?.parameters
                                    params?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                                    params?.previewFormat = ImageFormat.NV21
                                    camera?.parameters = params

                                    camera?.setPreviewCallback { data, cam ->
                                        val now = System.currentTimeMillis()
                                        if (isProcessing || isScanned || now - lastProcessTime < 250) return@setPreviewCallback
                                        isProcessing = true
                                        lastProcessTime = now

                                        try {
                                            val size = cam.parameters.previewSize
                                            val width = size.width
                                            val height = size.height

                                            val source = PlanarYUVLuminanceSource(
                                                data, width, height,
                                                0, 0, width, height, false
                                            )
                                            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                                            val reader = MultiFormatReader()
                                            val result = reader.decodeWithState(binaryBitmap)

                                            if (result != null && result.text.isNotBlank()) {
                                                handleScan(result.text)
                                            }
                                        } catch (_: Exception) {
                                            // No barcode detected in this frame
                                        } finally {
                                            isProcessing = false
                                        }
                                    }

                                    camera?.startPreview()
                                } catch (e: Exception) {
                                    cameraError = "Kamera tidak dapat diakses: ${e.message}"
                                    cameraRef = null
                                }
                            }

                            override fun surfaceChanged(sh: SurfaceHolder, format: Int, w: Int, h: Int) {}

                            override fun surfaceDestroyed(sh: SurfaceHolder) {
                                try {
                                    camera?.setPreviewCallback(null)
                                    camera?.stopPreview()
                                    camera?.release()
                                    camera = null
                                    cameraRef = null
                                } catch (_: Exception) {}
                            }
                        }

                        holder.addCallback(callback)
                        surfaceView
                    }
                )
            } else {
                // Permission Denied or Camera Error View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = null,
                        tint = GreenAccent,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = cameraError ?: "Izin Kamera Diperlukan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Izinkan akses kamera untuk memindai QR Code kartu member secara langsung, atau gunakan input manual.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    if (!hasCameraPermission) {
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                        ) {
                            Text("Beri Izin Kamera", color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    OutlinedButton(
                        onClick = { showManualInputDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenAccent),
                        border = BorderStroke(1.dp, GreenAccent)
                    ) {
                        Text("Input Kode Manual")
                    }
                }
            }

            // Target Overlay (Scanner Viewfinder)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .border(2.dp, GreenAccent, RoundedCornerShape(16.dp))
                ) {
                    // Scanning animated laser line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .offset(y = 260.dp * laserY)
                            .background(GreenAccent)
                    )
                }
            }

            // Top Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 40.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Tutup",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Scan Kartu Member",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Flashlight button
                    IconButton(
                        onClick = {
                            val cam = cameraRef
                            if (cam != null) {
                                try {
                                    val params = cam.parameters
                                    if (params.supportedFlashModes?.contains(Camera.Parameters.FLASH_MODE_TORCH) == true) {
                                        params.flashMode = if (isFlashOn) {
                                            Camera.Parameters.FLASH_MODE_OFF
                                        } else {
                                            Camera.Parameters.FLASH_MODE_TORCH
                                        }
                                        cam.parameters = params
                                        isFlashOn = !isFlashOn
                                    }
                                } catch (_: Exception) {}
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                            contentDescription = "Flashlight",
                            tint = if (isFlashOn) Color.Yellow else Color.White
                        )
                    }

                    // Keyboard input button
                    IconButton(
                        onClick = { showManualInputDialog = true },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Keyboard,
                            contentDescription = "Input Manual",
                            tint = Color.White
                        )
                    }
                }
            }

            // Bottom Instructions Guide
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp, start = 24.dp, end = 24.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Arahkan kamera ke QR Code kartu member",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pencarian member akan terisi otomatis saat QR terdeteksi",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Manual input dialog
    if (showManualInputDialog) {
        AlertDialog(
            onDismissRequest = { showManualInputDialog = false },
            title = {
                Text(
                    text = "Input Kode Member",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Masukkan kode kartu member (contoh: PRGM-01112):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = manualCodeInput,
                        onValueChange = { manualCodeInput = it },
                        placeholder = { Text("PRGM-XXXXX") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (manualCodeInput.isNotBlank()) {
                            showManualInputDialog = false
                            handleScan(manualCodeInput.trim())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                ) {
                    Text("Cari")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualInputDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }
}

// ============================================================================
// MEMBER CARD
// ============================================================================
@Composable
private fun MemberManagementCard(
    member: MemberUiModel,
    onClick: () -> Unit,
    onPreviewCardClick: () -> Unit,
    onShareCardClick: () -> Unit,
    onSaveCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val cardBgColor = when (member.status) {
        MemberStatus.EXPIRED -> Color(0xFFFFF5F5) // Soft red background
        MemberStatus.EXPIRING_SOON -> Color(0xFFFFFBEA) // Soft yellow/amber background
        MemberStatus.SUSPENDED -> Color(0xFFF9FAFB) // Soft gray background
        MemberStatus.ACTIVE -> CardBackground // Clean white
    }

    val cardBorderColor = when (member.status) {
        MemberStatus.EXPIRED -> Color(0xFFFFCDD2) // Red border
        MemberStatus.EXPIRING_SOON -> Color(0xFFFFE082) // Amber border
        MemberStatus.SUSPENDED -> Color(0xFFE5E7EB) // Gray border
        MemberStatus.ACTIVE -> Color(0xFFE8F5E9) // Subtle green border
    }

    val avatarBgColor = when (member.status) {
        MemberStatus.EXPIRED -> Color(0xFFFFE0E0)
        MemberStatus.EXPIRING_SOON -> Color(0xFFFFF3E0)
        MemberStatus.SUSPENDED -> Color(0xFFE5E7EB)
        MemberStatus.ACTIVE -> GreenLight
    }

    val avatarTextColor = when (member.status) {
        MemberStatus.EXPIRED -> Color(0xFFC62828)
        MemberStatus.EXPIRING_SOON -> Color(0xFFE65100)
        MemberStatus.SUSPENDED -> TextSecondary
        MemberStatus.ACTIVE -> GreenAccent
    }

    val planNameColor = when (member.status) {
        MemberStatus.EXPIRED -> Color(0xFFE53935)
        MemberStatus.EXPIRING_SOON -> Color(0xFFFB8C00)
        MemberStatus.SUSPENDED -> TextSecondary
        MemberStatus.ACTIVE -> GreenAccent
    }

    val expiredTextColor = when (member.status) {
        MemberStatus.EXPIRED -> Color(0xFFD32F2F)
        MemberStatus.EXPIRING_SOON -> Color(0xFFE65100)
        else -> TextMuted
    }

    val actionButtonBorderColor = when (member.status) {
        MemberStatus.EXPIRED -> Color(0xFFE53935)
        MemberStatus.EXPIRING_SOON -> Color(0xFFFB8C00)
        MemberStatus.SUSPENDED -> TextSecondary
        MemberStatus.ACTIVE -> GreenAccent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor
        ),
        border = BorderStroke(1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(avatarBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.avatarInitial,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = avatarTextColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = member.memberCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = member.planName.ifEmpty { "Member" },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = planNameColor
                        )
                        MemberStatusBadge(status = member.status)
                    }

                    val createdText = when {
                        member.createdAt != null -> {
                            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale("id", "ID")).apply {
                                timeZone = TimeZone.getTimeZone("Asia/Jakarta")
                            }
                            sdf.format(member.createdAt)
                        }
                        member.startDate.isNotBlank() -> member.startDate
                        else -> null
                    }
                    if (createdText != null) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Dibuat: $createdText",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    if (member.expiredDate.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        val formattedExpired = formatExpiredDateDisplay(member.expiredDate, member.createdAt)
                        Text(
                            text = "Expired: $formattedExpired",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (member.status == MemberStatus.EXPIRED || member.status == MemberStatus.EXPIRING_SOON) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = expiredTextColor
                        )
                    }
                }

                // Edit and Delete icons
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Member",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Hapus Member",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: Preview Kartu, Share, Simpan
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPreviewCardClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    border = BorderStroke(1.dp, actionButtonBorderColor)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CreditCard,
                        contentDescription = null,
                        tint = actionButtonBorderColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kartu Member",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = actionButtonBorderColor
                    )
                }

                OutlinedButton(
                    onClick = onShareCardClick,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share Kartu",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Share",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                OutlinedButton(
                    onClick = onSaveCardClick,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = "Simpan Kartu",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Simpan",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

// ============================================================================
// EMPTY STATE
// ============================================================================
@Composable
private fun MemberEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Belum ada member",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = TextSecondary
            )
        }
    }
}
