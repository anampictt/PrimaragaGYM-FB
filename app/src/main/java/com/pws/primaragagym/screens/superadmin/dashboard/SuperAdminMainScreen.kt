package com.pws.primaragagym.screens.superadmin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pws.primaragagym.navigation.AppScreen
import com.pws.primaragagym.navigation.sharedAdminRoutes
import com.pws.primaragagym.screens.superadmin.manajemancabang.ManajemenCabangScreen
import com.pws.primaragagym.screens.superadmin.manajemancabang.TambahCabangScreen
import com.pws.primaragagym.screens.superadmin.manajemenpengguna.ManajemenPenggunaScreen
import com.pws.primaragagym.screens.superadmin.manajemenpengguna.TambahPenggunaScreen
import com.pws.primaragagym.screens.superadmin.manajemenrole.ManajemenRoleScreen
import com.pws.primaragagym.screens.superadmin.manajemenrole.TambahRoleScreen

private val BackgroundColor = Color(0xFFF5F7FA)

@Composable
fun SuperAdminMainScreen(
    rootNavController: NavHostController
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val nestedNavController = rememberNavController()
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppScreen.SuperAdminDashboard.route

    // Map current route to BottomNavItem
    val selectedBottomNav = when (currentRoute) {
        AppScreen.SuperAdminDashboard.route -> BottomNavItem.DASHBOARD
        AppScreen.CatatanKeuangan.route -> BottomNavItem.KEUANGAN
        AppScreen.Profil.route -> BottomNavItem.PENGATURAN_AKUN
        else -> null
    }

    // Map current route to Menu index
    val selectedMenuIndex = when {
        currentRoute.startsWith("superadmin/manajemen-pengguna") -> 0
        currentRoute.startsWith("superadmin/manajemen-role") -> 1
        currentRoute.startsWith("superadmin/manajemen-cabang") -> 2
        currentRoute.startsWith(AppScreen.Member.route) -> 3
        currentRoute.startsWith(AppScreen.CheckInCheckout.route) -> 4
        currentRoute.startsWith(AppScreen.CatatanKeuangan.route) -> 5
        currentRoute.startsWith(AppScreen.Notifikasi.route) -> 6
        currentRoute.startsWith(AppScreen.LaporanKeuangan.route) -> 7
        else -> null
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
                onItemSelected = { item ->
                    val route = when(item) {
                        BottomNavItem.DASHBOARD -> AppScreen.SuperAdminDashboard.route
                        BottomNavItem.KEUANGAN -> AppScreen.CatatanKeuangan.route
                        BottomNavItem.PENGATURAN_AKUN -> AppScreen.Profil.route
                    }
                    if (currentRoute != route) {
                        nestedNavController.navigate(route) {
                            popUpTo(AppScreen.SuperAdminDashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onUserManagementClick = { nestedNavController.navigate(AppScreen.ManananakanPengguna.route) },
                onRoleManagementClick = { nestedNavController.navigate(AppScreen.ManananakanRole.route) },
                onBranchManagementClick = { nestedNavController.navigate(AppScreen.ManananakanCabang.route) },
                onMemberClick = { nestedNavController.navigate(AppScreen.Member.route) },
                onCheckInOutClick = { nestedNavController.navigate(AppScreen.CheckInCheckout.route) },
                onCatatanKeuanganClick = { nestedNavController.navigate(AppScreen.CatatanKeuangan.route) },
                onNotificationClick = { nestedNavController.navigate(AppScreen.Notifikasi.route) },
                onReportClick = { nestedNavController.navigate(AppScreen.LaporanKeuangan.route) }
            )

            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                SuperAdminNestedNavHost(
                    navController = nestedNavController,
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
                    onItemSelected = { item ->
                        val route = when(item) {
                            BottomNavItem.DASHBOARD -> AppScreen.SuperAdminDashboard.route
                            BottomNavItem.KEUANGAN -> AppScreen.CatatanKeuangan.route
                            BottomNavItem.PENGATURAN_AKUN -> AppScreen.Profil.route
                        }
                        if (currentRoute != route) {
                            nestedNavController.navigate(route) {
                                popUpTo(AppScreen.SuperAdminDashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
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
                    rootNavController = rootNavController
                )
            }
        }
    }
}

@Composable
private fun SuperAdminNestedNavHost(
    navController: NavHostController,
    rootNavController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = AppScreen.SuperAdminDashboard.route
    ) {
        composable(AppScreen.SuperAdminDashboard.route) {
            SuperAdminDashboardContent(
                onUserManagementClick = { navController.navigate(AppScreen.ManananakanPengguna.route) },
                onRoleManagementClick = { navController.navigate(AppScreen.ManananakanRole.route) },
                onBranchManagementClick = { navController.navigate(AppScreen.ManananakanCabang.route) },
                onMemberClick = { navController.navigate(AppScreen.Member.route) },
                onCheckInOutClick = { navController.navigate(AppScreen.CheckInCheckout.route) },
                onCatatanKeuanganClick = { navController.navigate(AppScreen.CatatanKeuangan.route) },
                onNotificationClick = { navController.navigate(AppScreen.Notifikasi.route) },
                onReportClick = { navController.navigate(AppScreen.LaporanKeuangan.route) },
                onAccountSettingsClick = { navController.navigate(AppScreen.Profil.route) }
            )
        }

        composable(AppScreen.ManananakanPengguna.route) {
            ManajemenPenggunaScreen(
                onBackClick = { navController.popBackStack() },
                onAddUserClick = { navController.navigate(AppScreen.TambahPengguna.route) },
                onEditUser = { },
                onDeleteUser = { }
            )
        }

        composable(AppScreen.TambahPengguna.route) {
            TambahPenggunaScreen(
                onBackClick = { navController.popBackStack() },
                onSubmitSuccess = { navController.popBackStack() }
            )
        }

        composable(AppScreen.ManananakanRole.route) {
            ManajemenRoleScreen(
                onBackClick = { navController.popBackStack() },
                onAddRoleClick = { navController.navigate(AppScreen.TambahRole.route) },
                onEditRole = { },
                onDeleteRole = { },
                onAccessClick = { }
            )
        }

        composable(AppScreen.TambahRole.route) {
            TambahRoleScreen(
                onBackClick = { navController.popBackStack() },
                onSubmitSuccess = { navController.popBackStack() }
            )
        }

        composable(AppScreen.ManananakanCabang.route) {
            ManajemenCabangScreen(
                onBackClick = { navController.popBackStack() },
                onAddBranchClick = { navController.navigate(AppScreen.TambahCabang.route) },
                onEditBranch = { },
                onDeleteBranch = { }
            )
        }

        composable(AppScreen.TambahCabang.route) {
            TambahCabangScreen(
                onBackClick = { navController.popBackStack() },
                onSubmitSuccess = { navController.popBackStack() }
            )
        }

        sharedAdminRoutes(navController)
    }
}
