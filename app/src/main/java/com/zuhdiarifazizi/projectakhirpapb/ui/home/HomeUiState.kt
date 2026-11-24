package com.zuhdiarifazizi.projectakhirpapb.ui.home

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ActivityCategory

data class HomeUiState(
    val categories: List<ActivityCategory> = emptyList(),
    val selectedCategory: ActivityCategory? = null,
    val isVoiceListening: Boolean = false,
    val feedbackMessage: String? = null
)
