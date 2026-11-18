package com.zuhdiarifazizi.projectakhirpapb.domain.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import kotlinx.coroutines.flow.Flow

interface ClothesRepository {
    fun observeClothes(userId: String): Flow<List<ClothingItem>>
    suspend fun getClothesByUser(userId: String): List<ClothingItem>
    suspend fun getClothingById(itemId: String): ClothingItem?
    suspend fun upsertClothing(item: ClothingItem)
    suspend fun deleteClothing(itemId: String)
}
