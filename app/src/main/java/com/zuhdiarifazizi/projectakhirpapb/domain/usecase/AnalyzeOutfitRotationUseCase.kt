package com.zuhdiarifazizi.projectakhirpapb.domain.usecase

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitHistory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitHistoryAnalysis
import com.zuhdiarifazizi.projectakhirpapb.domain.model.RotationRecommendation

/**
 * Analyzes outfit usage frequency across the last 30 days, marks outfits worn in the
 * last 7 days for score penalties, and highlights rarely worn garments for rotation.
 */
class AnalyzeOutfitRotationUseCase {

    operator fun invoke(
        clothes: List<ClothingItem>,
        historyEntries: List<OutfitHistory>,
        nowEpochMillis: Long = System.currentTimeMillis(),
        lowUsageThreshold: Int = DEFAULT_LOW_USAGE_THRESHOLD
    ): OutfitHistoryAnalysis {
        val thirtyDaysAgo = nowEpochMillis - THIRTY_DAYS_MILLIS
        val sevenDaysAgo = nowEpochMillis - SEVEN_DAYS_MILLIS

        val last30Days = historyEntries
            .filter { it.wornAtEpochMillis in thirtyDaysAgo..nowEpochMillis }
            .sortedByDescending { it.wornAtEpochMillis }

        val frequencyMap = mutableMapOf<String, Int>()
        val recentSignatures = mutableSetOf<String>()

        for (entry in last30Days) {
            for (item in entry.items) {
                frequencyMap[item.id] = (frequencyMap[item.id] ?: 0) + 1
            }
            if (entry.wornAtEpochMillis >= sevenDaysAgo) {
                recentSignatures += entry.items.map { it.id }.sorted().joinToString("|")
            }
        }

        val rotationCandidates = clothes
            .map { item ->
                val count = frequencyMap[item.id] ?: 0
                RotationRecommendation(
                    item = item,
                    usageCountLast30Days = count,
                    isRarelyWorn = count < lowUsageThreshold
                )
            }
            .sortedWith(compareBy<RotationRecommendation> { it.usageCountLast30Days }.thenBy { it.item.name })

        return OutfitHistoryAnalysis(
            historyLast30Days = last30Days,
            itemUsageFrequency = frequencyMap,
            recentlyWornOutfitSignatures = recentSignatures,
            rotationCandidates = rotationCandidates
        )
    }

    companion object {
        private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
        private const val THIRTY_DAYS_MILLIS = 30L * MILLIS_PER_DAY
        private const val SEVEN_DAYS_MILLIS = 7L * MILLIS_PER_DAY
        private const val DEFAULT_LOW_USAGE_THRESHOLD = 2
    }
}
