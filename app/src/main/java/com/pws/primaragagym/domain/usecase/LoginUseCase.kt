package com.pws.primaragagym.domain.usecase

import com.pws.primaragagym.domain.model.User
import com.pws.primaragagym.domain.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank()) {
            return Result.failure(Exception("Email wajib diisi."))
        }
        if (password.isBlank()) {
            return Result.failure(Exception("Password wajib diisi."))
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Format email tidak valid."))
        }
        return authRepository.login(email, password)
    }
}
