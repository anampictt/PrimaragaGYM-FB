package com.pws.primaragagym.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.DocumentSnapshot
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

    private fun documentToFirestoreMember(doc: DocumentSnapshot): com.pws.primaragagym.domain.model.FirestoreMember {
        val data = doc.data ?: emptyMap<String, Any?>()
        val fullName = (data["fullName"] as? String)
            ?: (data["name"] as? String)
            ?: (data["nama"] as? String)
            ?: ""
        val memberCode = (data["memberCode"] as? String)
            ?: (data["code"] as? String)
            ?: (data["kode"] as? String)
            ?: (data["barcode"] as? String)
            ?: doc.id
        val phoneNumber = (data["phoneNumber"] as? String)
            ?: (data["phone"] as? String)
            ?: (data["noHp"] as? String)
            ?: (data["telepon"] as? String)
            ?: ""
        val email = (data["email"] as? String) ?: ""
        val address = (data["address"] as? String) ?: (data["alamat"] as? String) ?: ""
        val photoUrl = data["photoUrl"] as? String
        val gender = (data["gender"] as? String) ?: ""
        val branchId = (data["branchId"] as? String) ?: ""
        val status = (data["status"] as? String)?.uppercase() ?: "ACTIVE"
        val activeMembershipId = data["activeMembershipId"] as? String
        val planId = (data["planId"] as? String) ?: ""
        val planName = (data["planName"] as? String)
            ?: (data["packageName"] as? String)
            ?: (data["membershipType"] as? String)
            ?: ((data["membership"] as? Map<*, *>)?.get("planName") as? String)
            ?: ""
        val planPrice = when (val p = data["planPrice"] ?: data["price"]) {
            is Number -> p.toLong()
            is String -> p.filter { it.isDigit() }.toLongOrNull() ?: 0L
            else -> 0L
        }
        val planType = (data["planType"] as? String) ?: (data["type"] as? String) ?: ""
        val duration = (data["duration"] as? String) ?: ""

        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

        val startDate = when (val s = data["startDate"] ?: data["start_date"] ?: data["joinedAt"]) {
            is String -> s
            is com.google.firebase.Timestamp -> dateFormat.format(s.toDate())
            is java.util.Date -> dateFormat.format(s)
            else -> ""
        }

        val expiredDate = when (val e = data["expiredDate"] ?: data["endDate"] ?: data["expired_date"] ?: data["end_date"]) {
            is String -> e
            is com.google.firebase.Timestamp -> dateFormat.format(e.toDate())
            is java.util.Date -> dateFormat.format(e)
            else -> ""
        }

        val paymentMethod = (data["paymentMethod"] as? String) ?: (data["metodePembayaran"] as? String) ?: ""
        val createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()
        val updatedAt = (data["updatedAt"] as? com.google.firebase.Timestamp)?.toDate()

        return com.pws.primaragagym.domain.model.FirestoreMember(
            memberId = doc.id,
            memberCode = memberCode,
            fullName = fullName,
            phoneNumber = phoneNumber,
            email = email,
            address = address,
            photoUrl = photoUrl,
            gender = gender,
            branchId = branchId,
            status = status,
            activeMembershipId = activeMembershipId,
            planId = planId,
            planName = planName,
            planPrice = planPrice,
            planType = planType,
            duration = duration,
            startDate = startDate,
            expiredDate = expiredDate,
            paymentMethod = paymentMethod,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    suspend fun getMembers(
        branchId: String? = null,
        status: String? = null,
        limit: Int = 100,
        lastDocumentId: String? = null
    ): Result<List<com.pws.primaragagym.domain.model.FirestoreMember>> {
        return try {
            var query: Query = membersCollection

            if (!branchId.isNullOrBlank()) {
                query = query.whereEqualTo("branchId", branchId)
            }

            if (!status.isNullOrBlank()) {
                query = query.whereEqualTo("status", status)
            }

            val snapshot = try {
                query.orderBy("createdAt", Query.Direction.DESCENDING).limit(limit.toLong()).get().await()
            } catch (e: Exception) {
                query.limit(limit.toLong()).get().await()
            }

            val members = snapshot.documents.map { doc ->
                documentToFirestoreMember(doc)
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
                val member = documentToFirestoreMember(doc)
                Result.success(member)
            } else {
                // Fallback: check by memberCode or memberId field
                val codeSnapshot = membersCollection
                    .whereEqualTo("memberCode", memberId)
                    .limit(1)
                    .get()
                    .await()
                if (!codeSnapshot.isEmpty) {
                    Result.success(documentToFirestoreMember(codeSnapshot.documents[0]))
                } else {
                    val idSnapshot = membersCollection
                        .whereEqualTo("memberId", memberId)
                        .limit(1)
                        .get()
                        .await()
                    if (!idSnapshot.isEmpty) {
                        Result.success(documentToFirestoreMember(idSnapshot.documents[0]))
                    } else {
                        Result.failure(Exception("Member tidak ditemukan."))
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat detail member: ${e.message}"))
        }
    }

    suspend fun getMemberByCode(memberCode: String): Result<com.pws.primaragagym.domain.model.FirestoreMember> {
        return try {
            val cleanCode = memberCode.trim()
            val snapshot = membersCollection
                .whereEqualTo("memberCode", cleanCode)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val member = documentToFirestoreMember(snapshot.documents[0])
                Result.success(member)
            } else {
                val upperCode = cleanCode.uppercase()
                if (upperCode != cleanCode) {
                    val upperSnapshot = membersCollection
                        .whereEqualTo("memberCode", upperCode)
                        .limit(1)
                        .get()
                        .await()
                    if (!upperSnapshot.isEmpty) {
                        return Result.success(documentToFirestoreMember(upperSnapshot.documents[0]))
                    }
                }
                Result.failure(Exception("Member dengan kode $cleanCode tidak ditemukan."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal mencari member. ${e.message}"))
        }
    }

    suspend fun searchMembers(
        query: String,
        branchId: String? = null,
        limit: Int = 100
    ): Result<List<com.pws.primaragagym.domain.model.FirestoreMember>> {
        return try {
            var baseQuery: Query = membersCollection
            if (!branchId.isNullOrBlank()) {
                baseQuery = baseQuery.whereEqualTo("branchId", branchId)
            }

            val snapshot = baseQuery.limit(limit.toLong()).get().await()
            val members = snapshot.documents
                .map { documentToFirestoreMember(it) }
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
            val docRef = membersCollection.document()
            val memberCode = if (member.memberCode.isNotBlank()) member.memberCode.trim() else generateMemberCode()
            val memberData = hashMapOf<String, Any?>(
                "memberId" to docRef.id,
                "memberCode" to memberCode,
                "fullName" to member.fullName.trim(),
                "name" to member.fullName.trim(),
                "phoneNumber" to member.phoneNumber.trim(),
                "phone" to member.phoneNumber.trim(),
                "email" to member.email.trim(),
                "address" to member.address.trim(),
                "planId" to member.planId,
                "planName" to member.planName,
                "planPrice" to member.planPrice,
                "planType" to member.planType,
                "duration" to member.duration,
                "startDate" to member.startDate,
                "expiredDate" to member.expiredDate,
                "paymentMethod" to member.paymentMethod,
                "branchId" to member.branchId,
                "status" to member.status.ifEmpty { "ACTIVE" },
                "photoUrl" to member.photoUrl,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val cleanMemberData = memberData.filterValues { value ->
                when (value) {
                    null -> false
                    is String -> value.isNotBlank()
                    else -> true
                }
            }
            docRef.set(cleanMemberData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal membuat member baru: ${e.message}"))
        }
    }

    suspend fun updateMember(member: com.pws.primaragagym.domain.model.FirestoreMember): Result<Unit> {
        return try {
            val targetId = member.memberId.ifBlank { member.id }
            val updates = hashMapOf<String, Any?>(
                "fullName" to member.fullName.trim(),
                "name" to member.fullName.trim(),
                "memberCode" to member.memberCode.trim(),
                "phoneNumber" to member.phoneNumber.trim(),
                "phone" to member.phoneNumber.trim(),
                "email" to member.email.trim(),
                "address" to member.address.trim(),
                "planId" to member.planId,
                "planName" to member.planName,
                "planPrice" to member.planPrice,
                "planType" to member.planType,
                "duration" to member.duration,
                "startDate" to member.startDate,
                "expiredDate" to member.expiredDate,
                "paymentMethod" to member.paymentMethod,
                "branchId" to member.branchId,
                "status" to member.status,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val cleanUpdates = updates.filterValues { value ->
                when (value) {
                    null -> false
                    is String -> value.isNotBlank()
                    else -> true
                }
            }
            membersCollection.document(targetId).set(cleanUpdates, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memperbarui member: ${e.message}"))
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
            membersCollection.document(memberId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghapus member: ${e.message}"))
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
