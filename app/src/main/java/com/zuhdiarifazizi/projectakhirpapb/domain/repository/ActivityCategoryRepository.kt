package com.zuhdiarifazizi.projectakhirpapb.domain.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ActivityCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion

interface ActivityCategoryRepository {
    fun getCategories(): List<ActivityCategory>
    fun getCategoryByOccasion(occasion: Occasion): ActivityCategory?
}
