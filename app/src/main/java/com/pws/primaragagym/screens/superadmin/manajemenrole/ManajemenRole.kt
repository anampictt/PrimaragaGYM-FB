package com.pws.primaragagym.screens.superadmin.manajemenrole

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import com.pws.primaragagym.ui.components.common.AnimatedStatusPopup
import com.pws.primaragagym.ui.components.common.StatusPopupType
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pws.primaragagym.ui.viewmodel.RoleListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ============================================================================
// COLORS - Match ManajemenPengguna visual style exactly
// ============================================================================
private val BackgroundColor = Color(0xFFF5F7FA)
private val CardBackground = Color.White
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B6B6B)
private val TextMuted = Color(0xFF9E9E9E)
private val GreenAccent = Color(0xFF32A060)
private val GreenLight = Color(0xFFE8F5E9)
private val DividerColor = Color(0xFFE8E8E8)

// ============================================================================
// ROLE MODEL
// ============================================================================
data class RoleUiModel(
    val id: String,
    val name: String,
    val description: String,
    val createdAt: Date? = null
)

// ============================================================================
// MOCK DATA
// ============================================================================
private val mockRoles = listOf(
    RoleUiModel(
        id = "1",
        name = "Super Admin",
        description = "Akses penuh ke seluruh sistem."
    ),
    RoleUiModel(
        id = "2",
        name = "Admin",
        description = "Mengelola operasional gym dan data member."
    ),
    RoleUiModel(
        id = "3",
        name = "Staff",
        description = "Mengelola aktivitas operasional sesuai hak akses."
    ),
    RoleUiModel(
        id = "4",
        name = "Trainer",
        description = "Mengakses data dan fitur yang berkaitan dengan trainer."
    )
)

// ============================================================================
// UI STATE
// ============================================================================
private data class RoleManagementUiState(
    val roles: List<RoleUiModel> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManajemenRoleScreen(
    viewModel: RoleListViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onAddRoleClick: () -> Unit = {},
    onEditRole: (RoleUiModel) -> Unit = {},
    onDeleteRole: (RoleUiModel) -> Unit = {},
    onAccessClick: (RoleUiModel) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    val roleState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var roleToDelete by remember { mutableStateOf<RoleUiModel?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadRoles()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadRoles()
    }

    val uiRoles = remember(roleState.roles) {
        roleState.roles.map {
            RoleUiModel(
                id = it.roleId,
                name = it.name,
                description = it.description.ifBlank { "Hak akses ${it.name}" },
                createdAt = it.createdAt
            )
        }
    }

    // Filter roles based on search query
    val filteredRoles = remember(searchQuery, uiRoles) {
        if (searchQuery.isBlank()) {
            uiRoles
        } else {
            uiRoles.filter { role ->
                role.name.contains(searchQuery, ignoreCase = true) ||
                role.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Determine horizontal padding for tablet
    val horizontalPadding = if (isTablet) {
        24.dp
    } else {
        14.dp
    }

    var showDeleteSuccessPopup by remember { mutableStateOf(false) }
    var deletedRoleName by remember { mutableStateOf("") }

    if (roleToDelete != null) {
        AlertDialog(
            onDismissRequest = { roleToDelete = null },
            title = { Text("Hapus Role", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus role \"${roleToDelete?.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = roleToDelete
                        roleToDelete = null
                        if (target != null) {
                            deletedRoleName = target.name
                            viewModel.deleteRole(target.id)
                            onDeleteRole(target)
                            showDeleteSuccessPopup = true
                        }
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { roleToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    AnimatedStatusPopup(
        visible = showDeleteSuccessPopup,
        type = StatusPopupType.SUCCESS_DELETE,
        title = "Role Berhasil Dihapus",
        message = "Role \"$deletedRoleName\" telah berhasil dihapus dari sistem.",
        confirmButtonText = "Selesai",
        onDismiss = { showDeleteSuccessPopup = false }
    )

    Scaffold(
        containerColor = BackgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            RoleManagementTopBar(
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRoleClick,
                containerColor = GreenAccent,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Tambah Role"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            // Search Field
            RoleSearchField(
                query = searchQuery,
                onQueryChange = { query -> searchQuery = query },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp,
                        vertical = 16.dp
                    )
            )

            if (roleState.isLoading && uiRoles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            } else if (filteredRoles.isEmpty()) {
                // Empty State
                RoleEmptyState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp)
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
                        items = filteredRoles,
                        key = { it.id }
                    ) { role ->
                        RoleCard(
                            role = role,
                            onEditClick = { onEditRole(role) },
                            onDeleteClick = { roleToDelete = role },
                            onAccessClick = { onAccessClick(role) }
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// TOP APP BAR
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleManagementTopBar(
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Manajemen Role",
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
private fun RoleSearchField(
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
                        text = "Cari role...",
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
                    keyboardActions = KeyboardActions(
                        onSearch = { }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ============================================================================
// ROLE CARD
// ============================================================================
@Composable
private fun RoleCard(
    role: RoleUiModel,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAccessClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Role Icon
            RoleIconContainer(
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Role Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Role Name
                Text(
                    text = role.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Description
                Text(
                    text = role.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (role.createdAt != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val dateStr = remember(role.createdAt) {
                        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale("id", "ID")).apply {
                            timeZone = TimeZone.getTimeZone("Asia/Jakarta")
                        }
                        sdf.format(role.createdAt)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Dibuat: $dateStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }

            // Actions
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(DividerColor)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Edit Button
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Hak Access Button (Green)
                IconButton(
                    onClick = onAccessClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = "Cek Hak Access",
                        tint = GreenAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Hapus",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// ROLE ICON CONTAINER
// ============================================================================
@Composable
private fun RoleIconContainer(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(GreenLight),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.AdminPanelSettings,
            contentDescription = null,
            tint = GreenAccent,
            modifier = Modifier.size(24.dp)
        )
    }
}

// ============================================================================
// EMPTY STATE
// ============================================================================
@Composable
private fun RoleEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Role tidak ditemukan",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = TextSecondary
            )
        }
    }
}
