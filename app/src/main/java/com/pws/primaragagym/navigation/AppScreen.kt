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
    data object SuperAdminRoot : AppScreen(
        title = R.string.dashboard,
        icon = R.drawable.ic_launcher_background,
        route = "superadmin/root"
    )

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

    data object TambahPengguna : AppScreen(
        title = R.string.tambah_pengguna,
        icon = R.drawable.ic_launcher_background,
        route = "superadmin/manajemen-pengguna/tambah"
    ) {
        fun createRoute(userId: String? = null): String =
            if (userId != null) "superadmin/manajemen-pengguna/tambah?userId=$userId" else "superadmin/manajemen-pengguna/tambah"
    }

    data object DetailPengguna : AppScreen(
        title = R.string.tambah_pengguna,
        icon = R.drawable.ic_launcher_background,
        route = "superadmin/manajemen-pengguna/detail?userId={userId}"
    ) {
        fun createRoute(userId: String): String =
            "superadmin/manajemen-pengguna/detail?userId=$userId"
    }

    data object TambahRole : AppScreen(
        title = R.string.tambah_role,
        icon = R.drawable.ic_launcher_background,
        route = "superadmin/manajemen-role/tambah"
    ) {
        fun createRoute(roleId: String? = null): String =
            if (roleId != null) "superadmin/manajemen-role/tambah?roleId=$roleId" else "superadmin/manajemen-role/tambah"
    }

    data object HakAksesRole : AppScreen(
        title = R.string.manajemen_role,
        icon = R.drawable.ic_launcher_background,
        route = "superadmin/manajemen-role/hak-akses?roleId={roleId}"
    ) {
        fun createRoute(roleId: String): String =
            "superadmin/manajemen-role/hak-akses?roleId=$roleId"
    }

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
        route = "admin/member-management/register?memberId={memberId}"
    ) {
        fun createRoute(memberId: String? = null) =
            if (memberId != null) "admin/member-management/register?memberId=$memberId" else "admin/member-management/register"
    }

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

    data object CheckInScanner : AppScreen(
        title = R.string.check_in_checkout,
        icon = R.drawable.ic_launcher_background,
        route = "admin/checkin-checkout/scanner"
    )

    data object CheckInMemberDetail : AppScreen(
        title = R.string.detail_member,
        icon = R.drawable.ic_launcher_background,
        route = "admin/checkin-checkout/member/{memberId}"
    ) {
        fun createRoute(memberId: String) = "admin/checkin-checkout/member/$memberId"
    }

    data object RiwayatCheckinCheckout : AppScreen(
        title = R.string.check_in_checkout,
        icon = R.drawable.ic_launcher_background,
        route = "admin/checkin-checkout/riwayat"
    )

    data object CatatanKeuangan : AppScreen(
        title = R.string.catatan_keuangan,
        icon = R.drawable.ic_launcher_background,
        route = "admin/catatan-keuangan"
    )

    data object Keuangan : AppScreen(
        title = R.string.keuangan,
        icon = R.drawable.ic_launcher_background,
        route = "admin/keuangan"
    )

    data object CatatPembayaran : AppScreen(
        title = R.string.catat_pembayaran,
        icon = R.drawable.ic_launcher_background,
        route = "admin/keuangan/catat-pembayaran"
    )

    data object Invoice : AppScreen(
        title = R.string.invoice,
        icon = R.drawable.ic_launcher_background,
        route = "admin/keuangan/invoice"
    )

    data object InvoiceDetail : AppScreen(
        title = R.string.invoice_detail,
        icon = R.drawable.ic_launcher_background,
        route = "admin/keuangan/invoice/{invoiceId}"
    ) {
        fun createRoute(invoiceId: String) = "admin/keuangan/invoice/$invoiceId"
    }

    data object LaporanPemasukan : AppScreen(
        title = R.string.laporan_pemasukan,
        icon = R.drawable.ic_launcher_background,
        route = "admin/keuangan/laporan"
    )

    data object Notifikasi : AppScreen(
        title = R.string.notifikasi,
        icon = R.drawable.ic_launcher_background,
        route = "admin/notifikasi"
    )

    data object NotificationDetail : AppScreen(
        title = R.string.notification_detail,
        icon = R.drawable.ic_launcher_background,
        route = "admin/notifikasi/{notificationId}"
    ) {
        fun createRoute(notificationId: String) = "admin/notifikasi/$notificationId"
    }

    data object NotificationSettings : AppScreen(
        title = R.string.notification_settings,
        icon = R.drawable.ic_launcher_background,
        route = "admin/notifikasi/settings"
    )

    data object MemberCardPreview : AppScreen(
        title = R.string.preview_kartu_member,
        icon = R.drawable.ic_launcher_background,
        route = "admin/membership-management/card-preview/{memberId}"
    ) {
        fun createRoute(memberId: String) = "admin/membership-management/card-preview/$memberId"
    }

    data object LaporanKeuangan : AppScreen(
        title = R.string.laporan_keuangan,
        icon = R.drawable.ic_launcher_background,
        route = "admin/laporan-keuangan"
    )

    // ==================== LAPORAN MODULE ====================
    data object Laporan : AppScreen(
        title = R.string.laporan,
        icon = R.drawable.ic_launcher_background,
        route = "admin/laporan"
    )

    data object LaporanMember : AppScreen(
        title = R.string.laporan_member,
        icon = R.drawable.ic_launcher_background,
        route = "admin/laporan/member"
    )

    data object LaporanKeuanganPeriodik : AppScreen(
        title = R.string.laporan_keuangan_periodik,
        icon = R.drawable.ic_launcher_background,
        route = "admin/laporan/keuangan"
    )

    data object ExportLaporan : AppScreen(
        title = R.string.export_laporan,
        icon = R.drawable.ic_launcher_background,
        route = "admin/laporan/export"
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
