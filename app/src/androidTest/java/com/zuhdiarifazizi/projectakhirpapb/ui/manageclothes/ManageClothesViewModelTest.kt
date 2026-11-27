package com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zuhdiarifazizi.projectakhirpapb.data.device.ClothingPhotoStorage
import com.zuhdiarifazizi.projectakhirpapb.data.local.AuraDatabase
import com.zuhdiarifazizi.projectakhirpapb.data.remote.DefaultClothingClassifier
import com.zuhdiarifazizi.projectakhirpapb.data.repository.RoomClothesRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.HapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.TextToSpeechManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManageClothesViewModelTest {
    @Test
    fun saveValidClothingWritesItToRepositoryForRecommendations() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, AuraDatabase::class.java).allowMainThreadQueries().build()
        val tts = TestTtsManager()
        val haptics = TestHapticManager()
        try {
            val repository = RoomClothesRepository(database)
            val viewModel = ManageClothesViewModel(repository, ClothingPhotoStorage(context), tts, haptics, DefaultClothingClassifier())
            viewModel.openAddForm()
            viewModel.onNameChange("Kaos Uji Pengguna")
            viewModel.onColorChange("Biru")
            viewModel.onOccasionChange(com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion.HANGOUT)
            viewModel.saveForm()

            val state = withTimeout(5_000) {
                viewModel.uiState.first { it.page == ManageClothesPage.LIST && it.items.any { item -> item.name == "Kaos Uji Pengguna" } }
            }
            assertEquals(14, state.items.size)
            assertTrue(tts.lastSpeech.orEmpty().contains("tersedia untuk rekomendasi"))
            assertTrue(haptics.successCount > 0)
        } finally {
            database.close()
        }
    }

    @Test
    fun emptyNameAndColorAreClearlyRejected() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, AuraDatabase::class.java).allowMainThreadQueries().build()
        val tts = TestTtsManager()
        val haptics = TestHapticManager()
        try {
            val repository = RoomClothesRepository(database)
            val viewModel = ManageClothesViewModel(repository, ClothingPhotoStorage(context), tts, haptics, DefaultClothingClassifier())
            viewModel.openAddForm()
            viewModel.saveForm()

            val state = withTimeout(5_000) {
                viewModel.uiState.first {
                    it.items.size == 13 && it.form.nameError != null && it.form.colorError != null
                }
            }
            assertEquals(13, state.items.size)
            assertTrue(tts.lastSpeech.orEmpty().contains("Nama pakaian wajib diisi"))
            assertTrue(tts.lastSpeech.orEmpty().contains("Pilih warna pakaian"))
            assertTrue(haptics.errorCount > 0)
        } finally {
            database.close()
        }
    }
}

private class TestTtsManager : TextToSpeechManager {
    override var isSpeaking: Boolean = false
    override var lastSpeech: String? = null
    override fun speak(text: String, queueMode: Int, recordInHistory: Boolean) { lastSpeech = text }
    override fun repeatLastSpeech() = Unit
    override fun stop() = Unit
    override fun shutdown() = Unit
}

private class TestHapticManager : HapticFeedbackManager {
    var successCount = 0
    var errorCount = 0
    override fun performButtonClick() = Unit
    override fun performCategorySelected() = Unit
    override fun performSuccess() { successCount++ }
    override fun performError() { errorCount++ }
}
