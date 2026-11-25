package com.zuhdiarifazizi.projectakhirpapb.ui.pickoutfit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitCandidate
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraCard
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraCheckbox
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraPrimaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraSecondaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.home.components.VoiceActionButton
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSpacing
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextSecondary

@Composable
fun PickOutfitScreen(
    uiState: PickOutfitUiState,
    isVoiceListening: Boolean,
    onBackToHome: () -> Unit,
    onBackStep: () -> Unit,
    onVoiceClick: () -> Unit,
    onCategoryChanged: (ClothingCategory, Boolean) -> Unit,
    onContinue: () -> Unit,
    onNextRecommendation: () -> Unit,
    onSpeakRecommendation: (Int) -> Unit,
    onViewDetails: (Int) -> Unit,
    onChooseRecommendation: (Int) -> Unit,
    onSpeakResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState.step) {
        PickOutfitStep.CATEGORY_SELECTION -> CategorySelectionContent(
            uiState = uiState,
            onBack = onBackToHome,
            onCategoryChanged = onCategoryChanged,
            onContinue = onContinue,
            modifier = modifier
        )
        PickOutfitStep.RECOMMENDATIONS -> OutfitRecommendationScreen(
            candidates = uiState.candidates,
            currentCandidateIndex = uiState.currentCandidateIndex,
            isVoiceListening = isVoiceListening,
            onBack = onBackStep,
            onVoiceClick = onVoiceClick,
            onSpeak = onSpeakRecommendation,
            onNext = onNextRecommendation,
            onViewDetails = onViewDetails,
            onChoose = onChooseRecommendation,
            errorMessage = uiState.errorMessage,
            statusMessage = uiState.statusMessage,
            modifier = modifier
        )
        PickOutfitStep.RESULT -> uiState.selectedCandidate?.let { candidate ->
            OutfitResultScreen(
                candidate = candidate,
                recommendationNumber = uiState.currentCandidateIndex + 1,
                recommendationCount = uiState.candidates.size,
                isVoiceListening = isVoiceListening,
                onBack = onBackStep,
                onVoiceClick = onVoiceClick,
                onSpeak = onSpeakResult,
                modifier = modifier
            )
        } ?: CategorySelectionContent(
            uiState = uiState,
            onBack = onBackToHome,
            onCategoryChanged = onCategoryChanged,
            onContinue = onContinue,
            modifier = modifier
        )
    }
}

@Composable
private fun CategorySelectionContent(
    uiState: PickOutfitUiState,
    onBack: () -> Unit,
    onCategoryChanged: (ClothingCategory, Boolean) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(AuraSpacing.md)
    ) {
        AuraSecondaryButton(text = "Kembali ke aktivitas", onClick = onBack)
        Text(
            text = "Pilih Kategori Pakaian",
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Aktivitas: ${uiState.selectedOccasion?.displayName ?: "belum dipilih"}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Pilih atasan dan bawahan. Pakaian luar bersifat opsional.",
            style = MaterialTheme.typography.bodyMedium,
            color = AuraTextSecondary
        )

        CategoryOption(
            category = ClothingCategory.TOP_WEAR,
            count = uiState.availableClothes.count { it.category == ClothingCategory.TOP_WEAR },
            isSelected = ClothingCategory.TOP_WEAR in uiState.selectedCategories,
            onCheckedChange = { onCategoryChanged(ClothingCategory.TOP_WEAR, it) }
        )
        CategoryOption(
            category = ClothingCategory.BOTTOM_WEAR,
            count = uiState.availableClothes.count { it.category == ClothingCategory.BOTTOM_WEAR },
            isSelected = ClothingCategory.BOTTOM_WEAR in uiState.selectedCategories,
            onCheckedChange = { onCategoryChanged(ClothingCategory.BOTTOM_WEAR, it) }
        )
        CategoryOption(
            category = ClothingCategory.OUTER_WEAR,
            count = uiState.availableClothes.count { it.category == ClothingCategory.OUTER_WEAR },
            isSelected = ClothingCategory.OUTER_WEAR in uiState.selectedCategories,
            onCheckedChange = { onCategoryChanged(ClothingCategory.OUTER_WEAR, it) }
        )

        if (uiState.isLoading) {
            Text(
                text = "Memuat koleksi pakaian…",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        AuraPrimaryButton(
            text = "Lanjutkan",
            onClick = onContinue,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(AuraSpacing.sm))
    }
}

@Composable
private fun CategoryOption(
    category: ClothingCategory,
    count: Int,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    AuraCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuraCheckbox(
                checked = isSelected,
                onCheckedChange = onCheckedChange,
                label = "${category.displayName}. $count pakaian tersedia. " +
                    if (category == ClothingCategory.OUTER_WEAR) "Opsional." else "Diperlukan untuk rekomendasi."
            )
        }
    }
}
