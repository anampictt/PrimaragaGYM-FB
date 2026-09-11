package com.pws.primaragagym.domain.repository

import com.pws.primaragagym.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: String?
    val authStateFlow: Flow<String?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout()
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun getCurrentUser(): User?
}
