package com.zuhdiarifazizi.projectakhirpapb.domain.usecase

import com.zuhdiarifazizi.projectakhirpapb.data.repository.DemoClothingCatalog
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateOutfitScoreUseCaseTest {
    private val useCase = CalculateOutfitScoreUseCase()
    private val requiredCategories = setOf(ClothingCategory.TOP_WEAR, ClothingCategory.BOTTOM_WEAR)

    @Test
    fun recommendationsUseOnlyAvailableItemsAndAlwaysHaveTopAndBottom() {
        val available = DemoClothingCatalog.items
        val recommendations = useCase(available, Occasion.WORK, requiredCategories)

        assertTrue(recommendations.isNotEmpty())
        recommendations.forEach { recommendation ->
            assertNotNull(recommendation.topWear)
            assertNotNull(recommendation.bottomWear)
            assertTrue(recommendation.topWear?.let(available::contains) == true)
            assertTrue(recommendation.bottomWear?.let(available::contains) == true)
            assertTrue(recommendation.outerWear == null || available.contains(recommendation.outerWear))
        }
    }

    @Test
    fun outerwearIsOptionalEvenWhenRequestedButUnavailable() {
        val top = DemoClothingCatalog.items.first { it.category == ClothingCategory.TOP_WEAR }
        val bottom = DemoClothingCatalog.items.first { it.category == ClothingCategory.BOTTOM_WEAR }
        val selectedWithOuter = requiredCategories + ClothingCategory.OUTER_WEAR

        val recommendations = useCase(listOf(top, bottom), Occasion.WORK, selectedWithOuter)

        assertEquals(1, recommendations.size)
        assertEquals(null, recommendations.single().outerWear)
    }

    @Test
    fun activityCategoryChangesActivityScore() {
        val top = DemoClothingCatalog.items.first { it.name == "Kemeja Putih Polos" }
        val bottom = DemoClothingCatalog.items.first { it.name == "Celana Chino Cream" }

        val work = useCase(listOf(top, bottom), Occasion.WORK, requiredCategories).single()
        val streetwear = useCase(listOf(top, bottom), Occasion.STREETWEAR, requiredCategories).single()

        assertTrue(work.occasionScore > streetwear.occasionScore)
        assertTrue(work.totalScore > streetwear.totalScore)
    }

    @Test
    fun missingRequiredTopOrBottomReturnsNoRecommendations() {
        val onlyTop = DemoClothingCatalog.items.filter { it.category == ClothingCategory.TOP_WEAR }

        assertTrue(useCase(onlyTop, Occasion.WORK, requiredCategories).isEmpty())
        assertTrue(useCase(DemoClothingCatalog.items, Occasion.WORK, setOf(ClothingCategory.TOP_WEAR)).isEmpty())
    }

    @Test
    fun recentlyWornOutfitAppliesHistoryPenaltyWithSortedIdSignature() {
        val top = DemoClothingCatalog.items.first { it.id == "demo_shirt_navy" }
        val bottom = DemoClothingCatalog.items.first { it.id == "demo_chinos_cream" }
        val sortedSignature = listOf(top.id, bottom.id).sorted().joinToString("|")

        val baseline = useCase(listOf(top, bottom), Occasion.WORK, requiredCategories).single()
        val penalized = useCase(
            clothes = listOf(top, bottom),
            targetOccasion = Occasion.WORK,
            selectedCategories = requiredCategories,
            recentlyWornSignatures = setOf(sortedSignature)
        ).single()

        assertEquals(0.0, baseline.historyPenalty, 0.0001)
        assertEquals(CalculateOutfitScoreUseCase.RECENT_HISTORY_PENALTY, penalized.historyPenalty, 0.0001)
        assertTrue(penalized.totalScore < baseline.totalScore)
    }
}
