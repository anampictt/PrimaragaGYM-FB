package com.pws.primaragagym.navigation

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pws.primaragagym.AuthActivity
import com.pws.primaragagym.navigation.AppScreen
import com.pws.primaragagym.screens.ProfileScreen
import com.pws.primaragagym.screens.SplashScreen
import com.pws.primaragagym.navigation.LoginNavHost
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
import com.pws.primaragagym.screens.admin.member.card.MemberCardData
import com.pws.primaragagym.screens.admin.member.card.MemberCardPreviewScreen

import com.pws.primaragagym.screens.admin.keuangan.KeuanganScreen
import com.pws.primaragagym.screens.admin.keuangan.CatatPembayaranScreen
import com.pws.primaragagym.screens.admin.keuangan.InvoiceScreen
import com.pws.primaragagym.screens.admin.keuangan.InvoiceDetailScreen
import com.pws.primaragagym.screens.admin.keuangan.LaporanPemasukanScreen
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiScreen
import com.pws.primaragagym.screens.admin.notifikasi.NotificationDetailScreen
import com.pws.primaragagym.screens.admin.notifikasi.NotificationSettingsScreen

import com.pws.primaragagym.screens.superadmin.manajemenpengguna.ManajemenPenggunaScreen
import com.pws.primaragagym.screens.superadmin.manajemenpengguna.TambahPenggunaScreen
import com.pws.primaragagym.screens.superadmin.manajemenrole.ManajemenRoleScreen
import com.pws.primaragagym.screens.superadmin.manajemenrole.TambahRoleScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pws.primaragagym.ui.viewmodel.MemberListViewModel
import com.pws.primaragagym.ui.viewmodel.AuthViewModel
import com.pws.primaragagym.ui.viewmodel.ProfileViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel = AuthViewModel(),
    startDestination: AppScreen = AppScreen.Splash,
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier
    ) {
        // ==================== SPLASH ====================
        composable(AppScreen.Splash.route) {
            val context = LocalContext.current
            SplashScreen(
                authViewModel = authViewModel,
                onSplashComplete = {
                    if (authState.isAuthenticated && authState.currentUser != null) {
                        val destination = AppScreen.SuperAdminRoot.route
                        navController.navigate(destination) {
                            popUpTo(AppScreen.Splash.route) { inclusive = true }
                        }
                    } else {
                        val intent = Intent(context, AuthActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    }
                }
            )
        }

        // ==================== AUTH ====================
        composable(AppScreen.Login.route) {
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                val intent = Intent(context, AuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
                (context as? Activity)?.finish()
            }
        }

        // ==================== SUPER ADMIN ====================
        composable(AppScreen.SuperAdminRoot.route) {
            com.pws.primaragagym.screens.superadmin.dashboard.SuperAdminMainScreen(
                rootNavController = navController,
                authViewModel = authViewModel
            )
        }

        // ==================== ADMIN (Unified Dashboard) ====================
        composable(AppScreen.AdminDashboard.route) {
            com.pws.primaragagym.screens.superadmin.dashboard.SuperAdminMainScreen(
                rootNavController = navController,
                authViewModel = authViewModel
            )
        }
        sharedAdminRoutes(navController, authViewModel, navController)
    }
}

