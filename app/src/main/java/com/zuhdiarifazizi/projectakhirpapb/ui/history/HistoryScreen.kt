package com.zuhdiarifazizi.projectakhirpapb.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zuhdiarifazizi.projectakhirpapb.R
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitHistory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.RotationRecommendation
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraCard
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraDestructiveButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraPrimaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraSecondaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.ClothingImage
import com.zuhdiarifazizi.projectakhirpapb.ui.home.components.VoiceActionButton
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraHighContrastInk
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraIndigoSoft
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraIndigoWash
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavyDeep
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSurfaceTint
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextSecondary
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTipCardBg
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraWarmSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    isVoiceListening: Boolean = false,
    partialTranscript: String = "",
    onVoiceClick: () -> Unit = {},
    onSpeakSummary: () -> Unit = {},
    onSpeakHistoryEntry: (OutfitHistory) -> Unit = {},
    onDeleteHistoryEntry: (OutfitHistory) -> Unit = {},
    onSpeakRotationRecommendation: (RotationRecommendation) -> Unit = {},
    onGoToPickOutfit: () -> Unit = {},
    onRequestClearHistory: () -> Unit = {},
    onCancelClearHistory: () -> Unit = {},
    onConfirmClearHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraSurfaceTint)
    ) {
        Surface(color = AuraWarmSurface.copy(alpha = 0.95f)) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.semantics { heading() }
                    ) {
                        Text(
                            text = "Aura",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = AuraNavyDeep,
                                fontSize = 26.sp
                            )
                        )
                        Text(
                            text = "  •  ",
                            style = MaterialTheme.typography.titleLarge.copy(color = AuraTextSecondary)
                        )
                        Text(
                            text = "Riwayat",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = AuraHighContrastInk,
                                fontSize = 20.sp
                            )
                        )
                    }
                }
                HorizontalDivider(color = Color(0xFFC5C5D3).copy(alpha = 0.4f), thickness = 1.dp)
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    color = AuraIndigoSoft,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(AuraNavyDeep),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_volume_up),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "ASISTEN SUARA\nRIWAYAT & ROTASI",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AuraNavyDeep,
                                    letterSpacing = 0.4.sp
                                )
                            )
                        }

                        Surface(
                            onClick = onSpeakSummary,
                            shape = RoundedCornerShape(8.dp),
                            color = AuraSurfaceTint,
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .semantics {
                                    contentDescription = "Nyalakan ringkasan suara riwayat dan rotasi pakaian"
                                    role = Role.Button
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_volume_up),
                                    contentDescription = null,
                                    tint = AuraNavyDeep,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Nyalakan",
                                    fontWeight = FontWeight.Bold,
                                    color = AuraNavyDeep
                                )
                            }
                        }
                    }
                }
            }

            item {
                VoiceActionButton(
                    isListening = isVoiceListening,
                    onClick = onVoiceClick,
                    partialTranscript = partialTranscript
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryStatCard(
                        label = "Dipakai (30 Hari)",
                        value = "${uiState.historyEntries.size} outfit",
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        label = "Jarang Dipakai",
                        value = "${uiState.rarelyWornItems.size} baju",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            uiState.statusMessage?.let { msg ->
                item {
                    Surface(
                        color = AuraTipCardBg,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { liveRegion = LiveRegionMode.Polite }
                    ) {
                        Text(
                            text = msg,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = AuraNavyDeep
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Rekomendasi Rotasi Pakaian",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF131B2E)
                        ),
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = "Pakaian yang dipakai kurang dari 2 kali dalam 30 hari terakhir agar seluruh koleksi terpakai merata.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AuraTextSecondary
                    )
                }
            }

            if (uiState.rarelyWornItems.isEmpty()) {
                item {
                    AuraCard {
                        Text(
                            text = if (uiState.totalWardrobeCount == 0) {
                                "Belum ada pakaian di lemari. Tambahkan pakaian di tab Kelola Baju."
                            } else {
                                "Bagus! Semua pakaian di lemari Anda sudah terotasi secara seimbang dalam 30 hari terakhir."
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(uiState.rarelyWornItems, key = { it.item.id }) { rec ->
                            RotationCandidateCard(
                                recommendation = rec,
                                onSpeak = { onSpeakRotationRecommendation(rec) }
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Riwayat Pemakaian Outfit",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF131B2E)
                        ),
                        modifier = Modifier.semantics { heading() }
                    )
                    if (uiState.historyEntries.isNotEmpty()) {
                        TextButton(
                            onClick = onRequestClearHistory,
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .semantics {
                                    contentDescription = "Bersihkan semua catatan riwayat pemakaian"
                                }
                        ) {
                            Text("Bersihkan", color = AuraNavyDeep, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            if (uiState.historyEntries.isEmpty()) {
                item {
                    AuraCard {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Belum ada riwayat pemakaian outfit.",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pilih rekomendasi outfit dari tab Pilih Pakaian dan tekan Pilih Pakaian Ini untuk mencatatnya di sini dan mengaktifkan penyesuaian skor rotasi 7 hari.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AuraTextSecondary
                            )
                            AuraPrimaryButton(
                                text = "Pilih Pakaian Sekarang",
                                onClick = onGoToPickOutfit,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            } else {
                items(uiState.historyEntries, key = { it.id }) { entry ->
                    HistoryEntryCard(
                        entry = entry,
                        onSpeak = { onSpeakHistoryEntry(entry) },
                        onDelete = { onDeleteHistoryEntry(entry) }
                    )
                }
            }
        }
    }

    if (uiState.showClearConfirmation) {
        AlertDialog(
            onDismissRequest = onCancelClearHistory,
            title = { Text("Bersihkan semua riwayat?") },
            text = {
                Text("Semua catatan pemakaian outfit 30 hari terakhir pada akun ini akan dihapus.")
            },
            confirmButton = {
                TextButton(onClick = onConfirmClearHistory) { Text("Bersihkan") }
            },
            dismissButton = {
                TextButton(onClick = onCancelClearHistory) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun SummaryStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "$label: $value"
        }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = AuraTextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AuraNavyDeep
                )
            )
        }
    }
}

@Composable
private fun RotationCandidateCard(
    recommendation: RotationRecommendation,
    onSpeak: () -> Unit
) {
    val item = recommendation.item
    val usageText = if (recommendation.usageCountLast30Days == 0) {
        "Belum dipakai (30 hari)"
    } else {
        "Dipakai ${recommendation.usageCountLast30Days}x (30 hari)"
    }

    AuraCard(
        modifier = Modifier
            .width(220.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "${item.name}, ${item.category.displayName}, warna ${item.color.colorName}, $usageText. Rekomendasi rotasi."
            }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ClothingImage(item = item, imageHeight = 110.dp)
            Surface(
                color = AuraIndigoSoft,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = usageText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AuraNavyDeep
                    )
                )
            }
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${item.category.displayName} • ${item.color.colorName}",
                style = MaterialTheme.typography.bodySmall,
                color = AuraTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            AuraSecondaryButton(
                text = "Dengarkan",
                onClick = onSpeak,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Dengarkan rekomendasi rotasi ${item.name}"
                    }
            )
        }
    }
}

@Composable
private fun HistoryEntryCard(
    entry: OutfitHistory,
    onSpeak: () -> Unit,
    onDelete: () -> Unit
) {
    val dateLabel = formatHistoryDate(entry.wornAtEpochMillis)
    val scorePercent = (entry.score * 100).toInt()
    val summaryItems = entry.items.joinToString(" + ") { it.name }.ifBlank { "Kombinasi outfit" }

    AuraCard(
        modifier = Modifier.semantics {
            contentDescription = "Riwayat $dateLabel, aktivitas ${entry.occasion.displayName}, skor $scorePercent persen: $summaryItems"
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(color = AuraIndigoWash, shape = RoundedCornerShape(6.dp)) {
                    Text(
                        text = entry.occasion.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AuraNavyDeep
                        )
                    )
                }
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = AuraTextSecondary
                )
            }

            Text(
                text = summaryItems,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = AuraHighContrastInk
            )

            entry.items.forEach { garment ->
                Text(
                    text = "• ${garment.category.displayName}: ${garment.name} (${garment.color.colorName})",
                    style = MaterialTheme.typography.bodySmall,
                    color = AuraTextSecondary
                )
            }

            Text(
                text = "Skor keselarasan: $scorePercent%",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = AuraNavyDeep,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AuraSecondaryButton(
                    text = "Dengarkan",
                    onClick = onSpeak,
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = "Dengarkan riwayat $dateLabel, ${entry.occasion.displayName}"
                        }
                )
                AuraDestructiveButton(
                    text = "Hapus",
                    onClick = onDelete,
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = "Hapus riwayat $dateLabel, ${entry.occasion.displayName}"
                        }
                )
            }
        }
    }
}

private fun formatHistoryDate(epochMillis: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
    return formatter.format(Date(epochMillis))
}
