package com.jimmcgaw.whatsayyou.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jimmcgaw.whatsayyou.data.TranscriptionStatus

@Composable
fun ViewScreen(
    recordId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ViewViewModel = viewModel(factory = ViewViewModel.factory(recordId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ViewEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        OutlinedTextField(
            value = uiState.titleInput,
            onValueChange = viewModel::onTitleChanged,
            label = { Text("Title") },
            isError = uiState.titleError != null,
            supportingText = { uiState.titleError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Button(onClick = viewModel::onPlayPauseClick) {
                Text(if (uiState.isPlaying) "Pause" else "Play")
            }
            Text("${formatMillis(uiState.positionMs)} / ${formatMillis(uiState.durationMs)}")
        }

        Slider(
            value = uiState.positionMs.toFloat(),
            onValueChange = { viewModel.onSeek(it.toLong()) },
            valueRange = 0f..uiState.durationMs.coerceAtLeast(1L).toFloat(),
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = if (uiState.transcriptionStatus == TranscriptionStatus.COMPLETED) {
                uiState.transcript.orEmpty()
            } else {
                uiState.transcriptionStatus.name
            },
            modifier = Modifier.padding(top = 16.dp),
        )

        IconButton(onClick = viewModel::onDeleteClick, modifier = Modifier.padding(top = 16.dp)) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete recording")
        }
    }

    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::onDeleteDismiss,
            title = { Text("Delete recording?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = viewModel::onDeleteConfirm) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDeleteDismiss) { Text("Cancel") }
            },
        )
    }
}

private fun formatMillis(ms: Long): String {
    val totalSeconds = ms / 1_000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