fun androidx.navigation.NavGraphBuilder.sharedAdminRoutes(
    navController: androidx.navigation.NavHostController,
    authViewModel: AuthViewModel = AuthViewModel(),
    rootNavController: androidx.navigation.NavHostController? = null
) {
    // ==================== MEMBER HUB ====================
    composable(AppScreen.Member.route) {
        MemberScreen(
            authViewModel = authViewModel,
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
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
            onChatTemplateClick = {
                navController.navigate(AppScreen.ChatTemplate.route)
            },
            onMemberClick = { memberId ->
                navController.navigate(AppScreen.MembershipDetail.createRoute(memberId))
            }
        )
    }

    composable(AppScreen.MemberManagement.route) {
        MemberManagementScreen(
            onBackClick = {
                navController.popBackStack()
            },
            onAddMemberClick = {
                navController.navigate(AppScreen.RegistrasiMember.createRoute())
            },
            onMemberClick = { memberId ->
                navController.navigate(AppScreen.MembershipDetail.createRoute(memberId))
            },
            onEditMemberClick = { memberId ->
                navController.navigate(AppScreen.RegistrasiMember.createRoute(memberId))
            },
            onPreviewCardClick = { memberId ->
                navController.navigate(AppScreen.MemberCardPreview.createRoute(memberId))
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
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
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
        route = AppScreen.RegistrasiMember.route,
        arguments = listOf(
            navArgument("memberId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val memberId = backStackEntry.arguments?.getString("memberId")
        RegistrasiMemberScreen(
            memberId = memberId,
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onSubmitSuccess = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onPreviewCardClick = { newMemberId ->
                navController.navigate(AppScreen.MemberCardPreview.createRoute(newMemberId))
            }
        )
    }

    // ==================== MEMBERSHIP MANAGEMENT ====================
    composable(AppScreen.MembershipManagement.route) {
        MembershipManagementScreen(
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
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
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onPerpanjangClick = {
                navController.navigate(AppScreen.PerpanjangMembership.createRoute(memberId))
            },
            onUpgradeClick = {
                navController.navigate(AppScreen.UpgradeDowngrade.createRoute(memberId))
            },
            onRiwayatClick = {
                navController.navigate(AppScreen.RiwayatTransaksi.createRoute(memberId))
            },
            onKartuMemberClick = { id ->
                navController.navigate(AppScreen.MemberCardPreview.createRoute(id))
            }
        )
    }

    composable(
        route = AppScreen.MemberCardPreview.route,
        arguments = listOf(navArgument("memberId") { type = NavType.StringType })
    ) { backStackEntry ->
        val memberId = backStackEntry.arguments?.getString("memberId") ?: ""
        MemberCardPreviewScreen(
            memberId = memberId,
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
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
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onSubmitSuccess = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
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
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onSubmitSuccess = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
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
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            }
        )
    }

    // ==================== MEMBERSHIP PLAN ====================
    composable(AppScreen.MembershipPlan.route) {
        MembershipPlanScreen(
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
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
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onSubmitSuccess = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
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
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onSubmitSuccess = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            }
        )
    }

    // ==================== CHAT TEMPLATE ====================
    composable(AppScreen.ChatTemplate.route) {
        com.pws.primaragagym.screens.admin.template.ChatTemplateScreen(
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            }
        )
    }

    // ==================== CHECK IN / OUT ====================
    composable(AppScreen.CheckInCheckout.route) {
        com.pws.primaragagym.screens.admin.checkin.CheckinCheckoutScreen(
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onNavigateToScanner = {
                navController.navigate(AppScreen.CheckInScanner.route)
            },
            onNavigateToDetail = { memberId ->
                navController.navigate(AppScreen.CheckInMemberDetail.createRoute(memberId))
            },
            onNavigateToHistory = {
                navController.navigate(AppScreen.RiwayatCheckinCheckout.route)
            }
        )
    }

    composable(AppScreen.CheckInScanner.route) {
        com.pws.primaragagym.screens.admin.checkin.CheckinScannerScreen(
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
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
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            }
        )
    }

    composable(AppScreen.RiwayatCheckinCheckout.route) {
        com.pws.primaragagym.screens.admin.checkin.RiwayatCheckinCheckoutScreen(
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onMemberClick = { memberId ->
                navController.navigate(AppScreen.CheckInMemberDetail.createRoute(memberId))
            }
        )
    }

    // ==================== KEUANGAN ====================
    composable(AppScreen.Keuangan.route) {
        KeuanganScreen(
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onCatatPembayaranClick = {
                navController.navigate(AppScreen.CatatPembayaran.route)
            },
            onInvoiceClick = {
                navController.navigate(AppScreen.Invoice.route)
            },
            onLaporanClick = {
                navController.navigate(AppScreen.LaporanPemasukan.route)
            }
        )
    }

    composable(AppScreen.CatatPembayaran.route) {
        CatatPembayaranScreen(
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onSuccess = {
                navController.navigate(AppScreen.Keuangan.route) {
                    popUpTo(AppScreen.Keuangan.route) { inclusive = true }
                }
            },
            onViewInvoice = {
                navController.navigate(AppScreen.InvoiceDetail.createRoute("INV-20260906-001"))
            }
        )
    }

    composable(AppScreen.Invoice.route) {
        InvoiceScreen(
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onInvoiceClick = { invoiceId ->
                navController.navigate(AppScreen.InvoiceDetail.createRoute(invoiceId))
            }
        )
    }

    composable(
        route = AppScreen.InvoiceDetail.route,
        arguments = listOf(navArgument("invoiceId") { type = NavType.StringType })
    ) { backStackEntry ->
        val invoiceId = backStackEntry.arguments?.getString("invoiceId") ?: ""
        InvoiceDetailScreen(
            invoiceId = invoiceId,
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            }
        )
    }

    composable(AppScreen.LaporanPemasukan.route) {
        LaporanPemasukanScreen(
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            }
        )
    }

    // ==================== NOTIFIKASI ====================
    composable(AppScreen.Notifikasi.route) {
        NotifikasiScreen(
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onNotificationClick = { notificationId ->
                navController.navigate(AppScreen.NotificationDetail.createRoute(notificationId))
            },
            onSettingsClick = {
                navController.navigate(AppScreen.NotificationSettings.route)
            }
        )
    }

    composable(
        route = AppScreen.NotificationDetail.route,
        arguments = listOf(navArgument("notificationId") { type = NavType.StringType })
    ) { backStackEntry ->
        val notificationId = backStackEntry.arguments?.getString("notificationId") ?: ""
        NotificationDetailScreen(
            notificationId = notificationId,
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onViewMemberClick = { navController.popBackStack() }
        )
    }

    composable(AppScreen.NotificationSettings.route) {
        NotificationSettingsScreen(
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            }
        )
    }

    // ==================== PLACEHOLDER ROUTES ====================
    composable(AppScreen.CatatanKeuangan.route) {
        PlaceholderScreen(
            title = "Catatan Keuangan",
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            }
        )
    }

    composable(AppScreen.LaporanKeuangan.route) {
        PlaceholderScreen(
            title = "Laporan Keuangan",
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            }
        )
    }

    // ==================== PROFILE ====================
    composable(AppScreen.Profil.route) {
        val context = LocalContext.current
        val profileViewModel = ProfileViewModel()
        ProfileScreen(
            viewModel = profileViewModel,
            onBackClick = {
                navController.popBackStack()
            },
            onChangePasswordClick = {
                navController.navigate(AppScreen.UbahKataSandi.route)
            },
            onLogoutSuccess = {
                profileViewModel.hideLogoutSuccess()
                authViewModel.clearUser()
                val intent = Intent(context, AuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
                (context as? Activity)?.finish()
            }
        )
    }

    composable(AppScreen.UbahKataSandi.route) {
        PlaceholderScreen(
            title = "Ubah Kata Sandi",
            onBackClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            }
        )
    }
}

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
