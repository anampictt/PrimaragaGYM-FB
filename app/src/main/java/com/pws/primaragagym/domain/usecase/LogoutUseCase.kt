package com.pws.primaragagym.domain.usecase

import com.pws.primaragagym.domain.repository.AuthRepository

class LogoutUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
