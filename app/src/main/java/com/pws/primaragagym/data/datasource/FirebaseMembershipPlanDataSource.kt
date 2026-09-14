package com.pws.primaragagym.data.datasource

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.pws.primaragagym.domain.model.FirestoreMembershipPlan
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseMembershipPlanDataSource {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val plansCollection = firestore.collection(FirestoreCollections.MEMBERSHIP_PLANS)

    private fun documentToFirestoreMembershipPlan(doc: DocumentSnapshot): FirestoreMembershipPlan {
        return try {
            val data = doc.data ?: emptyMap<String, Any?>()
            val name = data["name"] as? String ?: ""
            val description = data["description"] as? String ?: ""
            val type = (data["type"] as? String)?.uppercase() ?: when ((data["durationType"] as? String)?.uppercase()) {
                "DAY" -> "DAILY"
                "YEAR" -> "YEARLY"
                else -> "MONTHLY"
            }
            val durationType = (data["durationType"] as? String)?.uppercase() ?: when (type) {
                "DAILY" -> "DAY"
                "YEARLY" -> "YEAR"
                else -> "MONTH"
            }
            val duration = data["duration"] as? String ?: ""
            val durationValue = when (val dv = data["durationValue"]) {
                is Number -> dv.toInt()
                is String -> dv.toIntOrNull() ?: 1
                else -> duration.filter { it.isDigit() }.toIntOrNull() ?: 1
            }
            val price = when (val p = data["price"]) {
                is Number -> p.toLong()
                is String -> p.filter { it.isDigit() }.toLongOrNull() ?: 0L
                else -> 0L
            }
            val maxMembers = when (val m = data["maxMembers"]) {
                is Number -> m.toInt()
                is String -> m.toIntOrNull()
                else -> null
            }
            val isActive = when (val a = data["isActive"]) {
                is Boolean -> a
                is Number -> a.toInt() == 1
                is String -> a.equals("true", ignoreCase = true)
                else -> true
            }
            val createdAt = when (val c = data["createdAt"]) {
                is com.google.firebase.Timestamp -> c.toDate()
                is Date -> c
                is Number -> Date(c.toLong())
                else -> Date()
            }
            val updatedAt = when (val u = data["updatedAt"]) {
                is com.google.firebase.Timestamp -> u.toDate()
                is Date -> u
                is Number -> Date(u.toLong())
                else -> Date()
            }

            if (data.containsKey("maxMembers")) {
                try {
                    plansCollection.document(doc.id).update("maxMembers", FieldValue.delete())
                } catch (_: Exception) {}
            }

            FirestoreMembershipPlan(
                planId = doc.id,
                name = name,
                description = description,
                type = type,
                durationType = durationType,
                durationValue = durationValue,
                duration = duration,
                price = price,
                maxMembers = null,
                isActive = isActive,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            android.util.Log.e("MembershipPlanDS", "Error parsing document ${doc.id}: ${e.message}", e)
            FirestoreMembershipPlan(
                planId = doc.id,
                name = doc.getString("name") ?: "",
                type = (doc.getString("type") ?: "MONTHLY").uppercase(),
                price = doc.getLong("price") ?: 0L,
                duration = doc.getString("duration") ?: "",
                isActive = doc.getBoolean("isActive") ?: true
            )
        }
    }

    fun observeMembershipPlans(isActive: Boolean? = null): Flow<List<FirestoreMembershipPlan>> = callbackFlow {
        var query: Query = plansCollection
        if (isActive != null) {
            query = query.whereEqualTo("isActive", isActive)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("MembershipPlanDS", "Error observing plans: ${error.message}", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val plans = snapshot.documents.mapNotNull { doc ->
                    try {
                        documentToFirestoreMembershipPlan(doc)
                    } catch (e: Exception) {
                        android.util.Log.e("MembershipPlanDS", "Failed to parse doc ${doc.id}: ${e.message}", e)
                        null
                    }
                }.sortedBy { it.price }
                android.util.Log.d("MembershipPlanDS", "Observed ${plans.size} plans from collection ${FirestoreCollections.MEMBERSHIP_PLANS}")
                trySend(plans)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun getMembershipPlans(
        isActive: Boolean? = true,
        limit: Int = 50
    ): Result<List<FirestoreMembershipPlan>> {
        return try {
            var query: Query = plansCollection.limit(limit.toLong())
            if (isActive != null) {
                query = query.whereEqualTo("isActive", isActive)
            }

            val snapshot = query.get().await()
            val plans = snapshot.documents.mapNotNull { doc ->
                try {
                    documentToFirestoreMembershipPlan(doc)
                } catch (e: Exception) {
                    android.util.Log.e("MembershipPlanDS", "Failed to parse doc ${doc.id}: ${e.message}", e)
                    null
                }
            }.sortedBy { it.price }
            android.util.Log.d("MembershipPlanDS", "getMembershipPlans returned ${plans.size} plans")
            Result.success(plans)
        } catch (e: Exception) {
            if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            android.util.Log.e("MembershipPlanDS", "Error getting plans: ${e.message}", e)
            Result.failure(Exception("Gagal memuat paket membership. ${e.message}"))
        }
    }

    suspend fun getMembershipPlanById(planId: String): Result<FirestoreMembershipPlan> {
        return try {
            val doc = plansCollection.document(planId).get().await()
            if (doc.exists()) {
                Result.success(documentToFirestoreMembershipPlan(doc))
            } else {
                Result.failure(Exception("Paket membership tidak ditemukan."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat detail paket. ${e.message}"))
        }
    }

    suspend fun createMembershipPlan(plan: FirestoreMembershipPlan): Result<String> {
        return try {
            val docRef = if (plan.planId.isNotBlank()) plansCollection.document(plan.planId) else plansCollection.document()
            val trimmedName = plan.name.trim()
            val trimmedDuration = plan.duration.trim()

            // Field tunggal bersih sesuai form Tambah Paket Membership
            val planData = hashMapOf<String, Any?>(
                "planId" to docRef.id,
                "name" to trimmedName,
                "type" to plan.type,
                "price" to plan.price,
                "duration" to trimmedDuration,
                "isActive" to plan.isActive,
                "durationType" to plan.durationType,
                "durationValue" to plan.durationValue,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(planData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal membuat paket membership baru. ${e.message}"))
        }
    }

    suspend fun updateMembershipPlan(plan: FirestoreMembershipPlan): Result<Unit> {
        return try {
            val trimmedName = plan.name.trim()
            val trimmedDuration = plan.duration.trim()

            val updates = hashMapOf<String, Any?>(
                "name" to trimmedName,
                "type" to plan.type,
                "price" to plan.price,
                "duration" to trimmedDuration,
                "maxMembers" to FieldValue.delete(),
                "isActive" to plan.isActive,
                "durationType" to plan.durationType,
                "durationValue" to plan.durationValue,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            plansCollection.document(plan.planId).set(updates, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memperbarui paket membership. ${e.message}"))
        }
    }

    suspend fun deleteMembershipPlan(planId: String): Result<Unit> {
        return try {
            plansCollection.document(planId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghapus paket membership. ${e.message}"))
        }
    }
}
