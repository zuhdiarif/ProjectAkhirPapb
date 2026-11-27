package com.zuhdiarifazizi.projectakhirpapb.data.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitHistory

object DemoClothingCatalog {
    const val USER_ID = "aura-demo-user"
    const val DEMO_RFID_UID = "04:A1:B2:C3:D4"
    private const val DAY_MILLIS = 24L * 60L * 60L * 1000L

    val items: List<ClothingItem> by lazy {
        val now = System.currentTimeMillis()
        listOf(
            // 13 pakaian koleksi awal
            clothing("shirt_navy", "Kemeja Biru Dongker", ClothingCategory.TOP_WEAR, ClothingPattern.SOLID, Occasion.WORK, "Biru dongker", 220f, 0.82f, 0.25f, "demo_top_shirt_navy", "Katun Halus", 2, now),
            clothing("chinos_cream", "Celana Chino Cream", ClothingCategory.BOTTOM_WEAR, ClothingPattern.SOLID, Occasion.WORK, "Cream", 38f, 0.22f, 0.91f, "demo_bottom_chinos_cream", "Twill Katun", 4, now),
            clothing("blazer_black", "Blazer Hitam Formal", ClothingCategory.TOP_WEAR, ClothingPattern.SOLID, Occasion.WORK, "Hitam", 0f, 0.02f, 0.08f, "demo_top_blazer_black", "Acara Resmi", 7, now),
            clothing("shirt_white", "Kemeja Putih Polos", ClothingCategory.TOP_WEAR, ClothingPattern.SOLID, Occasion.WORK, "Putih", 0f, 0.02f, 0.98f, "demo_top_shirt_white", "Katun Oxford", 3, now),
            clothing("tshirt_black", "Kaos Hitam Basic", ClothingCategory.TOP_WEAR, ClothingPattern.SOLID, Occasion.HANGOUT, "Hitam", 0f, 0.02f, 0.08f, "demo_top_tshirt_black", "Katun Combed", 1, now),
            clothing("tshirt_white", "Kaos Putih Basic", ClothingCategory.TOP_WEAR, ClothingPattern.SOLID, Occasion.HANGOUT, "Putih", 0f, 0.02f, 0.98f, "demo_top_tshirt_white", "Katun Combed", 5, now),
            clothing("hoodie_gray", "Hoodie Abu-Abu", ClothingCategory.TOP_WEAR, ClothingPattern.SOLID, Occasion.STREETWEAR, "Abu-abu", 0f, 0.02f, 0.52f, "demo_top_hoodie_gray", "Fleece Tebal", 6, now),
            clothing("jeans_blue", "Celana Jeans Biru", ClothingCategory.BOTTOM_WEAR, ClothingPattern.SOLID, Occasion.HANGOUT, "Biru denim", 211f, 0.54f, 0.56f, "demo_bottom_jeans_blue", "Denim", 2, now),
            clothing("formal_black", "Celana Hitam Formal", ClothingCategory.BOTTOM_WEAR, ClothingPattern.SOLID, Occasion.WORK, "Hitam", 0f, 0.02f, 0.08f, "demo_bottom_formal_black", "Wol Formal", 5, now),
            clothing("cargo_olive", "Celana Cargo Olive", ClothingCategory.BOTTOM_WEAR, ClothingPattern.SOLID, Occasion.STREETWEAR, "Olive", 80f, 0.55f, 0.38f, "demo_bottom_cargo_olive", "Kanvas Ripstop", 9, now),
            clothing("outer_denim", "Jaket Denim", ClothingCategory.OUTER_WEAR, ClothingPattern.SOLID, Occasion.HANGOUT, "Biru denim", 211f, 0.54f, 0.56f, "demo_outer_denim", "Denim", 3, now),
            clothing("outer_cardigan", "Cardigan Cream", ClothingCategory.OUTER_WEAR, ClothingPattern.SOLID, Occasion.OTHER, "Cream", 38f, 0.22f, 0.91f, "demo_outer_cardigan_cream", "Rajut Lembut", 10, now),
            clothing("outer_bomber", "Jaket Bomber Hitam", ClothingCategory.OUTER_WEAR, ClothingPattern.SOLID, Occasion.STREETWEAR, "Hitam", 0f, 0.02f, 0.08f, "demo_outer_bomber_black", "Parasut", 4, now),

            // 9 pakaian tambahan bervariasi (Olahraga, Batik/Acara Khusus, Bergaris, Kotak-kotak, Bergambar, warna cerah/netral)
            clothing("sport_jersey_blue", "Kaos Olahraga Dry-Fit Biru", ClothingCategory.TOP_WEAR, ClothingPattern.STRIPED, Occasion.EXERCISE, "Biru elektrik", 212f, 0.78f, 0.85f, "demo_sport_jersey_blue", "Dry-Fit Adem", 5, now),
            clothing("sport_jogger_black", "Celana Jogger Olahraga Hitam", ClothingCategory.BOTTOM_WEAR, ClothingPattern.SOLID, Occasion.EXERCISE, "Hitam arang", 210f, 0.08f, 0.16f, "demo_sport_jogger_black", "Spandeks Elastis", 5, now),
            clothing("sport_windbreaker_sage", "Jaket Lari Hijau Sage", ClothingCategory.OUTER_WEAR, ClothingPattern.SOLID, Occasion.EXERCISE, "Hijau sage", 136f, 0.33f, 0.62f, "demo_sport_windbreaker_sage", "Parasut Ringan", null, now),
            clothing("other_batik_brown", "Kemeja Batik Sogan Lengan Panjang", ClothingCategory.TOP_WEAR, ClothingPattern.OTHER, Occasion.OTHER, "Cokelat sogan", 27f, 0.68f, 0.48f, "demo_other_batik_brown", "Katun Primisima", null, now),
            clothing("other_trousers_charcoal", "Celana Bahan Abu Tua", ClothingCategory.BOTTOM_WEAR, ClothingPattern.SOLID, Occasion.OTHER, "Abu tua", 215f, 0.11f, 0.31f, "demo_other_trousers_charcoal", "Wol Halus", 14, now),
            clothing("work_striped_lightblue", "Kemeja Salur Biru Muda", ClothingCategory.TOP_WEAR, ClothingPattern.STRIPED, Occasion.WORK, "Biru muda", 204f, 0.36f, 0.92f, "demo_work_striped_lightblue", "Katun Poplin", null, now, rfidTagUid = DEMO_RFID_UID),
            clothing("hangout_flannel_maroon", "Kemeja Flanel Kotak Maroon", ClothingCategory.TOP_WEAR, ClothingPattern.PLAID, Occasion.HANGOUT, "Maroon", 352f, 0.74f, 0.46f, "demo_hangout_flannel_maroon", "Flanel Tebal", null, now),
            clothing("street_graphic_mustard", "Kaos Oversize Kuning Mustard", ClothingCategory.TOP_WEAR, ClothingPattern.GRAPHIC, Occasion.STREETWEAR, "Kuning mustard", 42f, 0.78f, 0.88f, "demo_street_graphic_mustard", "Katun Heavyweight", null, now),
            clothing("hangout_chino_terracotta", "Celana Chino Terracotta", ClothingCategory.BOTTOM_WEAR, ClothingPattern.SOLID, Occasion.HANGOUT, "Terracotta", 12f, 0.64f, 0.74f, "demo_hangout_chino_terracotta", "Twill Stretch", 12, now)
        )
    }

