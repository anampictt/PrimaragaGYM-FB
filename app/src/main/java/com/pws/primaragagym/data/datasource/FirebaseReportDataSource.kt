package com.pws.primaragagym.data.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FirebaseReportDataSource {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val reportsCollection = firestore.collection(FirestoreCollections.REPORTS)

    suspend fun getDailyReport(branchId: String, date: Date): Result<com.pws.primaragagym.domain.model.FirestoreDailyReport?> {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = dateFormat.format(date)
            val reportId = "daily_${dateStr}_$branchId"

            val doc = reportsCollection.document(reportId).get().await()
            if (doc.exists()) {
                val report = doc.toObject(com.pws.primaragagym.domain.model.FirestoreDailyReport::class.java)
                Result.success(report)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat laporan harian. ${e.message}"))
        }
    }

    suspend fun getMonthlyReport(branchId: String, year: Int, month: Int): Result<com.pws.primaragagym.domain.model.FirestoreMonthlyReport?> {
        return try {
            val monthStr = String.format("%04d-%02d", year, month)
            val reportId = "monthly_${monthStr}_$branchId"

            val doc = reportsCollection.document(reportId).get().await()
            if (doc.exists()) {
                val report = doc.toObject(com.pws.primaragagym.domain.model.FirestoreMonthlyReport::class.java)
                Result.success(report)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat laporan bulanan. ${e.message}"))
        }
    }

    suspend fun updateDailyReport(
        branchId: String,
        date: Date,
        totalActiveMembers: Int,
        newMembers: Int,
        totalCheckins: Int,
        totalRevenue: Long,
        cashRevenue: Long,
        transferRevenue: Long,
        qrisRevenue: Long
    ): Result<Unit> {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = dateFormat.format(date)
            val reportId = "daily_${dateStr}_$branchId"

            val report = com.pws.primaragagym.domain.model.FirestoreDailyReport(
                reportId = reportId,
                date = dateStr,
                branchId = branchId,
                totalActiveMembers = totalActiveMembers,
                newMembers = newMembers,
                totalCheckins = totalCheckins,
                totalRevenue = totalRevenue,
                cashRevenue = cashRevenue,
                transferRevenue = transferRevenue,
                qrisRevenue = qrisRevenue,
                updatedAt = Date()
            )

            reportsCollection.document(reportId).set(report).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memperbarui laporan harian. ${e.message}"))
        }
    }

    suspend fun updateMonthlyReport(
        branchId: String,
        year: Int,
        month: Int,
        totalNewMembers: Int,
        totalTransactions: Int,
        totalRevenue: Long,
        cashRevenue: Long,
        transferRevenue: Long,
        qrisRevenue: Long
    ): Result<Unit> {
        return try {
            val monthStr = String.format("%04d-%02d", year, month)
            val reportId = "monthly_${monthStr}_$branchId"

            val report = com.pws.primaragagym.domain.model.FirestoreMonthlyReport(
                reportId = reportId,
                month = monthStr,
                branchId = branchId,
                totalNewMembers = totalNewMembers,
                totalTransactions = totalTransactions,
                totalRevenue = totalRevenue,
                cashRevenue = cashRevenue,
                transferRevenue = transferRevenue,
                qrisRevenue = qrisRevenue,
                updatedAt = Date()
            )

            reportsCollection.document(reportId).set(report).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memperbarui laporan bulanan. ${e.message}"))
        }
    }

    suspend fun refreshDailyReport(
        branchId: String,
        date: Date
    ): Result<com.pws.primaragagym.domain.model.FirestoreDailyReport> {
        return try {
            // Calculate actual data from collections
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = dateFormat.format(date)

            // Get start and end of day
            val calendar = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.time

            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val endOfDay = calendar.time

            // Count active members
            val membersSnapshot = firestore.collection(FirestoreCollections.MEMBERS)
                .whereEqualTo("branchId", branchId)
                .whereEqualTo("status", "ACTIVE")
                .get()
                .await()
            val activeMembers = membersSnapshot.size()

            // Count new members today
            val newMembersSnapshot = firestore.collection(FirestoreCollections.MEMBERS)
                .whereEqualTo("branchId", branchId)
                .whereGreaterThanOrEqualTo("joinedAt", startOfDay)
                .whereLessThan("joinedAt", endOfDay)
                .get()
                .await()
            val newMembers = newMembersSnapshot.size()

            // Count today's check-ins
            val checkinsSnapshot = firestore.collection(FirestoreCollections.CHECKINS)
                .whereEqualTo("branchId", branchId)
                .whereGreaterThanOrEqualTo("checkInAt", startOfDay)
                .get()
                .await()
            val totalCheckins = checkinsSnapshot.size()

            // Calculate today's revenue by method
            val paymentsSnapshot = firestore.collection(FirestoreCollections.PAYMENTS)
                .whereEqualTo("branchId", branchId)
                .whereEqualTo("status", "PAID")
                .whereGreaterThanOrEqualTo("paidAt", startOfDay)
                .whereLessThan("paidAt", endOfDay)
                .get()
                .await()

            var cashRevenue = 0L
            var transferRevenue = 0L
            var qrisRevenue = 0L

            paymentsSnapshot.documents.forEach { doc ->
                val method = doc.getString("paymentMethod") ?: "CASH"
                val amount = doc.getLong("amount") ?: 0L
                when (method.uppercase()) {
                    "CASH" -> cashRevenue += amount
                    "TRANSFER" -> transferRevenue += amount
                    "QRIS" -> qrisRevenue += amount
                }
            }

            val totalRevenue = cashRevenue + transferRevenue + qrisRevenue

            // Update the report
            updateDailyReport(
                branchId = branchId,
                date = date,
                totalActiveMembers = activeMembers,
                newMembers = newMembers,
                totalCheckins = totalCheckins,
                totalRevenue = totalRevenue,
                cashRevenue = cashRevenue,
                transferRevenue = transferRevenue,
                qrisRevenue = qrisRevenue
            )

            val reportId = "daily_${dateStr}_$branchId"
            val doc = reportsCollection.document(reportId).get().await()
            val report = doc.toObject(com.pws.primaragagym.domain.model.FirestoreDailyReport::class.java)
                ?: throw Exception("Failed to create report")

            Result.success(report)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menyegarkan laporan. ${e.message}"))
        }
    }
}
