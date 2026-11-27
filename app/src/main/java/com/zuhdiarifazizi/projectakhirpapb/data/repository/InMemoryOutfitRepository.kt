package com.zuhdiarifazizi.projectakhirpapb.data.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitHistory
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.OutfitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryOutfitRepository(
    seedEntries: List<OutfitHistory> = emptyList()
) : OutfitRepository {

    private val entries = MutableStateFlow(seedEntries.sortedByDescending { it.wornAtEpochMillis })

    override fun observeHistory(userId: String): Flow<List<OutfitHistory>> =
        entries.map { list ->
            list.filter { it.userId == userId }
                .sortedByDescending { it.wornAtEpochMillis }
        }

    override suspend fun getHistorySince(userId: String, fromEpochMillis: Long): List<OutfitHistory> =
        entries.value
            .filter { it.userId == userId && it.wornAtEpochMillis >= fromEpochMillis }
            .sortedByDescending { it.wornAtEpochMillis }

    override suspend fun saveOutfitHistory(entry: OutfitHistory) {
        entries.value = (entries.value.filterNot { it.id == entry.id } + entry)
            .sortedByDescending { it.wornAtEpochMillis }
    }

    override suspend fun deleteHistoryEntry(entryId: String) {
        entries.value = entries.value.filterNot { it.id == entryId }
    }

    override suspend fun clearHistory(userId: String) {
        entries.value = entries.value.filterNot { it.userId == userId }
    }
}
