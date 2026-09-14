package com.pws.primaragagym.data.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FirebasePaymentDataSource {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val paymentsCollection = firestore.collection(FirestoreCollections.PAYMENTS)

    suspend fun createPayment(
        memberId: String,
        memberName: String,
        membershipId: String?,
        branchId: String,
        amount: Long,
        paymentMethod: String,
        paymentType: String,
        planName: String = ""
    ): Result<String> {
        return try {
            val invoiceNumber = generateInvoiceNumber()

            val paymentData = mutableMapOf<String, Any>(
                "invoiceNumber" to invoiceNumber,
                "memberId" to memberId,
                "memberName" to memberName,
                "planName" to planName,
                "amount" to amount,
                "paymentMethod" to paymentMethod,
                "paymentType" to paymentType,
                "status" to "PAID",
                "paidAt" to Date(),
                "createdAt" to Date(),
                "updatedAt" to Date()
            )
            if (!branchId.isNullOrBlank()) {
                paymentData["branchId"] = branchId.trim()
            }
            if (!membershipId.isNullOrBlank()) {
                paymentData["membershipId"] = membershipId.trim()
            }

            val docRef = paymentsCollection.document()
            docRef.set(paymentData).await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal membuat pembayaran. ${e.message}"))
        }
    }

    suspend fun getPaymentsByBranch(
        branchId: String,
        startDate: Date? = null,
        endDate: Date? = null,
        limit: Int = 50,
        lastDocumentId: String? = null
    ): Result<List<com.pws.primaragagym.domain.model.FirestorePayment>> {
        return try {
            var query: Query = paymentsCollection
                .whereEqualTo("branchId", branchId)
                .orderBy("paidAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            if (startDate != null) {
                query = query.whereGreaterThanOrEqualTo("paidAt", startDate)
            }

            if (endDate != null) {
                query = query.whereLessThan("paidAt", endDate)
            }

            if (lastDocumentId != null) {
                val lastDoc = paymentsCollection.document(lastDocumentId).get().await()
                if (lastDoc.exists()) {
                    query = query.startAfter(lastDoc)
                }
            }

            val snapshot = query.get().await()
            val payments = snapshot.documents.mapNotNull { doc ->
                doc.toObject(com.pws.primaragagym.domain.model.FirestorePayment::class.java)
            }
            Result.success(payments)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat data pembayaran. ${e.message}"))
        }
    }

    suspend fun getPaymentsByMember(memberId: String): Result<List<com.pws.primaragagym.domain.model.FirestorePayment>> {
        return try {
            val snapshot = try {
                paymentsCollection
                    .whereEqualTo("memberId", memberId)
                    .orderBy("paidAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
            } catch (_: Exception) {
                paymentsCollection
                    .whereEqualTo("memberId", memberId)
                    .get()
                    .await()
            }

            val payments = snapshot.documents.mapNotNull { doc ->
                doc.toObject(com.pws.primaragagym.domain.model.FirestorePayment::class.java)
            }.sortedByDescending { it.paidAt ?: it.createdAt }

            Result.success(payments)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat riwayat pembayaran. ${e.message}"))
        }
    }

    suspend fun getTodayRevenue(branchId: String): Result<Long> {
        return try {
            val startOfDay = getStartOfDay()

            val snapshot = paymentsCollection
                .whereEqualTo("branchId", branchId)
                .whereEqualTo("status", "PAID")
                .whereGreaterThanOrEqualTo("paidAt", startOfDay)
                .get()
                .await()

            val totalRevenue = snapshot.documents
                .mapNotNull { it.getLong("amount") ?: 0L }
                .sum()

            Result.success(totalRevenue)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat pendapatan hari ini. ${e.message}"))
        }
    }

    suspend fun getTodayRevenueByMethod(branchId: String): Result<Map<String, Long>> {
        return try {
            val startOfDay = getStartOfDay()

            val snapshot = paymentsCollection
                .whereEqualTo("branchId", branchId)
                .whereEqualTo("status", "PAID")
                .whereGreaterThanOrEqualTo("paidAt", startOfDay)
                .get()
                .await()

            val revenueByMethod = mutableMapOf(
                "CASH" to 0L,
                "TRANSFER" to 0L,
                "QRIS" to 0L
            )

            snapshot.documents.forEach { doc ->
                val method = doc.getString("paymentMethod") ?: "CASH"
                val amount = doc.getLong("amount") ?: 0L
                revenueByMethod[method] = (revenueByMethod[method] ?: 0L) + amount
            }

            Result.success(revenueByMethod)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat pendapatan per metode. ${e.message}"))
        }
    }

    suspend fun getPaymentById(paymentId: String): Result<com.pws.primaragagym.domain.model.FirestorePayment> {
        return try {
            val doc = paymentsCollection.document(paymentId).get().await()
            if (doc.exists()) {
                val payment = doc.toObject(com.pws.primaragagym.domain.model.FirestorePayment::class.java)
                if (payment != null) {
                    Result.success(payment)
                } else {
                    Result.failure(Exception("Pembayaran tidak ditemukan."))
                }
            } else {
                Result.failure(Exception("Pembayaran tidak ditemukan."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat detail pembayaran. ${e.message}"))
        }
    }

    suspend fun cancelPayment(paymentId: String): Result<Unit> {
        return try {
            paymentsCollection.document(paymentId)
                .update(mapOf(
                    "status" to "CANCELLED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal membatalkan pembayaran. ${e.message}"))
        }
    }

    private suspend fun generateInvoiceNumber(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val dateStr = dateFormat.format(Date())
        val prefix = "INV-$dateStr-"

        return try {
            val snapshot = paymentsCollection
                .whereGreaterThanOrEqualTo("invoiceNumber", prefix)
                .orderBy("invoiceNumber", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                "${prefix}000001"
            } else {
                val lastNumber = snapshot.documents[0].getString("invoiceNumber")
                    ?.removePrefix(prefix)
                    ?.toIntOrNull() ?: 0
                String.format("${prefix}%06d", lastNumber + 1)
            }
        } catch (e: Exception) {
            "${prefix}${System.currentTimeMillis()}"
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
