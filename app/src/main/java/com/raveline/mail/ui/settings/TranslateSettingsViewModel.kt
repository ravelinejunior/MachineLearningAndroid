package com.raveline.mail.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raveline.mail.mlkit.TextTranslator
import com.raveline.mail.model.DownloadState
import com.raveline.mail.model.LanguageModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslateSettingsViewModel @Inject constructor(
    private val textTranslator: TextTranslator
) : ViewModel() {

    private val _uiState = MutableStateFlow(TranslateSettingsUiState())
    var uiState = _uiState.asStateFlow()

    init {
        loadLanguages()
    }


    internal fun loadLanguages() {
        viewModelScope.launch {
            delay(1000)
            _uiState.value = _uiState.value.copy(
                allLanguageModels = textTranslator.getAllModels()
            )
            updateLanguagesState()
            loadDownloadedLanguages()
        }
    }

    private fun updateLanguagesState() {
        val allModels = _uiState.value.allLanguageModels
        val notDownloadedModels = filterAllModelNotDownloaded()

        _uiState.value = _uiState.value.copy(
            notDownloadedLanguageModels = notDownloadedModels,
            loadModelsState = if (allModels.isNotEmpty() || notDownloadedModels.isNotEmpty()) {
                AppState.Loaded
            } else {
                AppState.Error
            }
        )
    }

    private fun filterAllModelNotDownloaded(): List<LanguageModel> {
        val downloadedModels = _uiState.value.downloadedLanguageModels

        return _uiState.value.allLanguageModels.filter { languageModel ->
            downloadedModels.none { it.id == languageModel.id }
        }
    }

    private fun loadDownloadedLanguages() {
        textTranslator.getDownloadedModels(
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    downloadedLanguageModels = it
                )

                updateLanguagesState()
            },
            onFailure = {
                if (_uiState.value.allLanguageModels.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        loadModelsState = AppState.Error
                    )
                }
                Log.e("TAG_loadDownloadedLanguages", it)
            }
        )
    }

    fun showDownloadDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(
            showDownloadLanguageDialog = show
        )
    }

    fun showDeleteDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(
            showDeleteLanguageDialog = show
        )
    }

    private fun updateDownloadState(languageModel: LanguageModel, downloadState: DownloadState) {
        val languages = _uiState.value.notDownloadedLanguageModels.toMutableList()
        val languageIndex = languages.indexOfFirst { it.id == languageModel.id }
        languages[languageIndex] = languageModel.copy(downloadState = downloadState)

        _uiState.value = _uiState.value.copy(
            notDownloadedLanguageModels = languages
        )
    }

    fun selectedLanguage(languageModel: LanguageModel) {
        _uiState.value = _uiState.value.copy(
            selectedLanguageModel = languageModel
        )
    }

    fun downloadLanguage(languageModel: LanguageModel) {
        updateDownloadState(
            languageModel = languageModel,
            downloadState = DownloadState.DOWNLOADING
        )

        textTranslator.downloadModel(
            modelName = languageModel.id,
            onSuccess = {
                loadLanguages()
            },
            onFailure = {
                updateDownloadState(
                    languageModel = languageModel,
                    downloadState = DownloadState.NOT_DOWNLOADED
                )
                Log.e("TAG_downloadLanguage", "downloadLanguage: $it")
            }
        )
    }

    fun removeLanguage(languageModel: LanguageModel) {
        textTranslator.removeModel(
            modelName = languageModel.id,
            onSuccess = {
                loadLanguages()
            },
            onFailure = {
                updateDownloadState(
                    languageModel = languageModel,
                    downloadState = DownloadState.DOWNLOADED
                )

                Log.e("TAG_removeLanguage", "downloadLanguage: $it")
            }
        )
    }

    fun cleanState() {
        _uiState.value = TranslateSettingsUiState()
    }
}