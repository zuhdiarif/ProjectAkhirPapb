package com.zuhdiarifazizi.projectakhirpapb.domain.model

data class ClothingItem(
    val id: String,
    val userId: String,
    val name: String,
    val category: ClothingCategory,
    val pattern: ClothingPattern,
    val occasion: Occasion,
    val color: HsvColor,
    val imageUri: String? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val material: String = "",
    val rfidTagUid: String? = null,
    val lastWornEpochMillis: Long? = null
)
