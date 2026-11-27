package com.zuhdiarifazizi.projectakhirpapb.data.voice

import com.zuhdiarifazizi.projectakhirpapb.domain.voice.IntentRecognizer
import com.zuhdiarifazizi.projectakhirpapb.domain.voice.VoiceCommand

class IndonesianIntentRecognizer : IntentRecognizer {

    private val cancelKeywords = listOf(
        "batal", "batalkan", "tidak jadi", "stop", "berhenti", "cancel", "tutup"
    )

    private val repeatKeywords = listOf(
        "ulang", "ulangi", "ulangi penjelasannya", "dengarkan lagi", "baca lagi",
        "panduan lagi", "jelaskan lagi", "ulangi suara", "ulangi perintah"
    )

    private val selectFirstRecommendationKeywords = listOf(
        "pilih rekomendasi pertama", "pilih rekomendasi nomor satu", "ambil rekomendasi pertama", "gunakan rekomendasi pertama"
    )

    private val selectCurrentRecommendationKeywords = listOf(
        "pilih rekomendasi ini", "pakai rekomendasi ini", "gunakan rekomendasi ini",
        "pilih pakaian ini", "pakai pakaian ini", "gunakan pakaian ini", "pilih outfit ini"
    )

    private val nextRecommendationKeywords = listOf(
        "rekomendasi berikutnya", "rekomendasi selanjutnya", "pilihan berikutnya",
        "pilihan selanjutnya", "ganti rekomendasi", "outfit berikutnya",
        "selanjutnya", "berikutnya"
    )

    private val listenRecommendationKeywords = listOf(
        "dengarkan rekomendasi", "bacakan rekomendasi", "baca rekomendasi",
        "jelaskan rekomendasi", "dengarkan deskripsi", "bacakan riwayat",
        "ringkasan riwayat", "dengarkan riwayat"
    )

    private val quickLoginKeywords = listOf(
        "masuk cepat", "tanpa ketik", "akun lokal", "mode tamu", "langsung masuk", "mulai demo"
    )

    private val logoutKeywords = listOf(
        "keluar akun", "logout", "keluar dari akun"
    )

    private val continueRecommendationKeywords = listOf(
        "lanjutkan", "buat rekomendasi", "cari rekomendasi",
        "tampilkan rekomendasi", "lihat rekomendasi", "minta rekomendasi"
    )

    private val addClothingKeywords = listOf(
        "tambahkan pakaian", "tambah pakaian", "tambahkan baju", "tambah baju", "buka form pakaian"
    )

    private val openClothingCameraKeywords = listOf(
        "buka kamera pakaian", "buka kamera baju", "ambil foto pakaian", "ambil foto baju",
        "buka kamera", "ambil foto", "foto sekarang", "jepret foto", "jepret"
    )

    private val scanRfidKeywords = listOf(
        "pindai rfid", "scan rfid", "pindai nfc", "scan nfc", "kenali baju", "kenali pakaian", "identifikasi baju"
    )

    private val openHomeKeywords = listOf(
        "buka beranda", "ke beranda", "kembali ke beranda", "halaman utama", "menu utama", "beranda"
    )

    private val historyKeywords = listOf(
        "riwayat", "history", "histori", "riwayat pakaian", "buka riwayat",
        "lihat riwayat", "riwayat baju", "catatan pakaian"
    )

    private val wardrobeKeywords = listOf(
        "lemari", "lemari saya", "kelola baju", "kelola pakaian", "buka lemari",
        "wardrobe", "daftar baju", "isi lemari", "atur baju",
        "ringkasan lemari", "bacakan isi lemari"
    )

    private val navigateBackKeywords = listOf(
        "kembali", "halaman sebelumnya", "mundur"
    )

    private val streetwearKeywords = listOf(
        "streetwear", "street wear", "jaket", "hoodie", "sneaker", "sneakers",
        "gaya santai", "casual", "kasual", "street"
    )

    private val sportKeywords = listOf(
        "olahraga", "gym", "lari", "jogging", "sepeda", "workout", "fitness",
        "aktif", "lari pagi", "baju olahraga"
    )

