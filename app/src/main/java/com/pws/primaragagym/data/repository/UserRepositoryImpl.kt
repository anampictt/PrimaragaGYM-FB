package com.pws.primaragagym.data.repository

import com.pws.primaragagym.data.datasource.FirebaseUserDataSource
import com.pws.primaragagym.domain.model.User
import com.pws.primaragagym.domain.repository.UserRepository

class UserRepositoryImpl : UserRepository {

    private val userDataSource = FirebaseUserDataSource()

    override suspend fun getUserProfile(uid: String): Result<User> {
        return userDataSource.getUserProfile(uid)
    }

    override suspend fun updateLastLogin(uid: String): Result<Unit> {
        return userDataSource.updateLastLogin(uid)
    }
}
