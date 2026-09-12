package com.pws.primaragagym.data.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseNotificationDataSource {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val notificationsCollection = firestore.collection(FirestoreCollections.NOTIFICATIONS)

    suspend fun getNotifications(
        userId: String? = null,
        branchId: String? = null,
        isRead: Boolean? = null,
        limit: Int = 50
    ): Result<List<com.pws.primaragagym.domain.model.FirestoreNotification>> {
        return try {
            var query: Query = notificationsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            if (userId != null) {
                query = query.whereEqualTo("userId", userId)
            }

            if (branchId != null) {
                query = query.whereEqualTo("branchId", branchId)
            }

            if (isRead != null) {
                query = query.whereEqualTo("isRead", isRead)
            }

            val snapshot = query.get().await()
            val notifications = snapshot.documents.mapNotNull { doc ->
                doc.toObject(com.pws.primaragagym.domain.model.FirestoreNotification::class.java)
            }
            Result.success(notifications)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat notifikasi. ${e.message}"))
        }
    }

    suspend fun getUnreadCount(userId: String? = null, branchId: String? = null): Result<Int> {
        return try {
            var query: Query = notificationsCollection
                .whereEqualTo("isRead", false)

            if (userId != null) {
                query = query.whereEqualTo("userId", userId)
            }

            if (branchId != null) {
                query = query.whereEqualTo("branchId", branchId)
            }

            val snapshot = query.get().await()
            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghitung notifikasi. ${e.message}"))
        }
    }

    suspend fun createNotification(
        userId: String? = null,
        memberId: String? = null,
        branchId: String? = null,
        type: String,
        title: String,
        message: String
    ): Result<String> {
        return try {
            val notification = com.pws.primaragagym.domain.model.FirestoreNotification(
                userId = userId,
                memberId = memberId,
                branchId = branchId,
                type = type,
                title = title,
                message = message,
                isRead = false,
                createdAt = Date()
            )

            val docRef = notificationsCollection.document()
            docRef.set(notification).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal membuat notifikasi. ${e.message}"))
        }
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            notificationsCollection.document(notificationId)
                .update(mapOf("isRead" to true)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menandai notifikasi. ${e.message}"))
        }
    }

    suspend fun markAllAsRead(userId: String? = null, branchId: String? = null): Result<Unit> {
        return try {
            var query: Query = notificationsCollection.whereEqualTo("isRead", false)

            if (userId != null) {
                query = query.whereEqualTo("userId", userId)
            }

            if (branchId != null) {
                query = query.whereEqualTo("branchId", branchId)
            }

            val snapshot = query.get().await()

            snapshot.documents.forEach { doc ->
                doc.reference.update(mapOf("isRead" to true)).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menandai semua notifikasi. ${e.message}"))
        }
    }

    suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            notificationsCollection.document(notificationId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghapus notifikasi. ${e.message}"))
        }
    }
}
