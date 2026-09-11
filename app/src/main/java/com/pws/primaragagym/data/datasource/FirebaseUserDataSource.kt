package com.pws.primaragagym.data.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.pws.primaragagym.domain.model.User
import com.pws.primaragagym.domain.model.UserRole
import kotlinx.coroutines.tasks.await

class FirebaseUserDataSource {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun getUserProfile(uid: String): Result<User> {
        return try {
            val doc = usersCollection.document(uid).get().await()
            if (doc.exists()) {
                val data = doc.data ?: return Result.failure(Exception("Data user tidak ditemukan."))
                Result.success(
                    User(
                        id = uid,
                        email = data["email"] as? String ?: "",
                        name = data["name"] as? String ?: data["displayName"] as? String ?: "User",
                        role = UserRole.fromString(data["role"] as? String ?: "ADMIN"),
                        photoUrl = data["photoUrl"] as? String,
                        address = data["address"] as? String ?: "",
                        phone = data["phone"] as? String ?: "",
                        lastLogin = data["lastLogin"] as? String ?: ""
                    )
                )
            } else {
                Result.failure(Exception("Profil user tidak ditemukan."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateLastLogin(uid: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "lastLogin" to FieldValue.serverTimestamp()
            )
            usersCollection.document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUserProfileIfNotExists(
        uid: String,
        email: String,
        name: String,
        role: UserRole = UserRole.ADMIN
    ): Result<Unit> {
        return try {
            val docRef = usersCollection.document(uid)
            val doc = docRef.get().await()
            if (!doc.exists()) {
                val userData = mapOf(
                    "email" to email,
                    "name" to name,
                    "role" to role.name,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                docRef.set(userData).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
