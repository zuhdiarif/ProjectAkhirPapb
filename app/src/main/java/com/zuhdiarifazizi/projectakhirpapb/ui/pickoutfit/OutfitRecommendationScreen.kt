package com.zuhdiarifazizi.projectakhirpapb.ui.pickoutfit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitCandidate
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraCard
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraPrimaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraSecondaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.OutfitGarmentGallery
import com.zuhdiarifazizi.projectakhirpapb.ui.home.components.VoiceActionButton
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSpacing
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextSecondary

@Composable
fun OutfitRecommendationScreen(
    candidates: List<OutfitCandidate>,
    currentCandidateIndex: Int,
    isVoiceListening: Boolean,
    onBack: () -> Unit,
    onVoiceClick: () -> Unit,
    onSpeak: (Int) -> Unit,
    onNext: () -> Unit,
    onViewDetails: (Int) -> Unit,
    onChoose: (Int) -> Unit,
    errorMessage: String?,
    statusMessage: String?,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LaunchedEffect(currentCandidateIndex, candidates.size) {
        if (candidates.isNotEmpty()) listState.animateScrollToItem(currentCandidateIndex.coerceIn(candidates.indices))
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AuraSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AuraSpacing.md)
    ) {
        item {
            ScreenBackButton(text = "Kembali ke kategori", onClick = onBack)
            Text(
                text = "Rekomendasi Pakaian",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${candidates.size} kombinasi dari koleksi pakaian yang tersedia.",
                style = MaterialTheme.typography.bodyMedium,
                color = AuraTextSecondary
            )
            Spacer(Modifier.height(AuraSpacing.xs))
            VoiceActionButton(isListening = isVoiceListening, onClick = onVoiceClick)
            Spacer(Modifier.height(AuraSpacing.xs))
            statusMessage?.let { message ->
                Text(
                    text = message,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            errorMessage?.let { message ->
                Text(
                    text = message,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        itemsIndexed(candidates, key = { _, candidate -> candidate.id }) { index, candidate ->
            RecommendationCard(
                candidate = candidate,
                index = index,
                total = candidates.size,
                isCurrent = index == currentCandidateIndex,
                onSpeak = { onSpeak(index) },
                onViewDetails = { onViewDetails(index) },
                onChoose = { onChoose(index) }
            )
        }

        item {
            AuraPrimaryButton(
                text = "Pakaian Berikutnya",
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(AuraSpacing.md))
        }
    }
}

@Composable
private fun RecommendationCard(
    candidate: OutfitCandidate,
    index: Int,
    total: Int,
    isCurrent: Boolean,
    onSpeak: () -> Unit,
    onViewDetails: () -> Unit,
    onChoose: () -> Unit
) {
    AuraCard {
        Column(verticalArrangement = Arrangement.spacedBy(AuraSpacing.sm)) {
            Text(
                text = "Rekomendasi ${index + 1} dari $total${if (isCurrent) ", sedang ditinjau" else ""}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            OutfitGarmentGallery(candidate = candidate, imageHeight = 116.dp)
            Text(
                text = outfitName(candidate),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Aktivitas: ${candidate.occasion.displayName}. " +
                    "Kecocokan aktivitas: ${fitLabel(candidate.occasionScore)}. " +
                    "Kecocokan warna: ${candidate.harmonyType}, ${percent(candidate.colorScore)} persen.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = explanation(candidate),
                style = MaterialTheme.typography.bodySmall,
                color = AuraTextSecondary
            )
            AuraSecondaryButton(
                text = "Dengarkan Deskripsi",
                onClick = onSpeak,
                modifier = Modifier.fillMaxWidth()
            )
            AuraSecondaryButton(
                text = "Lihat Detail",
                onClick = onViewDetails,
                modifier = Modifier.fillMaxWidth()
            )
            AuraPrimaryButton(
                text = "Pilih Pakaian Ini",
                onClick = onChoose,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ScreenBackButton(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AuraSpacing.xs),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AuraSecondaryButton(text = text, onClick = onClick)
    }
}

private fun outfitName(candidate: OutfitCandidate): String = buildString {
    append(candidate.topWear?.name ?: "Atasan tidak tersedia")
    append(" + ")
    append(candidate.bottomWear?.name ?: "bawahan tidak tersedia")
    candidate.outerWear?.let { append(" + ${it.name}") }
}

private fun explanation(candidate: OutfitCandidate): String = when {
    candidate.occasionScore >= 0.8 ->
        "Alasan kecocokan: kategori aktivitas pakaian ini sesuai untuk ${candidate.occasion.displayName.lowercase()}, dengan warna yang saling melengkapi."
    candidate.occasion == com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion.EXERCISE ->
        "Alasan kecocokan: ini pilihan terdekat dari koleksi demo. Koleksi belum berisi pakaian olahraga khusus."
    else ->
        "Alasan kecocokan: kombinasi ini dibuat dari atasan dan bawahan yang tersedia, lalu dinilai berdasarkan aktivitas dan harmoni warna."
}

private fun fitLabel(score: Double): String = when {
    score >= 0.8 -> "tinggi"
    score >= 0.55 -> "sedang"
    else -> "terbatas"
}

private fun percent(score: Double): Int = (score * 100).toInt()
