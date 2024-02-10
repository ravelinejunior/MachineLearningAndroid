package com.raveline.mail.ui.settings

import com.raveline.mail.model.LanguageModel

data class TranslateSettingsUiState(
    val allLanguageModels: List<LanguageModel> = emptyList(),
    val downloadedLanguageModels: List<LanguageModel> = emptyList(),
    val notDownloadedLanguageModels: List<LanguageModel> = emptyList(),
    val showDownloadLanguageDialog: Boolean = false,
    val showDeleteLanguageDialog: Boolean = false,
    val selectedLanguageModel: LanguageModel? = null,
    val loadModelsState: AppState = AppState.Loading
)

sealed class AppState {
    data object Loading : AppState()
    data object Loaded : AppState()
    data object Error : AppState()
}
