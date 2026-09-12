package com.pws.primaragagym.data.datasource

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.pws.primaragagym.domain.model.FirestoreBranch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseBranchDataSource {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val branchesCollection = firestore.collection(FirestoreCollections.BRANCHES)

    private fun documentToFirestoreBranch(doc: DocumentSnapshot): FirestoreBranch {
        val data = doc.data ?: emptyMap<String, Any?>()
        val name = (data["name"] ?: data["nama"]) as? String ?: ""
        val address = (data["address"] ?: data["alamat"]) as? String ?: ""
        val isActive = when (val v = data["isActive"]) {
            is Boolean -> v
            is Number -> v.toInt() == 1
            is String -> v.equals("true", ignoreCase = true)
            else -> true
        }
        val latitude = when (val lat = data["latitude"]) {
            is Number -> lat.toDouble()
            is String -> lat.toDoubleOrNull()
            else -> null
        }
        val longitude = when (val lng = data["longitude"]) {
            is Number -> lng.toDouble()
            is String -> lng.toDoubleOrNull()
            else -> null
        }
        val createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
        val updatedAt = (data["updatedAt"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()

        return FirestoreBranch(
            branchId = doc.id,
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun observeBranches(isActive: Boolean? = null): Flow<List<FirestoreBranch>> = callbackFlow {
        var query: Query = branchesCollection
        if (isActive != null) {
            query = query.whereEqualTo("isActive", isActive)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val branches = snapshot.documents.map { documentToFirestoreBranch(it) }
                    .sortedBy { it.name.lowercase() }
                trySend(branches)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun getBranches(isActive: Boolean? = true): Result<List<FirestoreBranch>> {
        return try {
            var query: Query = branchesCollection
                .orderBy("name", Query.Direction.ASCENDING)

            if (isActive != null) {
                query = query.whereEqualTo("isActive", isActive)
            }

            val snapshot = query.get().await()
            val branches = snapshot.documents.map { documentToFirestoreBranch(it) }
            Result.success(branches)
        } catch (e: Exception) {
            if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            try {
                var fallbackQuery: Query = branchesCollection
                if (isActive != null) {
                    fallbackQuery = fallbackQuery.whereEqualTo("isActive", isActive)
                }
                val snapshot = fallbackQuery.get().await()
                val branches = snapshot.documents.map { documentToFirestoreBranch(it) }.sortedBy { it.name }
                Result.success(branches)
            } catch (e2: Exception) {
                Result.failure(Exception("Gagal memuat data cabang. ${e.message}"))
            }
        }
    }

    suspend fun getBranchById(branchId: String): Result<FirestoreBranch> {
        return try {
            val doc = branchesCollection.document(branchId).get().await()
            if (doc.exists()) {
                Result.success(documentToFirestoreBranch(doc))
            } else {
                Result.failure(Exception("Cabang tidak ditemukan."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat detail cabang. ${e.message}"))
        }
    }

    suspend fun createBranch(branch: FirestoreBranch): Result<String> {
        return try {
            val docRef = if (branch.branchId.isNotBlank()) branchesCollection.document(branch.branchId) else branchesCollection.document()
            val trimmedName = branch.name.trim()
            val trimmedAddress = branch.address.trim()

            // Field tunggal bersih sesuai form Tambah Cabang & model FirestoreBranch
            val branchData = hashMapOf<String, Any?>(
                "branchId" to docRef.id,
                "name" to trimmedName,
                "address" to trimmedAddress,
                "isActive" to branch.isActive,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(branchData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal membuat cabang baru. ${e.message}"))
        }
    }

    suspend fun updateBranch(branch: FirestoreBranch): Result<Unit> {
        return try {
            val trimmedName = branch.name.trim()
            val trimmedAddress = branch.address.trim()
            val updates = hashMapOf<String, Any?>(
                "name" to trimmedName,
                "address" to trimmedAddress,
                "isActive" to branch.isActive,
                "updatedAt" to FieldValue.serverTimestamp(),
                "nama" to FieldValue.delete(),
                "alamat" to FieldValue.delete(),
                "id" to FieldValue.delete()
            )
            branchesCollection.document(branch.branchId).set(updates, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memperbarui cabang. ${e.message}"))
        }
    }

    suspend fun deleteBranch(branchId: String): Result<Unit> {
        return try {
            branchesCollection.document(branchId).delete().await()
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
