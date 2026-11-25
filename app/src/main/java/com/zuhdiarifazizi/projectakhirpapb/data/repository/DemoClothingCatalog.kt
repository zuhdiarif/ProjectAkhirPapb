package com.zuhdiarifazizi.projectakhirpapb.data.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion

object DemoClothingCatalog {
    const val USER_ID = "aura-demo-user"

    val items = listOf(
        clothing("shirt_white", "Kemeja Putih Polos", ClothingCategory.TOP_WEAR, ClothingPattern.SOLID, Occasion.WORK, "Putih", 0f, 0.02f, 0.98f, "demo_top_shirt_white"),
        clothing("shirt_navy", "Kemeja Biru Dongker", ClothingCategory.TOP_WEAR, ClothingPattern.SOLID, Occasion.WORK, "Biru dongker", 220f, 0.82f, 0.25f, "demo_top_shirt_navy"),
        clothing("tshirt_black", "Kaos Hitam Basic", ClothingCategory.TOP_WEAR, ClothingPattern.SOLID, Occasion.HANGOUT, "Hitam", 0f, 0.02f, 0.08f, "demo_top_tshirt_black"),
        clothing("tshirt_white", "Kaos Putih Basic", ClothingCategory.TOP_WEAR, ClothingPattern.SOLID, Occasion.HANGOUT, "Putih", 0f, 0.02f, 0.98f, "demo_top_tshirt_white"),
        clothing("hoodie_gray", "Hoodie Abu-Abu", ClothingCategory.TOP_WEAR, ClothingPattern.SOLID, Occasion.STREETWEAR, "Abu-abu", 0f, 0.02f, 0.52f, "demo_top_hoodie_gray"),
        clothing("blazer_black", "Blazer Hitam Formal", ClothingCategory.TOP_WEAR, ClothingPattern.SOLID, Occasion.WORK, "Hitam", 0f, 0.02f, 0.08f, "demo_top_blazer_black"),
        clothing("chinos_cream", "Celana Chino Cream", ClothingCategory.BOTTOM_WEAR, ClothingPattern.SOLID, Occasion.WORK, "Cream", 38f, 0.22f, 0.91f, "demo_bottom_chinos_cream"),
        clothing("jeans_blue", "Celana Jeans Biru", ClothingCategory.BOTTOM_WEAR, ClothingPattern.SOLID, Occasion.HANGOUT, "Biru denim", 211f, 0.54f, 0.56f, "demo_bottom_jeans_blue"),
        clothing("formal_black", "Celana Hitam Formal", ClothingCategory.BOTTOM_WEAR, ClothingPattern.SOLID, Occasion.WORK, "Hitam", 0f, 0.02f, 0.08f, "demo_bottom_formal_black"),
        clothing("cargo_olive", "Celana Cargo Olive", ClothingCategory.BOTTOM_WEAR, ClothingPattern.SOLID, Occasion.STREETWEAR, "Olive", 80f, 0.55f, 0.38f, "demo_bottom_cargo_olive"),
        clothing("outer_denim", "Jaket Denim", ClothingCategory.OUTER_WEAR, ClothingPattern.SOLID, Occasion.HANGOUT, "Biru denim", 211f, 0.54f, 0.56f, "demo_outer_denim"),
        clothing("outer_cardigan", "Cardigan Cream", ClothingCategory.OUTER_WEAR, ClothingPattern.SOLID, Occasion.OTHER, "Cream", 38f, 0.22f, 0.91f, "demo_outer_cardigan_cream"),
        clothing("outer_bomber", "Jaket Bomber Hitam", ClothingCategory.OUTER_WEAR, ClothingPattern.SOLID, Occasion.STREETWEAR, "Hitam", 0f, 0.02f, 0.08f, "demo_outer_bomber_black")
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
        imageAsset: String
    ) = ClothingItem(
        id = "demo_$id",
        userId = USER_ID,
        name = name,
        category = category,
        pattern = pattern,
        occasion = occasion,
        color = HsvColor(hue, saturation, value, colorName),
        imageUri = imageAsset,
        createdAtEpochMillis = 0L
    )
}
