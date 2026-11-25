package com.zuhdiarifazizi.projectakhirpapb.data.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ClothesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Demo data is reset when the application process is recreated. */
class InMemoryClothesRepository : ClothesRepository {

    private val clothes = MutableStateFlow(DemoClothingCatalog.items)

    override fun observeClothes(userId: String): Flow<List<ClothingItem>> =
        clothes.asStateFlow().map { items -> items.filter { it.userId == userId } }

    override suspend fun getClothesByUser(userId: String): List<ClothingItem> =
        clothes.value.filter { it.userId == userId }

    override suspend fun getClothingById(itemId: String): ClothingItem? =
        clothes.value.firstOrNull { it.id == itemId }

    override suspend fun upsertClothing(item: ClothingItem) {
        clothes.update { current -> current.filterNot { it.id == item.id } + item }
    }

    override suspend fun deleteClothing(itemId: String) {
        clothes.update { current -> current.filterNot { it.id == itemId } }
    }
}
