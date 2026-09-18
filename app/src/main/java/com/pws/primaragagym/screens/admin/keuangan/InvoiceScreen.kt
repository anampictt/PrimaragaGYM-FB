package com.pws.primaragagym.screens.admin.keuangan

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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.pws.primaragagym.domain.model.FirestorePayment
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.BackgroundColor
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.CardBackground
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.CashBg
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.GreenAccent
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.GreenLight
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.QrisBg
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextMuted
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextPrimary
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TextSecondary
import com.pws.primaragagym.screens.admin.keuangan.KeuanganColors.TransferBg
import com.pws.primaragagym.ui.theme.Dimens
import com.pws.primaragagym.ui.viewmodel.AuthViewModel
import com.pws.primaragagym.ui.viewmodel.InvoiceListViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

// ============================================================================
// MAIN SCREEN - RIWAYAT INVOICE MEMBER
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    viewModel: InvoiceListViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onInvoiceClick: (String) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val keyboardController = LocalSoftwareKeyboardController.current

    val authState by authViewModel.uiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showScannerDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit, authState.currentUser) {
        val branchId = authState.currentUser?.branchId ?: ""
        viewModel.loadInvoices(branchId)
    }

    // Filter Invoices by search query (invoice number, member name, plan, or method)
    val filteredInvoices = remember(uiState.invoices, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) {
            uiState.invoices
        } else {
            uiState.invoices.filter { invoice ->
                invoice.invoiceNumber.lowercase().contains(q) ||
                invoice.paymentId.lowercase().contains(q) ||
                invoice.memberId.lowercase().contains(q) ||
                invoice.memberName.lowercase().contains(q) ||
                invoice.planName.lowercase().contains(q) ||
                invoice.category.lowercase().contains(q) ||
                invoice.paymentMethod.lowercase().contains(q) ||
                (invoice.invoiceNumber.isNotBlank() && q.contains(invoice.invoiceNumber.lowercase())) ||
                (invoice.paymentId.isNotBlank() && q.contains(invoice.paymentId.lowercase()))
            }
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Riwayat Invoice Member",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${filteredInvoices.size} invoice tercatat",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val branchId = authState.currentUser?.branchId ?: ""
                            viewModel.loadInvoices(branchId)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Muat Ulang Data",
                            tint = TextPrimary
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
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            // ==================== SEARCH BAR WITH SCANNER BUTTON ====================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (isTablet) 32.dp else Dimens.screen_padding_horizontal,
                        vertical = Dimens.spacing_3
                    ),
                shape = RoundedCornerShape(Dimens.card_corner_radius),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.spacing_3, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Cari",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(Dimens.spacing_2))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Ketik no. invoice / nama member...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Hapus",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // QR Code Scanner Button
                    Surface(
                        onClick = { showScannerDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        color = GreenAccent,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.QrCodeScanner,
                                contentDescription = "Scan QR Invoice",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // ==================== INVOICE LIST / EMPTY STATE ====================
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            } else if (filteredInvoices.isEmpty()) {
                EmptyInvoiceState(
                    hasSearch = searchQuery.isNotBlank(),
                    onResetSearch = { searchQuery = "" }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = if (isTablet) 32.dp else Dimens.screen_padding_horizontal,
                        vertical = Dimens.spacing_3
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
                ) {
                    items(filteredInvoices, key = { it.paymentId }) { invoice ->
                        ResponsiveInvoiceCard(
                            invoice = invoice,
                            onClick = {
                                val targetId = invoice.invoiceNumber.ifBlank { invoice.paymentId }
                                onInvoiceClick(targetId)
                            }
                        )
                    }
                }
            }
        }
    }

    // ==================== QR SCANNER CAMERA DIALOG ====================
    if (showScannerDialog) {
        InvoiceQrScannerDialog(
            onDismiss = { showScannerDialog = false },
            onScanned = { scannedCode ->
                showScannerDialog = false
                val cleanScanned = scannedCode.trim()
                searchQuery = cleanScanned

                // Cari invoice yang cocok dari daftar yang telah dimuat
                val match = uiState.invoices.firstOrNull {
                    it.invoiceNumber.equals(cleanScanned, ignoreCase = true) ||
                    it.paymentId.equals(cleanScanned, ignoreCase = true) ||
                    (it.invoiceNumber.isNotBlank() && cleanScanned.contains(it.invoiceNumber, ignoreCase = true)) ||
                    (cleanScanned.isNotBlank() && it.invoiceNumber.contains(cleanScanned, ignoreCase = true))
                }

                if (match != null) {
                    val targetId = match.invoiceNumber.ifBlank { match.paymentId }
                    onInvoiceClick(targetId)
                } else {
                    // Jika belum ada di list lokal, langsung navigasikan dengan scanned code
                    // Halaman InvoiceDetailScreen akan query Firestore langsung secara real-time!
                    onInvoiceClick(cleanScanned)
                }
            }
        )
    }
}

