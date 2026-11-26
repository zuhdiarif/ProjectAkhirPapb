package com.zuhdiarifazizi.projectakhirpapb.data.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion

object DemoClothingCatalog {
    const val USER_ID = "aura-demo-user"
    private const val DAY_MILLIS = 24L * 60L * 60L * 1000L

    val items = listOf(
        clothing("shirt_navy", "Kemeja Biru Dongker", ClothingCategory.TOP_WEAR, ClothingPattern.SOLID, Occasion.WORK, "Biru dongker", 220f, 0.82f, 0.25f, "demo_top_shirt_navy", "Katun Halus", 2),
        clothing("chinos_cream", "Celana Chino Cream", ClothingCategory.BOTTOM_WEAR, ClothingPattern.SOLID, Occasion.WORK, "Cream", 38f, 0.22f, 0.91f, "demo_bottom_chinos_cream", "Twill Katun", 4),
        clothing("blazer_black", "Blazer Hitam Formal", ClothingCategory.TOP_WEAR, ClothingPattern.SOLID, Occasion.WORK, "Hitam", 0f, 0.02f, 0.08f, "demo_top_blazer_black", "Acara Resmi", 7),
        clothing("shirt_white", "Kemeja Putih Polos", ClothingCategory.TOP_WEAR, ClothingPattern.SOLID, Occasion.WORK, "Putih", 0f, 0.02f, 0.98f, "demo_top_shirt_white", "Katun Oxford", 3),
        clothing("tshirt_black", "Kaos Hitam Basic", ClothingCategory.TOP_WEAR, ClothingPattern.SOLID, Occasion.HANGOUT, "Hitam", 0f, 0.02f, 0.08f, "demo_top_tshirt_black", "Katun Combed", 1),
        clothing("tshirt_white", "Kaos Putih Basic", ClothingCategory.TOP_WEAR, ClothingPattern.SOLID, Occasion.HANGOUT, "Putih", 0f, 0.02f, 0.98f, "demo_top_tshirt_white", "Katun Combed", 5),
        clothing("hoodie_gray", "Hoodie Abu-Abu", ClothingCategory.TOP_WEAR, ClothingPattern.SOLID, Occasion.STREETWEAR, "Abu-abu", 0f, 0.02f, 0.52f, "demo_top_hoodie_gray", "Fleece Tebal", 6),
        clothing("jeans_blue", "Celana Jeans Biru", ClothingCategory.BOTTOM_WEAR, ClothingPattern.SOLID, Occasion.HANGOUT, "Biru denim", 211f, 0.54f, 0.56f, "demo_bottom_jeans_blue", "Denim", 2),
        clothing("formal_black", "Celana Hitam Formal", ClothingCategory.BOTTOM_WEAR, ClothingPattern.SOLID, Occasion.WORK, "Hitam", 0f, 0.02f, 0.08f, "demo_bottom_formal_black", "Wol Formal", 5),
        clothing("cargo_olive", "Celana Cargo Olive", ClothingCategory.BOTTOM_WEAR, ClothingPattern.SOLID, Occasion.STREETWEAR, "Olive", 80f, 0.55f, 0.38f, "demo_bottom_cargo_olive", "Kanvas Ripstop", 9),
        clothing("outer_denim", "Jaket Denim", ClothingCategory.OUTER_WEAR, ClothingPattern.SOLID, Occasion.HANGOUT, "Biru denim", 211f, 0.54f, 0.56f, "demo_outer_denim", "Denim", 3),
        clothing("outer_cardigan", "Cardigan Cream", ClothingCategory.OUTER_WEAR, ClothingPattern.SOLID, Occasion.OTHER, "Cream", 38f, 0.22f, 0.91f, "demo_outer_cardigan_cream", "Rajut Lembut", 10),
        clothing("outer_bomber", "Jaket Bomber Hitam", ClothingCategory.OUTER_WEAR, ClothingPattern.SOLID, Occasion.STREETWEAR, "Hitam", 0f, 0.02f, 0.08f, "demo_outer_bomber_black", "Parasut", 4)
    )

    private fun clothing(
        id: String,
        name: String,
        category: ClothingCategory,
        pattern: ClothingPattern,
        occasion: Occasion,
        colorName: String,
        hue: Float,
        saturation: Float,
        value: Float,
        imageAsset: String,
        material: String,
        daysAgo: Long
    ) = ClothingItem(
        id = "demo_$id",
        userId = USER_ID,
        name = name,
        category = category,
        pattern = pattern,
        occasion = occasion,
        color = HsvColor(hue, saturation, value, colorName),
        imageUri = imageAsset,
        createdAtEpochMillis = 0L,
        material = material,
        lastWornEpochMillis = System.currentTimeMillis() - daysAgo * DAY_MILLIS
    )
}
