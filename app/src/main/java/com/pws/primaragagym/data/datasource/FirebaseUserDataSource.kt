package com.pws.primaragagym.data.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.pws.primaragagym.domain.model.User
import com.pws.primaragagym.domain.model.UserRole
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class FirebaseUserDataSource {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun getUserProfile(uid: String): Result<User> {
        return try {
            val doc = withTimeoutOrNull(5000L) {
                usersCollection.document(uid).get().await()
            }
            if (doc != null && doc.exists()) {
                val data = doc.data ?: emptyMap<String, Any>()
                val email = (data["email"] as? String) ?: ""
                val name = (data["name"] as? String)
                    ?: (data["fullName"] as? String)
                    ?: (data["displayName"] as? String)
                    ?: "User"
                val roleStr = (data["role"] as? String)
                    ?: (data["roleId"] as? String)
                    ?: "ADMIN"
                val branchId = data["branchId"] as? String
                val photoUrl = data["photoUrl"] as? String
                val address = (data["address"] as? String) ?: ""
                val phone = (data["phone"] as? String)
                    ?: (data["phoneNumber"] as? String)
                    ?: ""
                val lastLogin = (data["lastLogin"] ?: data["lastLoginAt"])?.toString() ?: ""

                Result.success(
                    User(
                        id = uid,
                        email = email,
                        name = name,
                        role = UserRole.fromString(roleStr),
                        branchId = branchId,
                        photoUrl = photoUrl,
                        address = address,
                        phone = phone,
                        lastLogin = lastLogin
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
            withTimeoutOrNull(3000L) {
                usersCollection.document(uid).set(updates, SetOptions.merge()).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUserProfileIfNotExists(
        uid: String,
        email: String,
        name: String,
        role: UserRole = UserRole.ADMIN,
        branchId: String? = null
    ): Result<Unit> {
        return try {
            val docRef = usersCollection.document(uid)
            val doc = docRef.get().await()
            if (!doc.exists()) {
                val userData = mapOf(
                    "uid" to uid,
                    "email" to email,
                    "name" to name,
                    "fullName" to name,
                    "role" to role.name,
                    "roleId" to role.name,
                    "branchId" to branchId,
                    "isActive" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(userData).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
