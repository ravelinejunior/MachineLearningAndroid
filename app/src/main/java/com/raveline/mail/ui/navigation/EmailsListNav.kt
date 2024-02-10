package com.raveline.mail.ui.navigation

import androidx.compose.foundation.lazy.LazyListState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.raveline.mail.model.EmailDataModel
import com.raveline.mail.ui.home.EmailsListScreen

internal const val emailListRoute = "emailList"

fun NavGraphBuilder.emailsListScreen(
    onOpenEmail: (EmailDataModel) -> Unit = {},
    listState: LazyListState
) {
    composable(emailListRoute) {
        EmailsListScreen(
            listState = listState,
            onClick = {
                onOpenEmail(it)
            }
        )
    }
}