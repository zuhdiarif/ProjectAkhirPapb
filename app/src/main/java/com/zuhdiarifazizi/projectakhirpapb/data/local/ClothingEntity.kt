package com.zuhdiarifazizi.projectakhirpapb.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion

@Entity(
    tableName = "clothing_items",
    indices = [
        Index("userId"),
        Index(value = ["rfidTagUid"], unique = true)
    ]
)
data class ClothingEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val category: String,
    val pattern: String,
    val occasion: String,
    val hue: Float,
    val saturation: Float,
    val value: Float,
    val colorName: String,
    val imageUri: String?,
    val createdAtEpochMillis: Long,
    val material: String = "",
    val rfidTagUid: String? = null,
    val lastWornEpochMillis: Long? = null
)

fun ClothingEntity.toDomain() = ClothingItem(
    id = id,
    userId = userId,
    name = name,
    category = ClothingCategory.valueOf(category),
    pattern = ClothingPattern.valueOf(pattern),
    occasion = Occasion.valueOf(occasion),
    color = HsvColor(hue, saturation, value, colorName),
    imageUri = imageUri,
    createdAtEpochMillis = createdAtEpochMillis,
    material = material,
    rfidTagUid = rfidTagUid,
    lastWornEpochMillis = lastWornEpochMillis
)

fun ClothingItem.toEntity() = ClothingEntity(
    id = id,
    userId = userId,
    name = name,
    category = category.name,
    pattern = pattern.name,
    occasion = occasion.name,
    hue = color.hue,
    saturation = color.saturation,
    value = color.value,
    colorName = color.colorName,
    imageUri = imageUri,
    createdAtEpochMillis = createdAtEpochMillis,
    material = material,
    rfidTagUid = rfidTagUid?.takeIf { it.isNotBlank() },
    lastWornEpochMillis = lastWornEpochMillis
)

@Entity(tableName = "app_metadata")
data class AppMetadataEntity(
    @PrimaryKey val key: String,
    val value: String
)
