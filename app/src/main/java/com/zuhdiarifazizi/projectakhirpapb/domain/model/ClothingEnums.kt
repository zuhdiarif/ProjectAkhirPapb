package com.zuhdiarifazizi.projectakhirpapb.domain.model

enum class ClothingCategory(val displayName: String) {
    TOP_WEAR("Top Wear"),
    BOTTOM_WEAR("Bottom Wear"),
    OUTER_WEAR("Outer Wear")
}

enum class Occasion(val displayName: String) {
    WORK("Work"),
    HANGOUT("Hangout"),
    EXERCISE("Exercise"),
    STREETWEAR("Streetwear"),
    OTHER("Other")
}

enum class ClothingPattern(val displayName: String) {
    SOLID("Solid"),
    STRIPED("Striped"),
    PLAID("Plaid"),
    FLORAL("Floral"),
    GRAPHIC("Graphic"),
    OTHER("Other")
}
