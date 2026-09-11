package com.pws.primaragagym.di

import com.pws.primaragagym.data.datasource.FirebaseAuthDataSource
import com.pws.primaragagym.data.datasource.FirebaseUserDataSource
import com.pws.primaragagym.data.repository.AuthRepositoryImpl
import com.pws.primaragagym.data.repository.UserRepositoryImpl
import com.pws.primaragagym.domain.repository.AuthRepository
import com.pws.primaragagym.domain.repository.UserRepository
import com.pws.primaragagym.domain.usecase.ForgotPasswordUseCase
import com.pws.primaragagym.domain.usecase.GetCurrentUserUseCase
import com.pws.primaragagym.domain.usecase.LoginUseCase
import com.pws.primaragagym.domain.usecase.LogoutUseCase

/**
 * Simple Service Locator for Dependency Injection.
 * Replace with Hilt/Dagger module when needed.
 */
object ServiceLocator {

    // Data Sources
    private val firebaseAuthDataSource: FirebaseAuthDataSource by lazy { FirebaseAuthDataSource() }
    private val firebaseUserDataSource: FirebaseUserDataSource by lazy { FirebaseUserDataSource() }

    // Repositories
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl()
    }
    private val userRepository: UserRepository by lazy {
        UserRepositoryImpl()
    }

    // Use Cases
    val loginUseCase: LoginUseCase by lazy { LoginUseCase(authRepository) }
    val logoutUseCase: LogoutUseCase by lazy { LogoutUseCase(authRepository) }
    val getCurrentUserUseCase: GetCurrentUserUseCase by lazy { GetCurrentUserUseCase(authRepository) }
    val forgotPasswordUseCase: ForgotPasswordUseCase by lazy { ForgotPasswordUseCase(authRepository) }
}