// ============================================================================
// RESPONSIVE INVOICE CARD (CLEAN & NON-OVERFLOWING)
// ============================================================================
@Composable
private fun ResponsiveInvoiceCard(
    invoice: FirestorePayment,
    onClick: () -> Unit
) {
    val formatCurrency = { amount: Long ->
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0
        format.format(amount)
    }

    val formatDate = { date: java.util.Date? ->
        try {
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).apply {
                timeZone = java.util.TimeZone.getTimeZone("Asia/Jakarta")
            }
            date?.let { dateFormat.format(it) } ?: "-"
        } catch (_: Exception) { "-" }
    }

    val displayInvoiceNo = invoice.invoiceNumber.ifBlank { invoice.paymentId }
    val displayTitle = invoice.planName.ifBlank { invoice.category.ifBlank { invoice.paymentType } }

    val methodBg = when (invoice.paymentMethod.uppercase()) {
        "TRANSFER" -> TransferBg
        "QRIS" -> QrisBg
        else -> CashBg
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Dimens.card_corner_radius),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_4)
        ) {
            // ==================== BARIS 1: HEADER (INVOICE NO & STATUS BADGE) ====================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(GreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Receipt,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = displayInvoiceNo,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = GreenAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Badge LUNAS (Kokoh, horizontal, tidak terjepit)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GreenLight
                ) {
                    Text(
                        text = "LUNAS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = GreenAccent,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF2F2F2), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // ==================== BARIS 2: NAMA MEMBER & ITEM/PAKET ====================
            Text(
                text = invoice.memberName.ifBlank { "Member Gym" },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = displayTitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ==================== BARIS 3: TANGGAL DI KIRI, NOMINAL & METODE DI KANAN ====================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(invoice.paidAt ?: invoice.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatCurrency(invoice.amount),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = methodBg
                    ) {
                        Text(
                            text = invoice.paymentMethod,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = TextPrimary,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// CAMERA QR SCANNER DIALOG
// ============================================================================
@Composable
private fun InvoiceQrScannerDialog(
    onDismiss: () -> Unit,
    onScanned: (String) -> Unit
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

    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_anim"
    )

    fun handleScanResult(rawContent: String) {
        if (isScanned) return
        isScanned = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val clean = rawContent.trim()
            .removePrefix("PRIMARAGA_INVOICE:")
            .removePrefix("PRIMARAGA_MEMBER:")
        onScanned(clean)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (hasCameraPermission && cameraError == null) {
                // Live Camera Surface
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
                                    val supportedFocus = params?.supportedFocusModes ?: emptyList()
                                    if (supportedFocus.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                                        params?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                                    } else if (supportedFocus.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                                        params?.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                                    }
                                    params?.previewFormat = ImageFormat.NV21
                                    camera?.parameters = params

                                    val hints = mapOf(
                                        DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_128),
                                        DecodeHintType.TRY_HARDER to java.lang.Boolean.TRUE,
                                        DecodeHintType.CHARACTER_SET to "UTF-8"
                                    )
                                    val reader = MultiFormatReader().apply { setHints(hints) }

                                    camera?.setPreviewCallback { data, cam ->
                                        val now = System.currentTimeMillis()
                                        if (isProcessing || isScanned || now - lastProcessTime < 200) return@setPreviewCallback
                                        isProcessing = true
                                        lastProcessTime = now

                                        try {
                                            val size = cam.parameters.previewSize
                                            val width = size.width
                                            val height = size.height

                                            var decodedText: String? = null

                                            // Percobaan 1: Orientasi standar
                                            try {
                                                val source = PlanarYUVLuminanceSource(
                                                    data, width, height,
                                                    0, 0, width, height, false
                                                )
                                                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                                                val result = reader.decodeWithState(binaryBitmap)
                                                if (result != null && result.text.isNotBlank()) {
                                                    decodedText = result.text
                                                }
                                            } catch (_: Exception) {}

                                            // Percobaan 2: Rotasi 90 derajat (krusial untuk orientasi layar portrait HP Android)
                                            if (decodedText == null) {
                                                try {
                                                    val rotatedData = ByteArray(data.size)
                                                    for (y in 0 until height) {
                                                        for (x in 0 until width) {
                                                            rotatedData[x * height + height - y - 1] = data[x + y * width]
                                                        }
                                                    }
                                                    val rotatedSource = PlanarYUVLuminanceSource(
                                                        rotatedData, height, width,
                                                        0, 0, height, width, false
                                                    )
                                                    val rotatedBitmap = BinaryBitmap(HybridBinarizer(rotatedSource))
                                                    val result = reader.decodeWithState(rotatedBitmap)
                                                    if (result != null && result.text.isNotBlank()) {
                                                        decodedText = result.text
                                                    }
                                                } catch (_: Exception) {}
                                            }

                                            if (!decodedText.isNullOrBlank()) {
                                                handleScanResult(decodedText)
                                            }
                                        } catch (_: Exception) {
                                            // Frame without QR
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

                // Dark Overlay with Square Viewfinder Hole
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Viewfinder Box
                    val boxSize = 250.dp
                    Box(
                        modifier = Modifier
                            .size(boxSize)
                            .border(2.dp, GreenAccent, RoundedCornerShape(16.dp))
                    ) {
                        // Animated Scanning Laser Line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .offset(y = (boxSize * laserY))
                                .background(GreenAccent.copy(alpha = 0.8f))
                        )
                    }
                }
            } else if (!hasCameraPermission) {
                // Permission Warning
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
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Izin Kamera Diperlukan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Berikan izin kamera untuk memindai QR Code invoice secara otomatis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                    ) {
                        Text("Izinkan Kamera")
                    }
                }
            }

            // Top Bar Controls (Back & Flash)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Tutup",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Arahkan ke QR Invoice",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

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
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        contentDescription = "Flash",
                        tint = if (isFlashOn) Color.Yellow else Color.White
                    )
                }
            }

            // Bottom Guide Text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Arahkan kamera tepat pada QR Code nota invoice",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// ============================================================================
// EMPTY STATE
// ============================================================================
@Composable
private fun EmptyInvoiceState(
    hasSearch: Boolean,
    onResetSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Receipt,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_4))
            Text(
                text = if (hasSearch) "Invoice Tidak Ditemukan" else "Belum Ada Invoice",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_2))
            Text(
                text = if (hasSearch)
                    "Tidak ada invoice yang cocok dengan kata kunci atau scan QR tersebut."
                else
                    "Invoice pembayaran member atau transaksi kasir akan muncul di sini.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            if (hasSearch) {
                Spacer(modifier = Modifier.height(Dimens.spacing_4))
                Button(
                    onClick = onResetSearch,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                    shape = RoundedCornerShape(Dimens.button_corner_radius)
                ) {
                    Text("Tampilkan Semua Invoice")
                }
            }
        }
    }
}