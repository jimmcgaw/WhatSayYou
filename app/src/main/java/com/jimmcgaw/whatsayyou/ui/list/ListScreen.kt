package com.jimmcgaw.whatsayyou.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ListScreen(
    modifier: Modifier = Modifier,
    viewModel: ListViewModel = viewModel(factory = ListViewModel.Factory),
) {
    val recordings by viewModel.uiState.collectAsStateWithLifecycle()

    if (recordings.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No recordings yet")
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(recordings, key = { it.id }) { recording ->
                ListItem(
                    headlineContent = { Text(recording.displayTitle) },
                    supportingContent = {
                        Text(recording.transcript ?: recording.transcriptionStatus.name)
                    },
                )
                HorizontalDivider()
            }
        }
    }
}
