package com.pws.primaragagym.domain.repository

import com.pws.primaragagym.domain.model.User

interface UserRepository {
    suspend fun getUserProfile(uid: String): Result<User>
    suspend fun updateLastLogin(uid: String): Result<Unit>
}
