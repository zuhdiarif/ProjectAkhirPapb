package com.zuhdiarifazizi.projectakhirpapb.data.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class DefaultActivityCategoryRepositoryTest {

    private lateinit var repository: DefaultActivityCategoryRepository

    @Before
    fun setUp() {
        repository = DefaultActivityCategoryRepository()
    }

    @Test
    fun getCategories_returnsFiveCategories() {
        val categories = repository.getCategories()
        assertEquals(5, categories.size)
        assertEquals("Kerja", categories[0].title)
        assertEquals("Nongkrong", categories[1].title)
        assertEquals("Olahraga", categories[2].title)
        assertEquals("Streetwear", categories[3].title)
        assertEquals("Lainnya", categories[4].title)
    }

    @Test
    fun getCategoryByOccasion_returnsCorrectCategory() {
        val workCategory = repository.getCategoryByOccasion(Occasion.WORK)
        assertNotNull(workCategory)
        assertEquals("Kerja", workCategory?.title)
        assertEquals(Occasion.WORK, workCategory?.occasion)

        val sportCategory = repository.getCategoryByOccasion(Occasion.EXERCISE)
        assertNotNull(sportCategory)
        assertEquals("Olahraga", sportCategory?.title)
    }
}
