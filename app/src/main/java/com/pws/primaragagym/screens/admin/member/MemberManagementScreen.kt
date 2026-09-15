package com.pws.primaragagym.screens.admin.member

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
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
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
import com.pws.primaragagym.ui.theme.Dimens

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
                    qrContent = "PRIMARAGA_MEMBER:${member.memberCode}"
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
                    qrContent = "PRIMARAGA_MEMBER:${member.memberCode}"
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(horizontal = 16.dp),
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
        }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
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
                        .background(GreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.avatarInitial,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = GreenAccent
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
                            color = GreenAccent
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
                        Text(
                            text = "Expired: ${member.expiredDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, GreenAccent)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CreditCard,
                        contentDescription = null,
                        tint = GreenAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kartu Member",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = GreenAccent
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
