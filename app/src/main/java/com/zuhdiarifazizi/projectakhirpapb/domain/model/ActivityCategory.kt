package com.zuhdiarifazizi.projectakhirpapb.domain.model

data class ActivityCategory(
    val id: String,
    val occasion: Occasion,
    val title: String,
    val description: String,
    val iconResId: Int,
    val actionText: String = "Pilih"
)
