package com.pws.primaragagym.data.repository

import android.util.Log
import com.pws.primaragagym.data.datasource.FirebaseAuthDataSource
import com.pws.primaragagym.data.datasource.FirebaseUserDataSource
import com.pws.primaragagym.domain.model.User
import com.pws.primaragagym.domain.model.UserRole
import com.pws.primaragagym.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl : AuthRepository {

    private val authDataSource = FirebaseAuthDataSource()
    private val userDataSource = FirebaseUserDataSource()

    override val currentUserId: String?
        get() = authDataSource.currentUser?.uid

    override val authStateFlow: Flow<String?> = authDataSource.authStateFlow.map { firebaseUser ->
        firebaseUser?.uid
    }

    override suspend fun login(email: String, password: String): Result<User> {
        val loginResult = authDataSource.signInWithEmailAndPassword(email, password)
        if (loginResult.isFailure) {
            return Result.failure(loginResult.exceptionOrNull() ?: Exception("Login gagal."))
        }

        val firebaseUser = loginResult.getOrNull()!!
        val uid = firebaseUser.uid
        Log.d("AuthRepositoryImpl", "User logged in with UID: $uid")

        // Try to get user profile from Firestore
        val profileResult = userDataSource.getUserProfile(uid)

        val user = if (profileResult.isSuccess) {
            profileResult.getOrNull()!!
        } else {
            // If no Firestore profile, create basic user from Firebase Auth
            User(
                id = uid,
                email = firebaseUser.email ?: email,
                name = firebaseUser.displayName ?: email.substringBefore("@"),
                role = UserRole.ADMIN // Default role, will be updated in Firestore
            )
        }

        // Update last login
        userDataSource.updateLastLogin(uid)

        return Result.success(user)
    }

    override suspend fun logout() {
        authDataSource.signOut()
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return authDataSource.sendPasswordResetEmail(email)
    }

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = authDataSource.currentUser ?: return null
        val uid = firebaseUser.uid

        return try {
            val profileResult = userDataSource.getUserProfile(uid)
            if (profileResult.isSuccess) {
                profileResult.getOrNull()
            } else {
                User(
                    id = uid,
                    email = firebaseUser.email ?: "",
                    name = firebaseUser.displayName ?: "User",
                    role = UserRole.ADMIN
                )
            }
        } catch (e: Exception) {
            User(
                id = uid,
                email = firebaseUser.email ?: "",
                name = firebaseUser.displayName ?: "User",
                role = UserRole.ADMIN
            )
        }
    }
}
