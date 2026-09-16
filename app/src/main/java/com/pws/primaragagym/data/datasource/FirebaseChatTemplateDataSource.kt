package com.pws.primaragagym.data.datasource

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pws.primaragagym.domain.model.FirestoreChatTemplate
import com.pws.primaragagym.domain.model.defaultChatTemplates
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseChatTemplateDataSource {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val templatesCollection = firestore.collection(FirestoreCollections.CHAT_TEMPLATES)

    private fun documentToChatTemplate(doc: DocumentSnapshot): FirestoreChatTemplate {
        val data = doc.data ?: emptyMap<String, Any?>()
        val title = data["title"] as? String ?: ""
        val message = data["message"] as? String ?: ""
        val category = data["category"] as? String ?: "GENERAL"
        val isDefault = when (val def = data["isDefault"]) {
            is Boolean -> def
            is Number -> def.toInt() == 1
            is String -> def.equals("true", ignoreCase = true)
            else -> false
        }
        val createdAt = when (val c = data["createdAt"]) {
            is com.google.firebase.Timestamp -> c.toDate()
            is Date -> c
            is Number -> Date(c.toLong())
            else -> null
        }
        val updatedAt = when (val u = data["updatedAt"]) {
            is com.google.firebase.Timestamp -> u.toDate()
            is Date -> u
            is Number -> Date(u.toLong())
            else -> null
        }

        return FirestoreChatTemplate(
            id = doc.id,
            title = title,
            message = message,
            category = category,
            isDefault = isDefault,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun observeTemplates(): Flow<List<FirestoreChatTemplate>> = callbackFlow {
        val listener = templatesCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val templates = snapshot.documents.map { documentToChatTemplate(it) }
                    trySend(templates)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun getTemplates(): Result<List<FirestoreChatTemplate>> {
        return try {
            val snapshot = try {
                templatesCollection.orderBy("createdAt", Query.Direction.DESCENDING).get().await()
            } catch (e: Exception) {
                templatesCollection.get().await()
            }

            if (snapshot.isEmpty) {
                // Auto seed default templates if empty
                seedDefaultTemplates()
                val refreshed = templatesCollection.get().await()
                Result.success(refreshed.documents.map { documentToChatTemplate(it) })
            } else {
                Result.success(snapshot.documents.map { documentToChatTemplate(it) })
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat template chat: ${e.message}"))
        }
    }

    suspend fun getTemplateById(id: String): Result<FirestoreChatTemplate> {
        return try {
            val doc = templatesCollection.document(id).get().await()
            if (doc.exists()) {
                Result.success(documentToChatTemplate(doc))
            } else {
                val fallback = defaultChatTemplates.find { it.id == id }
                if (fallback != null) {
                    Result.success(fallback)
                } else {
                    Result.failure(Exception("Template tidak ditemukan"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTemplate(template: FirestoreChatTemplate): Result<String> {
        return try {
            val data = hashMapOf(
                "title" to template.title,
                "message" to template.message,
                "category" to template.category,
                "isDefault" to template.isDefault,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            val docRef = templatesCollection.add(data).await()
            val newId = docRef.id

            if (template.isDefault) {
                unsetOtherDefaults(newId, template.category)
            }

            Result.success(newId)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menyimpan template: ${e.message}"))
        }
    }

    suspend fun updateTemplate(template: FirestoreChatTemplate): Result<Unit> {
        return try {
            val data = hashMapOf(
                "title" to template.title,
                "message" to template.message,
                "category" to template.category,
                "isDefault" to template.isDefault,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            templatesCollection.document(template.id).update(data as Map<String, Any>).await()

            if (template.isDefault) {
                unsetOtherDefaults(template.id, template.category)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memperbarui template: ${e.message}"))
        }
    }

    suspend fun deleteTemplate(id: String): Result<Unit> {
        return try {
            templatesCollection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghapus template: ${e.message}"))
        }
    }

    suspend fun setDefaultTemplate(id: String, category: String): Result<Unit> {
        return try {
            unsetOtherDefaults(id, category)
            templatesCollection.document(id).update(
                mapOf(
                    "isDefault" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal mengatur template default: ${e.message}"))
        }
    }

    private suspend fun unsetOtherDefaults(exceptId: String, category: String) {
        try {
            val others = templatesCollection
                .whereEqualTo("category", category)
                .whereEqualTo("isDefault", true)
                .get()
                .await()

            for (doc in others.documents) {
                if (doc.id != exceptId) {
                    templatesCollection.document(doc.id).update("isDefault", false).await()
                }
            }
        } catch (e: Exception) {
            // Non-fatal if index not yet ready
        }
    }

    private suspend fun seedDefaultTemplates() {
        try {
            for (template in defaultChatTemplates) {
                val data = hashMapOf(
                    "title" to template.title,
                    "message" to template.message,
                    "category" to template.category,
                    "isDefault" to template.isDefault,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                templatesCollection.document(template.id).set(data).await()
            }
        } catch (e: Exception) {
            // Ignore seeding errors
        }
    }
}
