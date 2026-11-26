package com.zuhdiarifazizi.projectakhirpapb.data.voice

import com.zuhdiarifazizi.projectakhirpapb.domain.voice.VoiceCommand
import com.zuhdiarifazizi.projectakhirpapb.domain.voice.VoiceIntentType
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
