package com.raveline.mail.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raveline.mail.R
import com.raveline.mail.model.EmailDataModel
import com.raveline.mail.samples.EmailDao
import com.raveline.mail.ui.navigation.contentEmailFullPath
import com.raveline.mail.ui.navigation.emailListRoute
import com.raveline.mail.ui.navigation.translateSettingsRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(emptyList()))
    var uiState = _uiState.asStateFlow()

    init {
        loadEmails()
    }

    private fun loadEmails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(emailDataModels = EmailDao().getEmails())
        }
    }

    fun changeCurrentDestination(route: String) {
        _uiState.value = _uiState.value.copy(
            showEmailsList = route == emailListRoute,
            currentDestination = route
        )
    }

    fun setSelectedEmail(emailDataModel: EmailDataModel) {
        _uiState.value = _uiState.value.copy(
            selectedEmailDataModel = emailDataModel
        )
    }

    fun getAppBarTitle(): Int {
        return when (uiState.value.currentDestination) {
            translateSettingsRoute -> R.string.language_settings
            contentEmailFullPath -> R.string.back
            else -> R.string.default_appbar_title
        }
    }
}