package com.pws.primaragagym.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.pws.primaragagym.R

/**
 * Sealed class representing all app screens/routes.
 * Provides type-safe navigation with resource references.
 */
sealed class AppScreen(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val route: String
) {
    // ==================== GENERAL ====================
    data object Splash : AppScreen(
        title = R.string.app_name,
        icon = R.drawable.ic_launcher_background,
        route = "splash"
    )

    data object Login : AppScreen(
        title = R.string.app_name,
        icon = R.drawable.ic_launcher_background,
        route = "login"
    )

    data object ForgotPassword : AppScreen(
        title = R.string.app_name,
        icon = R.drawable.ic_launcher_background,
        route = "forgot_password"
    )

    // ==================== SUPER ADMIN ====================
    data object SuperAdminDashboard : AppScreen(
        title = R.string.dashboard,
        icon = R.drawable.ic_launcher_background,
        route = "superadmin/dashboard"
    )

    data object ManajemenPengguna : AppScreen(
        title = R.string.manajemen_pengguna,
        icon = R.drawable.ic_launcher_background,
        route = "superadmin/manajemen-pengguna"
    )

    data object ManajemenRole : AppScreen(
        title = R.string.manajemen_role,
        icon = R.drawable.ic_launcher_background,
        route = "superadmin/manajemen-role"
    )

    data object ManajemenCabang : AppScreen(
        title = R.string.manajemen_cabang,
        icon = R.drawable.ic_launcher_background,
        route = "superadmin/manajemen-cabang"
    )

    // Aliases for SuperAdminDashboard navigation callbacks
    data object ManananakanPengguna : AppScreen(
        title = R.string.manajemen_pengguna,
        icon = R.drawable.ic_launcher_background,
        route = "superadmin/manajemen-pengguna"
    )

    data object ManananakanRole : AppScreen(
        title = R.string.manajemen_role,
        icon = R.drawable.ic_launcher_background,
        route = "superadmin/manajemen-role"
    )

    data object ManananakanCabang : AppScreen(
        title = R.string.manajemen_cabang,
        icon = R.drawable.ic_launcher_background,
        route = "superadmin/manajemen-cabang"
    )

    data object TambahPengguna : AppScreen(
        title = R.string.tambah_pengguna,
        icon = R.drawable.ic_launcher_background,
        route = "superadmin/manajemen-pengguna/tambah"
    )

    data object TambahRole : AppScreen(
        title = R.string.tambah_role,
        icon = R.drawable.ic_launcher_background,
        route = "superadmin/manajemen-role/tambah"
    )

    data object TambahCabang : AppScreen(
        title = R.string.tambah_cabang,
        icon = R.drawable.ic_launcher_background,
        route = "superadmin/manajemen-cabang/tambah"
    )

    // ==================== ADMIN ====================
    data object AdminDashboard : AppScreen(
        title = R.string.dashboard,
        icon = R.drawable.ic_launcher_background,
        route = "admin/dashboard"
    )

    data object Member : AppScreen(
        title = R.string.member_hub,
        icon = R.drawable.ic_launcher_background,
        route = "admin/member"
    )

    // Member Management
    data object MemberManagement : AppScreen(
        title = R.string.member_management,
        icon = R.drawable.ic_launcher_background,
        route = "admin/member-management"
    )

    data object DetailMember : AppScreen(
        title = R.string.detail_member,
        icon = R.drawable.ic_launcher_background,
        route = "admin/member-management/detail/{memberId}"
    ) {
        fun createRoute(memberId: String) = "admin/member-management/detail/$memberId"
    }

    data object RegistrasiMember : AppScreen(
        title = R.string.registrasi_member,
        icon = R.drawable.ic_launcher_background,
        route = "admin/member-management/register"
    )

    // Membership Management
    data object MembershipManagement : AppScreen(
        title = R.string.membership_management,
        icon = R.drawable.ic_launcher_background,
        route = "admin/membership-management"
    )

    data object MembershipDetail : AppScreen(
        title = R.string.membership_detail,
        icon = R.drawable.ic_launcher_background,
        route = "admin/membership-management/detail/{memberId}"
    ) {
        fun createRoute(memberId: String) = "admin/membership-management/detail/$memberId"
    }

    data object PerpanjangMembership : AppScreen(
        title = R.string.perpanjang_membership,
        icon = R.drawable.ic_launcher_background,
        route = "admin/membership-management/extend/{memberId}"
    ) {
        fun createRoute(memberId: String) = "admin/membership-management/extend/$memberId"
    }

    data object UpgradeDowngrade : AppScreen(
        title = R.string.upgrade_downgrade,
        icon = R.drawable.ic_launcher_background,
        route = "admin/membership-management/change/{memberId}"
    ) {
        fun createRoute(memberId: String) = "admin/membership-management/change/$memberId"
    }

    data object RiwayatTransaksi : AppScreen(
        title = R.string.riwayat_transaksi,
        icon = R.drawable.ic_launcher_background,
        route = "admin/membership-management/transactions/{memberId}"
    ) {
        fun createRoute(memberId: String) = "admin/membership-management/transactions/$memberId"
    }

    // Membership Plan
    data object MembershipPlan : AppScreen(
        title = R.string.membership_plan,
        icon = R.drawable.ic_launcher_background,
        route = "admin/membership-plan"
    )

    data object TambahMembershipPlan : AppScreen(
        title = R.string.tambah_paket,
        icon = R.drawable.ic_launcher_background,
        route = "admin/membership-plan/add"
    )

    data object EditMembershipPlan : AppScreen(
        title = R.string.edit_paket,
        icon = R.drawable.ic_launcher_background,
        route = "admin/membership-plan/edit/{planId}"
    ) {
        fun createRoute(planId: String) = "admin/membership-plan/edit/$planId"
    }

    data object CheckInCheckout : AppScreen(
        title = R.string.check_in_checkout,
        icon = R.drawable.ic_launcher_background,
        route = "admin/checkin-checkout"
    )

    data object CatatanKeuangan : AppScreen(
        title = R.string.catatan_keuangan,
        icon = R.drawable.ic_launcher_background,
        route = "admin/catatan-keuangan"
    )

    data object Notifikasi : AppScreen(
        title = R.string.notifikasi,
        icon = R.drawable.ic_launcher_background,
        route = "admin/notifikasi"
    )

    data object LaporanKeuangan : AppScreen(
        title = R.string.laporan_keuangan,
        icon = R.drawable.ic_launcher_background,
        route = "admin/laporan-keuangan"
    )

    // ==================== PROFILE ====================
    data object Profil : AppScreen(
        title = R.string.profil,
        icon = R.drawable.ic_launcher_background,
        route = "profil"
    )

    data object UbahKataSandi : AppScreen(
        title = R.string.ubah_kata_sandi,
        icon = R.drawable.ic_launcher_background,
        route = "ubah-kata-sandi"
    )
}
