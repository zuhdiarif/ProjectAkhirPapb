package com.zuhdiarifazizi.projectakhirpapb.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zuhdiarifazizi.projectakhirpapb.R
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ActivityCategory
import com.zuhdiarifazizi.projectakhirpapb.ui.components.FeedbackBanner
import com.zuhdiarifazizi.projectakhirpapb.ui.home.components.ActivityCategoryCard
import com.zuhdiarifazizi.projectakhirpapb.ui.home.components.HomeHeader
import com.zuhdiarifazizi.projectakhirpapb.ui.home.components.HomeHeading
import com.zuhdiarifazizi.projectakhirpapb.ui.home.components.VoiceActionButton
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraBackground
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraCardBorder
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavy
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSurface

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onCategoryClick: (ActivityCategory) -> Unit,
    onVoiceButtonClick: () -> Unit,
    onRepeatGuidance: () -> Unit,
    onDismissBanner: () -> Unit,
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        onRepeatGuidance()
    }

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

            // Feedback Banner (Error or Notification)
            AnimatedVisibility(
                visible = uiState.feedbackBanner != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                uiState.feedbackBanner?.let { feedback ->
                    Spacer(modifier = Modifier.height(14.dp))
                    FeedbackBanner(
                        feedback = feedback,
                        onDismiss = onDismissBanner
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Heading section
            HomeHeading()

            Spacer(modifier = Modifier.height(12.dp))

            // Audio guidance repeat button (Accessibility first)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Dengarkan panduan halaman Beranda. Tombol. Ketuk dua kali untuk memutar ulang suara panduan."
                        role = Role.Button
                    }
                    .clickable(
                        onClickLabel = "memutar panduan suara beranda",
                        role = Role.Button,
                        onClick = onRepeatGuidance
                    ),
                shape = RoundedCornerShape(12.dp),
                color = AuraSurface,
                border = BorderStroke(1.dp, AuraCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_volume_up),
                        contentDescription = null,
                        tint = AuraNavy,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Dengarkan Panduan Halaman",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = AuraNavy,
                            fontSize = 13.sp
                        )
                    )
                }
            }

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
