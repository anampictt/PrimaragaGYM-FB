package com.pws.primaragagym.data.datasource

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.pws.primaragagym.domain.model.FirestoreNotification
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseNotificationDataSource {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val notificationsCollection = firestore.collection(FirestoreCollections.NOTIFICATIONS)

    /**
     * Helper manual mapping dari DocumentSnapshot ke FirestoreNotification
     * untuk menjamin notificationId selalu terisi doc.id dan isRead ter-parse dengan benar.
     */
    private fun documentToNotification(doc: DocumentSnapshot): FirestoreNotification {
        val data = doc.data ?: emptyMap<String, Any?>()
        val isRead = when (val raw = data["isRead"] ?: data["read"] ?: data["is_read"]) {
            is Boolean -> raw
            is String -> raw.equals("true", ignoreCase = true) || raw == "1"
            is Number -> raw.toInt() == 1
            else -> false
        }
        val type = (data["type"] as? String) ?: "SYSTEM"
        val title = (data["title"] as? String) ?: ""
        val message = (data["message"] as? String) ?: (data["body"] as? String) ?: ""
        val userId = data["userId"] as? String
        val memberId = data["memberId"] as? String
        val branchId = data["branchId"] as? String
        val createdAt = (data["createdAt"] as? Timestamp)?.toDate()
            ?: (data["createdAt"] as? Date)

        return FirestoreNotification(
            notificationId = doc.id,
            userId = userId,
            memberId = memberId,
            branchId = branchId,
            type = type,
            title = title,
            message = message,
            isRead = isRead,
            createdAt = createdAt
        )
    }

    /**
     * Real-time Flow notifikasi menggunakan Firestore snapshot listener.
     */
    fun observeNotifications(
        userId: String? = null,
        branchId: String? = null,
        limit: Int = 50
    ): Flow<List<FirestoreNotification>> = callbackFlow {
        var query: Query = notificationsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        if (!userId.isNullOrBlank()) query = query.whereEqualTo("userId", userId)
        if (!branchId.isNullOrBlank()) query = query.whereEqualTo("branchId", branchId)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                Log.e("NotifDataSource", "Error snapshot listener", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            val items = snapshot.documents.map { doc -> documentToNotification(doc) }
            trySend(items)
        }
        awaitClose { listener.remove() }
    }

    suspend fun getNotifications(
        userId: String? = null,
        branchId: String? = null,
        isRead: Boolean? = null,
        limit: Int = 50
    ): Result<List<FirestoreNotification>> {
        return try {
            var query: Query = notificationsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            if (!userId.isNullOrBlank()) {
                query = query.whereEqualTo("userId", userId)
            }

            if (!branchId.isNullOrBlank()) {
                query = query.whereEqualTo("branchId", branchId)
            }

            if (isRead != null) {
                query = query.whereEqualTo("isRead", isRead)
            }

            val snapshot = query.get().await()
            val notifications = snapshot.documents.map { doc -> documentToNotification(doc) }
            Result.success(notifications)
        } catch (e: Exception) {
            Log.e("NotifDataSource", "Gagal memuat notifikasi", e)
            Result.failure(Exception("Gagal memuat notifikasi. ${e.message}"))
        }
    }

    suspend fun getUnreadCount(userId: String? = null, branchId: String? = null): Result<Int> {
        return try {
            var query: Query = notificationsCollection

            if (!userId.isNullOrBlank()) {
                query = query.whereEqualTo("userId", userId)
            }

            if (!branchId.isNullOrBlank()) {
                query = query.whereEqualTo("branchId", branchId)
            }

            val snapshot = query.get().await()
            val unreadCount = snapshot.documents.count { doc ->
                val data = doc.data ?: emptyMap<String, Any?>()
                val isRead = (data["isRead"] as? Boolean) ?: (data["read"] as? Boolean) ?: false
                !isRead
            }
            Result.success(unreadCount)
        } catch (e: Exception) {
            Log.e("NotifDataSource", "Gagal menghitung unread notifikasi", e)
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
            val notification = FirestoreNotification(
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
            Log.e("NotifDataSource", "Gagal membuat notifikasi", e)
            Result.failure(Exception("Gagal membuat notifikasi. ${e.message}"))
        }
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            val trimmedId = notificationId.trim()
            if (trimmedId.isBlank()) return Result.failure(Exception("Notification ID kosong"))

            val updates = mapOf(
                "isRead" to true,
                "read" to true
            )
            notificationsCollection.document(trimmedId)
                .set(updates, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NotifDataSource", "Gagal markAsRead $notificationId", e)
            Result.failure(Exception("Gagal menandai notifikasi. ${e.message}"))
        }
    }

    suspend fun markMultipleAsRead(notificationIds: List<String>): Result<Unit> {
        return try {
            val validIds = notificationIds.map { it.trim() }.filter { it.isNotBlank() }
            if (validIds.isEmpty()) {
                Log.w("NotifDataSource", "markMultipleAsRead: tidak ada ID yang valid!")
                return Result.success(Unit)
            }

            val updates = mapOf(
                "isRead" to true,
                "read" to true
            )
            val chunks = validIds.chunked(500)
            for (chunk in chunks) {
                val batch = firestore.batch()
                for (id in chunk) {
                    val docRef = notificationsCollection.document(id)
                    batch.set(docRef, updates, SetOptions.merge())
                }
                batch.commit().await()
            }
            Log.d("NotifDataSource", "Berhasil markMultipleAsRead untuk ${validIds.size} notifikasi")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NotifDataSource", "Gagal markMultipleAsRead", e)
            Result.failure(Exception("Gagal menandai notifikasi. ${e.message}"))
        }
    }

    suspend fun markAllAsRead(userId: String? = null, branchId: String? = null): Result<Unit> {
        return try {
            var query: Query = notificationsCollection

            if (!userId.isNullOrBlank()) {
                query = query.whereEqualTo("userId", userId)
            }

            if (!branchId.isNullOrBlank()) {
                query = query.whereEqualTo("branchId", branchId)
            }

            val snapshot = query.get().await()
            val unreadDocs = snapshot.documents.filter { doc ->
                val data = doc.data ?: emptyMap<String, Any?>()
                val isRead = (data["isRead"] as? Boolean) ?: (data["read"] as? Boolean) ?: false
                !isRead
            }

            val updates = mapOf(
                "isRead" to true,
                "read" to true
            )

            if (unreadDocs.isNotEmpty()) {
                val chunks = unreadDocs.chunked(500)
                for (chunk in chunks) {
                    val batch = firestore.batch()
                    for (doc in chunk) {
                        batch.set(doc.reference, updates, SetOptions.merge())
                    }
                    batch.commit().await()
                }
            }

            Log.d("NotifDataSource", "Berhasil markAllAsRead untuk ${unreadDocs.size} notifikasi")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NotifDataSource", "Gagal markAllAsRead", e)
            Result.failure(Exception("Gagal menandai semua notifikasi. ${e.message}"))
        }
    }

    suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            notificationsCollection.document(notificationId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NotifDataSource", "Gagal menghapus notifikasi $notificationId", e)
            Result.failure(Exception("Gagal menghapus notifikasi. ${e.message}"))
        }
    }
}
