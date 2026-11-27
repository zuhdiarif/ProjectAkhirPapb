package com.zuhdiarifazizi.projectakhirpapb.data.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryClothesRepositoryTest {
    @Test
    fun demoCatalogIncludesVariedStarterGarmentsForAllCategoriesAndOccasions() = runBlocking {
        val repository = InMemoryClothesRepository()
        val clothes = repository.getClothesByUser(DemoClothingCatalog.USER_ID)

        assertEquals(22, clothes.size)
        assertEquals(
            setOf(ClothingCategory.TOP_WEAR, ClothingCategory.BOTTOM_WEAR, ClothingCategory.OUTER_WEAR),
            clothes.map { it.category }.toSet()
        )
        assertEquals(Occasion.entries.toSet(), clothes.map { it.occasion }.toSet())
        assertTrue(clothes.map { it.pattern }.toSet().size >= 5)
        assertTrue(clothes.all { it.imageUri?.isNotBlank() == true })
        assertNotNull(repository.getClothingByRfid(DemoClothingCatalog.DEMO_RFID_UID))
    }

    @Test
    fun seedStarterWardrobe_createsIsolatedVariedCollectionForNewUsers() = runBlocking {
        val repository = InMemoryClothesRepository()
        repository.seedStarterWardrobe("user-baru-1")
        val newUserClothes = repository.getClothesByUser("user-baru-1")

        assertEquals(22, newUserClothes.size)
        assertTrue(newUserClothes.all { it.id.startsWith("user-baru-1_") && it.rfidTagUid == null })
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
