package com.pws.primaragagym.data.datasource

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pws.primaragagym.domain.model.FirestoreUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseUserAdminDataSource {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection(FirestoreCollections.USERS)

    private fun documentToFirestoreUser(doc: com.google.firebase.firestore.DocumentSnapshot): FirestoreUser {
        val data = doc.data ?: emptyMap<String, Any?>()
        val fullName = (data["fullName"] as? String)?.ifBlank { null }
            ?: (data["name"] as? String)?.ifBlank { null }
            ?: ""
        val email = (data["email"] as? String) ?: ""
        val address = (data["address"] as? String) ?: ""
        val role = (data["role"] as? String)?.ifBlank { null }
            ?: (data["roleId"] as? String)?.ifBlank { null }
            ?: ""
        val photoUrl = data["photoUrl"] as? String
        val isActive = (data["isActive"] as? Boolean) ?: true

        val createdAt = when (val c = data["createdAt"]) {
            is Timestamp -> c.toDate()
            is Date -> c
            else -> null
        }
        val updatedAt = when (val u = data["updatedAt"]) {
            is Timestamp -> u.toDate()
            is Date -> u
            else -> null
        }

        return FirestoreUser(
            uid = doc.id,
            email = email,
            fullName = fullName,
            name = fullName,
            address = address,
            photoUrl = photoUrl,
            roleId = role,
            role = role,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun observeUsers(): Flow<List<FirestoreUser>> = callbackFlow {
        val listener = usersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val users = snapshot.documents.map { doc ->
                    documentToFirestoreUser(doc)
                }.sortedBy { it.displayName }
                trySend(users)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun getUsers(
        roleId: String? = null,
        branchId: String? = null,
        isActive: Boolean? = null,
        limit: Int = 100,
        lastDocumentId: String? = null
    ): Result<List<FirestoreUser>> {
        return try {
            val snapshot = usersCollection.get().await()
            var users = snapshot.documents.map { doc ->
                documentToFirestoreUser(doc)
            }

            if (roleId != null) {
                users = users.filter { it.role.equals(roleId, ignoreCase = true) || it.roleId.equals(roleId, ignoreCase = true) }
            }

            if (isActive != null) {
                users = users.filter { it.isActive == isActive }
            }

            users = users.sortedBy { it.displayName }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat data pengguna. ${e.message}"))
        }
    }

    suspend fun getUserById(userId: String): Result<FirestoreUser> {
        return try {
            val doc = usersCollection.document(userId).get().await()
            if (doc.exists()) {
                Result.success(documentToFirestoreUser(doc))
            } else {
                Result.failure(Exception("Pengguna tidak ditemukan."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat detail pengguna. ${e.message}"))
        }
    }

    suspend fun createUser(user: FirestoreUser): Result<String> {
        return try {
            val docRef = if (user.uid.isNotBlank()) usersCollection.document(user.uid) else usersCollection.document()
            val fullName = user.fullName.ifBlank { user.name }
            val role = user.role.ifBlank { user.roleId }

            // Field koleksi disesuaikan murni dengan form Tambah Pengguna untuk pengguna aplikasi manajemen gym
            val userData = hashMapOf<String, Any?>(
                "uid" to docRef.id,
                "fullName" to fullName,
                "name" to fullName,
                "email" to user.email,
                "address" to user.address,
                "role" to role,
                "roleId" to role,
                "photoUrl" to user.photoUrl,
                "isActive" to user.isActive,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(userData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal membuat pengguna baru. ${e.message}"))
        }
    }

    suspend fun updateUser(user: FirestoreUser): Result<Unit> {
        return try {
            val fullName = user.fullName.ifBlank { user.name }
            val role = user.role.ifBlank { user.roleId }

            // Field koleksi disesuaikan murni dengan form Tambah Pengguna untuk pengguna aplikasi manajemen gym
            val updates = hashMapOf<String, Any?>(
                "fullName" to fullName,
                "name" to fullName,
                "email" to user.email,
                "address" to user.address,
                "role" to role,
                "roleId" to role,
                "photoUrl" to user.photoUrl,
                "isActive" to user.isActive,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            usersCollection.document(user.uid).set(updates, com.google.firebase.firestore.SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memperbarui pengguna. ${e.message}"))
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghapus pengguna. ${e.message}"))
        }
    }

    suspend fun deactivateUser(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId)
                .update(mapOf(
                    "isActive" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menonaktifkan pengguna. ${e.message}"))
        }
    }

    suspend fun getUsersCount(branchId: String? = null): Result<Int> {
        return try {
            val query: Query = usersCollection.whereEqualTo("isActive", true)
            val snapshot = query.get().await()
            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghitung pengguna. ${e.message}"))
        }
    }

    suspend fun searchUsers(query: String, limit: Int = 25): Result<List<FirestoreUser>> {
        return try {
            val snapshot = usersCollection
                .limit(limit.toLong())
                .get()
                .await()

            val users = snapshot.documents.map { doc ->
                documentToFirestoreUser(doc)
            }.filter { user ->
                user.displayName.contains(query, ignoreCase = true) ||
                user.email.contains(query, ignoreCase = true) ||
                user.resolvedRole.contains(query, ignoreCase = true) ||
                user.address.contains(query, ignoreCase = true)
            }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal mencari pengguna. ${e.message}"))
        }
    }
}
