package com.pws.primaragagym.data.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class FirebaseMembershipDataSource {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val membershipsCollection = firestore.collection(FirestoreCollections.MEMBERSHIPS)
    private val membersCollection = firestore.collection(FirestoreCollections.MEMBERS)

    suspend fun getMembershipsByMemberId(memberId: String): Result<List<com.pws.primaragagym.domain.model.FirestoreMembership>> {
        return try {
            val snapshot = membershipsCollection
                .whereEqualTo("memberId", memberId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val memberships = snapshot.documents.mapNotNull { doc ->
                doc.toObject(com.pws.primaragagym.domain.model.FirestoreMembership::class.java)
            }
            Result.success(memberships)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat riwayat membership. ${e.message}"))
        }
    }

    suspend fun getActiveMembership(memberId: String): Result<com.pws.primaragagym.domain.model.FirestoreMembership?> {
        return try {
            val snapshot = membershipsCollection
                .whereEqualTo("memberId", memberId)
                .whereEqualTo("status", "ACTIVE")
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val membership = snapshot.documents[0]
                    .toObject(com.pws.primaragagym.domain.model.FirestoreMembership::class.java)
                Result.success(membership)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat membership aktif. ${e.message}"))
        }
    }

    suspend fun getMembershipById(membershipId: String): Result<com.pws.primaragagym.domain.model.FirestoreMembership> {
        return try {
            val doc = membershipsCollection.document(membershipId).get().await()
            if (doc.exists()) {
                val membership = doc.toObject(com.pws.primaragagym.domain.model.FirestoreMembership::class.java)
                if (membership != null) {
                    Result.success(membership)
                } else {
                    Result.failure(Exception("Membership tidak ditemukan."))
                }
            } else {
                Result.failure(Exception("Membership tidak ditemukan."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat detail membership. ${e.message}"))
        }
    }

    suspend fun createMembership(
        memberId: String,
        memberName: String,
        planId: String,
        planName: String,
        branchId: String,
        durationType: String,
        durationValue: Int,
        price: Long
    ): Result<String> {
        return try {
            val startDate = Date()
            val endDate = calculateEndDate(startDate, durationType, durationValue)

            val membership = com.pws.primaragagym.domain.model.FirestoreMembership(
                memberId = memberId,
                memberName = memberName,
                planId = planId,
                planName = planName,
                branchId = branchId,
                startDate = startDate,
                endDate = endDate,
                status = "ACTIVE",
                price = price,
                createdAt = Date(),
                updatedAt = Date()
            )

            val docRef = membershipsCollection.document()
            docRef.set(membership).await()

            // Update member's activeMembershipId
            membersCollection.document(memberId)
                .update(mapOf(
                    "activeMembershipId" to docRef.id,
                    "status" to "ACTIVE",
                    "updatedAt" to FieldValue.serverTimestamp()
                )).await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal membuat membership. ${e.message}"))
        }
    }

    suspend fun renewMembership(
        currentMembershipId: String,
        memberId: String,
        memberName: String,
        planId: String,
        planName: String,
        branchId: String,
        durationType: String,
        durationValue: Int,
        price: Long
    ): Result<String> {
        return try {
            // Get current membership first
            val currentMembershipDoc = membershipsCollection.document(currentMembershipId).get().await()
            val currentMembership = currentMembershipDoc.toObject(com.pws.primaragagym.domain.model.FirestoreMembership::class.java)

            // Calculate new end date - start from current end date or now
            val baseDate = currentMembership?.endDate ?: Date()
            val newStartDate = if (baseDate.after(Date())) baseDate else Date()
            val newEndDate = calculateEndDate(newStartDate, durationType, durationValue)

            // Use batch for atomic operations
            val batch = firestore.batch()

            // Deactivate current membership
            batch.update(
                membershipsCollection.document(currentMembershipId),
                mapOf(
                    "status" to "EXPIRED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            // Create new membership
            val newDocRef = membershipsCollection.document()
            val newMembership = mapOf(
                "membershipId" to newDocRef.id,
                "memberId" to memberId,
                "memberName" to memberName,
                "planId" to planId,
                "planName" to planName,
                "branchId" to branchId,
                "startDate" to newStartDate,
                "endDate" to newEndDate,
                "status" to "ACTIVE",
                "price" to price,
                "createdAt" to Date(),
                "updatedAt" to Date()
            )
            batch.set(newDocRef, newMembership)

            // Update member's activeMembershipId
            batch.update(
                membersCollection.document(memberId),
                mapOf(
                    "activeMembershipId" to newDocRef.id,
                    "status" to "ACTIVE",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            batch.commit().await()

            Result.success(newDocRef.id)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memperpanjang membership. ${e.message}"))
        }
    }

    suspend fun suspendMembership(membershipId: String, memberId: String): Result<Unit> {
        return try {
            // Use batch for atomic operations
            val batch = firestore.batch()

            batch.update(
                membershipsCollection.document(membershipId),
                mapOf(
                    "status" to "SUSPENDED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            batch.update(
                membersCollection.document(memberId),
                mapOf(
                    "status" to "SUSPENDED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menangguhkan membership. ${e.message}"))
        }
    }

    suspend fun getExpiringMemberships(branchId: String, daysAhead: Int = 7): Result<List<com.pws.primaragagym.domain.model.FirestoreMembership>> {
        return try {
            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, daysAhead)
            }
            val threshold = calendar.time

            val snapshot = membershipsCollection
                .whereEqualTo("branchId", branchId)
                .whereEqualTo("status", "ACTIVE")
                .whereLessThanOrEqualTo("endDate", threshold)
                .get()
                .await()

            val memberships = snapshot.documents.mapNotNull { doc ->
                doc.toObject(com.pws.primaragagym.domain.model.FirestoreMembership::class.java)
            }
            Result.success(memberships)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat membership yang akan expire. ${e.message}"))
        }
    }

    private fun calculateEndDate(startDate: Date, durationType: String, durationValue: Int): Date {
        val calendar = Calendar.getInstance().apply {
            time = startDate
        }

        when (durationType.uppercase()) {
            "DAY" -> calendar.add(Calendar.DAY_OF_YEAR, durationValue)
            "MONTH" -> calendar.add(Calendar.MONTH, durationValue)
            "YEAR" -> calendar.add(Calendar.YEAR, durationValue)
            else -> calendar.add(Calendar.MONTH, durationValue)
        }

        // Subtract 1 day to make it inclusive
        calendar.add(Calendar.DAY_OF_YEAR, -1)

        return calendar.time
    }
}
