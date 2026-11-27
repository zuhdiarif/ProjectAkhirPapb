package com.zuhdiarifazizi.projectakhirpapb.data.device

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.IntentRecognizer
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ItemVoiceAction
import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoiceCommand
import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoicePage
import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoiceScreenContext

class IndonesianIntentRecognizer : IntentRecognizer {

    private val cancelKeywords = listOf(
        "batal", "batalkan", "tidak jadi", "stop", "berhenti", "cancel", "tutup"
    )

    private val repeatKeywords = listOf(
        "ulang", "ulangi", "ulangi penjelasannya", "dengarkan lagi", "baca lagi",
        "panduan lagi", "jelaskan lagi", "ulangi suara", "ulangi perintah"
    )

    private val selectFirstRecommendationKeywords = listOf(
        "pilih rekomendasi pertama", "pilih rekomendasi nomor satu",
        "ambil rekomendasi pertama", "gunakan rekomendasi pertama"
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
        "pindai rfid", "scan rfid", "pindai nfc", "scan nfc",
        "kenali baju", "kenali pakaian", "identifikasi baju"
    )

    private val openHomeKeywords = listOf(
        "buka beranda", "ke beranda", "kembali ke beranda", "halaman utama", "menu utama", "beranda"
    )

    private val openOnboardingKeywords = listOf(
        "panduan aura", "buka pengenalan", "tampilkan pengenalan", "tentang aura", "buka onboarding"
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

    override fun recognize(spokenText: String): VoiceCommand =
        recognize(spokenText, VoiceScreenContext())

    override fun recognize(spokenText: String, context: VoiceScreenContext): VoiceCommand {
        val normalized = normalize(spokenText)
        if (normalized.isBlank()) {
            return VoiceCommand.Unknown(spokenText)
        }

        // 1. Always-safe control commands
        if (matchesAny(normalized, cancelKeywords)) {
            return VoiceCommand.CancelAction
        }
        if (matchesAny(normalized, repeatKeywords)) {
            return VoiceCommand.RepeatResponse
        }

        // 2. Dialog confirmation takes highest contextual priority when open
        if (context.hasConfirmationDialog) {
            if (matchesAny(normalized, listOf("ya", "iya", "setuju", "konfirmasi", "hapus", "lanjutkan", "ok", "oke"))) {
                return VoiceCommand.ConfirmPositive
            }
        }

        // 3. Context-sensitive page actions BEFORE global occasion keywords
        recognizeForPageContext(normalized, context.page)?.let { return it }

        // 4. Specific cross-screen commands (preserving global shortcuts)
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
            return VoiceCommand.ListenRecommendations(null)
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
        if (matchesAny(normalized, openOnboardingKeywords)) {
            return VoiceCommand.OpenOnboarding
        }

        // 5. Global navigation commands
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

        // 6. Global Activity Category selections
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

    private fun recognizeForPageContext(normalized: String, page: VoicePage): VoiceCommand? {
        return when (page) {
            VoicePage.GLOBAL -> null
            VoicePage.ONBOARDING -> recognizeOnboarding(normalized)
            VoicePage.HOME -> recognizeHome(normalized)
            VoicePage.PICK_CATEGORY -> recognizePickCategory(normalized)
            VoicePage.PICK_RECOMMENDATIONS -> recognizePickRecommendations(normalized)
            VoicePage.PICK_RESULT -> recognizePickResult(normalized)
            VoicePage.HISTORY -> recognizeHistory(normalized)
            VoicePage.WARDROBE_LIST -> recognizeWardrobeList(normalized)
            VoicePage.ADD_GUIDE -> recognizeAddGuide(normalized)
            VoicePage.RFID_ADD -> recognizeRfidAdd(normalized)
            VoicePage.CAMERA_LIVE -> recognizeCameraLive(normalized)
            VoicePage.CAMERA_PREVIEW -> recognizeCameraPreview(normalized)
            VoicePage.CLOTHING_FORM -> recognizeClothingForm(normalized)
            VoicePage.CLOTHING_DETAIL -> recognizeClothingDetail(normalized)
            VoicePage.SCAN_FIND -> recognizeScanFind(normalized)
        }
    }

    private fun recognizeOnboarding(normalized: String): VoiceCommand? = when {
        matchesAny(normalized, listOf("lewati", "skip", "mulai gunakan", "mulai", "selesai", "langsung ke beranda")) ->
            VoiceCommand.OnboardingFinish
        matchesAny(normalized, listOf("sebelumnya", "kembali", "mundur")) ->
            VoiceCommand.OnboardingPrevious
        matchesAny(normalized, listOf("selanjutnya", "berikutnya", "lanjutkan", "lanjut")) ->
            VoiceCommand.OnboardingNext
        matchesAny(normalized, listOf("dengarkan", "bacakan", "jelaskan", "panduan", "penjelasan")) ->
            VoiceCommand.ReadPageGuidance
        else -> null
    }

    private fun recognizeHome(normalized: String): VoiceCommand? {
        val idx = extractNumber(normalized)
        if (idx != null && matchesAny(normalized, listOf("nomor", "nomer", "pilihan", "opsi", "pilih"))) {
            return when (idx) {
                1 -> VoiceCommand.SelectWork
                2 -> VoiceCommand.SelectHangout
                3 -> VoiceCommand.SelectSport
                4 -> VoiceCommand.SelectStreetwear
                5 -> VoiceCommand.SelectOther
                else -> null
            }
        }
        return when {
            matchesAny(normalized, listOf("dengarkan panduan", "panduan halaman", "baca halaman", "dengarkan", "panduan")) ->
                VoiceCommand.ReadPageGuidance
            matchesAny(normalized, listOf("info aplikasi", "panduan aura", "tentang aura")) ->
                VoiceCommand.OpenOnboarding
            else -> null
        }
    }

    private fun recognizePickCategory(normalized: String): VoiceCommand? {
        if (matchesAny(normalized, listOf("semua kategori", "pilih semua", "semua", "atasan bawahan luaran"))) {
            return VoiceCommand.SelectAllOutfitCategories
        }
        if (matchesAny(normalized, listOf("lanjut", "lanjutkan", "buat rekomendasi", "cari rekomendasi", "tampilkan rekomendasi", "selesai"))) {
            return VoiceCommand.ContinueRecommendation
        }
        if (matchesAny(normalized, listOf("kembali ke aktivitas", "kembali"))) {
            return VoiceCommand.NavigateBack
        }
        if (matchesAny(normalized, listOf("dengarkan", "panduan", "baca"))) {
            return VoiceCommand.ReadPageGuidance
        }

        val disable = matchesAny(normalized, listOf("tanpa", "hapus", "matikan", "nonaktifkan", "hilangkan", "tidak pakai", "lepas"))
        val enable = matchesAny(normalized, listOf("pakai", "pilih", "tambah", "gunakan", "centang", "aktifkan"))
        val desired: Boolean? = when {
            disable -> false
            enable -> true
            else -> null
        }

        return when {
            matchesAny(normalized, listOf("pakaian luar", "luaran", "jaket", "outer", "cardigan", "blazer")) ->
                VoiceCommand.ToggleOutfitCategory(ClothingCategory.OUTER_WEAR, desired)
            matchesAny(normalized, listOf("atasan", "baju atas", "kemeja", "kaos", "top")) ->
                VoiceCommand.ToggleOutfitCategory(ClothingCategory.TOP_WEAR, desired)
            matchesAny(normalized, listOf("bawahan", "celana", "rok", "bottom")) ->
                VoiceCommand.ToggleOutfitCategory(ClothingCategory.BOTTOM_WEAR, desired)
            else -> null
        }
    }

    private fun recognizePickRecommendations(normalized: String): VoiceCommand? {
        val number = extractNumber(normalized)
        val zeroBased = number?.let { it - 1 }
        return when {
            matchesAny(normalized, listOf("sebelumnya", "rekomendasi sebelumnya")) ->
                VoiceCommand.PreviousRecommendation
            matchesAny(normalized, listOf("berikutnya", "selanjutnya", "ganti rekomendasi")) ->
                VoiceCommand.NextRecommendation
            matchesAny(normalized, listOf("lihat detail", "buka detail", "detail")) ->
                VoiceCommand.ViewRecommendationDetail(zeroBased)
            matchesAny(normalized, listOf("dengarkan", "bacakan", "baca", "jelaskan")) ->
                VoiceCommand.ListenRecommendations(zeroBased)
            zeroBased != null && matchesAny(normalized, listOf("pilih", "pakai", "ambil", "gunakan", "nomor", "nomer", "rekomendasi")) ->
                VoiceCommand.SelectNumberedRecommendation(zeroBased)
            matchesAny(normalized, listOf("pilih pakaian ini", "pilih rekomendasi ini", "pakai ini", "pilih ini", "pilih", "pakai")) ->
                VoiceCommand.SelectCurrentRecommendation
            else -> null
        }
    }

    private fun recognizePickResult(normalized: String): VoiceCommand? = when {
        matchesAny(normalized, listOf("pakai dan simpan", "simpan ke riwayat", "simpan", "pakai", "konfirmasi", "pilih pakaian ini")) ->
            VoiceCommand.ConfirmWearOutfit
        matchesAny(normalized, listOf("dengarkan", "bacakan", "baca", "jelaskan", "deskripsi")) ->
            VoiceCommand.ListenRecommendations(null)
        else -> null
    }

    private fun recognizeHistory(normalized: String): VoiceCommand? {
        val number = extractNumber(normalized)
        return when {
            matchesAny(normalized, listOf("hapus semua", "bersihkan riwayat", "kosongkan riwayat")) ->
                VoiceCommand.RequestClearHistory
            number != null && matchesAny(normalized, listOf("hapus")) ->
                VoiceCommand.DeleteHistoryItem(number)
            number != null && matchesAny(normalized, listOf("rotasi", "jarang dipakai")) ->
                VoiceCommand.SpeakRotationItem(number)
            number != null ->
                VoiceCommand.SpeakHistoryItem(number)
            matchesAny(normalized, listOf("nyalakan", "dengarkan", "bacakan", "ringkasan", "rotasi")) ->
                VoiceCommand.ReadPageGuidance
            matchesAny(normalized, listOf("pilih pakaian", "cari outfit", "mulai pilih")) ->
                VoiceCommand.OpenHome
            else -> null
        }
    }

    private fun recognizeWardrobeList(normalized: String): VoiceCommand? {
        if (matchesAny(normalized, listOf("muat koleksi contoh", "muat contoh", "isi contoh", "reset contoh"))) {
            return VoiceCommand.SeedStarterWardrobe
        }
        if (matchesAny(normalized, listOf("hapus pencarian", "reset pencarian", "tampilkan semua"))) {
            return VoiceCommand.ClearWardrobeSearch
        }
        if (normalized.startsWith("cari ")) {
            val q = normalized.removePrefix("cari ").trim()
            if (q.isNotBlank()) return VoiceCommand.SearchWardrobe(q)
        }
        if (matchesAny(normalized, listOf("semua", "semua kategori", "tampilkan semua"))) {
            return VoiceCommand.FilterWardrobeCategory(null)
        }
        if (matchesAny(normalized, listOf("filter atasan", "tampilkan atasan", "atasan saja", "atasan"))) {
            return VoiceCommand.FilterWardrobeCategory(ClothingCategory.TOP_WEAR)
        }
        if (matchesAny(normalized, listOf("filter bawahan", "tampilkan bawahan", "bawahan saja", "bawahan"))) {
            return VoiceCommand.FilterWardrobeCategory(ClothingCategory.BOTTOM_WEAR)
        }
        if (matchesAny(normalized, listOf("filter luaran", "tampilkan luaran", "luaran saja", "luaran", "pakaian luar"))) {
            return VoiceCommand.FilterWardrobeCategory(ClothingCategory.OUTER_WEAR)
        }

        val action: ItemVoiceAction? = when {
            matchesAny(normalized, listOf("hapus", "buang", "hilangkan")) -> ItemVoiceAction.DELETE
            matchesAny(normalized, listOf("edit", "ubah", "ganti")) -> ItemVoiceAction.EDIT
            matchesAny(normalized, listOf("detail", "lihat", "buka detail")) -> ItemVoiceAction.DETAIL
            matchesAny(normalized, listOf("dengarkan deskripsi", "dengarkan", "bacakan", "baca")) -> ItemVoiceAction.SPEAK
            else -> null
        }
        if (action != null) {
            val number = extractNumber(normalized)
            val targetName = extractTargetNameAfterAction(normalized)
            if (number != null || !targetName.isNullOrBlank()) {
                return VoiceCommand.WardrobeItemAction(action, number, targetName)
            }
            if (action == ItemVoiceAction.SPEAK) {
                return VoiceCommand.ReadPageGuidance
            }
        }
        if (matchesAny(normalized, listOf("nyalakan", "ringkasan", "dengarkan"))) {
            return VoiceCommand.ReadPageGuidance
        }
        if (matchesAny(normalized, listOf("tambah baju", "tambah pakaian", "tambah"))) {
            return VoiceCommand.AddClothing
        }
        if (matchesAny(normalized, listOf("pindai rfid", "pindai", "rfid", "nfc", "kenali baju"))) {
            return VoiceCommand.ScanRfid
        }
        return null
    }

    private fun recognizeAddGuide(normalized: String): VoiceCommand? = when {
        matchesAny(normalized, listOf("opsi satu", "opsi 1", "nomor satu", "nomor 1", "video", "kamera", "pindai video", "mulai pindai", "ambil foto")) ->
            VoiceCommand.StartAddVideoAi
        matchesAny(normalized, listOf("opsi dua", "opsi 2", "nomor dua", "nomor 2", "rfid", "nfc", "scan rfid", "pindai rfid")) ->
            VoiceCommand.StartAddRfid
        matchesAny(normalized, listOf("opsi tiga", "opsi 3", "nomor tiga", "nomor 3", "manual", "isi manual", "isi detail", "formulir")) ->
            VoiceCommand.StartAddManual
        matchesAny(normalized, listOf("galeri", "pilih foto")) ->
            VoiceCommand.PickFromGallery
        matchesAny(normalized, listOf("dengarkan", "panduan", "bacakan", "jelaskan")) ->
            VoiceCommand.ReadPageGuidance
        else -> null
    }

    private fun recognizeRfidAdd(normalized: String): VoiceCommand? = when {
        matchesAny(normalized, listOf("simulasi", "tap demo", "tap baru", "simulasi tap")) ->
            VoiceCommand.SimulateRfidDemo
        matchesAny(normalized, listOf("tanpa tag", "manual", "lanjut", "form", "lewati")) ->
            VoiceCommand.StartAddManual
        matchesAny(normalized, listOf("dengarkan", "langkah", "panduan", "bacakan")) ->
            VoiceCommand.ReadPageGuidance
        else -> null
    }

    private fun recognizeCameraLive(normalized: String): VoiceCommand? = when {
        matchesAny(normalized, listOf("ambil foto", "foto sekarang", "jepret", "potret", "foto", "capture", "ambil")) ->
            VoiceCommand.CapturePhotoNow
        matchesAny(normalized, listOf("balik kamera", "putar kamera", "kamera depan", "kamera belakang", "ganti kamera")) ->
            VoiceCommand.FlipCamera
        matchesAny(normalized, listOf("nyalakan senter", "hidupkan senter", "nyalakan lampu")) ->
            VoiceCommand.ToggleTorch(true)
        matchesAny(normalized, listOf("matikan senter", "matikan lampu")) ->
            VoiceCommand.ToggleTorch(false)
        matchesAny(normalized, listOf("senter", "lampu", "flash")) ->
            VoiceCommand.ToggleTorch(null)
        matchesAny(normalized, listOf("matikan suara", "suara mati", "bisukan")) ->
            VoiceCommand.ToggleScannerVoice(false)
        matchesAny(normalized, listOf("nyalakan suara", "aktifkan suara", "suara aktif")) ->
            VoiceCommand.ToggleScannerVoice(true)
        matchesAny(normalized, listOf("galeri", "pilih foto")) ->
            VoiceCommand.PickFromGallery
        matchesAny(normalized, listOf("dengarkan", "petunjuk", "panduan", "posisi")) ->
            VoiceCommand.ReadPageGuidance
        else -> null
    }

    private fun recognizeCameraPreview(normalized: String): VoiceCommand? = when {
        matchesAny(normalized, listOf("gunakan foto", "gunakan", "pakai foto", "analisis", "lanjut", "lanjutkan", "oke", "ya")) ->
            VoiceCommand.UseCapturedPhoto
        matchesAny(normalized, listOf("ambil ulang", "foto ulang", "ulang foto", "ulangi foto")) ->
            VoiceCommand.RetakePhoto
        matchesAny(normalized, listOf("kembali")) ->
            VoiceCommand.NavigateBack
        else -> null
    }

    private fun recognizeClothingForm(normalized: String): VoiceCommand? {
        if (matchesAny(normalized, listOf("simpan", "simpan pakaian", "simpan baju", "selesai"))) {
            return VoiceCommand.SaveClothingForm
        }
        if (matchesAny(normalized, listOf("hapus foto", "buang foto"))) {
            return VoiceCommand.RemoveFormPhoto
        }
        if (matchesAny(normalized, listOf("lepas rfid", "hapus rfid"))) {
            return VoiceCommand.ClearRfidTag
        }
        if (matchesAny(normalized, listOf("batal tap", "batalkan tap"))) {
            return VoiceCommand.CancelAction
        }
        if (matchesAny(normalized, listOf("simulasi tap", "simulasi demo", "simulasi"))) {
            return VoiceCommand.SimulateRfidDemo
        }
        if (matchesAny(normalized, listOf("tautkan rfid", "scan rfid", "tempel rfid", "rfid", "nfc"))) {
            return VoiceCommand.BindRfidTag
        }
        if (matchesAny(normalized, listOf("kamera", "ambil foto", "foto"))) {
            return VoiceCommand.OpenClothingCamera
        }
        if (matchesAny(normalized, listOf("galeri"))) {
            return VoiceCommand.PickFromGallery
        }
        if (normalized.startsWith("nama ")) {
            val value = normalized.removePrefix("nama ").trim()
            if (value.isNotBlank()) return VoiceCommand.SetFormName(value.replaceFirstChar { it.uppercase() })
        }
        if (normalized.startsWith("bahan ")) {
            val value = normalized.removePrefix("bahan ").trim()
            if (value.isNotBlank()) return VoiceCommand.SetFormMaterial(value.replaceFirstChar { it.uppercase() })
        }
        detectColorOption(normalized)?.let { return VoiceCommand.SetFormColor(it) }
        detectPatternOption(normalized)?.let { return VoiceCommand.SetFormPattern(it) }
        when {
            matchesAny(normalized, listOf("atasan")) -> return VoiceCommand.SetFormCategory(ClothingCategory.TOP_WEAR)
            matchesAny(normalized, listOf("bawahan")) -> return VoiceCommand.SetFormCategory(ClothingCategory.BOTTOM_WEAR)
            matchesAny(normalized, listOf("pakaian luar", "luaran")) -> return VoiceCommand.SetFormCategory(ClothingCategory.OUTER_WEAR)
            matchesAny(normalized, listOf("kerja")) -> return VoiceCommand.SetFormOccasion(Occasion.WORK)
            matchesAny(normalized, listOf("nongkrong")) -> return VoiceCommand.SetFormOccasion(Occasion.HANGOUT)
            matchesAny(normalized, listOf("olahraga")) -> return VoiceCommand.SetFormOccasion(Occasion.EXERCISE)
            matchesAny(normalized, listOf("streetwear", "street wear")) -> return VoiceCommand.SetFormOccasion(Occasion.STREETWEAR)
            matchesAny(normalized, listOf("lainnya")) -> return VoiceCommand.SetFormOccasion(Occasion.OTHER)
        }
        if (matchesAny(normalized, listOf("dengarkan", "panduan", "baca"))) {
            return VoiceCommand.ReadPageGuidance
        }
        return null
    }

    private fun recognizeClothingDetail(normalized: String): VoiceCommand? = when {
        matchesAny(normalized, listOf("pakai hari ini", "tandai dipakai", "tandai", "sudah dipakai")) ->
            VoiceCommand.MarkWornToday
        matchesAny(normalized, listOf("edit", "ubah", "perbarui")) ->
            VoiceCommand.EditCurrentClothing
        matchesAny(normalized, listOf("hapus", "buang")) ->
            VoiceCommand.DeleteCurrentClothing
        matchesAny(normalized, listOf("dengarkan", "bacakan", "baca", "deskripsi")) ->
            VoiceCommand.ReadPageGuidance
        else -> null
    }

    private fun recognizeScanFind(normalized: String): VoiceCommand? = when {
        matchesAny(normalized, listOf("kamera", "kenali lewat kamera", "ambil foto")) ->
            VoiceCommand.RecognizeViaCameraAi
        matchesAny(normalized, listOf("galeri")) ->
            VoiceCommand.PickFromGallery
        matchesAny(normalized, listOf("simulasi", "tap demo", "demo")) ->
            VoiceCommand.SimulateRfidDemo
        matchesAny(normalized, listOf("detail", "buka detail")) ->
            VoiceCommand.ViewRecommendationDetail(null)
        matchesAny(normalized, listOf("dengarkan", "bacakan", "baca", "deskripsi")) ->
            VoiceCommand.ReadPageGuidance
        else -> null
    }

    private fun detectColorOption(normalized: String): String? = when {
        matchesAny(normalized, listOf("biru dongker", "dongker", "navy")) -> "Biru dongker"
        matchesAny(normalized, listOf("biru denim", "denim")) -> "Biru denim"
        matchesAny(normalized, listOf("biru", "warna biru")) -> "Biru"
        matchesAny(normalized, listOf("putih", "warna putih")) -> "Putih"
        matchesAny(normalized, listOf("hitam", "warna hitam")) -> "Hitam"
        matchesAny(normalized, listOf("abu abu", "abu")) -> "Abu-abu"
        matchesAny(normalized, listOf("cream", "krem", "beige")) -> "Cream"
        matchesAny(normalized, listOf("olive", "hijau olive", "hijau")) -> "Olive"
        matchesAny(normalized, listOf("merah", "maroon")) -> "Merah"
        matchesAny(normalized, listOf("cokelat", "coklat")) -> "Cokelat"
        else -> null
    }

    private fun detectPatternOption(normalized: String): ClothingPattern? = when {
        matchesAny(normalized, listOf("polos", "motif polos")) -> ClothingPattern.SOLID
        matchesAny(normalized, listOf("bergaris", "salur", "garis")) -> ClothingPattern.STRIPED
        matchesAny(normalized, listOf("kotak kotak", "kotak", "flanel", "plaid")) -> ClothingPattern.PLAID
        matchesAny(normalized, listOf("motif bunga", "bunga", "floral")) -> ClothingPattern.FLORAL
        matchesAny(normalized, listOf("bergambar", "grafis", "graphic")) -> ClothingPattern.GRAPHIC
        matchesAny(normalized, listOf("motif lainnya", "batik")) -> ClothingPattern.OTHER
        else -> null
    }

    private fun extractNumber(normalized: String): Int? {
        Regex("\\b(\\d{1,2})\\b").find(normalized)?.groupValues?.get(1)?.toIntOrNull()?.let { num ->
            if (num in 1..99) return num
        }
        val map = listOf(
            "pertama" to 1, "kesatu" to 1, "satu" to 1,
            "kedua" to 2, "dua" to 2,
            "ketiga" to 3, "tiga" to 3,
            "keempat" to 4, "empat" to 4,
            "kelima" to 5, "lima" to 5,
            "keenam" to 6, "enam" to 6,
            "ketujuh" to 7, "tujuh" to 7,
            "kedelapan" to 8, "delapan" to 8,
            "kesembilan" to 9, "sembilan" to 9,
            "kesepuluh" to 10, "sepuluh" to 10,
            "sebelas" to 11, "dua belas" to 12, "tiga belas" to 13,
            "empat belas" to 14, "lima belas" to 15, "enam belas" to 16,
            "tujuh belas" to 17, "delapan belas" to 18, "sembilan belas" to 19,
            "dua puluh" to 20, "dua puluh satu" to 21, "dua puluh dua" to 22
        )
        return map.firstOrNull { (word, _) ->
            Regex("\\b${Regex.escape(word)}\\b").containsMatchIn(normalized)
        }?.second
    }

    private fun extractTargetNameAfterAction(normalized: String): String? {
        val prefixes = listOf(
            "dengarkan deskripsi ", "dengarkan ", "bacakan ", "baca ",
            "buka detail ", "lihat detail ", "detail ", "lihat ",
            "edit ", "ubah ", "ganti ", "hapus ", "buang "
        )
        val remainder = prefixes.firstNotNullOfOrNull { p ->
            if (normalized.startsWith(p)) normalized.removePrefix(p).trim() else null
        } ?: return null
        val cleaned = remainder
            .replace(Regex("\\b(nomor|nomer|baju|pakaian)\\b"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return cleaned.takeIf { it.isNotBlank() && extractNumber(it) == null }
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
