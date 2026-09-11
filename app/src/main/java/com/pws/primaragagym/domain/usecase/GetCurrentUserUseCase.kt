package com.pws.primaragagym.domain.usecase

import com.pws.primaragagym.domain.model.User
import com.pws.primaragagym.domain.repository.AuthRepository

class GetCurrentUserUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}