    fun starterItemsForUser(userId: String): List<ClothingItem> {
        if (userId == USER_ID) return items
        return items.map { item ->
            item.copy(
                id = "${userId}_${item.id.removePrefix("demo_")}",
                userId = userId,
                rfidTagUid = null
            )
        }
    }

    fun starterHistoryForUser(
        userId: String,
        userClothes: List<ClothingItem>,
        nowEpochMillis: Long = System.currentTimeMillis()
    ): List<OutfitHistory> {
        if (userClothes.isEmpty()) return emptyList()
        val bySuffix = userClothes.associateBy { it.id.substringAfter("${userId}_").removePrefix("demo_") }

        val workTop = bySuffix["shirt_navy"]
        val workBottom = bySuffix["chinos_cream"]
        val sportTop = bySuffix["sport_jersey_blue"]
        val sportBottom = bySuffix["sport_jogger_black"]

        return buildList {
            if (workTop != null && workBottom != null) {
                val chosen = listOf(workTop, workBottom)
                add(
                    OutfitHistory(
                        id = "seed_${userId}_work_1",
                        userId = userId,
                        outfitId = chosen.map { it.id }.sorted().joinToString("|"),
                        items = chosen,
                        occasion = Occasion.WORK,
                        score = 0.92,
                        wornAtEpochMillis = nowEpochMillis - 2L * DAY_MILLIS
                    )
                )
            }
            if (sportTop != null && sportBottom != null) {
                val chosen = listOf(sportTop, sportBottom)
                add(
                    OutfitHistory(
                        id = "seed_${userId}_sport_1",
                        userId = userId,
                        outfitId = chosen.map { it.id }.sorted().joinToString("|"),
                        items = chosen,
                        occasion = Occasion.EXERCISE,
                        score = 0.94,
                        wornAtEpochMillis = nowEpochMillis - 5L * DAY_MILLIS
                    )
                )
            }
        }
    }

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
        daysAgo: Long?,
        nowEpochMillis: Long,
        rfidTagUid: String? = null
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
        rfidTagUid = rfidTagUid,
        lastWornEpochMillis = daysAgo?.let { nowEpochMillis - it * DAY_MILLIS }
    )
}
