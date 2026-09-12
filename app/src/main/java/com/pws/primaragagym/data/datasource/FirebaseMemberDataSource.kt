package com.pws.primaragagym.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class FirestoreCollections {
    companion object {
        const val USERS = "users"
        const val ROLES = "roles"
        const val BRANCHES = "branches"
        const val MEMBERS = "members"
        const val MEMBERSHIP_PLANS = "membershipPlans"
        const val MEMBERSHIPS = "memberships"
        const val CHECKINS = "checkins"
        const val PAYMENTS = "payments"
        const val NOTIFICATIONS = "notifications"
        const val REPORTS = "reports"
    }
}

class FirebaseMemberDataSource {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val membersCollection = firestore.collection(FirestoreCollections.MEMBERS)

    suspend fun getMembers(
        branchId: String? = null,
        status: String? = null,
        limit: Int = 25,
        lastDocumentId: String? = null
    ): Result<List<com.pws.primaragagym.domain.model.FirestoreMember>> {
        return try {
            var query: Query = membersCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            if (branchId != null) {
                query = query.whereEqualTo("branchId", branchId)
            }

            if (status != null) {
                query = query.whereEqualTo("status", status)
            }

            if (lastDocumentId != null) {
                val lastDoc = membersCollection.document(lastDocumentId).get().await()
                if (lastDoc.exists()) {
                    query = query.startAfter(lastDoc)
                }
            }

            val snapshot = query.get().await()
            val members = snapshot.documents.mapNotNull { doc ->
                doc.toObject(com.pws.primaragagym.domain.model.FirestoreMember::class.java)
            }
            Result.success(members)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat data member. ${e.message}"))
        }
    }

