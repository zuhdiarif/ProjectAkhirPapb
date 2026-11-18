package com.zuhdiarifazizi.projectakhirpapb.data.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitHistory
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.OutfitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class InMemoryOutfitRepository : OutfitRepository {

    override fun observeHistory(userId: String): Flow<List<OutfitHistory>> = flowOf(emptyList())

    override suspend fun getHistorySince(userId: String, fromEpochMillis: Long): List<OutfitHistory> =
        emptyList()

    override suspend fun saveOutfitHistory(entry: OutfitHistory) {
    }
}
