package com.zuhdiarifazizi.projectakhirpapb.data.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ClothesRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.security.SecurityInputValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryClothesRepository(
    seedItems: List<ClothingItem> = DemoClothingCatalog.items
) : ClothesRepository {

    private val items = MutableStateFlow(seedItems)

    override fun observeClothes(userId: String): Flow<List<ClothingItem>> =
        items.map { rows -> rows.filter { it.userId == userId } }

    override suspend fun getClothesByUser(userId: String): List<ClothingItem> =
        items.value.filter { it.userId == userId }

    override suspend fun getClothingById(itemId: String): ClothingItem? =
        items.value.firstOrNull { it.id == itemId }

    override suspend fun getClothingByRfid(rfidTagUid: String): ClothingItem? {
        val normalized = SecurityInputValidator.normalizeNfcUidString(rfidTagUid) ?: return null
        return items.value.firstOrNull { it.rfidTagUid == normalized }
    }

    override suspend fun upsertClothing(item: ClothingItem) {
        val normalizedUid = item.rfidTagUid?.let { SecurityInputValidator.normalizeNfcUidString(it) }
        val sanitized = item.copy(rfidTagUid = normalizedUid)
        items.value = items.value.filterNot { it.id == sanitized.id } + sanitized
    }

    override suspend fun deleteClothing(itemId: String) {
        items.value = items.value.filterNot { it.id == itemId }
    }
}
