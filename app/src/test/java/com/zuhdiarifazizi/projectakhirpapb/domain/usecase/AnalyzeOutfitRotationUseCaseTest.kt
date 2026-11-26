package com.zuhdiarifazizi.projectakhirpapb.domain.usecase

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitHistory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyzeOutfitRotationUseCaseTest {

    private val useCase = AnalyzeOutfitRotationUseCase()
    private val dayMillis = 24L * 60L * 60L * 1000L

    private fun item(id: String, name: String) = ClothingItem(
        id = id,
        userId = "u1",
        name = name,
        category = ClothingCategory.TOP_WEAR,
        pattern = ClothingPattern.SOLID,
        occasion = Occasion.WORK,
        color = HsvColor(0f, 0.02f, 0.98f, "Putih")
    )

    @Test
    fun invoke_filtersThirtyDayHistoryCountsUsageFlagsRecentSignaturesAndSuggestsUnworn() {
        val now = 1_000L * dayMillis
        val shirtA = item("a", "Kemeja A")
        val chinoB = item("b", "Celana B")
        val jacketC = item("c", "Jaket C")
        val clothes = listOf(shirtA, chinoB, jacketC)

        val history = listOf(
            OutfitHistory("h1", "u1", "o1", listOf(chinoB, shirtA), Occasion.WORK, 0.9, now - 2L * dayMillis),
            OutfitHistory("h2", "u1", "o2", listOf(shirtA), Occasion.WORK, 0.85, now - 14L * dayMillis),
            OutfitHistory("h3_old", "u1", "o3", listOf(jacketC), Occasion.WORK, 0.8, now - 35L * dayMillis)
        )

        val analysis = useCase(clothes, history, nowEpochMillis = now, lowUsageThreshold = 2)

        assertEquals(2, analysis.historyLast30Days.size)
        assertEquals(2, analysis.itemUsageFrequency["a"])
        assertEquals(1, analysis.itemUsageFrequency["b"])
        assertEquals(null, analysis.itemUsageFrequency["c"])

        assertTrue(analysis.recentlyWornOutfitSignatures.contains("a|b"))
        assertEquals(listOf("c", "b", "a"), analysis.rotationCandidates.map { it.item.id })
        assertTrue(analysis.rotationCandidates.first { it.item.id == "c" }.isRarelyWorn)
        assertFalse(analysis.rotationCandidates.first { it.item.id == "a" }.isRarelyWorn)
    }
}
