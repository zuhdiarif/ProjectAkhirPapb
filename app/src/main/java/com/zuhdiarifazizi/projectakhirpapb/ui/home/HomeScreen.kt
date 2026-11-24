package com.zuhdiarifazizi.projectakhirpapb.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ActivityCategory
import com.zuhdiarifazizi.projectakhirpapb.ui.home.components.ActivityCategoryCard
import com.zuhdiarifazizi.projectakhirpapb.ui.home.components.HomeHeader
import com.zuhdiarifazizi.projectakhirpapb.ui.home.components.HomeHeading
import com.zuhdiarifazizi.projectakhirpapb.ui.home.components.VoiceActionButton
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraBackground

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onCategoryClick: (ActivityCategory) -> Unit,
    onVoiceButtonClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = AuraBackground,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            HomeHeader(
                onProfileClick = onProfileClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Heading section
            HomeHeading()

            Spacer(modifier = Modifier.height(18.dp))

            // Voice Interaction Button (Prominent, High Accessibility)
            VoiceActionButton(
                isListening = uiState.isVoiceListening,
                onClick = onVoiceButtonClick,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 5 Category Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                uiState.categories.forEach { category ->
                    val isSelected = uiState.selectedCategory?.id == category.id
                    ActivityCategoryCard(
                        category = category,
                        isSelected = isSelected,
                        onSelect = onCategoryClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
