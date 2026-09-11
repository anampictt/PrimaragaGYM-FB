package com.pws.primaragagym.screens.admin.member.card

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MemberCardPreviewUiState(
    val cardData: MemberCardData? = null,
    val isSavingImage: Boolean = false,
    val isSavingPdf: Boolean = false,
    val isGenerating: Boolean = false,
    val snackbarMessage: String? = null
)

sealed class MemberCardPreviewEvent {
    data class ImageSaved(val success: Boolean, val message: String) : MemberCardPreviewEvent()
    data class PdfSaved(val success: Boolean, val message: String) : MemberCardPreviewEvent()
    data class ShareImage(val bitmap: Bitmap, val fileName: String) : MemberCardPreviewEvent()
    data class SharePdf(val pdfBytes: ByteArray, val fileName: String) : MemberCardPreviewEvent()
    data object ClearSnackbar : MemberCardPreviewEvent()
}

class MemberCardPreviewViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MemberCardPreviewUiState())
    val uiState: StateFlow<MemberCardPreviewUiState> = _uiState.asStateFlow()

    fun setCardData(data: MemberCardData) {
        _uiState.value = _uiState.value.copy(cardData = data, isGenerating = false)
    }

    fun showSnackbar(message: String) {
        _uiState.value = _uiState.value.copy(snackbarMessage = message)
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }
}
