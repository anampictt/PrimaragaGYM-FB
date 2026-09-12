package com.pws.primaragagym.data.datasource

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.pws.primaragagym.domain.model.FirestoreRole
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseRoleDataSource {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val rolesCollection = firestore.collection(FirestoreCollections.ROLES)

    private fun documentToFirestoreRole(doc: com.google.firebase.firestore.DocumentSnapshot): FirestoreRole {
        val data = doc.data ?: emptyMap<String, Any?>()
        val name = (data["name"] as? String) ?: ""
        val description = (data["description"] as? String) ?: ""
        val isActive = (data["isActive"] as? Boolean) ?: true
        val permissionsRaw = data["permissions"] as? Map<*, *> ?: emptyMap<Any, Any>()
        val permissions = permissionsRaw.entries.associate { (k, v) ->
            val boolVal = when (v) {
                is Boolean -> v
                is Number -> v.toInt() == 1
                is String -> v.equals("true", ignoreCase = true)
                else -> false
            }
            k.toString() to boolVal
        }
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
        return FirestoreRole(
            roleId = doc.id,
            name = name,
            description = description,
            permissions = permissions,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun observeRoles(): Flow<List<FirestoreRole>> = callbackFlow {
        val listener = rolesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val roles = snapshot.documents.map { doc ->
                    documentToFirestoreRole(doc)
                }.sortedBy { it.name }
                trySend(roles)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun getRoles(isActive: Boolean? = null): Result<List<FirestoreRole>> {
        return try {
            val snapshot = rolesCollection.get().await()
            var roles = snapshot.documents.map { doc ->
                documentToFirestoreRole(doc)
            }

            // Seed default roles if Firestore has no roles yet
            if (roles.isEmpty()) {
                seedDefaultRoles()
                val newSnapshot = rolesCollection.get().await()
                roles = newSnapshot.documents.map { doc ->
                    documentToFirestoreRole(doc)
                }
            }

            if (isActive != null) {
                roles = roles.filter { it.isActive == isActive }
            }

            roles = roles.sortedBy { it.name }
            Result.success(roles)
        } catch (e: Exception) {
            if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            Result.failure(Exception("Gagal memuat data role. ${e.message}"))
        }
    }

    private suspend fun seedDefaultRoles() {
        val superAdminPermissions = mapOf(
            "dashboard" to true,
            "manajemen_pengguna" to true,
            "manajemen_role" to true,
            "manajemen_cabang" to true,
            "manajemen_member" to true,
            "check_in_out" to true,
            "check_in" to true,
            "check_out" to true,
            "notifikasi" to true,
            "laporan" to true
        )
        val adminPermissions = mapOf(
            "dashboard" to true,
            "manajemen_pengguna" to false,
            "manajemen_role" to false,
            "manajemen_cabang" to false,
            "manajemen_member" to true,
            "check_in_out" to true,
            "check_in" to true,
            "check_out" to true,
            "notifikasi" to true,
            "laporan" to true
        )
        val staffPermissions = mapOf(
            "dashboard" to true,
            "manajemen_pengguna" to false,
            "manajemen_role" to false,
            "manajemen_cabang" to false,
            "manajemen_member" to true,
            "check_in_out" to true,
            "check_in" to true,
            "check_out" to true,
            "notifikasi" to true,
            "laporan" to false
        )

        val defaultRoles = listOf(
            FirestoreRole(
                name = "Super Admin",
                description = "Akses penuh ke seluruh sistem.",
                permissions = superAdminPermissions,
                isActive = true,
                createdAt = Date(),
                updatedAt = Date()
            ),
            FirestoreRole(
                name = "Admin",
                description = "Mengelola operasional gym dan data member.",
                permissions = adminPermissions,
                isActive = true,
                createdAt = Date(),
                updatedAt = Date()
            ),
            FirestoreRole(
                name = "Staff",
                description = "Mengelola aktivitas operasional sesuai hak akses.",
                permissions = staffPermissions,
                isActive = true,
                createdAt = Date(),
                updatedAt = Date()
            )
        )

        for (role in defaultRoles) {
            val docRef = rolesCollection.document()
            docRef.set(role.copy(roleId = docRef.id)).await()
        }
    }

    suspend fun getRoleById(roleId: String): Result<FirestoreRole> {
        return try {
            val doc = rolesCollection.document(roleId).get().await()
            if (doc.exists()) {
                Result.success(documentToFirestoreRole(doc))
            } else {
                Result.failure(Exception("Role tidak ditemukan."))
            }
        } catch (e: Exception) {
            if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            Result.failure(Exception("Gagal memuat detail role. ${e.message}"))
        }
    }

    suspend fun getRoleByName(roleName: String): Result<FirestoreRole> {
        return try {
            val allRolesResult = getRoles()
            if (allRolesResult.isSuccess) {
                val found = allRolesResult.getOrNull()?.find {
                    it.name.equals(roleName, ignoreCase = true) ||
                    it.name.replace(" ", "_").equals(roleName.replace(" ", "_"), ignoreCase = true)
                }
                if (found != null) {
                    return Result.success(found)
                }
            }
            Result.failure(Exception("Role tidak ditemukan."))
        } catch (e: Exception) {
            if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun createRole(role: FirestoreRole): Result<String> {
        return try {
            val docRef = rolesCollection.document()
            val roleWithId = role.copy(
                roleId = docRef.id,
                createdAt = Date(),
                updatedAt = Date()
            )
            docRef.set(roleWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            Result.failure(Exception("Gagal membuat role baru. ${e.message}"))
        }
    }

    suspend fun updateRole(role: FirestoreRole): Result<Unit> {
        return try {
            val updates = mapOf(
                "name" to role.name,
                "description" to role.description,
                "permissions" to role.permissions,
                "isActive" to role.isActive,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            rolesCollection.document(role.roleId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            Result.failure(Exception("Gagal memperbarui role. ${e.message}"))
        }
    }

    suspend fun updateRolePermissions(roleId: String, permissions: Map<String, Boolean>): Result<Unit> {
        return try {
            rolesCollection.document(roleId).update(
                mapOf(
                    "permissions" to permissions,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            Result.failure(Exception("Gagal memperbarui hak akses role. ${e.message}"))
        }
    }

    suspend fun deleteRole(roleId: String): Result<Unit> {
        return try {
            val doc = rolesCollection.document(roleId).get().await()
            val roleName = doc.getString("name") ?: ""
            if (roleName.equals("Super Admin", ignoreCase = true) || roleName.equals("SUPER_ADMIN", ignoreCase = true)) {
                return Result.failure(Exception("Role Super Admin tidak dapat dihapus."))
            }
            rolesCollection.document(roleId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            Result.failure(Exception("Gagal menghapus role. ${e.message}"))
        }
    }
}