    suspend fun getMemberById(memberId: String): Result<com.pws.primaragagym.domain.model.FirestoreMember> {
        return try {
            val doc = membersCollection.document(memberId).get().await()
            if (doc.exists()) {
                val member = doc.toObject(com.pws.primaragagym.domain.model.FirestoreMember::class.java)
                if (member != null) {
                    Result.success(member)
                } else {
                    Result.failure(Exception("Data member tidak ditemukan."))
                }
            } else {
                Result.failure(Exception("Member tidak ditemukan."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat detail member. ${e.message}"))
        }
    }

    suspend fun getMemberByCode(memberCode: String): Result<com.pws.primaragagym.domain.model.FirestoreMember> {
        return try {
            val snapshot = membersCollection
                .whereEqualTo("memberCode", memberCode)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val member = snapshot.documents[0]
                    .toObject(com.pws.primaragagym.domain.model.FirestoreMember::class.java)
                if (member != null) {
                    Result.success(member)
                } else {
                    Result.failure(Exception("Data member tidak ditemukan."))
                }
            } else {
                Result.failure(Exception("Member dengan kode $memberCode tidak ditemukan."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal mencari member. ${e.message}"))
        }
    }

    suspend fun searchMembers(
        query: String,
        branchId: String? = null,
        limit: Int = 25
    ): Result<List<com.pws.primaragagym.domain.model.FirestoreMember>> {
        return try {
            // Simple search by name - Firestore doesn't support LIKE queries
            // For production, consider Algolia or similar for advanced search
            val snapshot = membersCollection
                .whereEqualTo("branchId", branchId ?: "")
                .orderBy("fullName")
                .limit(limit.toLong())
                .get()
                .await()

            val members = snapshot.documents
                .mapNotNull { it.toObject(com.pws.primaragagym.domain.model.FirestoreMember::class.java) }
                .filter { member ->
                    member.fullName.contains(query, ignoreCase = true) ||
                    member.memberCode.contains(query, ignoreCase = true) ||
                    member.phoneNumber.contains(query, ignoreCase = true)
                }

            Result.success(members)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal mencari member. ${e.message}"))
        }
    }

    suspend fun createMember(member: com.pws.primaragagym.domain.model.FirestoreMember): Result<String> {
        return try {
            val memberCode = generateMemberCode()
            val memberWithCode = member.copy(
                memberCode = memberCode,
                createdAt = Date(),
                updatedAt = Date()
            )

            val docRef = membersCollection.document()
            docRef.set(memberWithCode).await()

            // Update the document with the generated ID
            docRef.update("memberId", docRef.id).await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal membuat member baru. ${e.message}"))
        }
    }

    suspend fun updateMember(member: com.pws.primaragagym.domain.model.FirestoreMember): Result<Unit> {
        return try {
            val updates = mapOf(
                "fullName" to member.fullName,
                "phoneNumber" to member.phoneNumber,
                "email" to member.email,
                "address" to member.address,
                "photoUrl" to member.photoUrl,
                "gender" to member.gender,
                "dateOfBirth" to member.dateOfBirth,
                "status" to member.status,
                "activeMembershipId" to member.activeMembershipId,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            membersCollection.document(member.memberId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memperbarui member. ${e.message}"))
        }
    }

    suspend fun updateMemberStatus(memberId: String, status: String): Result<Unit> {
        return try {
            membersCollection.document(memberId)
                .update(mapOf(
                    "status" to status,
                    "updatedAt" to FieldValue.serverTimestamp()
                )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memperbarui status member. ${e.message}"))
        }
    }

    suspend fun deleteMember(memberId: String): Result<Unit> {
        return try {
            // Soft delete - just update status
            membersCollection.document(memberId)
                .update(mapOf(
                    "status" to "INACTIVE",
                    "updatedAt" to FieldValue.serverTimestamp()
                )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghapus member. ${e.message}"))
        }
    }

    suspend fun getActiveMembersCount(branchId: String): Result<Int> {
        return try {
            val snapshot = membersCollection
                .whereEqualTo("branchId", branchId)
                .whereEqualTo("status", "ACTIVE")
                .get()
                .await()
            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghitung member aktif. ${e.message}"))
        }
    }

    suspend fun getNewMembersCount(branchId: String, date: Date): Result<Int> {
        return try {
            val calendar = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.time

            val snapshot = membersCollection
                .whereEqualTo("branchId", branchId)
                .whereGreaterThanOrEqualTo("joinedAt", startOfDay)
                .get()
                .await()
            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghitung member baru. ${e.message}"))
        }
    }

    suspend fun getMembersWithExpiringMembership(branchId: String, daysAhead: Int = 7): Result<List<com.pws.primaragagym.domain.model.FirestoreMember>> {
        return try {
            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, daysAhead)
            }
            val expiryThreshold = calendar.time

            // Get active memberships that expire within threshold
            val membershipSnapshot = firestore.collection(FirestoreCollections.MEMBERSHIPS)
                .whereEqualTo("branchId", branchId)
                .whereEqualTo("status", "ACTIVE")
                .whereLessThanOrEqualTo("endDate", expiryThreshold)
                .get()
                .await()

            val memberIds = membershipSnapshot.documents.mapNotNull {
                it.getString("memberId")
            }

            if (memberIds.isEmpty()) {
                return Result.success(emptyList())
            }

            // Fetch member details
            val members = memberIds.mapNotNull { memberId ->
                try {
                    membersCollection.document(memberId).get().await()
                        .toObject(com.pws.primaragagym.domain.model.FirestoreMember::class.java)
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(members)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat member dengan membership expiring. ${e.message}"))
        }
    }

    private suspend fun generateMemberCode(): String {
        return try {
            val snapshot = membersCollection
                .orderBy("memberCode", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                "MBR-000001"
            } else {
                val lastCode = snapshot.documents[0].getString("memberCode") ?: "MBR-000000"
                val number = lastCode.removePrefix("MBR-").toIntOrNull() ?: 0
                String.format("MBR-%06d", number + 1)
            }
        } catch (e: Exception) {
            "MBR-${System.currentTimeMillis()}"
        }
    }
}
