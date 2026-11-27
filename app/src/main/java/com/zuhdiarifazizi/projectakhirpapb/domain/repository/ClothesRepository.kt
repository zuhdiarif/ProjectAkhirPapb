package com.zuhdiarifazizi.projectakhirpapb.domain.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import kotlinx.coroutines.flow.Flow

interface ClothesRepository {
    fun observeClothes(userId: String): Flow<List<ClothingItem>>
    suspend fun getClothesByUser(userId: String): List<ClothingItem>
    suspend fun getClothingById(itemId: String): ClothingItem?
    suspend fun getClothingByRfid(rfidTagUid: String): ClothingItem? = null
    suspend fun upsertClothing(item: ClothingItem)
    suspend fun deleteClothing(itemId: String)
    suspend fun seedStarterWardrobe(userId: String) {}

    companion object {
        const val DEFAULT_USER_ID = "aura-demo-user"
        const val DEMO_RFID_UID = "04:A1:B2:C3:D4"
    }
}
