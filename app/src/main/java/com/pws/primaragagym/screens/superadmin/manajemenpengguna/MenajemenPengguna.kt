package com.pws.primaragagym.screens.superadmin.manajemenpengguna

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.pws.primaragagym.ui.components.common.AnimatedStatusPopup
import com.pws.primaragagym.ui.components.common.StatusPopupType
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pws.primaragagym.ui.viewmodel.UserListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ============================================================================
// COLORS - Match design system
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
// USER MODEL
// ============================================================================
data class UserUiModel(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val status: String,
    val avatarInitial: String,
    val photoUrl: String? = null,
    val createdAt: Date? = null
)

// ============================================================================
// MOCK DATA
// ============================================================================
private val mockUsers = listOf(
    UserUiModel(
        id = "1",
        name = "Sarah Connor",
        email = "sarah@kigym.com",
        role = "Admin",
        status = "Active",
        avatarInitial = "SC"
    ),
    UserUiModel(
        id = "2",
        name = "John Smith",
        email = "john.s@kigym.com",
        role = "Admin",
        status = "Active",
        avatarInitial = "JS"
    ),
    UserUiModel(
        id = "3",
        name = "Sarah Connor",
        email = "sarah@kigym.com",
        role = "Admin",
        status = "Active",
        avatarInitial = "SC"
    ),
    UserUiModel(
        id = "4",
        name = "John Smith",
        email = "john.s@kigym.com",
        role = "Admin",
        status = "Active",
        avatarInitial = "JS"
    )
)

// ============================================================================
// UI STATE
// ============================================================================
private data class UserManagementUiState(
    val users: List<UserUiModel> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManajemenPenggunaScreen(
    viewModel: UserListViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onAddUserClick: () -> Unit = {},
    onUserClick: (UserUiModel) -> Unit = {},
    onEditUser: (UserUiModel) -> Unit = {},
    onDeleteUser: (UserUiModel) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    val userState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var userToDelete by remember { mutableStateOf<UserUiModel?>(null) }
    var showDeleteSuccessDialog by remember { mutableStateOf(false) }
    var deletedUserName by remember { mutableStateOf("") }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadUsers()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    val uiUsers = remember(userState.users) {
        userState.users.map {
            val initials = it.displayName.split(" ")
                .take(2)
                .mapNotNull { part -> part.firstOrNull()?.uppercaseChar() }
                .joinToString("")
                .ifEmpty { "U" }
            UserUiModel(
                id = it.uid,
                name = it.displayName,
                email = it.email,
                role = it.resolvedRole,
                status = if (it.isActive) "Active" else "Inactive",
                avatarInitial = initials,
                photoUrl = it.photoUrl,
                createdAt = it.createdAt
            )
        }
    }

    // Filter users based on search query
    val filteredUsers = remember(searchQuery, uiUsers) {
        if (searchQuery.isBlank()) {
            uiUsers
        } else {
            uiUsers.filter { user ->
                user.name.contains(searchQuery, ignoreCase = true) ||
                user.email.contains(searchQuery, ignoreCase = true) ||
                user.role.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Determine horizontal padding for tablet
    val horizontalPadding = if (isTablet) {
        24.dp
    } else {
        14.dp
    }

    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Hapus Pengguna", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus pengguna \"${userToDelete?.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = userToDelete
                        userToDelete = null
                        if (target != null) {
                            viewModel.deleteUser(target.id) { success, _ ->
                                if (success) {
                                    deletedUserName = target.name
                                    showDeleteSuccessDialog = true
                                    onDeleteUser(target)
                                }
                            }
                        }
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    AnimatedStatusPopup(
        visible = showDeleteSuccessDialog,
        type = StatusPopupType.SUCCESS_DELETE,
        title = "Pengguna Berhasil Dihapus",
        message = "Data pengguna \"$deletedUserName\" berhasil dihapus dari sistem.",
        confirmButtonText = "Selesai",
        onDismiss = { showDeleteSuccessDialog = false }
    )

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            UserManagementTopBar(
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddUserClick,
                containerColor = GreenAccent,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Tambah Pengguna"
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
            SearchField(
                query = searchQuery,
                onQueryChange = { query -> searchQuery = query },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp,
                        vertical = 16.dp
                    )
            )

            if (userState.isLoading && uiUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            } else if (filteredUsers.isEmpty()) {
                // Empty State
                EmptyState(
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
                        items = filteredUsers,
                        key = { it.id }
                    ) { user ->
                        UserCard(
                            user = user,
                            onClick = { onUserClick(user) },
                            onEditClick = { onEditUser(user) },
                            onDeleteClick = { userToDelete = user }
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
private fun UserManagementTopBar(
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Manajemen User",
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
private fun SearchField(
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
                        text = "Cari pengguna...",
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
// USER CARD
// ============================================================================
@Composable
private fun UserCard(
    user: UserUiModel,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
            // Avatar
            UserAvatar(
                initial = user.avatarInitial,
                photoUrl = user.photoUrl,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // User Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Name
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Email
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoleBadge(role = user.role)
                    StatusBadge(status = user.status)
                }

                if (user.createdAt != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val dateStr = remember(user.createdAt) {
                        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale("id", "ID")).apply {
                            timeZone = TimeZone.getTimeZone("Asia/Jakarta")
                        }
                        sdf.format(user.createdAt)
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
// USER AVATAR
// ============================================================================
@Composable
private fun UserAvatar(
    initial: String,
    photoUrl: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(GreenLight),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = initial,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = GreenAccent
            )
        }
    }
}

// ============================================================================
// ROLE BADGE
// ============================================================================
@Composable
private fun RoleBadge(role: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(GreenLight)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = role,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = GreenAccent
        )
    }
}

// ============================================================================
// STATUS BADGE
// ============================================================================
@Composable
private fun StatusBadge(status: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Status Dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(GreenAccent)
            )

            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = GreenAccent
            )
        }
    }
}

// ============================================================================
// EMPTY STATE
// ============================================================================
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tidak ada pengguna ditemukan",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = TextSecondary
            )
        }
    }
}
