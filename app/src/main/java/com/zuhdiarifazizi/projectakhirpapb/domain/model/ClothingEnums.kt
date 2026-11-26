package com.zuhdiarifazizi.projectakhirpapb.domain.model

enum class ClothingCategory(val displayName: String) {
    TOP_WEAR("Atasan"),
    BOTTOM_WEAR("Bawahan"),
    OUTER_WEAR("Pakaian luar")
}

enum class Occasion(val displayName: String) {
    WORK("Kerja"),
    HANGOUT("Nongkrong"),
    EXERCISE("Olahraga"),
    STREETWEAR("Streetwear"),
    OTHER("Lainnya")
}

enum class ClothingPattern(val displayName: String) {
    SOLID("Polos"),
    STRIPED("Bergaris"),
    PLAID("Kotak-kotak"),
    FLORAL("Motif bunga"),
    GRAPHIC("Bergambar"),
    OTHER("Lainnya")
}
