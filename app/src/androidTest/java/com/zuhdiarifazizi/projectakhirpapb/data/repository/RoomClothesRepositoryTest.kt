package com.zuhdiarifazizi.projectakhirpapb.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zuhdiarifazizi.projectakhirpapb.data.local.AuraDatabase
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.usecase.CalculateOutfitScoreUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RoomClothesRepositoryTest {
    @Test
    fun demoCatalogSeedsExactlyOnceAndDoesNotReseedAfterDeletions() = runBlocking {
        val database = inMemoryDatabase()
        try {
            val repository = RoomClothesRepository(database)
            assertEquals(13, repository.getClothesByUser(DemoClothingCatalog.USER_ID).size)
            assertEquals(13, repository.getClothesByUser(DemoClothingCatalog.USER_ID).size)

            repository.getClothesByUser(DemoClothingCatalog.USER_ID).forEach { repository.deleteClothing(it.id) }
            assertTrue(repository.getClothesByUser(DemoClothingCatalog.USER_ID).isEmpty())
            assertTrue(RoomClothesRepository(database).getClothesByUser(DemoClothingCatalog.USER_ID).isEmpty())
        } finally {
            database.close()
        }
    }

    @Test
    fun userClothesPersistAndCreateUpdateDeleteChangesRecommendations() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val databaseName = "clothes-test-${UUID.randomUUID()}.db"
        val firstDatabase = Room.databaseBuilder(context, AuraDatabase::class.java, databaseName)
            .allowMainThreadQueries()
            .build()
        val topId = UUID.randomUUID().toString()
        val userTop = DemoClothingCatalog.items.first { it.category == ClothingCategory.TOP_WEAR }
            .copy(id = topId, name = "Kaos Demo Pengguna", occasion = Occasion.HANGOUT)
        try {
            val repository = RoomClothesRepository(firstDatabase)
            repository.upsertClothing(userTop)
            val useCase = CalculateOutfitScoreUseCase()
            val categories = setOf(ClothingCategory.TOP_WEAR, ClothingCategory.BOTTOM_WEAR)
            val firstClothes = repository.getClothesByUser(DemoClothingCatalog.USER_ID)
            assertTrue(useCase(firstClothes, Occasion.HANGOUT, categories).any { it.topWear?.id == topId })

            val edited = userTop.copy(name = "Kaos Pengguna Diperbarui", color = HsvColor(0f, 0.02f, 0.08f, "Hitam"))
            repository.upsertClothing(edited)
            val editedClothes = repository.getClothesByUser(DemoClothingCatalog.USER_ID)
            assertEquals(14, editedClothes.size)
            assertTrue(useCase(editedClothes, Occasion.HANGOUT, categories).any { it.topWear?.name == edited.name })

            firstDatabase.close()
            val reopenedDatabase = Room.databaseBuilder(context, AuraDatabase::class.java, databaseName)
                .allowMainThreadQueries()
                .build()
            try {
                val reopenedRepository = RoomClothesRepository(reopenedDatabase)
                val persisted = reopenedRepository.getClothesByUser(DemoClothingCatalog.USER_ID)
                assertEquals(14, persisted.size)
                assertTrue(persisted.any { it.id == topId && it.name == edited.name })

                reopenedRepository.deleteClothing(topId)
                val afterDelete = reopenedRepository.getClothesByUser(DemoClothingCatalog.USER_ID)
                assertEquals(13, afterDelete.size)
                assertTrue(useCase(afterDelete, Occasion.HANGOUT, categories).none { it.topWear?.id == topId })
            } finally {
                reopenedDatabase.close()
            }
        } finally {
            if (firstDatabase.isOpen) firstDatabase.close()
            context.deleteDatabase(databaseName)
        }
    }

    private fun inMemoryDatabase(): AuraDatabase = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(), AuraDatabase::class.java
    ).allowMainThreadQueries().build()
}
