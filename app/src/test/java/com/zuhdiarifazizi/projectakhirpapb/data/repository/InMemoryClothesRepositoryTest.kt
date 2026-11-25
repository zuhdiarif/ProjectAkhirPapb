package com.zuhdiarifazizi.projectakhirpapb.data.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryClothesRepositoryTest {
    @Test
    fun demoCatalogIncludesActualLocalImageKeysForAllGarmentCategories() = runBlocking {
        val repository = InMemoryClothesRepository()
        val clothes = repository.getClothesByUser(DemoClothingCatalog.USER_ID)

        assertEquals(13, clothes.size)
        assertEquals(setOf(ClothingCategory.TOP_WEAR, ClothingCategory.BOTTOM_WEAR, ClothingCategory.OUTER_WEAR), clothes.map { it.category }.toSet())
        assertTrue(clothes.all { it.imageUri?.isNotBlank() == true })
    }

    @Test
    fun supportsInMemoryUpsertAndDelete() = runBlocking {
        val repository = InMemoryClothesRepository()
        val original = DemoClothingCatalog.items.first()
        val changed = original.copy(name = "Nama yang diperbarui")

        repository.upsertClothing(changed)
        assertEquals(changed, repository.getClothingById(original.id))
        assertNotNull(repository.getClothingById(original.id))

        repository.deleteClothing(original.id)
        assertNull(repository.getClothingById(original.id))
    }
}
