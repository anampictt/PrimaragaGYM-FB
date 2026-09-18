package com.pws.primaragagym.screens.superadmin.manajemenrole

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pws.primaragagym.di.ServiceLocator
import com.pws.primaragagym.domain.model.FirestoreRole
import com.pws.primaragagym.ui.viewmodel.RoleListViewModel

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

// ============================================================================
// ACCESS MENU MODEL - Sesuai menu nyata aplikasi (8 menu)
// ============================================================================
data class RoleAccessMenuItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val enabled: Boolean
)

private fun getInitialRoleAccessMenus(): List<RoleAccessMenuItem> = listOf(
    RoleAccessMenuItem("dashboard", "Dashboard", Icons.Filled.Dashboard, false),
    RoleAccessMenuItem("manajemen_pengguna", "Manajemen Pengguna", Icons.Filled.Person, false),
    RoleAccessMenuItem("manajemen_role", "Manajemen Role", Icons.Filled.Security, false),
    RoleAccessMenuItem("manajemen_member", "Member", Icons.Filled.Groups, false),
    RoleAccessMenuItem("check_in_out", "Check In & Check Out", Icons.Filled.QrCodeScanner, false),
    RoleAccessMenuItem("keuangan", "Keuangan", Icons.Filled.TrendingUp, false),
    RoleAccessMenuItem("notifikasi", "Notifikasi", Icons.Filled.Notifications, false)
)

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HakAksesScreen(
    roleId: String,
    viewModel: RoleListViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val roleState by viewModel.uiState.collectAsState()

    var currentRole by remember { mutableStateOf<FirestoreRole?>(null) }
    var accessMenus by remember { mutableStateOf(getInitialRoleAccessMenus()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load role directly from repository to ensure 100% fresh and accurate permission state
    LaunchedEffect(roleId, roleState.roles) {
        if (roleId.isNotBlank()) {
            // First check memory cache
            val cachedRole = roleState.roles.find { it.roleId == roleId }
            if (cachedRole != null) {
                currentRole = cachedRole
                accessMenus = getInitialRoleAccessMenus().map { menu ->
                    val isEnabled = when (menu.id) {
                        "check_in_out" -> cachedRole.permissions["check_in_out"] == true ||
                                cachedRole.permissions["check_in"] == true ||
                                cachedRole.permissions["check_out"] == true
                        else -> cachedRole.permissions[menu.id] == true
                    }
                    menu.copy(enabled = isEnabled)
                }
                isLoading = false
            }

            // Also query repository to get guaranteed fresh permissions directly from Firestore
            val result = ServiceLocator.roleRepository.getRoleById(roleId)
            result.onSuccess { role ->
                currentRole = role
                accessMenus = getInitialRoleAccessMenus().map { menu ->
                    val isEnabled = when (menu.id) {
                        "check_in_out" -> role.permissions["check_in_out"] == true ||
                                role.permissions["check_in"] == true ||
                                role.permissions["check_out"] == true
                        else -> role.permissions[menu.id] == true
                    }
                    menu.copy(enabled = isEnabled)
                }
                isLoading = false
            }.onFailure { e ->
                if (currentRole == null) {
                    errorMessage = e.message ?: "Gagal memuat detail role"
                }
                isLoading = false
            }
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hak Akses Role",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBackground
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenAccent)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = if (isTablet) 32.dp else 16.dp)
                    .padding(vertical = 20.dp)
            ) {
                // Role Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = null,
                                tint = GreenAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentRole?.name ?: "Role",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentRole?.description?.ifBlank { "Pengaturan hak akses role" }
                                    ?: "Pengaturan hak akses role",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section Title
                Text(
                    text = "Hak Akses Menu",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "Tentukan menu apa saja yang dapat diakses oleh role ini",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                // Menu Access List Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        accessMenus.forEachIndexed { index, menu ->
                            HakAksesMenuRow(
                                menu = menu,
                                onToggle = { enabled ->
                                    accessMenus = accessMenus.map {
                                        if (it.id == menu.id) it.copy(enabled = enabled) else it
                                    }
                                }
                            )

                            if (index < accessMenus.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .height(1.dp)
                                        .background(Color(0xFFF0F0F0))
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Save Permissions Button
                Button(
                    onClick = {
                        isSubmitting = true
                        errorMessage = null

                        val permissionsMap = accessMenus.associate { it.id to it.enabled }.toMutableMap()
                        if (permissionsMap.containsKey("check_in_out")) {
                            val checkVal = permissionsMap["check_in_out"] ?: false
                            permissionsMap["check_in"] = checkVal
                            permissionsMap["check_out"] = checkVal
                        }
                        viewModel.updateRolePermissions(roleId, permissionsMap) { success, errorMsg ->
                            isSubmitting = false
                            if (success) {
                                onSubmitSuccess()
                            } else {
                                errorMessage = errorMsg ?: "Gagal memperbarui hak akses"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenAccent,
                        contentColor = Color.White,
                        disabledContainerColor = GreenAccent.copy(alpha = 0.4f),
                        disabledContentColor = Color.White.copy(alpha = 0.6f)
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "Simpan Hak Akses",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ============================================================================
// HAK AKSES MENU ROW
// ============================================================================
@Composable
private fun HakAksesMenuRow(
    menu: RoleAccessMenuItem,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = menu.icon,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = menu.name,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }

        Switch(
            checked = menu.enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = GreenAccent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE0E0E0)
            )
        )
    }
}
