package com.zuhdiarifazizi.projectakhirpapb.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zuhdiarifazizi.projectakhirpapb.ui.home.components.VoiceActionButton
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextSecondary

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    isVoiceListening: Boolean = false,
    partialTranscript: String = "",
    onVoiceClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Riwayat & Rotasi Pakaian",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = if (uiState.historyEntries.isEmpty()) {
                "Belum ada catatan riwayat pakaian yang dipakai. Anda dapat memakai perintah suara untuk kembali ke Beranda atau membuka Lemari."
            } else {
                "Tersimpan ${uiState.historyEntries.size} catatan riwayat pemakaian pakaian."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = AuraTextSecondary
        )
        VoiceActionButton(
            isListening = isVoiceListening,
            onClick = onVoiceClick,
            partialTranscript = partialTranscript
        )
    }
}
