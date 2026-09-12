package com.pws.primaragagym.data.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseBranchDataSource {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val branchesCollection = firestore.collection(FirestoreCollections.BRANCHES)

    suspend fun getBranches(isActive: Boolean? = true): Result<List<com.pws.primaragagym.domain.model.FirestoreBranch>> {
        return try {
            var query: Query = branchesCollection
                .orderBy("name", Query.Direction.ASCENDING)

            if (isActive != null) {
                query = query.whereEqualTo("isActive", isActive)
            }

            val snapshot = query.get().await()
            val branches = snapshot.documents.mapNotNull { doc ->
                doc.toObject(com.pws.primaragagym.domain.model.FirestoreBranch::class.java)
            }
            Result.success(branches)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat data cabang. ${e.message}"))
        }
    }

    suspend fun getBranchById(branchId: String): Result<com.pws.primaragagym.domain.model.FirestoreBranch> {
        return try {
            val doc = branchesCollection.document(branchId).get().await()
            if (doc.exists()) {
                val branch = doc.toObject(com.pws.primaragagym.domain.model.FirestoreBranch::class.java)
                if (branch != null) {
                    Result.success(branch)
                } else {
                    Result.failure(Exception("Cabang tidak ditemukan."))
                }
            } else {
                Result.failure(Exception("Cabang tidak ditemukan."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat detail cabang. ${e.message}"))
        }
    }

    suspend fun createBranch(branch: com.pws.primaragagym.domain.model.FirestoreBranch): Result<String> {
        return try {
            val docRef = branchesCollection.document()
            val branchWithId = branch.copy(
                branchId = docRef.id,
                createdAt = Date(),
                updatedAt = Date()
            )
            docRef.set(branchWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal membuat cabang baru. ${e.message}"))
        }
    }

    suspend fun updateBranch(branch: com.pws.primaragagym.domain.model.FirestoreBranch): Result<Unit> {
        return try {
            val updates = mapOf(
                "name" to branch.name,
                "address" to branch.address,
                "latitude" to branch.latitude,
                "longitude" to branch.longitude,
                "isActive" to branch.isActive,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            branchesCollection.document(branch.branchId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memperbarui cabang. ${e.message}"))
        }
    }

    suspend fun deleteBranch(branchId: String): Result<Unit> {
        return try {
            // Soft delete
            branchesCollection.document(branchId)
                .update(mapOf(
                    "isActive" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghapus cabang. ${e.message}"))
        }
    }

    suspend fun getActiveBranchesCount(): Result<Int> {
        return try {
            val snapshot = branchesCollection
                .whereEqualTo("isActive", true)
                .get()
                .await()
            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghitung cabang aktif. ${e.message}"))
        }
    }
}
