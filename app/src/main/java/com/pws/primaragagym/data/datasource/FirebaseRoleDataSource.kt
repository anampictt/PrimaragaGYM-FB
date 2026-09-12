package com.pws.primaragagym.data.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseRoleDataSource {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val rolesCollection = firestore.collection(FirestoreCollections.ROLES)

    suspend fun getRoles(isActive: Boolean? = true): Result<List<com.pws.primaragagym.domain.model.FirestoreRole>> {
        return try {
            val snapshot = rolesCollection.get().await()
            var roles = snapshot.documents.mapNotNull { doc ->
                val role = doc.toObject(com.pws.primaragagym.domain.model.FirestoreRole::class.java)
                role?.copy(roleId = doc.id)
            }

            // Seed default roles if Firestore has no roles yet
            if (roles.isEmpty()) {
                seedDefaultRoles()
                val newSnapshot = rolesCollection.get().await()
                roles = newSnapshot.documents.mapNotNull { doc ->
                    val role = doc.toObject(com.pws.primaragagym.domain.model.FirestoreRole::class.java)
                    role?.copy(roleId = doc.id)
                }
            }

            if (isActive != null) {
                roles = roles.filter { it.isActive == isActive }
            }

            roles = roles.sortedBy { it.name }
            Result.success(roles)
        } catch (e: Exception) {
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
            "keuangan" to true,
            "notifikasi" to true,
            "laporan" to true,
            "pengaturan" to true
        )
        val adminPermissions = mapOf(
            "dashboard" to true,
            "manajemen_pengguna" to false,
            "manajemen_role" to false,
            "manajemen_cabang" to false,
            "manajemen_member" to true,
            "check_in_out" to true,
            "keuangan" to true,
            "notifikasi" to true,
            "laporan" to true,
            "pengaturan" to true
        )
        val staffPermissions = mapOf(
            "dashboard" to true,
            "manajemen_pengguna" to false,
            "manajemen_role" to false,
            "manajemen_cabang" to false,
            "manajemen_member" to true,
            "check_in_out" to true,
            "keuangan" to false,
            "notifikasi" to true,
            "laporan" to false,
            "pengaturan" to true
        )

        val defaultRoles = listOf(
            com.pws.primaragagym.domain.model.FirestoreRole(
                name = "Super Admin",
                description = "Akses penuh ke seluruh sistem.",
                permissions = superAdminPermissions,
                isActive = true,
                createdAt = Date(),
                updatedAt = Date()
            ),
            com.pws.primaragagym.domain.model.FirestoreRole(
                name = "Admin",
                description = "Mengelola operasional gym dan data member.",
                permissions = adminPermissions,
                isActive = true,
                createdAt = Date(),
                updatedAt = Date()
            ),
            com.pws.primaragagym.domain.model.FirestoreRole(
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

    suspend fun getRoleById(roleId: String): Result<com.pws.primaragagym.domain.model.FirestoreRole> {
        return try {
            val doc = rolesCollection.document(roleId).get().await()
            if (doc.exists()) {
                val role = doc.toObject(com.pws.primaragagym.domain.model.FirestoreRole::class.java)
                if (role != null) {
                    Result.success(role.copy(roleId = doc.id))
                } else {
                    Result.failure(Exception("Role tidak ditemukan."))
                }
            } else {
                Result.failure(Exception("Role tidak ditemukan."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat detail role. ${e.message}"))
        }
    }

    suspend fun getRoleByName(roleName: String): Result<com.pws.primaragagym.domain.model.FirestoreRole> {
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
            Result.failure(e)
        }
    }

    suspend fun createRole(role: com.pws.primaragagym.domain.model.FirestoreRole): Result<String> {
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
            Result.failure(Exception("Gagal membuat role baru. ${e.message}"))
        }
    }

    suspend fun updateRole(role: com.pws.primaragagym.domain.model.FirestoreRole): Result<Unit> {
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
            Result.failure(Exception("Gagal memperbarui role. ${e.message}"))
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
            Result.failure(Exception("Gagal menghapus role. ${e.message}"))
        }
    }
}
