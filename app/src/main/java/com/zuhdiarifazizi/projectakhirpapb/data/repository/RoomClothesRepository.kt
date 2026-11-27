package com.zuhdiarifazizi.projectakhirpapb.data.repository

import androidx.room.withTransaction
import com.zuhdiarifazizi.projectakhirpapb.data.local.AppMetadataEntity
import com.zuhdiarifazizi.projectakhirpapb.data.local.AuraDatabase
import com.zuhdiarifazizi.projectakhirpapb.data.local.ClothingEntity
import com.zuhdiarifazizi.projectakhirpapb.data.local.toDomain
import com.zuhdiarifazizi.projectakhirpapb.data.local.toEntity
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ClothesRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.usecase.SecurityInputValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RoomClothesRepository(private val database: AuraDatabase) : ClothesRepository {
    private val initializationMutex = Mutex()

    override fun observeClothes(userId: String): Flow<List<ClothingItem>> = flow {
        ensureUserSeeded(userId)
        emitAll(database.clothingDao().observeByUser(userId).map { rows -> rows.map(ClothingEntity::toDomain) })
    }

    override suspend fun getClothesByUser(userId: String): List<ClothingItem> {
        ensureUserSeeded(userId)
        return database.clothingDao().getByUser(userId).map(ClothingEntity::toDomain)
    }

    override suspend fun getClothingById(itemId: String): ClothingItem? {
        ensureUserSeeded(DemoClothingCatalog.USER_ID)
        return database.clothingDao().getById(itemId)?.toDomain()
    }

    override suspend fun getClothingByRfid(rfidTagUid: String): ClothingItem? {
        ensureUserSeeded(DemoClothingCatalog.USER_ID)
        val normalized = SecurityInputValidator.normalizeNfcUidString(rfidTagUid) ?: return null
        return database.clothingDao().getByRfidTag(normalized)?.toDomain()
    }

    override suspend fun upsertClothing(item: ClothingItem) {
        ensureUserSeeded(item.userId)
        val normalizedUid = item.rfidTagUid?.let { SecurityInputValidator.normalizeNfcUidString(it) }
        database.clothingDao().upsert(item.copy(rfidTagUid = normalizedUid).toEntity())
    }

    override suspend fun deleteClothing(itemId: String) {
        ensureUserSeeded(DemoClothingCatalog.USER_ID)
        database.clothingDao().deleteById(itemId)
    }

    override suspend fun seedStarterWardrobe(userId: String) {
        if (userId.isBlank()) return
        initializationMutex.withLock {
            database.withTransaction {
                database.clothingDao().insertAll(
                    DemoClothingCatalog.starterItemsForUser(userId).map(ClothingItem::toEntity)
                )
            }
        }
    }

    private suspend fun ensureUserSeeded(userId: String) = initializationMutex.withLock {
        val targetUserId = userId.ifBlank { DemoClothingCatalog.USER_ID }
        database.withTransaction {
            val metadataDao = database.appMetadataDao()
            val clothingDao = database.clothingDao()

            if (targetUserId == DemoClothingCatalog.USER_ID) {
                if (metadataDao.getValue(DEMO_SEED_MARKER) == null) {
                    clothingDao.insertAll(DemoClothingCatalog.items.map(ClothingItem::toEntity))
                    metadataDao.insert(AppMetadataEntity(DEMO_SEED_MARKER, "done"))
                }
                if (metadataDao.getValue(DEMO_ENRICH_V2_MARKER) == null) {
                    DemoClothingCatalog.items.forEach { demo ->
                        val existing = clothingDao.getById(demo.id) ?: return@forEach
                        val needsMaterial = existing.material.isBlank() && demo.material.isNotBlank()
                        val needsLastWorn = existing.lastWornEpochMillis == null && demo.lastWornEpochMillis != null
                        if (needsMaterial || needsLastWorn) {
                            clothingDao.upsert(
                                existing.copy(
                                    material = if (needsMaterial) demo.material else existing.material,
                                    lastWornEpochMillis = existing.lastWornEpochMillis ?: demo.lastWornEpochMillis
                                )
                            )
                        }
                    }
                    metadataDao.insert(AppMetadataEntity(DEMO_ENRICH_V2_MARKER, "done"))
                }
                if (metadataDao.getValue(DEMO_VARIED_V3_MARKER) == null) {
                    clothingDao.insertAll(DemoClothingCatalog.items.map(ClothingItem::toEntity))
                    DemoClothingCatalog.items.firstOrNull { !it.rfidTagUid.isNullOrBlank() }?.let { rfidDemo ->
                        val existingRfidOwner = rfidDemo.rfidTagUid?.let { clothingDao.getByRfidTag(it) }
                        if (existingRfidOwner == null) {
                            clothingDao.getById(rfidDemo.id)?.let { row ->
                                clothingDao.upsert(row.copy(rfidTagUid = rfidDemo.rfidTagUid))
                            }
                        }
                    }
                    metadataDao.insert(AppMetadataEntity(DEMO_VARIED_V3_MARKER, "done"))
                }
            } else {
                val userMarker = "$USER_SEED_PREFIX$targetUserId"
                if (metadataDao.getValue(userMarker) == null) {
                    clothingDao.insertAll(
                        DemoClothingCatalog.starterItemsForUser(targetUserId).map(ClothingItem::toEntity)
                    )
                    metadataDao.insert(AppMetadataEntity(userMarker, "done"))
                }
            }
        }
    }

    private companion object {
        const val DEMO_SEED_MARKER = "demo_clothing_seed_v1"
        const val DEMO_ENRICH_V2_MARKER = "demo_clothing_enrich_v2"
        const val DEMO_VARIED_V3_MARKER = "demo_clothing_varied_v3"
        const val USER_SEED_PREFIX = "wardrobe_seed_v3_"
    }
}
