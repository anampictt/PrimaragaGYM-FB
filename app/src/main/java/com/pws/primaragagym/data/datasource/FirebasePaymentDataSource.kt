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
        planName: String = "",
        proofUrl: String? = null,
        transactionType: String = "INCOME",
        category: String = "",
        notes: String = "",
        transactionDate: Date = Date(),
        customInvoiceNumber: String? = null,
        memberCode: String = ""
    ): Result<String> {
        return try {
            val invoiceNumber = if (!customInvoiceNumber.isNullOrBlank()) {
                customInvoiceNumber.trim()
            } else {
                generateInvoiceNumber()
            }

            val paymentData = mutableMapOf<String, Any>(
                "invoiceNumber" to invoiceNumber,
                "memberId" to memberId,
                "memberName" to memberName,
                "memberCode" to memberCode,
                "planName" to planName,
                "amount" to amount,
                "paymentMethod" to paymentMethod,
                "paymentType" to paymentType,
                "transactionType" to transactionType,
                "category" to category,
                "notes" to notes,
                "status" to "PAID",
                "paidAt" to transactionDate,
                "createdAt" to transactionDate,
                "updatedAt" to Date()
            )
            if (!proofUrl.isNullOrBlank()) {
                paymentData["proofUrl"] = proofUrl
            }
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
        limit: Int = 100,
        lastDocumentId: String? = null
    ): Result<List<com.pws.primaragagym.domain.model.FirestorePayment>> {
        return try {
            val snapshot = try {
                if (branchId.isNotBlank()) {
                    val bSnap = paymentsCollection.whereEqualTo("branchId", branchId).get().await()
                    if (bSnap.isEmpty) {
                        paymentsCollection.get().await()
                    } else bSnap
                } else {
                    paymentsCollection.get().await()
                }
            } catch (_: Exception) {
                paymentsCollection.get().await()
            }

            var payments = snapshot.documents.mapNotNull { doc ->
                doc.toObject(com.pws.primaragagym.domain.model.FirestorePayment::class.java)
            }.filter { payment ->
                branchId.isBlank() || payment.branchId.isBlank() || payment.branchId == branchId
            }

            if (startDate != null) {
                payments = payments.filter { (it.paidAt ?: it.createdAt) != null && (it.paidAt ?: it.createdAt)!!.time >= startDate.time }
            }
            if (endDate != null) {
                payments = payments.filter { (it.paidAt ?: it.createdAt) != null && (it.paidAt ?: it.createdAt)!!.time <= endDate.time }
            }
            payments = payments.sortedByDescending { it.paidAt ?: it.createdAt }.take(limit)
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
            val endOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            val snapshot = try {
                if (branchId.isNotBlank()) {
                    val bSnap = paymentsCollection.whereEqualTo("branchId", branchId).get().await()
                    if (bSnap.isEmpty) {
                        paymentsCollection.get().await()
                    } else bSnap
                } else {
                    paymentsCollection.get().await()
                }
            } catch (_: Exception) {
                paymentsCollection.get().await()
            }

            val totalRevenue = snapshot.documents.mapNotNull { doc ->
                doc.toObject(com.pws.primaragagym.domain.model.FirestorePayment::class.java)
            }.filter { payment ->
                val pDate = payment.paidAt ?: payment.createdAt
                val isToday = pDate != null && pDate.time >= startOfDay.time && pDate.time <= endOfDay.time
                val isIncome = payment.transactionType != "EXPENSE"
                val branchMatches = branchId.isBlank() || payment.branchId.isBlank() || payment.branchId == branchId
                isToday && isIncome && branchMatches
            }.sumOf { it.amount }

            Result.success(totalRevenue)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat pendapatan hari ini. ${e.message}"))
        }
    }

    suspend fun getTodayExpense(branchId: String): Result<Long> {
        return try {
            val startOfDay = getStartOfDay()
            val endOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            val snapshot = try {
                if (branchId.isNotBlank()) {
                    val bSnap = paymentsCollection.whereEqualTo("branchId", branchId).get().await()
                    if (bSnap.isEmpty) {
                        paymentsCollection.get().await()
                    } else bSnap
                } else {
                    paymentsCollection.get().await()
                }
            } catch (_: Exception) {
                paymentsCollection.get().await()
            }

            val totalExpense = snapshot.documents.mapNotNull { doc ->
                doc.toObject(com.pws.primaragagym.domain.model.FirestorePayment::class.java)
            }.filter { payment ->
                val pDate = payment.paidAt ?: payment.createdAt
                val isToday = pDate != null && pDate.time >= startOfDay.time && pDate.time <= endOfDay.time
                val isExpense = payment.transactionType == "EXPENSE"
                val branchMatches = branchId.isBlank() || payment.branchId.isBlank() || payment.branchId == branchId
                isToday && isExpense && branchMatches
            }.sumOf { it.amount }

            Result.success(totalExpense)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat pengeluaran hari ini. ${e.message}"))
        }
    }

    suspend fun getTodayRevenueByMethod(branchId: String): Result<Map<String, Long>> {
        return try {
            val startOfDay = getStartOfDay()
            val endOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            val snapshot = try {
                if (branchId.isNotBlank()) {
                    val bSnap = paymentsCollection.whereEqualTo("branchId", branchId).get().await()
                    if (bSnap.isEmpty) {
                        paymentsCollection.get().await()
                    } else bSnap
                } else {
                    paymentsCollection.get().await()
                }
            } catch (_: Exception) {
                paymentsCollection.get().await()
            }

            val revenueByMethod = mutableMapOf(
                "CASH" to 0L,
                "TRANSFER" to 0L,
                "QRIS" to 0L
            )

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(com.pws.primaragagym.domain.model.FirestorePayment::class.java)
            }.filter { payment ->
                val pDate = payment.paidAt ?: payment.createdAt
                val isToday = pDate != null && pDate.time >= startOfDay.time && pDate.time <= endOfDay.time
                val isIncome = payment.transactionType != "EXPENSE"
                val branchMatches = branchId.isBlank() || payment.branchId.isBlank() || payment.branchId == branchId
                isToday && isIncome && branchMatches
            }.forEach { payment ->
                val method = payment.paymentMethod.uppercase()
                val targetKey = when {
                    method.contains("CASH") -> "CASH"
                    method.contains("TRANSFER") -> "TRANSFER"
                    method.contains("QRIS") -> "QRIS"
                    else -> "CASH"
                }
                revenueByMethod[targetKey] = (revenueByMethod[targetKey] ?: 0L) + payment.amount
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
                    return Result.success(payment)
                }
            }

            // Fallback 1: cari by invoiceNumber
            val querySnap = paymentsCollection.whereEqualTo("invoiceNumber", paymentId).limit(1).get().await()
            if (!querySnap.isEmpty) {
                val payment = querySnap.documents[0].toObject(com.pws.primaragagym.domain.model.FirestorePayment::class.java)
                if (payment != null) {
                    return Result.success(payment)
                }
            }

            // Fallback 2: cari by memberId atau paymentId parsial
            val recentSnap = paymentsCollection.orderBy("createdAt", Query.Direction.DESCENDING).limit(60).get().await()
            val matched = recentSnap.documents.mapNotNull {
                it.toObject(com.pws.primaragagym.domain.model.FirestorePayment::class.java)
            }.firstOrNull { p ->
                p.invoiceNumber.equals(paymentId, ignoreCase = true) ||
                p.paymentId.equals(paymentId, ignoreCase = true) ||
                (p.invoiceNumber.isNotBlank() && paymentId.contains(p.invoiceNumber, ignoreCase = true)) ||
                (p.memberId.isNotBlank() && paymentId.contains(p.memberId, ignoreCase = true))
            }
            if (matched != null) {
                return Result.success(matched)
            }

            Result.failure(Exception("Invoice tidak ditemukan."))
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat detail invoice. ${e.message}"))
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
