package com.pws.primaragagym.screens.admin.member.card

import android.graphics.Bitmap
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.pws.primaragagym.screens.admin.member.MemberStatus
import com.pws.primaragagym.screens.admin.member.formatExpiredDateDisplay
import com.pws.primaragagym.screens.admin.member.resolveMemberStatus
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pws.primaragagym.ui.theme.Dimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberCardPreviewScreen(
    memberId: String = "",
    cardData: MemberCardData? = null,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val horizontalPadding = if (isTablet) 32.dp else Dimens.screen_padding_horizontal

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var currentCardData by remember { mutableStateOf(cardData) }
    var isLoading by remember { mutableStateOf(currentCardData == null && memberId.isNotBlank()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var isSavingImage by remember { mutableStateOf(false) }
    var isSavingPdf by remember { mutableStateOf(false) }
    var cardBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val greenAccent = com.pws.primaragagym.screens.admin.member.MemberColors.GreenAccent
    val textPrimary = com.pws.primaragagym.screens.admin.member.MemberColors.TextPrimary
    val bgColor = com.pws.primaragagym.screens.admin.member.MemberColors.BackgroundColor
    val cardBg = com.pws.primaragagym.screens.admin.member.MemberColors.CardBackground

    val successMessage = "Kartu member berhasil disimpan"
    val errorSaveMessage = "Gagal menyimpan kartu member"
    val errorShareMessage = "Gagal membagikan kartu member"

    LaunchedEffect(memberId, cardData) {
        if (cardData != null) {
            currentCardData = cardData
            isLoading = false
        } else if (memberId.isNotBlank()) {
            isLoading = true
            errorMessage = null
            withContext(Dispatchers.IO) {
                try {
                    val memberRepo = com.pws.primaragagym.data.repository.MemberRepositoryImpl()
                    val membershipRepo = com.pws.primaragagym.data.repository.MembershipRepositoryImpl()
                    val result = memberRepo.getMemberById(memberId)
                    result.fold(
                        onSuccess = { member ->
                            val name = member.fullName.ifBlank { "Member" }
                            val initials = name.split(" ")
                                .filter { it.isNotBlank() }
                                .take(2)
                                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                .joinToString("")
                                .ifEmpty { "?" }
                            val code = member.memberCode.ifBlank { member.memberId }

                            var plan = member.planName
                            var expired = member.expiredDate
                            var start = member.startDate

                            if (plan.isBlank() || expired.isBlank()) {
                                val activeMs = membershipRepo.getActiveMembership(member.memberId).getOrNull()
                                if (activeMs != null) {
                                    if (plan.isBlank()) plan = activeMs.planName
                                    if (expired.isBlank()) {
                                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                        expired = activeMs.endDate?.let { sdf.format(it) } ?: ""
                                    }
                                    if (start.isBlank()) {
                                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                        start = activeMs.startDate?.let { sdf.format(it) } ?: ""
                                    }
                                }
                            }

                            val resolvedStatusEnum = resolveMemberStatus(member.status, expired, member.createdAt)
                            val statusStr = when (resolvedStatusEnum) {
                                MemberStatus.ACTIVE -> "Aktif"
                                MemberStatus.EXPIRING_SOON -> "Akan Habis"
                                MemberStatus.EXPIRED -> "Kadaluarsa"
                                MemberStatus.SUSPENDED -> "Nonaktif"
                            }

                            withContext(Dispatchers.Main) {
                                currentCardData = MemberCardData(
                                    memberCode = code,
                                    name = name,
                                    planName = plan.ifBlank { "Member" },
                                    startDate = start.ifBlank { "-" },
                                    expiredDate = formatExpiredDateDisplay(expired, member.createdAt).ifBlank { "-" },
                                    status = statusStr,
                                    avatarInitial = initials,
                                    qrContent = "PRIMARAGA_MEMBER:$code",
                                    dateOfBirth = member.dateOfBirth.ifBlank { "-" },
                                    photoUrl = member.photoUrl
                                )
                                isLoading = false
                            }
                        },
                        onFailure = { err ->
                            withContext(Dispatchers.Main) {
                                val dummy = com.pws.primaragagym.screens.admin.member.dummyMembers.find { it.id == memberId }
                                if (dummy != null) {
                                    currentCardData = MemberCardData(
                                        memberCode = dummy.memberCode,
                                        name = dummy.name,
                                        planName = dummy.planName,
                                        startDate = dummy.startDate,
                                        expiredDate = dummy.expiredDate,
                                        status = dummy.status.displayName,
                                        avatarInitial = dummy.avatarInitial,
                                        qrContent = "PRIMARAGA_MEMBER:${dummy.memberCode}",
                                        dateOfBirth = dummy.dateOfBirth.ifBlank { "-" }
                                    )
                                } else {
                                    errorMessage = err.message ?: "Data member tidak ditemukan"
                                }
                                isLoading = false
                            }
                        }
                    )
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        errorMessage = e.message ?: "Gagal memuat kartu member"
                        isLoading = false
                    }
                }
            }
        }
    }

    fun generateBitmap() {
        val data = currentCardData ?: return
        scope.launch {
            cardBitmap = withContext(Dispatchers.Default) {
                MemberCardImageGenerator.generateCardBitmap(context, data)
            }
        }
    }

    LaunchedEffect(currentCardData) {
        if (currentCardData != null) {
            generateBitmap()
        }
    }

    LaunchedEffect(cardBitmap) {
        if (cardBitmap != null) {
            delay(100)
        }
    }

    fun saveImage() {
        val data = currentCardData ?: return
        if (isSavingImage) return
        isSavingImage = true
        scope.launch {
            val bitmap = cardBitmap ?: withContext(Dispatchers.Default) {
                MemberCardImageGenerator.generateCardBitmap(context, data)
            }
            val fileName = "PrimaragaGYM_${data.memberCode}.png"
            val result = withContext(Dispatchers.IO) {
                MemberCardImageGenerator.saveBitmapToFile(context, bitmap, fileName)
            }
            isSavingImage = false
            snackbarHostState.showSnackbar(
                if (result.isSuccess) successMessage else errorSaveMessage
            )
        }
    }

    fun savePdf() {
        val data = currentCardData ?: return
        if (isSavingPdf) return
        isSavingPdf = true
        scope.launch {
            val pdfResult = withContext(Dispatchers.Default) {
                MemberCardPdfGenerator.generatePdf(context, data)
            }
            pdfResult.fold(
                onSuccess = { bytes ->
                    val fileName = "PrimaragaGYM_${data.memberCode}.pdf"
                    val saveResult = withContext(Dispatchers.IO) {
                        MemberCardPdfGenerator.savePdfToFile(context, bytes, fileName)
                    }
                    isSavingPdf = false
                    snackbarHostState.showSnackbar(
                        if (saveResult.isSuccess) "PDF berhasil disimpan" else errorSaveMessage
                    )
                },
                onFailure = {
                    isSavingPdf = false
                    snackbarHostState.showSnackbar(errorSaveMessage)
                }
            )
        }
    }

    fun shareImage() {
        val data = currentCardData ?: return
        scope.launch {
            val bitmap = cardBitmap ?: withContext(Dispatchers.Default) {
                MemberCardImageGenerator.generateCardBitmap(context, data)
            }
            val fileName = "PrimaragaGYM_${data.memberCode}.png"
            val uri = MemberCardImageGenerator.getShareUri(context, bitmap, fileName)
            if (uri != null) {
                ShareHelper.shareImage(context, uri, data.name)
            } else {
                snackbarHostState.showSnackbar(errorShareMessage)
            }
        }
    }

    fun sharePdf() {
        val data = currentCardData ?: return
        scope.launch {
            val pdfResult = withContext(Dispatchers.Default) {
                MemberCardPdfGenerator.generatePdf(context, data)
            }
            pdfResult.fold(
                onSuccess = { bytes ->
                    val fileName = "PrimaragaGYM_${data.memberCode}.pdf"
                    val uri = MemberCardPdfGenerator.getShareUri(context, bytes, fileName)
                    if (uri != null) {
                        ShareHelper.sharePdf(context, uri, data.name)
                    } else {
                        snackbarHostState.showSnackbar(errorShareMessage)
                    }
                },
                onFailure = {
                    snackbarHostState.showSnackbar(errorShareMessage)
                }
            )
        }
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Preview Kartu Member",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = textPrimary
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
                            tint = textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cardBg
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = greenAccent)
            }
        } else if (currentCardData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "Data member tidak ditemukan",
                    style = MaterialTheme.typography.bodyLarge,
                    color = textPrimary
                )
            }
        } else {
            val data = currentCardData!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = Dimens.spacing_5),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Card Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                    contentAlignment = Alignment.Center
                ) {
                    MemberCard(
                        data = data,
                        width = if (isTablet) 400.dp else null,
                        cornerRadius = 20.dp
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_8))

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary actions row
                if (isTablet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { saveImage() },
                            modifier = Modifier.weight(1f),
                            enabled = !isSavingImage,
                            colors = ButtonDefaults.buttonColors(containerColor = greenAccent),
                            shape = RoundedCornerShape(Dimens.button_corner_radius)
                        ) {
                            if (isSavingImage) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = cardBg,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Simpan Gambar")
                        }
                        Button(
                            onClick = { savePdf() },
                            modifier = Modifier.weight(1f),
                            enabled = !isSavingPdf,
                            colors = ButtonDefaults.buttonColors(containerColor = greenAccent),
                            shape = RoundedCornerShape(Dimens.button_corner_radius)
                        ) {
                            if (isSavingPdf) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = cardBg,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Simpan PDF")
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Share buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { shareImage() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(Dimens.button_corner_radius)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bagikan PNG", color = greenAccent)
                        }
                        OutlinedButton(
                            onClick = { sharePdf() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(Dimens.button_corner_radius)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bagikan PDF", color = greenAccent)
                        }
                    }
                } else {
                    // Phone layout - stacked
                    Button(
                        onClick = { saveImage() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSavingImage,
                        colors = ButtonDefaults.buttonColors(containerColor = greenAccent),
                        shape = RoundedCornerShape(Dimens.button_corner_radius)
                    ) {
                        if (isSavingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = cardBg,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Simpan Gambar")
                    }
                    Button(
                        onClick = { savePdf() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSavingPdf,
                        colors = ButtonDefaults.buttonColors(containerColor = greenAccent),
                        shape = RoundedCornerShape(Dimens.button_corner_radius)
                    ) {
                        if (isSavingPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = cardBg,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Simpan PDF")
                    }
                    OutlinedButton(
                        onClick = { shareImage() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Dimens.button_corner_radius)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bagikan PNG", color = greenAccent)
                    }
                    OutlinedButton(
                        onClick = { sharePdf() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Dimens.button_corner_radius)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bagikan PDF", color = greenAccent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_8))
        }
    }
}
}

