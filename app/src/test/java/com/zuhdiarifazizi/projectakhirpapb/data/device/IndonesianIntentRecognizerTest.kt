package com.zuhdiarifazizi.projectakhirpapb.data.device

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ItemVoiceAction
import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoiceCommand
import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoiceIntentType
import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoicePage
import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoiceScreenContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IndonesianIntentRecognizerTest {

    private lateinit var recognizer: IndonesianIntentRecognizer

    @Before
    fun setUp() {
        recognizer = IndonesianIntentRecognizer()
    }

    @Test
    fun recognize_workIntentVariations() {
        val variations = listOf(
            "Aku mau pergi kerja.",
            "Mau ke kantor hari ini",
            "Ada rapat formal jam 9",
            "Baju kerja yang rapi",
            "Saya mau bekerja"
        )
        for (phrase in variations) {
            val result = recognizer.recognize(phrase)
            assertEquals("Failed for: $phrase", VoiceIntentType.SELECT_WORK, result.type)
            assertTrue(result is VoiceCommand.SelectWork)
        }
    }

    @Test
    fun recognize_hangoutIntentVariations() {
        val variations = listOf(
            "Hari ini mau nongkrong",
            "Yuk ngopi bareng teman",
            "Mau ke kafe santai",
            "Mau makan di luar sama teman",
            "Pilih gaya nongki"
        )
        for (phrase in variations) {
            val result = recognizer.recognize(phrase)
            assertEquals("Failed for: $phrase", VoiceIntentType.SELECT_HANGOUT, result.type)
            assertTrue(result is VoiceCommand.SelectHangout)
        }
    }

    @Test
    fun recognize_sportIntentVariations() {
        val variations = listOf(
            "Aku mau olahraga.",
            "Mau pergi ke gym",
            "Lari pagi di lapangan",
            "Mau jogging sore",
            "Pilih baju fitness aktif"
        )
        for (phrase in variations) {
            val result = recognizer.recognize(phrase)
            assertEquals("Failed for: $phrase", VoiceIntentType.SELECT_SPORT, result.type)
            assertTrue(result is VoiceCommand.SelectSport)
        }
    }

    @Test
    fun recognize_streetwearIntentVariations() {
        val variations = listOf(
            "Gaya streetwear keren",
            "Mau pakai jaket dan sneakers",
            "Pilih gaya santai kasual",
            "Pakai hoodie favorit"
        )
        for (phrase in variations) {
            val result = recognizer.recognize(phrase)
            assertEquals("Failed for: $phrase", VoiceIntentType.SELECT_STREETWEAR, result.type)
            assertTrue(result is VoiceCommand.SelectStreetwear)
        }
    }

    @Test
    fun recognize_otherIntentVariations() {
        val variations = listOf(
            "Ada acara khusus malam ini",
            "Mau pergi ke pesta pernikahan",
            "Pakaian untuk kondangan",
            "Lihat kategori lainnya",
            "Sesuaikan dengan cuaca hujan"
        )
        for (phrase in variations) {
            val result = recognizer.recognize(phrase)
            assertEquals("Failed for: $phrase", VoiceIntentType.SELECT_OTHER, result.type)
            assertTrue(result is VoiceCommand.SelectOther)
        }
    }

    @Test
    fun recognize_historyIntentVariations() {
        val variations = listOf(
            "Tolong buka riwayat pakaian.",
            "Buka histori saya",
            "Lihat riwayat baju kemarin"
        )
        for (phrase in variations) {
            val result = recognizer.recognize(phrase)
            assertEquals("Failed for: $phrase", VoiceIntentType.OPEN_HISTORY, result.type)
            assertTrue(result is VoiceCommand.OpenHistory)
        }
    }

    @Test
    fun recognize_wardrobeIntentVariations() {
        val variations = listOf(
            "Buka lemari saya.",
            "Kelola pakaian di lemari",
            "Lihat isi lemari",
            "Atur baju lemari"
        )
        for (phrase in variations) {
            val result = recognizer.recognize(phrase)
            assertEquals("Failed for: $phrase", VoiceIntentType.OPEN_WARDROBE, result.type)
            assertTrue(result is VoiceCommand.OpenWardrobe)
        }
    }

    @Test
    fun recognize_repeatIntentVariations() {
        val variations = listOf(
            "Ulangi penjelasannya.",
            "Ulangi lagi suaranya",
            "Dengarkan lagi panduan"
        )
        for (phrase in variations) {
            val result = recognizer.recognize(phrase)
            assertEquals("Failed for: $phrase", VoiceIntentType.REPEAT_RESPONSE, result.type)
            assertTrue(result is VoiceCommand.RepeatResponse)
        }
    }

    @Test
    fun recognize_recommendationCommands() {
        assertTrue(recognizer.recognize("Dengarkan rekomendasi").let {
            it is VoiceCommand.ListenRecommendations
        })
        assertTrue(recognizer.recognize("Dengarkan deskripsi").let {
            it is VoiceCommand.ListenRecommendations
        })
        assertTrue(recognizer.recognize("Pilih rekomendasi pertama").let {
            it is VoiceCommand.SelectFirstRecommendation
        })
        assertTrue(recognizer.recognize("Pilih rekomendasi ini").let {
            it is VoiceCommand.SelectCurrentRecommendation
        })
        assertTrue(recognizer.recognize("Pilih pakaian ini").let {
            it is VoiceCommand.SelectCurrentRecommendation
        })
        assertTrue(recognizer.recognize("Rekomendasi berikutnya").let {
            it is VoiceCommand.NextRecommendation
        })
        assertTrue(recognizer.recognize("Selanjutnya").let {
            it is VoiceCommand.NextRecommendation
        })
        assertTrue(recognizer.recognize("Lanjutkan").let {
            it is VoiceCommand.ContinueRecommendation
        })
    }

    @Test
    fun recognize_homeAndBackNavigationCommands() {
        assertTrue(recognizer.recognize("Buka beranda").let { it is VoiceCommand.OpenHome })
        assertTrue(recognizer.recognize("Kembali ke beranda").let { it is VoiceCommand.OpenHome })
        assertTrue(recognizer.recognize("Kembali").let { it is VoiceCommand.NavigateBack })
        assertTrue(recognizer.recognize("Halaman sebelumnya").let { it is VoiceCommand.NavigateBack })
        assertTrue(recognizer.recognize("Panduan Aura").let { it is VoiceCommand.OpenOnboarding })
        assertTrue(recognizer.recognize("Bacakan riwayat").let { it is VoiceCommand.ListenRecommendations })
    }

    @Test
    fun recognize_contextualActionsAcrossEveryPage() {
        // Onboarding
        assertTrue(
            recognizer.recognize("Selanjutnya", VoiceScreenContext(VoicePage.ONBOARDING)) is VoiceCommand.OnboardingNext
        )
        assertTrue(
            recognizer.recognize("Lewati", VoiceScreenContext(VoicePage.ONBOARDING)) is VoiceCommand.OnboardingFinish
        )

        // Home numbered activity selection
        assertTrue(
            recognizer.recognize("Pilih nomor tiga", VoiceScreenContext(VoicePage.HOME)) is VoiceCommand.SelectSport
        )

        // Pick Outfit Category: "pakai jaket" / "pakaian luar" toggles OUTER_WEAR (does NOT jump to Streetwear)
        val outerToggle = recognizer.recognize("Pakai jaket luar", VoiceScreenContext(VoicePage.PICK_CATEGORY))
        assertTrue(outerToggle is VoiceCommand.ToggleOutfitCategory)
        assertEquals(ClothingCategory.OUTER_WEAR, (outerToggle as VoiceCommand.ToggleOutfitCategory).category)
        assertEquals(true, outerToggle.selected)

        // Pick Outfit Recommendations
        val pickNum = recognizer.recognize("Pilih nomor dua", VoiceScreenContext(VoicePage.PICK_RECOMMENDATIONS))
        assertTrue(pickNum is VoiceCommand.SelectNumberedRecommendation)
        assertEquals(1, (pickNum as VoiceCommand.SelectNumberedRecommendation).index)
        assertTrue(
            recognizer.recognize("Sebelumnya", VoiceScreenContext(VoicePage.PICK_RECOMMENDATIONS)) is VoiceCommand.PreviousRecommendation
        )
        val detailNum = recognizer.recognize("Lihat detail nomor 3", VoiceScreenContext(VoicePage.PICK_RECOMMENDATIONS))
        assertTrue(detailNum is VoiceCommand.ViewRecommendationDetail)
        assertEquals(2, (detailNum as VoiceCommand.ViewRecommendationDetail).index)

        // Pick Outfit Result
        assertTrue(
            recognizer.recognize("Simpan", VoiceScreenContext(VoicePage.PICK_RESULT)) is VoiceCommand.ConfirmWearOutfit
        )

        // History
        val histDel = recognizer.recognize("Hapus nomor 2", VoiceScreenContext(VoicePage.HISTORY))
        assertTrue(histDel is VoiceCommand.DeleteHistoryItem)
        assertEquals(2, (histDel as VoiceCommand.DeleteHistoryItem).oneBasedIndex)

        // Wardrobe list
        val editItem = recognizer.recognize("Edit nomor 1", VoiceScreenContext(VoicePage.WARDROBE_LIST))
        assertTrue(editItem is VoiceCommand.WardrobeItemAction)
        assertEquals(ItemVoiceAction.EDIT, (editItem as VoiceCommand.WardrobeItemAction).action)
        assertEquals(1, editItem.oneBasedNumber)
        val filterBottom = recognizer.recognize("Bawahan", VoiceScreenContext(VoicePage.WARDROBE_LIST))
        assertTrue(filterBottom is VoiceCommand.FilterWardrobeCategory)
        assertEquals(ClothingCategory.BOTTOM_WEAR, (filterBottom as VoiceCommand.FilterWardrobeCategory).category)

        // Add Clothing 3 Options & RFID Add
        assertTrue(recognizer.recognize("Opsi dua RFID", VoiceScreenContext(VoicePage.ADD_GUIDE)) is VoiceCommand.StartAddRfid)
        assertTrue(recognizer.recognize("Manual", VoiceScreenContext(VoicePage.ADD_GUIDE)) is VoiceCommand.StartAddManual)
        assertTrue(recognizer.recognize("Simulasi tap", VoiceScreenContext(VoicePage.RFID_ADD)) is VoiceCommand.SimulateRfidDemo)

        // Camera & Form
        assertTrue(recognizer.recognize("Nyalakan senter", VoiceScreenContext(VoicePage.CAMERA_LIVE)) is VoiceCommand.ToggleTorch)
        assertTrue(recognizer.recognize("Gunakan foto", VoiceScreenContext(VoicePage.CAMERA_PREVIEW)) is VoiceCommand.UseCapturedPhoto)
        val formColor = recognizer.recognize("Warna biru dongker", VoiceScreenContext(VoicePage.CLOTHING_FORM))
        assertTrue(formColor is VoiceCommand.SetFormColor)
        assertEquals("Biru dongker", (formColor as VoiceCommand.SetFormColor).colorName)
        val formPattern = recognizer.recognize("Kotak kotak", VoiceScreenContext(VoicePage.CLOTHING_FORM))
        assertTrue(formPattern is VoiceCommand.SetFormPattern)
        assertEquals(ClothingPattern.PLAID, (formPattern as VoiceCommand.SetFormPattern).pattern)
        val formOcc = recognizer.recognize("Olahraga", VoiceScreenContext(VoicePage.CLOTHING_FORM))
        assertTrue(formOcc is VoiceCommand.SetFormOccasion)
        assertEquals(Occasion.EXERCISE, (formOcc as VoiceCommand.SetFormOccasion).occasion)
        assertTrue(recognizer.recognize("Simpan", VoiceScreenContext(VoicePage.CLOTHING_FORM)) is VoiceCommand.SaveClothingForm)

        // Confirmation dialog
        assertTrue(
            recognizer.recognize("Ya", VoiceScreenContext(VoicePage.WARDROBE_LIST, hasConfirmationDialog = true)) is VoiceCommand.ConfirmPositive
        )
    }

    @Test
    fun recognize_clothingManagementCommands() {
        assertTrue(recognizer.recognize("Tambahkan pakaian").let { it is VoiceCommand.AddClothing })
        assertTrue(recognizer.recognize("Buka kamera pakaian").let { it is VoiceCommand.OpenClothingCamera })
        assertTrue(recognizer.recognize("Ambil foto baju").let { it is VoiceCommand.OpenClothingCamera })
        assertTrue(recognizer.recognize("Ambil foto").let { it is VoiceCommand.OpenClothingCamera })
        assertTrue(recognizer.recognize("Buka kamera").let { it is VoiceCommand.OpenClothingCamera })
        assertTrue(recognizer.recognize("Pindai RFID pakaian").let { it is VoiceCommand.ScanRfid })
        assertTrue(recognizer.recognize("Scan NFC baju ini").let { it is VoiceCommand.ScanRfid })
        assertTrue(recognizer.recognize("Kenali pakaian saya").let { it is VoiceCommand.ScanRfid })
    }

    @Test
    fun recognize_cancelIntentVariations() {
        val variations = listOf(
            "Batalkan.",
            "Tidak jadi",
            "Stop perintah",
            "Cancel dong"
        )
        for (phrase in variations) {
            val result = recognizer.recognize(phrase)
            assertEquals("Failed for: $phrase", VoiceIntentType.CANCEL_ACTION, result.type)
            assertTrue(result is VoiceCommand.CancelAction)
        }
    }

    @Test
    fun recognize_unknownPhrase_returnsUnknown() {
        val result = recognizer.recognize("Pesawat terbang ke luar angkasa")
        assertEquals(VoiceIntentType.UNKNOWN, result.type)
        assertTrue(result is VoiceCommand.Unknown)
    }

    @Test
    fun recognize_substringFalsePositives_returnsUnknown() {
        val trickyPhrases = listOf(
            "Mau pulang sekarang",
            "Selain kemeja putih",
            "Stopkontak rusak"
        )
        for (phrase in trickyPhrases) {
            val result = recognizer.recognize(phrase)
            assertEquals("Expected UNKNOWN for substring trap: $phrase", VoiceIntentType.UNKNOWN, result.type)
        }
    }
}