    private val hangoutKeywords = listOf(
        "nongkrong", "ngopi", "kopi", "cafe", "kafe", "nongki", "makan",
        "teman", "main bareng", "santai bareng teman", "baju nongkrong"
    )

    private val workKeywords = listOf(
        "kerja", "kantor", "rapat", "meeting", "formal", "pergi kerja",
        "mau kerja", "bekerja", "pakaian kerja", "baju kerja", "ke kantor"
    )

    private val otherKeywords = listOf(
        "lainnya", "lain", "khusus", "acara khusus", "pesta", "kondangan",
        "cuaca", "hujan"
    )

    override fun recognize(spokenText: String): VoiceCommand {
        val normalized = normalize(spokenText)
        if (normalized.isBlank()) {
            return VoiceCommand.Unknown(spokenText)
        }

        // 1. Control commands: Cancel & Repeat
        if (matchesAny(normalized, cancelKeywords)) {
            return VoiceCommand.CancelAction
        }
        if (matchesAny(normalized, repeatKeywords)) {
            return VoiceCommand.RepeatResponse
        }

        // 2. Specific PickOutfit, Wardrobe, Camera, RFID commands
        if (matchesAny(normalized, selectFirstRecommendationKeywords)) {
            return VoiceCommand.SelectFirstRecommendation
        }
        if (matchesAny(normalized, selectCurrentRecommendationKeywords)) {
            return VoiceCommand.SelectCurrentRecommendation
        }
        if (matchesAny(normalized, nextRecommendationKeywords)) {
            return VoiceCommand.NextRecommendation
        }
        if (matchesAny(normalized, listenRecommendationKeywords)) {
            return VoiceCommand.ListenRecommendations
        }
        if (matchesAny(normalized, continueRecommendationKeywords)) {
            return VoiceCommand.ContinueRecommendation
        }
        if (matchesAny(normalized, addClothingKeywords)) {
            return VoiceCommand.AddClothing
        }
        if (matchesAny(normalized, openClothingCameraKeywords)) {
            return VoiceCommand.OpenClothingCamera
        }
        if (matchesAny(normalized, scanRfidKeywords)) {
            return VoiceCommand.ScanRfid
        }
        if (matchesAny(normalized, quickLoginKeywords)) {
            return VoiceCommand.QuickLogin
        }
        if (matchesAny(normalized, logoutKeywords)) {
            return VoiceCommand.LogoutAccount
        }

        // 3. Navigation commands: Home, History, Wardrobe, Back
        if (matchesAny(normalized, openHomeKeywords)) {
            return VoiceCommand.OpenHome
        }
        if (matchesAny(normalized, historyKeywords)) {
            return VoiceCommand.OpenHistory
        }
        if (matchesAny(normalized, wardrobeKeywords)) {
            return VoiceCommand.OpenWardrobe
        }
        if (matchesAny(normalized, navigateBackKeywords)) {
            return VoiceCommand.NavigateBack
        }

        // 4. Activity Category selections
        if (matchesAny(normalized, streetwearKeywords)) {
            return VoiceCommand.SelectStreetwear
        }
        if (matchesAny(normalized, sportKeywords)) {
            return VoiceCommand.SelectSport
        }
        if (matchesAny(normalized, hangoutKeywords)) {
            return VoiceCommand.SelectHangout
        }
        if (matchesAny(normalized, workKeywords)) {
            return VoiceCommand.SelectWork
        }
        if (matchesAny(normalized, otherKeywords)) {
            return VoiceCommand.SelectOther
        }

        return VoiceCommand.Unknown(spokenText)
    }

    private fun normalize(text: String): String {
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun matchesAny(text: String, keywords: List<String>): Boolean {
        return keywords.any { keyword ->
            val cleanKeyword = normalize(keyword)
            cleanKeyword.isNotEmpty() &&
                Regex("\\b${Regex.escape(cleanKeyword)}\\b").containsMatchIn(text)
        }
    }
}
