package com.pws.primaragagym.domain.usecase

import com.pws.primaragagym.domain.repository.AuthRepository

class ChangePasswordUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> {
        if (currentPassword.isBlank()) {
            return Result.failure(Exception("Kata sandi saat ini wajib diisi."))
        }
        if (newPassword.isBlank()) {
            return Result.failure(Exception("Kata sandi baru wajib diisi."))
        }
        if (newPassword.length < 6) {
            return Result.failure(Exception("Kata sandi baru minimal harus 6 karakter."))
        }
        if (newPassword == currentPassword) {
            return Result.failure(Exception("Kata sandi baru tidak boleh sama dengan kata sandi lama."))
        }
        return authRepository.changePassword(currentPassword, newPassword)
    }
}
