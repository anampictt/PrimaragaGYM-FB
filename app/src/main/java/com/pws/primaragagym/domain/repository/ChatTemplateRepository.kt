package com.pws.primaragagym.domain.repository

import com.pws.primaragagym.domain.model.ChatTemplateCategory
import com.pws.primaragagym.domain.model.FirestoreChatTemplate
import kotlinx.coroutines.flow.Flow

interface ChatTemplateRepository {
    fun observeTemplates(): Flow<List<FirestoreChatTemplate>>
    suspend fun getTemplates(): Result<List<FirestoreChatTemplate>>
    suspend fun getTemplateById(id: String): Result<FirestoreChatTemplate>
    suspend fun getDefaultTemplate(category: ChatTemplateCategory): Result<FirestoreChatTemplate?>
    suspend fun createTemplate(template: FirestoreChatTemplate): Result<String>
    suspend fun updateTemplate(template: FirestoreChatTemplate): Result<Unit>
    suspend fun deleteTemplate(id: String): Result<Unit>
    suspend fun setDefaultTemplate(id: String, category: String): Result<Unit>
}
