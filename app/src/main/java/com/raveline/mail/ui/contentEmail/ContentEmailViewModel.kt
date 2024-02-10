/**
 * ViewModel class for managing the content of an email.
 * This class is responsible for loading the email, identifying the email language,
 * identifying the local language, translating the email if necessary, and managing the UI state.
 *
 * @property textTranslator The text translator used for language detection and translation.
 * @property savedStateHandle The saved state handle for retrieving the email ID.
 * @property emailDao The email data access object for retrieving email data.
 * @property emailId The ID of the email being displayed.
 * @property _uiState The mutable state flow representing the UI state of the content email screen.
 * @property uiState The immutable state flow representing the UI state of the content email screen.
 * @property _errorMessageState The mutable state flow representing the error message state.
 * @property errorMessageState The immutable state flow representing the error message state.
 */

/*
 * Copyright (c) 2024. Copyright from Junior Raveline
 */

package com.raveline.mail.ui.contentEmail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raveline.mail.mlkit.TextTranslator
import com.raveline.mail.model.LanguageDataModel
import com.raveline.mail.samples.EmailDao
import com.raveline.mail.ui.navigation.emailIdArgument
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ContentEmailViewModel @Inject constructor(
    private val textTranslator: TextTranslator,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val emailDao = EmailDao()

    private val emailId: String = checkNotNull(savedStateHandle[emailIdArgument])

    private val _uiState = MutableStateFlow(ContentEmailUiState())
    var uiState = _uiState.asStateFlow()

    private val _errorMessageState = MutableStateFlow(Pair<Boolean, String?>(false, null))
    val errorMessageState = _errorMessageState.asStateFlow()

    init {
        loadEmail()
    }

    private fun loadEmail() {
        viewModelScope.launch {
            val email = emailDao.getEmailById(emailId)
            _uiState.value = _uiState.value.copy(
                selectedEmailDataModel = email,
                originalContent = email?.content,
                originalSubject = email?.subject
            )
            identifyEmailLanguage()
            identifyLocalLanguage()
        }
    }

    private fun identifyEmailLanguage() {
        _uiState.value.selectedEmailDataModel?.let { email ->
            textTranslator.getDetectedLanguage(
                text = email.content,
                onSuccess = { languageDataModel ->
                    _uiState.value = _uiState.value.copy(
                        languageDataModelIdentified = languageDataModel
                    )
                    verifyIfNeedTranslate()
                },
                onFailure = {
                    _errorMessageState.value = Pair(true, it)
                    viewModelScope.launch {
                        delay(1000)
                        _errorMessageState.value = Pair(false, null)
                    }
                    Log.e("TAG_identifyEmailLanguage", "Error identifying email language: $it")
                }
            )
        }
    }

    private fun identifyLocalLanguage() {
        val languageCode = Locale.getDefault().language
        val languageName = Locale.getDefault().displayLanguage

        _uiState.value = _uiState.value.copy(
            localLanguageDataModel = LanguageDataModel(
                code = languageCode,
                name = languageName
            )
        )
        verifyIfNeedTranslate()
        downloadDefaultLanguageModel()
    }

    private fun downloadDefaultLanguageModel() {
        val currentLanguage = _uiState.value.localLanguageDataModel?.code.toString()

        textTranslator.downloadModel(
            modelName = currentLanguage,
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    showDownloadLanguageDialog = false
                )
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(
                    translatedState = TranslatedState.NOT_TRANSLATED
                )
                _errorMessageState.value = Pair(true, it)
                Log.e("TAG_downloadDefaultLanguageModel", "Error downloading language model: $it")
            }
        )
    }

    private fun verifyIfNeedTranslate() {
        val languageIdentified = _uiState.value.languageDataModelIdentified
        val localLanguage = _uiState.value.localLanguageDataModel

        if (languageIdentified != null && localLanguage != null) {
            if (languageIdentified.code != localLanguage.code) {
                _uiState.value = _uiState.value.copy(
                    canBeTranslate = true
                )
            }
        }
    }

    fun tryTranslateEmail() {
        with(_uiState.value) {
            if (translatedState == TranslatedState.TRANSLATED) {
                selectedEmailDataModel?.let { email ->
                    _uiState.value = _uiState.value.copy(
                        selectedEmailDataModel = email.copy(
                            content = originalContent.toString(),
                            subject = originalSubject.toString()
                        ),
                        translatedState = TranslatedState.NOT_TRANSLATED
                    )
                }
            } else {
                val languageIdentified = _uiState.value.languageDataModelIdentified?.code ?: return

                textTranslator.verifyDownloadModel(
                    modelCode = languageIdentified,
                    onSuccess = {
                        translateEmail()
                    },
                    onFailure = {

                        it?.let {
                            _errorMessageState.value = Pair(true, it)
                        }
                        _uiState.value = _uiState.value.copy(
                            showDownloadLanguageDialog = true
                        )
                    }
                )

                setTranslateState(TranslatedState.TRANSLATING)
            }
        }
    }

    fun setTranslateState(state: TranslatedState) {
        _uiState.value = _uiState.value.copy(
            translatedState = state
        )
    }

    private fun translateEmail() {
        with(_uiState.value) {
            selectedEmailDataModel?.let { email ->
                languageDataModelIdentified?.let { languageIdentified ->
                    localLanguageDataModel?.let { localLanguage ->
                        textTranslator.translateText(
                            text = email.content,
                            sourceLanguage = languageIdentified.code,
                            targetLanguage = localLanguage.code,
                            onSuccess = { _, translatedText ->
                                _uiState.value = _uiState.value.copy(
                                    selectedEmailDataModel = email.copy(content = translatedText),
                                    translatedState = TranslatedState.TRANSLATED
                                )

                                translateSubject()
                            },
                            onFailure = {
                                _uiState.value = _uiState.value.copy(
                                    translatedState = TranslatedState.NOT_TRANSLATED
                                )
                                Log.e("TAG_translateEmail", it)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun translateSubject() {
        with(_uiState.value) {
            selectedEmailDataModel?.let { email ->
                languageDataModelIdentified?.let { languageIdentified ->
                    localLanguageDataModel?.let { localLanguage ->
                        textTranslator.translateText(
                            text = email.subject,
                            sourceLanguage = languageIdentified.code,
                            targetLanguage = localLanguage.code,
                            onSuccess = { _, translatedText ->
                                _uiState.value = _uiState.value.copy(
                                    selectedEmailDataModel = selectedEmailDataModel.copy(
                                        subject = translatedText
                                    ),
                                    translatedState = TranslatedState.TRANSLATED
                                )

                            },
                            onFailure = {
                                _uiState.value = _uiState.value.copy(
                                    translatedState = TranslatedState.NOT_TRANSLATED
                                )
                                Log.e("TAG_translateEmail", it)
                            }
                        )
                    }
                }
            }
        }
    }

    fun downloadLanguageModel() {
        val languageIdentified = _uiState.value.languageDataModelIdentified?.code ?: return
        textTranslator.downloadModel(
            modelName = languageIdentified,
            onSuccess = {
                translateEmail()
                Log.i(
                    "TAG_downloadDefaultLanguageModel",
                    "Model downloaded successfully! $languageIdentified"
                )
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(
                    translatedState = TranslatedState.NOT_TRANSLATED
                )
                Log.e("TAG_downloadDefaultLanguageModel", "Error downloading language model: $it")
            }
        )

    }

    fun showDownloadLanguageDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(
            showDownloadLanguageDialog = show
        )
    }

    fun hideTranslateButton() {
        _uiState.value = _uiState.value.copy(
            showTranslateButton = false
        )
    }
}