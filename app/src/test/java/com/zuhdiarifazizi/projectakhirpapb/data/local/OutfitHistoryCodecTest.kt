package com.zuhdiarifazizi.projectakhirpapb.data.local

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import org.junit.Assert.assertEquals
import org.junit.Test

class OutfitHistoryCodecTest {

    @Test
    fun outfitHistoryCodec_roundTripsSpecialCharactersWithoutOrgJson() {
        val items = listOf(
            ClothingItem(
                id = "top_1",
                userId = "u1",
                name = "Kemeja Batik | Parang \\ Klasik",
                category = ClothingCategory.TOP_WEAR,
                pattern = ClothingPattern.OTHER,
                occasion = Occasion.WORK,
                color = HsvColor(27f, 0.68f, 0.48f, "Cokelat sogan"),
                imageUri = "demo_other_batik_brown",
                createdAtEpochMillis = 1234L,
                material = "Katun Primisima",
                rfidTagUid = "04:A1:B2:C3:D4"
            ),
            ClothingItem(
                id = "bottom_1",
                userId = "u1",
                name = "Celana Formal",
                category = ClothingCategory.BOTTOM_WEAR,
                pattern = ClothingPattern.SOLID,
                occasion = Occasion.WORK,
                color = HsvColor(0f, 0.02f, 0.08f, "Hitam"),
                imageUri = null,
                createdAtEpochMillis = 5678L,
                material = "Wol"
            )
        )

        val encoded = OutfitHistoryCodec.encodeItems(items)
        val decoded = OutfitHistoryCodec.decodeItems(encoded, fallbackUserId = "u1")

        assertEquals(2, decoded.size)
        assertEquals(items[0].name, decoded[0].name)
        assertEquals(items[0].rfidTagUid, decoded[0].rfidTagUid)
        assertEquals(items[1].color.colorName, decoded[1].color.colorName)
    }
}
