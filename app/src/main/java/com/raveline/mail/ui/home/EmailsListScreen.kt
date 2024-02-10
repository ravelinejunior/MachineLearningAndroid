package com.raveline.mail.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raveline.mail.extensions.toFormattedDate
import com.raveline.mail.model.EmailDataModel
import com.raveline.mail.samples.EmailDao


@Composable
fun EmailsListScreen(
    onClick: (EmailDataModel) -> Unit,
    listState: LazyListState
) {
    val homeViewModel = hiltViewModel<HomeViewModel>()
    val state by homeViewModel.uiState.collectAsState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
    ) {
        items(state.emailDataModels) { email ->
            EmailItem(email) { onClick(email) }
        }
    }
}


@Composable
fun EmailItem(
    emailDataModel: EmailDataModel,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .clickable { onClick() },
        headlineContent = {
            Text(
                text = emailDataModel.user.name,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
            )
        },
        supportingContent = {
            Text(
                text = emailDataModel.subject,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = emailDataModel.content,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(emailDataModel.color)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = emailDataModel.user.name.first().toString(),
                    color = Color.White,
                    fontSize = 22.sp,
                )
            }
        },
        trailingContent = {
            Column {
                Text(text = emailDataModel.time.toFormattedDate())
                Spacer(modifier = Modifier.height(32.dp))
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background,
        )
    )
}


@Preview
@Composable
fun EmailItemPreview() {
    EmailItem(
        emailDataModel = EmailDao().getEmails().first(),
        onClick = { }
    )
}

