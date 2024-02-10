package com.raveline.mail.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.raveline.mail.ui.settings.TranslateSettingsScreen

internal const val translateSettingsRoute = "translateSettings"

fun NavGraphBuilder.translateSettingsScreen() {
    composable(translateSettingsRoute) {
        TranslateSettingsScreen()
    }
}