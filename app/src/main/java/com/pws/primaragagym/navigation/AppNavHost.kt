package com.pws.primaragagym.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pws.primaragagym.screens.ProfileScreen
import com.pws.primaragagym.screens.SplashScreen
import com.pws.primaragagym.screens.admin.dashboard.AdminDashboardScreen
import com.pws.primaragagym.screens.admin.member.DetailMemberScreen
import com.pws.primaragagym.screens.admin.member.MemberManagementScreen
import com.pws.primaragagym.screens.admin.member.MemberScreen
import com.pws.primaragagym.screens.admin.member.MembershipDetailScreen
import com.pws.primaragagym.screens.admin.member.MembershipManagementScreen
import com.pws.primaragagym.screens.admin.member.MembershipPlanScreen
import com.pws.primaragagym.screens.admin.member.PerpanjangMembershipScreen
import com.pws.primaragagym.screens.admin.member.RegistrasiMemberScreen
import com.pws.primaragagym.screens.admin.member.RiwayatTransaksiScreen
import com.pws.primaragagym.screens.admin.member.TambahMembershipPlanScreen
import com.pws.primaragagym.screens.admin.member.UpgradeDowngradeScreen

import com.pws.primaragagym.screens.superadmin.manajemancabang.ManajemenCabangScreen
import com.pws.primaragagym.screens.superadmin.manajemancabang.TambahCabangScreen
import com.pws.primaragagym.screens.superadmin.manajemenpengguna.ManajemenPenggunaScreen
import com.pws.primaragagym.screens.superadmin.manajemenpengguna.TambahPenggunaScreen
import com.pws.primaragagym.screens.superadmin.manajemenrole.ManajemenRoleScreen
import com.pws.primaragagym.screens.superadmin.manajemenrole.TambahRoleScreen

