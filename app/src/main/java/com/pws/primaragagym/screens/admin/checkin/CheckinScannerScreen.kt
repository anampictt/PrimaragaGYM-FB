package com.pws.primaragagym.screens.admin.checkin

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer

private val BackgroundColor = Color(0xFF121212)
private val GreenAccent = Color(0xFF32A060)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckinScannerScreen(
    onBackClick: () -> Unit,
    onSimulateScan: (String) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current

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

    // Manual input state
    var showManualInputDialog by remember { mutableStateOf(false) }
    var manualCodeInput by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    fun handleScanResult(rawContent: String) {
        if (isScanned) return
        isScanned = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

        val cleanCode = rawContent.trim().removePrefix("PRIMARAGA_MEMBER:")
        onSimulateScan(cleanCode)
    }

    Scaffold(
        containerColor = BackgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Scan Barcode Member",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Flash toggle
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
                        }
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                            contentDescription = "Flashlight",
                            tint = if (isFlashOn) Color.Yellow else Color.White
                        )
                    }

                    // Manual input button
                    IconButton(onClick = { showManualInputDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Keyboard,
                            contentDescription = "Input Manual",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.6f)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            if (hasCameraPermission && cameraError == null) {
                // Live Camera View
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
                                                handleScanResult(result.text)
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
                // Fallback background when camera is disabled or permission denied
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1E1E1E)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCodeScanner,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = cameraError ?: "Izin kamera diperlukan untuk memindai kartu member.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (!hasCameraPermission) {
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                            ) {
                                Text("Izinkan Akses Kamera")
                            }
                        }
                    }
                }
            }

            // Viewfinder Reticle Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Arahkan ke Barcode / QR Code",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Posisikan barcode pada kartu member di dalam kotak pemindai",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                // Frame Viewfinder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.25f))
                        .border(2.dp, GreenAccent.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                ) {
                    // Animated laser line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = (240.dp * laserY))
                            .background(GreenAccent)
                    )

                    // Corner accents
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(160.dp, 80.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Manual Input Trigger Card
                Card(
                    onClick = { showManualInputDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 28.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GreenAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = GreenAccent
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Input Kode Member Manual",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF1A1A1A)
                            )
                            Text(
                                text = "Gunakan jika barcode kartu tidak terbaca",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B6B6B)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog Input Manual
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
                        text = "Ketik kode ID member (misal: MBR-001) untuk memproses check-in atau check-out:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = manualCodeInput,
                        onValueChange = { manualCodeInput = it },
                        placeholder = { Text("Contoh: MBR-001") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (manualCodeInput.isNotBlank()) {
                                    keyboardController?.hide()
                                    showManualInputDialog = false
                                    handleScanResult(manualCodeInput.trim())
                                }
                            }
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (manualCodeInput.isNotBlank()) {
                            keyboardController?.hide()
                            showManualInputDialog = false
                            handleScanResult(manualCodeInput.trim())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                ) {
                    Text("Cari Member")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualInputDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
