package com.pws.primaragagym.data.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class FirebaseCheckinDataSource {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val checkinsCollection = firestore.collection(FirestoreCollections.CHECKINS)

    suspend fun checkIn(
        memberId: String,
        memberName: String,
        memberCode: String,
        membershipId: String,
        branchId: String
    ): Result<String> {
        return try {
            // Cek apakah member sedang check-in (belum check-out)
            val existingCheckin = checkinsCollection
                .whereEqualTo("memberId", memberId)
                .whereEqualTo("status", "CHECKED_IN")
                .limit(1)
                .get()
                .await()

            if (!existingCheckin.isEmpty) {
                return Result.failure(Exception("Member sudah melakukan check-in dan belum check-out."))
            }

            val docRef = checkinsCollection.document()
            val now = Date()

            // Bersihkan field agar tidak ada null atau string kosong di dokumen Firestore
            val checkinData = mutableMapOf<String, Any>(
                "checkinId" to docRef.id,
                "memberId" to memberId.trim(),
                "memberName" to memberName.trim(),
                "memberCode" to memberCode.trim(),
                "status" to "CHECKED_IN",
                "checkInAt" to now,
                "createdAt" to now
            )
            if (membershipId.isNotBlank()) {
                checkinData["membershipId"] = membershipId.trim()
            }
            if (branchId.isNotBlank()) {
                checkinData["branchId"] = branchId.trim()
            }

            docRef.set(checkinData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal melakukan check-in: ${e.message}"))
        }
    }

    suspend fun checkOut(memberId: String): Result<String> {
        return try {
            val cleanId = memberId.trim()
            var activeCheckin = checkinsCollection
                .whereEqualTo("memberId", cleanId)
                .whereEqualTo("status", "CHECKED_IN")
                .limit(1)
                .get()
                .await()

            if (activeCheckin.isEmpty) {
                activeCheckin = checkinsCollection
                    .whereEqualTo("memberCode", cleanId)
                    .whereEqualTo("status", "CHECKED_IN")
                    .limit(1)
                    .get()
                    .await()
            }

            if (activeCheckin.isEmpty) {
                return Result.failure(Exception("Tidak ada sesi check-in aktif untuk member ini."))
            }

            val checkinDoc = activeCheckin.documents[0]
            val checkinId = checkinDoc.id

            checkinsCollection.document(checkinId)
                .update(
                    mapOf(
                        "checkOutAt" to Date(),
                        "status" to "CHECKED_OUT"
                    )
                ).await()

            Result.success(checkinId)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal melakukan check-out: ${e.message}"))
        }
    }

    private fun documentToFirestoreCheckin(doc: com.google.firebase.firestore.DocumentSnapshot): com.pws.primaragagym.domain.model.FirestoreCheckin {
        val data = doc.data ?: emptyMap<String, Any?>()
        val checkinId = (data["checkinId"] as? String)?.ifBlank { doc.id } ?: doc.id
        val memberId = (data["memberId"] as? String) ?: ""
        val memberName = (data["memberName"] as? String) ?: ""
        val memberCode = (data["memberCode"] as? String) ?: ""
        val membershipId = (data["membershipId"] as? String) ?: ""
        val branchId = (data["branchId"] as? String) ?: ""
        val status = (data["status"] as? String)?.uppercase() ?: "CHECKED_IN"

        val checkInAt = when (val raw = data["checkInAt"]) {
            is com.google.firebase.Timestamp -> raw.toDate()
            is Date -> raw
            is Long -> Date(raw)
            else -> null
        }

        val checkOutAt = when (val raw = data["checkOutAt"]) {
            is com.google.firebase.Timestamp -> raw.toDate()
            is Date -> raw
            is Long -> Date(raw)
            else -> null
        }

        val createdAt = when (val raw = data["createdAt"]) {
            is com.google.firebase.Timestamp -> raw.toDate()
            is Date -> raw
            is Long -> Date(raw)
            else -> null
        }

        return com.pws.primaragagym.domain.model.FirestoreCheckin(
            checkinId = checkinId,
            memberId = memberId,
            memberName = memberName,
            memberCode = memberCode,
            membershipId = membershipId,
            branchId = branchId,
            checkInAt = checkInAt,
            checkOutAt = checkOutAt,
            status = status,
            createdAt = createdAt
        )
    }

    suspend fun getActiveCheckinForMember(memberId: String): Result<com.pws.primaragagym.domain.model.FirestoreCheckin?> {
        return try {
            val cleanId = memberId.trim()
            var snapshot = checkinsCollection
                .whereEqualTo("memberId", cleanId)
                .whereEqualTo("status", "CHECKED_IN")
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                // Fallback pencarian dengan memberCode
                snapshot = checkinsCollection
                    .whereEqualTo("memberCode", cleanId)
                    .whereEqualTo("status", "CHECKED_IN")
                    .limit(1)
                    .get()
                    .await()
            }

            if (!snapshot.isEmpty) {
                val doc = snapshot.documents[0]
                val checkin = documentToFirestoreCheckin(doc)
                Result.success(checkin)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memeriksa status check-in: ${e.message}"))
        }
    }

    suspend fun getTodayCheckins(branchId: String): Result<List<com.pws.primaragagym.domain.model.FirestoreCheckin>> {
        return try {
            val startOfDay = getStartOfDay()

            val snapshot = try {
                checkinsCollection
                    .whereGreaterThanOrEqualTo("checkInAt", startOfDay)
                    .get()
                    .await()
            } catch (_: Exception) {
                // Fallback ambil seluruh dokumen jika index/filter query gagal
                checkinsCollection.get().await()
            }

            val checkins = snapshot.documents.map { doc ->
                documentToFirestoreCheckin(doc)
            }.filter { checkin ->
                val date = checkin.checkInAt ?: checkin.createdAt
                val isToday = date != null && !date.before(startOfDay)
                val isBranchMatch = branchId.isBlank() || checkin.branchId.isBlank() || checkin.branchId == branchId
                isToday && isBranchMatch
            }.sortedByDescending { it.checkInAt ?: it.createdAt }

            Result.success(checkins)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat data check-in hari ini: ${e.message}"))
        }
    }

    suspend fun getTodayCheckinsCount(branchId: String): Result<Int> {
        return try {
            val result = getTodayCheckins(branchId)
            Result.success(result.getOrDefault(emptyList()).size)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghitung check-in: ${e.message}"))
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

            val dayNames = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")

            for (i in 0..6) {
                val cal = Calendar.getInstance().apply {
                    time = startDate
                    add(Calendar.DAY_OF_YEAR, i)
                }
                val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                result[dayNames[dayIndex.coerceIn(0, 6)]] = 0
            }

            val snapshot = try {
                checkinsCollection.whereGreaterThanOrEqualTo("checkInAt", startDate).get().await()
            } catch (_: Exception) {
                checkinsCollection.get().await()
            }

            snapshot.documents.forEach { doc ->
                val checkin = documentToFirestoreCheckin(doc)
                val date = checkin.checkInAt ?: checkin.createdAt
                val isBranchMatch = branchId.isBlank() || checkin.branchId.isBlank() || checkin.branchId == branchId
                if (date != null && !date.before(startDate) && isBranchMatch) {
                    val cal = Calendar.getInstance().apply { time = date }
                    val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                    val dayName = dayNames.getOrElse(dayIndex.coerceIn(0, 6)) { "Sen" }
                    result[dayName] = (result[dayName] ?: 0) + 1
                }
            }

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat data check-in mingguan: ${e.message}"))
        }
    }

    suspend fun getRecentActivities(branchId: String, limit: Int = 20): Result<List<com.pws.primaragagym.domain.model.FirestoreCheckin>> {
        return try {
            val startOfDay = getStartOfDay()

            val snapshot = try {
                checkinsCollection.get().await()
            } catch (e: Exception) {
                return Result.failure(Exception("Gagal memuat aktivitas terbaru: ${e.message}"))
            }

            val activities = snapshot.documents.map { doc ->
                documentToFirestoreCheckin(doc)
            }.filter { checkin ->
                val date = checkin.checkInAt ?: checkin.createdAt
                val isToday = date != null && !date.before(startOfDay)
                val isBranchMatch = branchId.isBlank() || checkin.branchId.isBlank() || checkin.branchId == branchId
                isToday && isBranchMatch
            }.sortedByDescending { it.checkInAt ?: it.createdAt }.take(limit)

            Result.success(activities)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat aktivitas terbaru: ${e.message}"))
        }
    }

    suspend fun isMemberCheckedIn(memberId: String): Result<Boolean> {
        return try {
            val cleanId = memberId.trim()
            var snapshot = checkinsCollection
                .whereEqualTo("memberId", cleanId)
                .whereEqualTo("status", "CHECKED_IN")
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                snapshot = checkinsCollection
                    .whereEqualTo("memberCode", cleanId)
                    .whereEqualTo("status", "CHECKED_IN")
                    .limit(1)
                    .get()
                    .await()
            }

            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memeriksa status check-in: ${e.message}"))
        }
    }

    suspend fun getMemberCheckinHistory(
        memberId: String,
        limit: Int = 50
    ): Result<List<com.pws.primaragagym.domain.model.FirestoreCheckin>> {
        return try {
            val cleanId = memberId.trim()
            var snapshot = checkinsCollection
                .whereEqualTo("memberId", cleanId)
                .get()
                .await()

            if (snapshot.isEmpty) {
                snapshot = checkinsCollection
                    .whereEqualTo("memberCode", cleanId)
                    .get()
                    .await()
            }

            val checkins = snapshot.documents.map { doc ->
                documentToFirestoreCheckin(doc)
            }.sortedByDescending { it.checkInAt ?: it.createdAt }.take(limit)

            Result.success(checkins)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat riwayat check-in: ${e.message}"))
        }
    }

    suspend fun getAllCheckins(
        branchId: String,
        limit: Int = 100
    ): Result<List<com.pws.primaragagym.domain.model.FirestoreCheckin>> {
        return try {
            val snapshot = try {
                checkinsCollection.get().await()
            } catch (e: Exception) {
                return Result.failure(Exception("Gagal memuat riwayat check-in: ${e.message}"))
            }

            val checkins = snapshot.documents.map { doc ->
                documentToFirestoreCheckin(doc)
            }.filter { checkin ->
                branchId.isBlank() || checkin.branchId.isBlank() || checkin.branchId == branchId
            }.sortedByDescending { it.checkInAt ?: it.createdAt }.take(limit)

            Result.success(checkins)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat riwayat check-in: ${e.message}"))
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