/**
 * Main navigation host for the app.
 * Manages all navigation routes and back stack.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: AppScreen = AppScreen.Splash,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier
    ) {
        // ==================== SPLASH ====================
        composable(AppScreen.Splash.route) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(AppScreen.Login.route) {
                        popUpTo(AppScreen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ==================== AUTH ====================
        composable(AppScreen.Login.route) {
            LoginNavHost(navController = navController)
        }
        // ==================== SUPER ADMIN ====================
        composable(AppScreen.SuperAdminRoot.route) {
            com.pws.primaragagym.screens.superadmin.dashboard.SuperAdminMainScreen(
                rootNavController = navController
            )
        }


        // ==================== ADMIN ====================
        composable(AppScreen.AdminDashboard.route) {
            AdminDashboardScreen(
                onMemberClick = {
                    navController.navigate(AppScreen.Member.route)
                },
                onCheckInOutClick = {
                    navController.navigate(AppScreen.CheckInCheckout.route)
                },
                onFinanceClick = {
                    navController.navigate(AppScreen.CatatanKeuangan.route)
                },
                onNotificationClick = {
                    navController.navigate(AppScreen.Notifikasi.route)
                },
                onReportClick = {
                    navController.navigate(AppScreen.LaporanKeuangan.route)
                },
                onProfileClick = {
                    navController.navigate(AppScreen.Profil.route)
                }
            )
        }
        sharedAdminRoutes(navController)
    }
}

fun androidx.navigation.NavGraphBuilder.sharedAdminRoutes(navController: androidx.navigation.NavHostController) {


    // ==================== MEMBER HUB ====================
    composable(AppScreen.Member.route) {
        MemberScreen(
            onBackClick = {
                navController.popBackStack()
            },
            onMemberManagementClick = {
                navController.navigate(AppScreen.MemberManagement.route)
            },
            onMembershipManagementClick = {
                navController.navigate(AppScreen.MembershipManagement.route)
            },
            onMembershipPlanClick = {
                navController.navigate(AppScreen.MembershipPlan.route)
            },
            onMemberClick = { memberId ->
                navController.navigate(AppScreen.DetailMember.createRoute(memberId))
            }
        )
    }

    // ==================== MEMBER MANAGEMENT ====================
    composable(AppScreen.MemberManagement.route) {
        MemberManagementScreen(
            onBackClick = {
                navController.popBackStack()
            },
            onAddMemberClick = {
                navController.navigate(AppScreen.RegistrasiMember.route)
            },
            onMemberClick = { memberId ->
                navController.navigate(AppScreen.DetailMember.createRoute(memberId))
            }
        )
    }

    composable(
        route = AppScreen.DetailMember.route,
        arguments = listOf(navArgument("memberId") { type = NavType.StringType })
    ) { backStackEntry ->
        val memberId = backStackEntry.arguments?.getString("memberId") ?: ""
        DetailMemberScreen(
            memberId = memberId,
            onBackClick = { navController.popBackStack() },
            onPerpanjangClick = {
                navController.navigate(AppScreen.PerpanjangMembership.createRoute(memberId))
            },
            onUpgradeClick = {
                navController.navigate(AppScreen.UpgradeDowngrade.createRoute(memberId))
            },
            onRiwayatClick = {
                navController.navigate(AppScreen.RiwayatTransaksi.createRoute(memberId))
            }
        )
    }

    composable(AppScreen.RegistrasiMember.route) {
        RegistrasiMemberScreen(
            onBackClick = { navController.popBackStack() },
            onSubmitSuccess = {
                navController.popBackStack()
            }
        )
    }

    // ==================== MEMBERSHIP MANAGEMENT ====================
    composable(AppScreen.MembershipManagement.route) {
        MembershipManagementScreen(
            onBackClick = { navController.popBackStack() },
            onMembershipClick = { memberId ->
                navController.navigate(AppScreen.MembershipDetail.createRoute(memberId))
            }
        )
    }

    composable(
        route = AppScreen.MembershipDetail.route,
        arguments = listOf(navArgument("memberId") { type = NavType.StringType })
    ) { backStackEntry ->
        val memberId = backStackEntry.arguments?.getString("memberId") ?: ""
        MembershipDetailScreen(
            memberId = memberId,
            onBackClick = { navController.popBackStack() },
            onPerpanjangClick = {
                navController.navigate(AppScreen.PerpanjangMembership.createRoute(memberId))
            },
            onUpgradeClick = {
                navController.navigate(AppScreen.UpgradeDowngrade.createRoute(memberId))
            },
            onRiwayatClick = {
                navController.navigate(AppScreen.RiwayatTransaksi.createRoute(memberId))
            }
        )
    }

    composable(
        route = AppScreen.PerpanjangMembership.route,
        arguments = listOf(navArgument("memberId") { type = NavType.StringType })
    ) { backStackEntry ->
        val memberId = backStackEntry.arguments?.getString("memberId") ?: ""
        PerpanjangMembershipScreen(
            memberId = memberId,
            onBackClick = { navController.popBackStack() },
            onSubmitSuccess = {
                navController.popBackStack()
            }
        )
    }

    composable(
        route = AppScreen.UpgradeDowngrade.route,
        arguments = listOf(navArgument("memberId") { type = NavType.StringType })
    ) { backStackEntry ->
        val memberId = backStackEntry.arguments?.getString("memberId") ?: ""
        UpgradeDowngradeScreen(
            memberId = memberId,
            onBackClick = { navController.popBackStack() },
            onSubmitSuccess = {
                navController.popBackStack()
            }
        )
    }

    composable(
        route = AppScreen.RiwayatTransaksi.route,
        arguments = listOf(navArgument("memberId") { type = NavType.StringType })
    ) { backStackEntry ->
        val memberId = backStackEntry.arguments?.getString("memberId") ?: ""
        RiwayatTransaksiScreen(
            memberId = memberId,
            onBackClick = { navController.popBackStack() }
        )
    }

    // ==================== MEMBERSHIP PLAN ====================
    composable(AppScreen.MembershipPlan.route) {
        MembershipPlanScreen(
            onBackClick = { navController.popBackStack() },
            onAddPlanClick = {
                navController.navigate(AppScreen.TambahMembershipPlan.route)
            },
            onEditPlan = { planId ->
                navController.navigate(AppScreen.EditMembershipPlan.createRoute(planId))
            }
        )
    }

    composable(AppScreen.TambahMembershipPlan.route) {
        TambahMembershipPlanScreen(
            planId = null,
            onBackClick = { navController.popBackStack() },
            onSubmitSuccess = {
                navController.popBackStack()
            }
        )
    }

    composable(
        route = AppScreen.EditMembershipPlan.route,
        arguments = listOf(navArgument("planId") { type = NavType.StringType })
    ) { backStackEntry ->
        val planId = backStackEntry.arguments?.getString("planId") ?: ""
        TambahMembershipPlanScreen(
            planId = planId,
            onBackClick = { navController.popBackStack() },
            onSubmitSuccess = {
                navController.popBackStack()
            }
        )
    }

    // ==================== PLACEHOLDER ROUTES ====================
    composable(AppScreen.CheckInCheckout.route) {
        com.pws.primaragagym.screens.admin.checkin.CheckinCheckoutScreen(
            onBackClick = { navController.popBackStack() },
            onNavigateToScanner = {
                navController.navigate(AppScreen.CheckInScanner.route)
            },
            onNavigateToDetail = { memberId ->
                navController.navigate(AppScreen.CheckInMemberDetail.createRoute(memberId))
            }
        )
    }

    composable(AppScreen.CheckInScanner.route) {
        com.pws.primaragagym.screens.admin.checkin.CheckinScannerScreen(
            onBackClick = { navController.popBackStack() },
            onSimulateScan = { memberId ->
                navController.navigate(AppScreen.CheckInMemberDetail.createRoute(memberId)) {
                    popUpTo(AppScreen.CheckInCheckout.route)
                }
            }
        )
    }

    composable(
        route = AppScreen.CheckInMemberDetail.route,
        arguments = listOf(androidx.navigation.navArgument("memberId") { type = androidx.navigation.NavType.StringType })
    ) { backStackEntry ->
        val memberId = backStackEntry.arguments?.getString("memberId") ?: ""
        com.pws.primaragagym.screens.admin.checkin.CheckinMemberDetailScreen(
            memberId = memberId,
            onBackClick = { navController.popBackStack() }
        )
    }

    composable(AppScreen.CatatanKeuangan.route) {
        PlaceholderScreen(
            title = "Catatan Keuangan",
            onBackClick = { navController.popBackStack() }
        )
    }

    composable(AppScreen.Notifikasi.route) {
        PlaceholderScreen(
            title = "Notifikasi",
            onBackClick = { navController.popBackStack() }
        )
    }

    composable(AppScreen.LaporanKeuangan.route) {
        PlaceholderScreen(
            title = "Laporan Keuangan",
            onBackClick = { navController.popBackStack() }
        )
    }

    // ==================== PROFILE ====================
    composable(AppScreen.Profil.route) {
        ProfileScreen(
            onBackClick = {
                navController.popBackStack()
            },
            onChangePasswordClick = {
                navController.navigate(AppScreen.UbahKataSandi.route)
            },
            onLogoutConfirm = {
                navController.navigate(AppScreen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    composable(AppScreen.UbahKataSandi.route) {
        PlaceholderScreen(
            title = "Ubah Kata Sandi",
            onBackClick = { navController.popBackStack() }
        )
    }
}


/**
 * Placeholder composable for screens that are not yet implemented.
 * Used for route registration only.
 */
@Composable
private fun PlaceholderScreen(
    title: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title\n(Route registered but screen is not yet implemented)",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF6B6B6B),
            modifier = Modifier.padding(16.dp)
        )
    }
}
