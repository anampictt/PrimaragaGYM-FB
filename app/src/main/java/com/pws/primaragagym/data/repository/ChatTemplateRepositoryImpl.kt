package com.pws.primaragagym.data.repository

import com.pws.primaragagym.data.datasource.FirebaseChatTemplateDataSource
import com.pws.primaragagym.domain.model.ChatTemplateCategory
import com.pws.primaragagym.domain.model.FirestoreChatTemplate
import com.pws.primaragagym.domain.repository.ChatTemplateRepository
import kotlinx.coroutines.flow.Flow

class ChatTemplateRepositoryImpl(
    private val dataSource: FirebaseChatTemplateDataSource = FirebaseChatTemplateDataSource()
) : ChatTemplateRepository {

    override fun observeTemplates(): Flow<List<FirestoreChatTemplate>> =
        dataSource.observeTemplates()

    override suspend fun getTemplates(): Result<List<FirestoreChatTemplate>> =
        dataSource.getTemplates()

    override suspend fun getTemplateById(id: String): Result<FirestoreChatTemplate> =
        dataSource.getTemplateById(id)

    override suspend fun getDefaultTemplate(category: ChatTemplateCategory): Result<FirestoreChatTemplate?> {
        val result = dataSource.getTemplates()
        return result.map { list ->
            list.find { it.category == category.code && it.isDefault }
                ?: list.find { it.category == category.code }
        }
    }

    override suspend fun createTemplate(template: FirestoreChatTemplate): Result<String> =
        dataSource.createTemplate(template)

    override suspend fun updateTemplate(template: FirestoreChatTemplate): Result<Unit> =
        dataSource.updateTemplate(template)

    override suspend fun deleteTemplate(id: String): Result<Unit> =
        dataSource.deleteTemplate(id)

    override suspend fun setDefaultTemplate(id: String, category: String): Result<Unit> =
        dataSource.setDefaultTemplate(id, category)
}
