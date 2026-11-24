package com.zuhdiarifazizi.projectakhirpapb.domain.model

import androidx.annotation.DrawableRes

data class ActivityCategory(
    val id: String,
    val occasion: Occasion,
    val title: String,
    val description: String,
    @get:DrawableRes val iconResId: Int,
    val actionText: String = "Pilih"
)
