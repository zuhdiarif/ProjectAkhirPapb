package com.zuhdiarifazizi.projectakhirpapb.data.repository

import androidx.room.withTransaction
import com.zuhdiarifazizi.projectakhirpapb.data.local.AppMetadataEntity
import com.zuhdiarifazizi.projectakhirpapb.data.local.AuraDatabase
import com.zuhdiarifazizi.projectakhirpapb.data.local.ClothingEntity
import com.zuhdiarifazizi.projectakhirpapb.data.local.toDomain
import com.zuhdiarifazizi.projectakhirpapb.data.local.toEntity
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ClothesRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.security.SecurityInputValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RoomClothesRepository(private val database: AuraDatabase) : ClothesRepository {
    private val initializationMutex = Mutex()

    override fun observeClothes(userId: String): Flow<List<ClothingItem>> = flow {
        ensureDemoSeeded()
        emitAll(database.clothingDao().observeByUser(userId).map { rows -> rows.map(ClothingEntity::toDomain) })
    }

    override suspend fun getClothesByUser(userId: String): List<ClothingItem> {
        ensureDemoSeeded()
        return database.clothingDao().getByUser(userId).map(ClothingEntity::toDomain)
    }

    override suspend fun getClothingById(itemId: String): ClothingItem? {
        ensureDemoSeeded()
        return database.clothingDao().getById(itemId)?.toDomain()
    }

    override suspend fun getClothingByRfid(rfidTagUid: String): ClothingItem? {
        ensureDemoSeeded()
        val normalized = SecurityInputValidator.normalizeNfcUidString(rfidTagUid) ?: return null
        return database.clothingDao().getByRfidTag(normalized)?.toDomain()
    }

    override suspend fun upsertClothing(item: ClothingItem) {
        ensureDemoSeeded()
        val normalizedUid = item.rfidTagUid?.let { SecurityInputValidator.normalizeNfcUidString(it) }
        database.clothingDao().upsert(item.copy(rfidTagUid = normalizedUid).toEntity())
    }

    override suspend fun deleteClothing(itemId: String) {
        ensureDemoSeeded()
        database.clothingDao().deleteById(itemId)
    }

    private suspend fun ensureDemoSeeded() = initializationMutex.withLock {
        database.withTransaction {
            val metadataDao = database.appMetadataDao()
            if (metadataDao.getValue(DEMO_SEED_MARKER) == null) {
                database.clothingDao().insertAll(DemoClothingCatalog.items.map(ClothingItem::toEntity))
                metadataDao.insert(AppMetadataEntity(DEMO_SEED_MARKER, "done"))
            }
        }
    }

    private companion object {
        const val DEMO_SEED_MARKER = "demo_clothing_seed_v1"
    }
}
