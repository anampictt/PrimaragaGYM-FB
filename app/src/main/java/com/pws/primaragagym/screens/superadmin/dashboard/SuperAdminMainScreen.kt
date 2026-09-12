package com.pws.primaragagym.screens.superadmin.dashboard


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pws.primaragagym.navigation.AppScreen
import com.pws.primaragagym.navigation.sharedAdminRoutes
import com.pws.primaragagym.screens.superadmin.manajemancabang.ManajemenCabangScreen
import com.pws.primaragagym.screens.superadmin.manajemancabang.TambahCabangScreen
import com.pws.primaragagym.screens.superadmin.manajemenpengguna.ManajemenPenggunaScreen
import com.pws.primaragagym.screens.superadmin.manajemenpengguna.TambahPenggunaScreen
import com.pws.primaragagym.screens.superadmin.manajemenrole.HakAksesScreen
import com.pws.primaragagym.screens.superadmin.manajemenrole.ManajemenRoleScreen
import com.pws.primaragagym.screens.superadmin.manajemenrole.TambahRoleScreen
import com.pws.primaragagym.ui.viewmodel.AuthViewModel

private val BackgroundColor = Color(0xFFF5F7FA)

@Composable
fun SuperAdminMainScreen(
    rootNavController: NavHostController,
    authViewModel: AuthViewModel = AuthViewModel()
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val nestedNavController = rememberNavController()
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppScreen.SuperAdminDashboard.route

    // Debouncing: prevent rapid successive navigation calls
    val navigationDebounceMillis = 500L

    // Guard: prevent multiple concurrent pop operations
    var isPopInProgress by remember { mutableStateOf(false) }

    // Map current route to BottomNavItem
    val selectedBottomNav = when {
        currentRoute.startsWith(AppScreen.Keuangan.route) || currentRoute.startsWith("admin/keuangan") -> BottomNavItem.KEUANGAN
        currentRoute == AppScreen.Profil.route -> BottomNavItem.PENGATURAN_AKUN
        currentRoute == AppScreen.SuperAdminDashboard.route -> BottomNavItem.DASHBOARD
        else -> if (!isTablet) BottomNavItem.DASHBOARD else null
    }

    // Map current route to Menu key & index
    val selectedMenuKey = when {
        currentRoute.startsWith("superadmin/manajemen-pengguna") -> "manajemen_pengguna"
        currentRoute.startsWith("superadmin/manajemen-role") -> "manajemen_role"
        currentRoute.startsWith("superadmin/manajemen-cabang") -> "manajemen_cabang"
        currentRoute.startsWith(AppScreen.Member.route) -> "manajemen_member"
        currentRoute.startsWith(AppScreen.CheckInCheckout.route) -> "check_in_out"
        currentRoute.startsWith(AppScreen.Notifikasi.route) -> "notifikasi"
        currentRoute.startsWith(AppScreen.LaporanKeuangan.route) || currentRoute.startsWith(AppScreen.LaporanPemasukan.route) -> "laporan"
        else -> null
    }

    val selectedMenuIndex = when (selectedMenuKey) {
        "manajemen_pengguna" -> 0
        "manajemen_role" -> 1
        "manajemen_cabang" -> 2
        "manajemen_member" -> 3
        "check_in_out" -> 4
        "notifikasi" -> 5
        "laporan" -> 6
        else -> null
    }

    // Safe navigate with debounce and launchSingleTop
    // Using a data class + mutableState for debounce to avoid label issues with typed lambdas
    val navDebouncer = remember { mutableLongStateOf(0L) }

    fun safeNavigate(route: String) {
        val now = System.currentTimeMillis()
        val lastTime = navDebouncer.value
        if (now - lastTime < navigationDebounceMillis) return
        navDebouncer.value = now

        // Navigating to Dashboard: clear entire stack first, then navigate to it.
        // This handles the case where user is on any child screen and taps Dashboard.
        if (route == AppScreen.SuperAdminDashboard.route) {
            nestedNavController.navigate(AppScreen.SuperAdminDashboard.route) {
                popUpTo(0) { inclusive = false }
            }
        } else if (currentRoute != route) {
            nestedNavController.navigate(route) {
                popUpTo(AppScreen.SuperAdminDashboard.route) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    // Safe pop with guard against empty back stack
    fun safePopBack(): Boolean {
        if (isPopInProgress) return false
        val hasEntries = nestedNavController.previousBackStackEntry != null
        if (!hasEntries) return false
        isPopInProgress = true
        val result = nestedNavController.popBackStack()
        isPopInProgress = false
        return result
    }

    // Intercept system/device back button
    BackHandler {
        val hasNestedEntries = nestedNavController.previousBackStackEntry != null
        if (hasNestedEntries) {
            safePopBack()
        } else {
            // At nested root: finish the activity (exit app)
            (context as? android.app.Activity)?.finish()
        }
    }

    val onBottomNavSelected: (BottomNavItem) -> Unit = { item ->
        val route = when (item) {
            BottomNavItem.DASHBOARD -> AppScreen.SuperAdminDashboard.route
            BottomNavItem.KEUANGAN -> AppScreen.Keuangan.route
            BottomNavItem.PENGATURAN_AKUN -> AppScreen.Profil.route
        }
        safeNavigate(route)
    }

    if (isTablet) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
        ) {
            TabletSidebar(
                selectedItem = selectedBottomNav,
                selectedMenuIndex = selectedMenuIndex,
                selectedMenuKey = selectedMenuKey,
                authViewModel = authViewModel,
                onItemSelected = onBottomNavSelected,
                onUserManagementClick = { safeNavigate(AppScreen.ManajemenPengguna.route) },
                onRoleManagementClick = { safeNavigate(AppScreen.ManajemenRole.route) },
                onBranchManagementClick = { safeNavigate(AppScreen.ManajemenCabang.route) },
                onMemberClick = { safeNavigate(AppScreen.Member.route) },
                onCheckInOutClick = { safeNavigate(AppScreen.CheckInCheckout.route) },
                onCatatanKeuanganClick = { safeNavigate(AppScreen.Keuangan.route) },
                onNotificationClick = { safeNavigate(AppScreen.Notifikasi.route) },
                onReportClick = { safeNavigate(AppScreen.LaporanPemasukan.route) }
            )

            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                SuperAdminNestedNavHost(
                    navController = nestedNavController,
                    safePopBack = { safePopBack() },
                    authViewModel = authViewModel,
                    rootNavController = rootNavController
                )
            }
        }
    } else {
        Scaffold(
            containerColor = BackgroundColor,
            bottomBar = {
                BottomNavigationBar(
                    selectedItem = selectedBottomNav,
                    onItemSelected = onBottomNavSelected
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                SuperAdminNestedNavHost(
                    navController = nestedNavController,
                    safePopBack = { safePopBack() },
                    authViewModel = authViewModel,
                    rootNavController = rootNavController
                )
            }
        }
    }
}

@Composable
private fun SuperAdminNestedNavHost(
    navController: NavHostController,
    safePopBack: () -> Boolean,
    authViewModel: AuthViewModel,
    rootNavController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = AppScreen.SuperAdminDashboard.route
    ) {
        composable(AppScreen.SuperAdminDashboard.route) {
            SuperAdminDashboardContent(
                authViewModel = authViewModel,
                onUserManagementClick = { navController.navigate(AppScreen.ManajemenPengguna.route) },
                onRoleManagementClick = { navController.navigate(AppScreen.ManajemenRole.route) },
                onBranchManagementClick = { navController.navigate(AppScreen.ManajemenCabang.route) },
                onMemberClick = { navController.navigate(AppScreen.Member.route) },
                onCheckInOutClick = { navController.navigate(AppScreen.CheckInCheckout.route) },
                onCatatanKeuanganClick = { navController.navigate(AppScreen.Keuangan.route) },
                onNotificationClick = { navController.navigate(AppScreen.Notifikasi.route) },
                onReportClick = { navController.navigate(AppScreen.LaporanPemasukan.route) },
                onAccountSettingsClick = { navController.navigate(AppScreen.Profil.route) }
            )
        }

        composable(AppScreen.ManajemenPengguna.route) {
            ManajemenPenggunaScreen(
                onBackClick = { safePopBack() },
                onAddUserClick = { navController.navigate(AppScreen.TambahPengguna.createRoute()) },
                onEditUser = { user ->
                    navController.navigate(AppScreen.TambahPengguna.createRoute(user.id))
                },
                onDeleteUser = { }
            )
        }

        composable(
            route = "superadmin/manajemen-pengguna/tambah?userId={userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            TambahPenggunaScreen(
                userId = userId,
                onBackClick = { safePopBack() },
                onSubmitSuccess = { safePopBack() }
            )
        }

        composable(AppScreen.ManajemenRole.route) {
            ManajemenRoleScreen(
                onBackClick = { safePopBack() },
                onAddRoleClick = { navController.navigate(AppScreen.TambahRole.createRoute()) },
                onEditRole = { role ->
                    navController.navigate(AppScreen.TambahRole.createRoute(role.id))
                },
                onDeleteRole = { },
                onAccessClick = { role ->
                    navController.navigate(AppScreen.HakAksesRole.createRoute(role.id))
                }
            )
        }

        composable(
            route = "superadmin/manajemen-role/tambah?roleId={roleId}",
            arguments = listOf(
                navArgument("roleId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val roleId = backStackEntry.arguments?.getString("roleId")
            TambahRoleScreen(
                roleId = roleId,
                onBackClick = { safePopBack() },
                onSubmitSuccess = { safePopBack() }
            )
        }

        composable(
            route = "superadmin/manajemen-role/hak-akses?roleId={roleId}",
            arguments = listOf(
                navArgument("roleId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val roleId = backStackEntry.arguments?.getString("roleId") ?: ""
            HakAksesScreen(
                roleId = roleId,
                onBackClick = { safePopBack() },
                onSubmitSuccess = { safePopBack() }
            )
        }

        composable(AppScreen.ManajemenCabang.route) {
            ManajemenCabangScreen(
                onBackClick = { safePopBack() },
                onAddBranchClick = { navController.navigate(AppScreen.TambahCabang.route) },
                onEditBranch = { },
                onDeleteBranch = { }
            )
        }

        composable(AppScreen.TambahCabang.route) {
            TambahCabangScreen(
                onBackClick = { safePopBack() },
                onSubmitSuccess = { safePopBack() }
            )
        }

        sharedAdminRoutes(navController, authViewModel, rootNavController)
    }
}