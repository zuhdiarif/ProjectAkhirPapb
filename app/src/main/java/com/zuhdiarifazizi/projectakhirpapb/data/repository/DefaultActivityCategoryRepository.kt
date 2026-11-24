package com.zuhdiarifazizi.projectakhirpapb.data.repository

import com.zuhdiarifazizi.projectakhirpapb.R
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ActivityCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ActivityCategoryRepository

class DefaultActivityCategoryRepository : ActivityCategoryRepository {

    private val categories = listOf(
        ActivityCategory(
            id = "work",
            occasion = Occasion.WORK,
            title = "Kerja",
            description = "Kantor, rapat & formal",
            iconResId = R.drawable.ic_work
        ),
        ActivityCategory(
            id = "hangout",
            occasion = Occasion.HANGOUT,
            title = "Nongkrong",
            description = "Kopi, makan & teman",
            iconResId = R.drawable.ic_hangout
        ),
        ActivityCategory(
            id = "sport",
            occasion = Occasion.EXERCISE,
            title = "Olahraga",
            description = "Gym, lari & aktif",
            iconResId = R.drawable.ic_sport
        ),
        ActivityCategory(
            id = "streetwear",
            occasion = Occasion.STREETWEAR,
            title = "Streetwear",
            description = "Gaya santai, jaket & sneakers",
            iconResId = R.drawable.ic_streetwear
        ),
        ActivityCategory(
            id = "other",
            occasion = Occasion.OTHER,
            title = "Lainnya",
            description = "Acara khusus atau cuaca",
            iconResId = R.drawable.ic_other
        )
    )

    override fun getCategories(): List<ActivityCategory> = categories

    override fun getCategoryByOccasion(occasion: Occasion): ActivityCategory? {
        return categories.firstOrNull { it.occasion == occasion }
    }
}
