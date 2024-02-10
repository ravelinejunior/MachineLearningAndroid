package com.raveline.mail.ui.home

import com.raveline.mail.model.EmailDataModel

data class HomeUiState(
    val emailDataModels: List<EmailDataModel>,
    val showEmailsList: Boolean = true,
    val selectedEmailDataModel: EmailDataModel? = null,
    val currentDestination: String = ""
)