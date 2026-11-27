package com.zuhdiarifazizi.projectakhirpapb.data.repository

import androidx.room.withTransaction
import com.zuhdiarifazizi.projectakhirpapb.data.local.AppMetadataEntity
import com.zuhdiarifazizi.projectakhirpapb.data.local.AuraDatabase
import com.zuhdiarifazizi.projectakhirpapb.data.local.OutfitHistoryEntity
import com.zuhdiarifazizi.projectakhirpapb.data.local.toDomain
import com.zuhdiarifazizi.projectakhirpapb.data.local.toEntity
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitHistory
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.OutfitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RoomOutfitRepository(private val database: AuraDatabase) : OutfitRepository {
    private val seedMutex = Mutex()

    override fun observeHistory(userId: String): Flow<List<OutfitHistory>> = flow {
        val target = userId.ifBlank { DemoClothingCatalog.USER_ID }
        ensureHistorySeeded(target)
        emitAll(
            database.outfitHistoryDao()
                .observeByUser(target)
                .map { rows -> rows.map(OutfitHistoryEntity::toDomain) }
        )
    }

    override suspend fun getHistorySince(userId: String, fromEpochMillis: Long): List<OutfitHistory> {
        val target = userId.ifBlank { DemoClothingCatalog.USER_ID }
        ensureHistorySeeded(target)
        return database.outfitHistoryDao()
            .getSince(target, fromEpochMillis)
            .map(OutfitHistoryEntity::toDomain)
    }

    override suspend fun saveOutfitHistory(entry: OutfitHistory) {
        ensureHistorySeeded(entry.userId)
        database.outfitHistoryDao().upsert(entry.toEntity())
    }

    override suspend fun deleteHistoryEntry(entryId: String) {
        database.outfitHistoryDao().deleteById(entryId)
    }

    override suspend fun clearHistory(userId: String) {
        val target = userId.ifBlank { DemoClothingCatalog.USER_ID }
        database.outfitHistoryDao().deleteAllByUser(target)
    }

    private suspend fun ensureHistorySeeded(userId: String) = seedMutex.withLock {
        val marker = "$HISTORY_SEED_PREFIX$userId"
        database.withTransaction {
            val metadataDao = database.appMetadataDao()
            if (metadataDao.getValue(marker) == null) {
                val dbClothes = database.clothingDao().getByUser(userId).map { it.toDomain() }
                val sourceClothes = dbClothes.ifEmpty { DemoClothingCatalog.starterItemsForUser(userId) }
                val starterHistory = DemoClothingCatalog.starterHistoryForUser(userId, sourceClothes)
                if (starterHistory.isNotEmpty()) {
                    database.outfitHistoryDao().insertAll(starterHistory.map(OutfitHistory::toEntity))
                }
                metadataDao.insert(AppMetadataEntity(marker, "done"))
            }
        }
    }

    private companion object {
        const val HISTORY_SEED_PREFIX = "history_seed_v3_"
    }
}
