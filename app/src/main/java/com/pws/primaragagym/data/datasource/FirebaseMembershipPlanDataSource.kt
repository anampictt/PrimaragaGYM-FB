package com.pws.primaragagym.data.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseMembershipPlanDataSource {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val plansCollection = firestore.collection(FirestoreCollections.MEMBERSHIP_PLANS)

    suspend fun getMembershipPlans(
        isActive: Boolean? = true,
        limit: Int = 50
    ): Result<List<com.pws.primaragagym.domain.model.FirestoreMembershipPlan>> {
        return try {
            var query: Query = plansCollection
                .orderBy("price", Query.Direction.ASCENDING)
                .limit(limit.toLong())

            if (isActive != null) {
                query = query.whereEqualTo("isActive", isActive)
            }

            val snapshot = query.get().await()
            val plans = snapshot.documents.mapNotNull { doc ->
                doc.toObject(com.pws.primaragagym.domain.model.FirestoreMembershipPlan::class.java)
            }
            Result.success(plans)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat paket membership. ${e.message}"))
        }
    }

    suspend fun getMembershipPlanById(planId: String): Result<com.pws.primaragagym.domain.model.FirestoreMembershipPlan> {
        return try {
            val doc = plansCollection.document(planId).get().await()
            if (doc.exists()) {
                val plan = doc.toObject(com.pws.primaragagym.domain.model.FirestoreMembershipPlan::class.java)
                if (plan != null) {
                    Result.success(plan)
                } else {
                    Result.failure(Exception("Paket membership tidak ditemukan."))
                }
            } else {
                Result.failure(Exception("Paket membership tidak ditemukan."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat detail paket. ${e.message}"))
        }
    }

    suspend fun createMembershipPlan(plan: com.pws.primaragagym.domain.model.FirestoreMembershipPlan): Result<String> {
        return try {
            val docRef = plansCollection.document()
            val planWithId = plan.copy(
                planId = docRef.id,
                createdAt = Date(),
                updatedAt = Date()
            )
            docRef.set(planWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal membuat paket membership. ${e.message}"))
        }
    }

    suspend fun updateMembershipPlan(plan: com.pws.primaragagym.domain.model.FirestoreMembershipPlan): Result<Unit> {
        return try {
            val updates = mapOf(
                "name" to plan.name,
                "description" to plan.description,
                "durationType" to plan.durationType,
                "durationValue" to plan.durationValue,
                "price" to plan.price,
                "maxMembers" to plan.maxMembers,
                "isActive" to plan.isActive,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            plansCollection.document(plan.planId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memperbarui paket membership. ${e.message}"))
        }
    }

    suspend fun deleteMembershipPlan(planId: String): Result<Unit> {
        return try {
            // Soft delete
            plansCollection.document(planId)
                .update(mapOf(
                    "isActive" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghapus paket membership. ${e.message}"))
        }
    }
}
