package com.zuhdiarifazizi.projectakhirpapb.data.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ClothesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class InMemoryClothesRepository : ClothesRepository {

    override fun observeClothes(userId: String): Flow<List<ClothingItem>> = flowOf(emptyList())

    override suspend fun getClothesByUser(userId: String): List<ClothingItem> = emptyList()

    override suspend fun getClothingById(itemId: String): ClothingItem? = null

    override suspend fun upsertClothing(item: ClothingItem) {
    }

    override suspend fun deleteClothing(itemId: String) {
    }
}
