package com.pws.primaragagym.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pws.primaragagym.data.repository.ChatTemplateRepositoryImpl
import com.pws.primaragagym.domain.model.ChatTemplateCategory
import com.pws.primaragagym.domain.model.FirestoreChatTemplate
import com.pws.primaragagym.domain.model.defaultChatTemplates
import com.pws.primaragagym.domain.repository.ChatTemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatTemplateUiState(
    val isLoading: Boolean = false,
    val templates: List<FirestoreChatTemplate> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

class ChatTemplateViewModel(
    private val repository: ChatTemplateRepository = ChatTemplateRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatTemplateUiState(templates = defaultChatTemplates))
    val uiState: StateFlow<ChatTemplateUiState> = _uiState.asStateFlow()

    init {
        loadTemplates()
    }

    fun loadTemplates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getTemplates()
            result.fold(
                onSuccess = { templates ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            templates = if (templates.isNotEmpty()) templates else defaultChatTemplates
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = err.message,
                            templates = if (it.templates.isEmpty()) defaultChatTemplates else it.templates
                        )
                    }
                }
            )
        }
    }

    fun saveTemplate(
        id: String?,
        title: String,
        message: String,
        category: ChatTemplateCategory,
        isDefault: Boolean,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val template = FirestoreChatTemplate(
                id = id ?: "",
                title = title.trim(),
                message = message.trim(),
                category = category.code,
                isDefault = isDefault
            )

            val result = if (id.isNullOrBlank()) {
                repository.createTemplate(template)
            } else {
                repository.updateTemplate(template).map { id }
            }

            result.fold(
                onSuccess = {
                    loadTemplates()
                    onComplete(true, null)
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.message) }
                    onComplete(false, err.message)
                }
            )
        }
    }

    fun deleteTemplate(id: String, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.deleteTemplate(id)
            result.fold(
                onSuccess = {
                    loadTemplates()
                    onComplete?.invoke(true)
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.message) }
                    onComplete?.invoke(false)
                }
            )
        }
    }

    fun setDefaultTemplate(id: String, category: String) {
        viewModelScope.launch {
            val result = repository.setDefaultTemplate(id, category)
            if (result.isSuccess) {
                loadTemplates()
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
