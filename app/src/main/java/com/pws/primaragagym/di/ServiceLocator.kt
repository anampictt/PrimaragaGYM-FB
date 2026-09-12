package com.pws.primaragagym.di

import com.pws.primaragagym.data.datasource.*
import com.pws.primaragagym.data.repository.*
import com.pws.primaragagym.domain.repository.*
import com.pws.primaragagym.domain.usecase.*
import com.pws.primaragagym.ui.viewmodel.*

/**
 * Extended Service Locator for Dependency Injection.
 * Provides instances for Firestore repositories and ViewModels.
 */
object ServiceLocator {

    // ==================== DATA SOURCES ====================
    private val firebaseAuthDataSource: FirebaseAuthDataSource by lazy { FirebaseAuthDataSource() }
    private val firebaseUserDataSource: FirebaseUserDataSource by lazy { FirebaseUserDataSource() }
    private val firebaseMemberDataSource: FirebaseMemberDataSource by lazy { FirebaseMemberDataSource() }
    private val firebaseMembershipPlanDataSource: FirebaseMembershipPlanDataSource by lazy { FirebaseMembershipPlanDataSource() }
    private val firebaseMembershipDataSource: FirebaseMembershipDataSource by lazy { FirebaseMembershipDataSource() }
    private val firebaseCheckinDataSource: FirebaseCheckinDataSource by lazy { FirebaseCheckinDataSource() }
    private val firebasePaymentDataSource: FirebasePaymentDataSource by lazy { FirebasePaymentDataSource() }
    private val firebaseBranchDataSource: FirebaseBranchDataSource by lazy { FirebaseBranchDataSource() }
    private val firebaseRoleDataSource: FirebaseRoleDataSource by lazy { FirebaseRoleDataSource() }
    private val firebaseUserAdminDataSource: FirebaseUserAdminDataSource by lazy { FirebaseUserAdminDataSource() }
    private val firebaseNotificationDataSource: FirebaseNotificationDataSource by lazy { FirebaseNotificationDataSource() }
    private val firebaseReportDataSource: FirebaseReportDataSource by lazy { FirebaseReportDataSource() }

    // ==================== REPOSITORIES ====================
    val authRepository: AuthRepository by lazy { AuthRepositoryImpl() }
    private val userRepository: UserRepository by lazy { UserRepositoryImpl() }

    // Firestore Repositories
    val memberRepository: MemberRepository by lazy { MemberRepositoryImpl() }
    val membershipPlanRepository: MembershipPlanRepository by lazy { MembershipPlanRepositoryImpl() }
    val membershipRepository: MembershipRepository by lazy { MembershipRepositoryImpl() }
    val checkinRepository: CheckinRepository by lazy { CheckinRepositoryImpl() }
    val paymentRepository: PaymentRepository by lazy { PaymentRepositoryImpl() }
    val branchRepository: BranchRepository by lazy { BranchRepositoryImpl() }
    val roleRepository: RoleRepository by lazy { RoleRepositoryImpl() }
    val userAdminRepository: UserAdminRepository by lazy { UserAdminRepositoryImpl() }
    val notificationRepository: NotificationRepository by lazy { NotificationRepositoryImpl() }
    val reportRepository: ReportRepository by lazy { ReportRepositoryImpl() }

    // ==================== AUTH USE CASES ====================
    val loginUseCase: LoginUseCase by lazy { LoginUseCase(authRepository) }
    val logoutUseCase: LogoutUseCase by lazy { LogoutUseCase(authRepository) }
    val getCurrentUserUseCase: GetCurrentUserUseCase by lazy { GetCurrentUserUseCase(authRepository) }
    val forgotPasswordUseCase: ForgotPasswordUseCase by lazy { ForgotPasswordUseCase(authRepository) }

    // ==================== VIEWMODELS ====================
    fun provideAuthViewModel(): AuthViewModel = AuthViewModel(getCurrentUserUseCase)
    fun provideLoginViewModel(): LoginViewModel = LoginViewModel(loginUseCase)
    fun provideForgotPasswordViewModel(): ForgotPasswordViewModel = ForgotPasswordViewModel(forgotPasswordUseCase)
    fun provideProfileViewModel(): ProfileViewModel = ProfileViewModel(getCurrentUserUseCase, logoutUseCase)

    fun provideAdminDashboardViewModel(): AdminDashboardViewModel = AdminDashboardViewModel()
    fun provideMemberListViewModel(): MemberListViewModel = MemberListViewModel()
    fun providePlanListViewModel(): PlanListViewModel = PlanListViewModel()
    fun provideCheckinFirestoreViewModel(): CheckinFirestoreViewModel = CheckinFirestoreViewModel()
    fun provideSuperAdminDashboardViewModel(): SuperAdminDashboardViewModel = SuperAdminDashboardViewModel()
    fun provideBranchListViewModel(): BranchListViewModel = BranchListViewModel()
    fun provideRoleListViewModel(): RoleListViewModel = RoleListViewModel()
    fun provideUserListViewModel(): UserListViewModel = UserListViewModel()
}
