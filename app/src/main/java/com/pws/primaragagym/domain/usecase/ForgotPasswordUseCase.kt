package com.pws.primaragagym.domain.usecase

import com.pws.primaragagym.domain.repository.AuthRepository

class ForgotPasswordUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> {
        if (email.isBlank()) {
            return Result.failure(Exception("Email wajib diisi."))
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Format email tidak valid."))
        }
        return authRepository.sendPasswordReset(email)
    }
}
