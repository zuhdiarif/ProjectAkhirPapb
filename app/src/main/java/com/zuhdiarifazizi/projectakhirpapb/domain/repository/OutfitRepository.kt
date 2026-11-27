package com.zuhdiarifazizi.projectakhirpapb.domain.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitHistory
import kotlinx.coroutines.flow.Flow

interface OutfitRepository {
    fun observeHistory(userId: String): Flow<List<OutfitHistory>>
    suspend fun getHistorySince(userId: String, fromEpochMillis: Long): List<OutfitHistory>
    suspend fun saveOutfitHistory(entry: OutfitHistory)
    suspend fun deleteHistoryEntry(entryId: String) {}
    suspend fun clearHistory(userId: String) {}
}
