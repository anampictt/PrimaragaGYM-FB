package com.pws.primaragagym.data.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class FirebaseCheckinDataSource {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val checkinsCollection = firestore.collection(FirestoreCollections.CHECKINS)
    private val membersCollection = firestore.collection(FirestoreCollections.MEMBERS)

    suspend fun checkIn(
        memberId: String,
        memberName: String,
        memberCode: String,
        membershipId: String,
        branchId: String
    ): Result<String> {
        return try {
            // Check if member already checked in
            val existingCheckin = checkinsCollection
                .whereEqualTo("memberId", memberId)
                .whereEqualTo("status", "CHECKED_IN")
                .limit(1)
                .get()
                .await()

            if (!existingCheckin.isEmpty) {
                return Result.failure(Exception("Member sudah melakukan check-in hari ini."))
            }

            val checkin = com.pws.primaragagym.domain.model.FirestoreCheckin(
                memberId = memberId,
                memberName = memberName,
                memberCode = memberCode,
                membershipId = membershipId,
                branchId = branchId,
                checkInAt = Date(),
                status = "CHECKED_IN",
                createdAt = Date()
            )

            val docRef = checkinsCollection.document()
            docRef.set(checkin).await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal melakukan check-in. ${e.message}"))
        }
    }

    suspend fun checkOut(memberId: String): Result<String> {
        return try {
            // Find active check-in
            val activeCheckin = checkinsCollection
                .whereEqualTo("memberId", memberId)
                .whereEqualTo("status", "CHECKED_IN")
                .limit(1)
                .get()
                .await()

            if (activeCheckin.isEmpty) {
                return Result.failure(Exception("Tidak ada check-in aktif untuk member ini."))
            }

            val checkinId = activeCheckin.documents[0].id

            checkinsCollection.document(checkinId)
                .update(mapOf(
                    "checkOutAt" to FieldValue.serverTimestamp(),
                    "status" to "CHECKED_OUT"
                )).await()

            Result.success(checkinId)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal melakukan check-out. ${e.message}"))
        }
    }

    suspend fun getTodayCheckins(branchId: String): Result<List<com.pws.primaragagym.domain.model.FirestoreCheckin>> {
        return try {
            val startOfDay = getStartOfDay()

            val snapshot = checkinsCollection
                .whereEqualTo("branchId", branchId)
                .whereGreaterThanOrEqualTo("checkInAt", startOfDay)
                .orderBy("checkInAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val checkins = snapshot.documents.mapNotNull { doc ->
                doc.toObject(com.pws.primaragagym.domain.model.FirestoreCheckin::class.java)
            }
            Result.success(checkins)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat data check-in hari ini. ${e.message}"))
        }
    }

    suspend fun getTodayCheckinsCount(branchId: String): Result<Int> {
        return try {
            val startOfDay = getStartOfDay()

            val snapshot = checkinsCollection
                .whereEqualTo("branchId", branchId)
                .whereGreaterThanOrEqualTo("checkInAt", startOfDay)
                .get()
                .await()

            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghitung check-in. ${e.message}"))
        }
    }

    suspend fun getWeeklyCheckins(branchId: String): Result<Map<String, Int>> {
        return try {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, -6)
            }

            val startDate = calendar.time
            val result = mutableMapOf<String, Int>()

            // Initialize all days with 0
            val dayNames = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")

            for (i in 0..6) {
                val cal = Calendar.getInstance().apply {
                    time = startDate
                    add(Calendar.DAY_OF_YEAR, i)
                }
                val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Adjust to start from Monday
                result[dayNames[dayIndex.coerceIn(0, 6)]] = 0
            }

            val snapshot = checkinsCollection
                .whereEqualTo("branchId", branchId)
                .whereGreaterThanOrEqualTo("checkInAt", startDate)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                val checkin = doc.toObject(com.pws.primaragagym.domain.model.FirestoreCheckin::class.java) ?: return@forEach
                checkin.checkInAt?.let { date ->
                    val cal = Calendar.getInstance().apply { time = date }
                    val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                    val dayName = dayNames.getOrElse(dayIndex.coerceIn(0, 6)) { "Sen" }
                    result[dayName] = (result[dayName] ?: 0) + 1
                }
            }

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat data check-in mingguan. ${e.message}"))
        }
    }

    suspend fun getRecentActivities(branchId: String, limit: Int = 10): Result<List<com.pws.primaragagym.domain.model.FirestoreCheckin>> {
        return try {
            val startOfDay = getStartOfDay()

            val snapshot = checkinsCollection
                .whereEqualTo("branchId", branchId)
                .whereGreaterThanOrEqualTo("checkInAt", startOfDay)
                .orderBy("checkInAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val activities = snapshot.documents.mapNotNull { doc ->
                doc.toObject(com.pws.primaragagym.domain.model.FirestoreCheckin::class.java)
            }
            Result.success(activities)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat aktivitas terbaru. ${e.message}"))
        }
    }

    suspend fun isMemberCheckedIn(memberId: String): Result<Boolean> {
        return try {
            val snapshot = checkinsCollection
                .whereEqualTo("memberId", memberId)
                .whereEqualTo("status", "CHECKED_IN")
                .limit(1)
                .get()
                .await()

            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memeriksa status check-in. ${e.message}"))
        }
    }

    suspend fun getMemberCheckinHistory(
        memberId: String,
        limit: Int = 50
    ): Result<List<com.pws.primaragagym.domain.model.FirestoreCheckin>> {
        return try {
            val snapshot = checkinsCollection
                .whereEqualTo("memberId", memberId)
                .orderBy("checkInAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val checkins = snapshot.documents.mapNotNull { doc ->
                doc.toObject(com.pws.primaragagym.domain.model.FirestoreCheckin::class.java)
            }
            Result.success(checkins)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat riwayat check-in. ${e.message}"))
        }
    }

    private fun getStartOfDay(): Date {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }
}
