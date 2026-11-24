package com.zuhdiarifazizi.projectakhirpapb.ui.home

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ActivityCategory
import com.zuhdiarifazizi.projectakhirpapb.ui.components.FeedbackMessage

data class HomeUiState(
    val categories: List<ActivityCategory> = emptyList(),
    val selectedCategory: ActivityCategory? = null,
    val isVoiceListening: Boolean = false,
    val feedbackBanner: FeedbackMessage? = null,
    val hasPlayedWelcome: Boolean = false
)
