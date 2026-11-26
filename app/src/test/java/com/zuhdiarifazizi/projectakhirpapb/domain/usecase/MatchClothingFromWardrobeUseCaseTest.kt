package com.zuhdiarifazizi.projectakhirpapb.domain.usecase

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MatchClothingFromWardrobeUseCaseTest {

    private val useCase = MatchClothingFromWardrobeUseCase()

    private val wardrobe = listOf(
        ClothingItem(
            id = "top_navy",
            userId = "u1",
            name = "Kemeja Biru Dongker",
            category = ClothingCategory.TOP_WEAR,
            pattern = ClothingPattern.SOLID,
            occasion = Occasion.WORK,
            color = HsvColor(220f, 0.82f, 0.25f, "Biru dongker"),
            rfidTagUid = "04:AB:12:7E"
        ),
        ClothingItem(
            id = "bottom_navy",
            userId = "u1",
            name = "Celana Chino Navy",
            category = ClothingCategory.BOTTOM_WEAR,
            pattern = ClothingPattern.SOLID,
            occasion = Occasion.WORK,
            color = HsvColor(220f, 0.82f, 0.25f, "Biru dongker"),
            rfidTagUid = "12:34:56:78"
        ),
        ClothingItem(
            id = "top_cream",
            userId = "u1",
            name = "Kaos Cream",
            category = ClothingCategory.TOP_WEAR,
            pattern = ClothingPattern.SOLID,
            occasion = Occasion.HANGOUT,
            color = HsvColor(38f, 0.22f, 0.91f, "Cream")
        )
    )

    @Test
    fun matchByRfid_normalizesInputFormatsAndReturnsMatchingGarment() {
        assertEquals("top_navy", useCase.matchByRfid(wardrobe, "04ab127e")?.id)
        assertEquals("bottom_navy", useCase.matchByRfid(wardrobe, " 12-34-56-78 ")?.id)
        assertNull(useCase.matchByRfid(wardrobe, "FF:FF:FF:FF"))
        assertNull(useCase.matchByRfid(wardrobe, "INVALID"))
    }

    @Test
    fun matchByVisualSimilarity_prefersMatchingCategoryAndClosestHsvColor() {
        val detectedColor = HsvColor(219f, 0.80f, 0.27f, "Biru dongker")
        val topMatch = useCase.matchByVisualSimilarity(
            clothes = wardrobe,
            detectedColor = detectedColor,
            detectedCategory = ClothingCategory.TOP_WEAR
        )

        assertEquals("top_navy", topMatch?.item?.id)
        assertEquals(1.0, topMatch?.similarityScore ?: 0.0, 0.05)

        val bottomMatch = useCase.matchByVisualSimilarity(
            clothes = wardrobe,
            detectedColor = detectedColor,
            detectedCategory = ClothingCategory.BOTTOM_WEAR
        )
        assertEquals("bottom_navy", bottomMatch?.item?.id)

        assertNull(useCase.matchByVisualSimilarity(emptyList(), detectedColor))
    }
}
