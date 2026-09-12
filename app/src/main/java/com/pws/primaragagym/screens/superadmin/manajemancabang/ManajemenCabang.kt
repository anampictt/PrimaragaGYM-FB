package com.pws.primaragagym.screens.superadmin.manajemancabang

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pws.primaragagym.ui.viewmodel.BranchListViewModel

// ============================================================================
// COLORS - Match ManajemenPengguna & ManajemenRole visual style exactly
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
// BRANCH MODEL
// ============================================================================
data class BranchUiModel(
    val id: String,
    val name: String,
    val address: String
)

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManajemenCabangScreen(
    viewModel: BranchListViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onAddBranchClick: () -> Unit = {},
    onEditBranch: (BranchUiModel) -> Unit = {},
    onDeleteBranch: (BranchUiModel) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    val branchState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var branchToDelete by remember { mutableStateOf<BranchUiModel?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadBranches()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadBranches()
    }

    val uiBranches = remember(branchState.branches) {
        branchState.branches.map {
            BranchUiModel(
                id = it.branchId,
                name = it.name,
                address = it.address
            )
        }
    }

    // Filter branches based on search query
    val filteredBranches = remember(searchQuery, uiBranches) {
        if (searchQuery.isBlank()) {
            uiBranches
        } else {
            uiBranches.filter { branch ->
                branch.name.contains(searchQuery, ignoreCase = true) ||
                branch.address.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Determine horizontal padding for tablet
    val horizontalPadding = if (isTablet) {
        24.dp
    } else {
        14.dp
    }

    if (branchToDelete != null) {
        AlertDialog(
            onDismissRequest = { branchToDelete = null },
            title = { Text("Hapus Cabang", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus cabang \"${branchToDelete?.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = branchToDelete
                        branchToDelete = null
                        if (target != null) {
                            viewModel.deleteBranch(target.id)
                            onDeleteBranch(target)
                        }
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { branchToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            BranchManagementTopBar(
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBranchClick,
                containerColor = GreenAccent,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Tambah Cabang"
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
            BranchSearchField(
                query = searchQuery,
                onQueryChange = { query -> searchQuery = query },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp,
                        vertical = 16.dp
                    )
            )

            // Loading state
            if (branchState.isLoading && uiBranches.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = GreenAccent,
                        modifier = Modifier.size(36.dp)
                    )
                }
            } else if (filteredBranches.isEmpty()) {
                // Empty State
                BranchEmptyState(
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
                        items = filteredBranches,
                        key = { it.id }
                    ) { branch ->
                        BranchCard(
                            branch = branch,
                            onEditClick = { onEditBranch(branch) },
                            onDeleteClick = { branchToDelete = branch }
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
private fun BranchManagementTopBar(
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Manajemen Cabang",
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
private fun BranchSearchField(
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
                        text = "Cari cabang...",
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
// BRANCH CARD
// ============================================================================
@Composable
private fun BranchCard(
    branch: BranchUiModel,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
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
            // Branch Icon
            BranchIconContainer(
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Branch Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Branch Name
                Text(
                    text = branch.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Address with Location Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = GreenAccent,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = branch.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
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
// BRANCH ICON CONTAINER
// ============================================================================
@Composable
private fun BranchIconContainer(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(GreenLight),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Business,
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
private fun BranchEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Cabang tidak ditemukan",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = TextSecondary
            )
        }
    }
}
