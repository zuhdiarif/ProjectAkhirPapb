package com.zuhdiarifazizi.projectakhirpapb.ui.pickoutfit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitCandidate
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraCard
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraPrimaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraSecondaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.OutfitGarmentGallery
import com.zuhdiarifazizi.projectakhirpapb.ui.home.components.VoiceActionButton
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraCardSelectedBg
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSpacing
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextSecondary

@Composable
fun OutfitResultScreen(
    candidate: OutfitCandidate,
    recommendationNumber: Int,
    recommendationCount: Int,
    isVoiceListening: Boolean,
    onBack: () -> Unit,
    onVoiceClick: () -> Unit,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(AuraSpacing.md)
    ) {
        AuraSecondaryButton(text = "Kembali ke rekomendasi", onClick = onBack)
        Text(
            text = "Hasil Pakaian",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Rekomendasi $recommendationNumber dari $recommendationCount untuk ${candidate.occasion.displayName}.",
            style = MaterialTheme.typography.bodyMedium,
            color = AuraTextSecondary
        )
        VoiceActionButton(isListening = isVoiceListening, onClick = onVoiceClick)

        AuraCard {
            Column(verticalArrangement = Arrangement.spacedBy(AuraSpacing.sm)) {
                OutfitGarmentGallery(candidate = candidate, imageHeight = 152.dp)
                Text(
                    text = outfitName(candidate),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Atasan: ${candidate.topWear?.name ?: "tidak tersedia"}. " +
                        "Bawahan: ${candidate.bottomWear?.name ?: "tidak tersedia"}. " +
                        (candidate.outerWear?.let { "Pakaian luar: ${it.name}." }
                            ?: "Pakaian luar tidak dipilih karena bersifat opsional."),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Aktivitas: ${candidate.occasion.displayName}. " +
                        "Kecocokan aktivitas ${fitLabel(candidate.occasionScore)}. " +
                        "Warna ${candidate.harmonyType.lowercase()}, nilai kecocokan ${percent(candidate.colorScore)} persen.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = explanation(candidate),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { liveRegion = LiveRegionMode.Polite },
            shape = MaterialTheme.shapes.medium,
            color = AuraCardSelectedBg
        ) {
            Text(
                text = "Pilihan ini hanya disimpan sementara selama sesi aplikasi. Belum tersimpan permanen atau masuk ke Riwayat.",
                modifier = Modifier.padding(AuraSpacing.md),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        AuraPrimaryButton(
            text = "Dengarkan Deskripsi",
            onClick = onSpeak,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(AuraSpacing.sm))
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
        "Alasan pemilihan: kategori pakaian cocok untuk ${candidate.occasion.displayName.lowercase()} dan ${candidate.harmonyType.lowercase()}."
    candidate.occasion == com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion.EXERCISE ->
        "Alasan pemilihan: ini pilihan terdekat dari koleksi demo. Belum ada pakaian olahraga khusus."
    else ->
        "Alasan pemilihan: rekomendasi dihitung dari kategori aktivitas, kategori atasan dan bawahan, serta kecocokan warna pakaian yang tersedia."
}

private fun fitLabel(score: Double): String = when {
    score >= 0.8 -> "tinggi"
    score >= 0.55 -> "sedang"
    else -> "terbatas"
}

private fun percent(score: Double): Int = (score * 100).toInt()
