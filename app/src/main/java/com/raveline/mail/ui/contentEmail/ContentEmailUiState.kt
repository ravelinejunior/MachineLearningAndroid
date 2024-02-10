package com.raveline.mail.ui.contentEmail

import com.raveline.mail.model.EmailDataModel
import com.raveline.mail.model.LanguageDataModel

data class ContentEmailUiState(
    val selectedEmailDataModel: EmailDataModel? = null,
    val originalContent: String? = null,
    val originalSubject: String? = null,
    val localLanguageDataModel: LanguageDataModel? = null,
    val languageDataModelIdentified: LanguageDataModel? = null,
    val canBeTranslate: Boolean = false,
    val translatedState: TranslatedState = TranslatedState.NOT_TRANSLATED,
    val showDownloadLanguageDialog: Boolean = false,
    val showTranslateButton: Boolean = true,
)

enum class TranslatedState {
    TRANSLATED,
    NOT_TRANSLATED,
    TRANSLATING
}