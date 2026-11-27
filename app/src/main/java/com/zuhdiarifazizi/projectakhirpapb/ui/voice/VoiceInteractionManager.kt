package com.zuhdiarifazizi.projectakhirpapb.ui.voice

import com.zuhdiarifazizi.projectakhirpapb.domain.repository.HapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.TextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ItemVoiceAction
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoiceCommand
import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoicePage
import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoiceRecognitionError
import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoiceScreenContext
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.IntentRecognizer
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.SpeechRecognizerManager
import com.zuhdiarifazizi.projectakhirpapb.ui.voice.VoiceInteractionState
import com.zuhdiarifazizi.projectakhirpapb.ui.voice.VoiceSessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface VoiceNavigationCallback {
    fun onSelectOccasion(occasion: Occasion)
    fun onOpenHistory()
    fun onOpenWardrobe()
    fun onOpenHome() {}
    fun onOpenOnboarding() {}
    fun onNavigateBack() {}
    fun onConfirmPositive() {}
    fun onReadPageGuidance() {}
    fun onOnboardingNext() {}
    fun onOnboardingPrevious() {}
    fun onOnboardingFinish() {}
    fun onToggleOutfitCategory(category: ClothingCategory, selected: Boolean?) {}
    fun onSelectAllOutfitCategories() {}
    fun onContinueRecommendation() {}
    fun onNextRecommendation() {}
    fun onPreviousRecommendation() {}
    fun onSelectFirstRecommendation() {}
    fun onSelectCurrentRecommendation() {}
    fun onSelectNumberedRecommendation(zeroBasedIndex: Int) {}
    fun onViewRecommendationDetail(zeroBasedIndex: Int?) {}
    fun onListenRecommendations(zeroBasedIndex: Int? = null) {}
    fun onConfirmWearOutfit() {}
    fun onSpeakHistoryItem(oneBasedIndex: Int) {}
    fun onSpeakRotationItem(oneBasedIndex: Int) {}
    fun onDeleteHistoryItem(oneBasedIndex: Int) {}
    fun onRequestClearHistory() {}
    fun onFilterWardrobeCategory(category: ClothingCategory?) {}
    fun onSearchWardrobe(query: String) {}
    fun onClearWardrobeSearch() {}
    fun onWardrobeItemAction(action: ItemVoiceAction, oneBasedNumber: Int?, nameQuery: String?) {}
    fun onSeedStarterWardrobe() {}
    fun onAddClothing() {}
    fun onStartAddVideoAi() {}
    fun onStartAddRfid() {}
    fun onStartAddManual() {}
    fun onPickFromGallery() {}
    fun onOpenClothingCamera() {}
    fun onCapturePhotoNow() {}
    fun onUseCapturedPhoto() {}
    fun onRetakePhoto() {}
    fun onToggleTorch(enable: Boolean?) {}
    fun onFlipCamera() {}
    fun onToggleScannerVoice(enable: Boolean?) {}
    fun onScanRfid() {}
    fun onBindRfidTag() {}
    fun onClearRfidTag() {}
    fun onSimulateRfidDemo() {}
    fun onRecognizeViaCameraAi() {}
    fun onSetFormName(name: String) {}
    fun onSetFormMaterial(material: String) {}
    fun onSetFormCategory(category: ClothingCategory) {}
    fun onSetFormColor(colorName: String) {}
    fun onSetFormPattern(pattern: ClothingPattern) {}
    fun onSetFormOccasion(occasion: Occasion) {}
    fun onRemoveFormPhoto() {}
    fun onSaveClothingForm() {}
    fun onMarkWornToday() {}
    fun onEditCurrentClothing() {}
    fun onDeleteCurrentClothing() {}
}

class VoiceInteractionManager(
    private val speechRecognizerManager: SpeechRecognizerManager,
    private val intentRecognizer: IntentRecognizer,
    private val ttsManager: TextToSpeechManager,
    private val hapticManager: HapticFeedbackManager,
    private val contextProvider: () -> VoiceScreenContext = { VoiceScreenContext() }
) {
    private val _state = MutableStateFlow(VoiceInteractionState())
    val state: StateFlow<VoiceInteractionState> = _state.asStateFlow()

    private var listenSessionToken: Long = 0L

    fun toggleVoiceInteraction(
        hasRecordAudioPermission: Boolean,
        navigationCallback: VoiceNavigationCallback? = null
    ) {
        if (_state.value.isListening) {
            cancelListening()
        } else {
            startListening(hasRecordAudioPermission, navigationCallback)
        }
    }

    fun startListening(
        hasRecordAudioPermission: Boolean,
        navigationCallback: VoiceNavigationCallback? = null
    ) {
        if (!hasRecordAudioPermission) {
            ++listenSessionToken
            hapticManager.performError()
            val msg = "Izin mikrofon diperlukan untuk menggunakan perintah suara."
            _state.update {
                it.copy(
                    status = VoiceSessionStatus.ERROR,
                    isListening = false,
                    lastTranscript = "",
                    feedbackMessage = msg
                )
            }
            ttsManager.speak(
                text = "Izin mikrofon belum diberikan. Silakan izinkan akses mikrofon.",
                recordInHistory = false
            )
            return
        }

        val token = ++listenSessionToken
        hapticManager.performButtonClick()
        _state.update {
            it.copy(
                status = VoiceSessionStatus.LISTENING,
                isListening = true,
                lastTranscript = "",
                feedbackMessage = "Mendengarkan..."
            )
        }

        ttsManager.speakCue("Mendengarkan.") {
            if (token != listenSessionToken) return@speakCue
            speechRecognizerManager.startListening(
                onResult = { recognizedText ->
                    if (token != listenSessionToken) return@startListening
                    processRecognizedSpeech(recognizedText, navigationCallback)
                },
                onError = { error ->
                    if (token != listenSessionToken) return@startListening
                    handleSpeechError(error)
                },
                onPartialResult = { partial ->
                    if (token != listenSessionToken) return@startListening
                    _state.update { it.copy(lastTranscript = partial) }
                },
                onListeningStateChanged = { listening ->
                    if (token != listenSessionToken) return@startListening
                    _state.update { it.copy(isListening = listening) }
                }
            )
        }
    }

    fun cancelListening() {
        ++listenSessionToken
        speechRecognizerManager.stopListening()
        hapticManager.performButtonClick()
        _state.update {
            it.copy(
                status = VoiceSessionStatus.IDLE,
                isListening = false,
                lastTranscript = "",
                feedbackMessage = null
            )
        }
        ttsManager.speak("Perintah suara dibatalkan.", recordInHistory = false)
    }

    fun processRecognizedSpeech(
        spokenText: String,
        navigationCallback: VoiceNavigationCallback?
    ) {
        _state.update {
            it.copy(
                status = VoiceSessionStatus.PROCESSING,
                isListening = false,
                lastTranscript = spokenText
            )
        }

        val screenContext = contextProvider()
        val command = intentRecognizer.recognize(spokenText, screenContext)
        _state.update {
            it.copy(
                lastCommand = command,
                lastIntent = command.toLegacyVoiceIntent()
            )
        }

        when (command) {
            is VoiceCommand.SelectWork -> dispatchSuccess("Tujuan Kerja dipilih", "Baik, Anda ingin pergi bekerja.") {
                navigationCallback?.onSelectOccasion(Occasion.WORK)
            }
            is VoiceCommand.SelectHangout -> dispatchSuccess("Tujuan Nongkrong dipilih", "Baik, Anda ingin nongkrong bersama teman.") {
                navigationCallback?.onSelectOccasion(Occasion.HANGOUT)
            }
            is VoiceCommand.SelectSport -> dispatchSuccess("Tujuan Olahraga dipilih", "Baik, Anda ingin berolahraga.") {
                navigationCallback?.onSelectOccasion(Occasion.EXERCISE)
            }
            is VoiceCommand.SelectStreetwear -> dispatchSuccess("Tujuan Streetwear dipilih", "Baik, Anda memilih gaya streetwear santai.") {
                navigationCallback?.onSelectOccasion(Occasion.STREETWEAR)
            }
            is VoiceCommand.SelectOther -> dispatchSuccess("Kategori Lainnya dipilih", "Baik, Anda memilih kategori pakaian lainnya.") {
                navigationCallback?.onSelectOccasion(Occasion.OTHER)
            }
            is VoiceCommand.OpenHome -> dispatchSuccess("Membuka Beranda", "Membuka halaman Beranda.") {
                navigationCallback?.onOpenHome()
            }
            is VoiceCommand.OpenHistory -> dispatchSuccess("Membuka riwayat pakaian", "Membuka riwayat pakaian Anda.") {
                navigationCallback?.onOpenHistory()
            }
            is VoiceCommand.OpenWardrobe -> dispatchSuccess("Membuka lemari pakaian", "Membuka lemari pakaian Anda.") {
                navigationCallback?.onOpenWardrobe()
            }
            is VoiceCommand.OpenOnboarding -> dispatchSuccess("Membuka panduan Aura", "Membuka pengenalan aplikasi Aura.") {
                navigationCallback?.onOpenOnboarding()
            }
            is VoiceCommand.NavigateBack -> dispatchSilentSuccess("Kembali ke halaman sebelumnya") {
                if (navigationCallback != null) navigationCallback.onNavigateBack()
                else ttsManager.speak("Kembali ke halaman sebelumnya.")
            }
            is VoiceCommand.RepeatResponse -> {
                hapticManager.performButtonClick()
                ttsManager.repeatLastSpeech()
                _state.update { it.copy(status = VoiceSessionStatus.SUCCESS, feedbackMessage = "Mengulang penjelasan terakhir") }
            }
            is VoiceCommand.ConfirmPositive -> dispatchSilentSuccess("Mengonfirmasi tindakan") {
                navigationCallback?.onConfirmPositive()
            }
            is VoiceCommand.ReadPageGuidance -> dispatchSilentSuccess("Membacakan panduan halaman") {
                navigationCallback?.onReadPageGuidance()
            }
            is VoiceCommand.OnboardingNext -> dispatchSilentSuccess("Halaman pengenalan berikutnya") {
                navigationCallback?.onOnboardingNext()
            }
            is VoiceCommand.OnboardingPrevious -> dispatchSilentSuccess("Halaman pengenalan sebelumnya") {
                navigationCallback?.onOnboardingPrevious()
            }
            is VoiceCommand.OnboardingFinish -> dispatchSilentSuccess("Memulai aplikasi Aura") {
                navigationCallback?.onOnboardingFinish()
            }
            is VoiceCommand.ToggleOutfitCategory -> dispatchSilentSuccess("Mengatur kategori ${command.category.displayName}") {
                navigationCallback?.onToggleOutfitCategory(command.category, command.selected)
            }
            is VoiceCommand.SelectAllOutfitCategories -> dispatchSilentSuccess("Memilih semua kategori pakaian") {
                navigationCallback?.onSelectAllOutfitCategories()
            }
            is VoiceCommand.ContinueRecommendation -> dispatchSilentSuccess("Membuat rekomendasi pakaian") {
                if (navigationCallback != null) navigationCallback.onContinueRecommendation()
                else ttsManager.speak("Pilih kategori aktivitas terlebih dahulu untuk membuat rekomendasi.")
            }
            is VoiceCommand.NextRecommendation -> dispatchSilentSuccess("Menampilkan rekomendasi berikutnya") {
                if (navigationCallback != null) navigationCallback.onNextRecommendation()
                else ttsManager.speak("Buka daftar rekomendasi untuk melihat pilihan berikutnya.")
            }
            is VoiceCommand.PreviousRecommendation -> dispatchSilentSuccess("Menampilkan rekomendasi sebelumnya") {
                navigationCallback?.onPreviousRecommendation()
            }
            is VoiceCommand.ListenRecommendations -> dispatchSilentSuccess("Membacakan rekomendasi pakaian") {
                if (navigationCallback != null) navigationCallback.onListenRecommendations(command.index)
                else ttsManager.speak("Buka halaman rekomendasi pakaian untuk mendengarkan rekomendasi.")
            }
            is VoiceCommand.ViewRecommendationDetail -> dispatchSilentSuccess("Membuka detail rekomendasi") {
                navigationCallback?.onViewRecommendationDetail(command.index)
            }
            is VoiceCommand.SelectFirstRecommendation -> dispatchSilentSuccess("Memilih rekomendasi pertama") {
                if (navigationCallback != null) navigationCallback.onSelectFirstRecommendation()
                else ttsManager.speak("Buka halaman rekomendasi pakaian untuk memilih rekomendasi pertama.")
            }
            is VoiceCommand.SelectCurrentRecommendation -> dispatchSilentSuccess("Memilih rekomendasi saat ini") {
                if (navigationCallback != null) navigationCallback.onSelectCurrentRecommendation()
                else ttsManager.speak("Buka halaman rekomendasi pakaian untuk memilih pakaian ini.")
            }
            is VoiceCommand.SelectNumberedRecommendation -> dispatchSilentSuccess("Memilih rekomendasi nomor ${command.index + 1}") {
                navigationCallback?.onSelectNumberedRecommendation(command.index)
            }
            is VoiceCommand.ConfirmWearOutfit -> dispatchSilentSuccess("Menyimpan outfit ke Riwayat") {
                navigationCallback?.onConfirmWearOutfit()
            }
            is VoiceCommand.SpeakHistoryItem -> dispatchSilentSuccess("Membacakan riwayat nomor ${command.oneBasedIndex}") {
                navigationCallback?.onSpeakHistoryItem(command.oneBasedIndex)
            }
            is VoiceCommand.SpeakRotationItem -> dispatchSilentSuccess("Membacakan rekomendasi rotasi") {
                navigationCallback?.onSpeakRotationItem(command.oneBasedIndex)
            }
            is VoiceCommand.DeleteHistoryItem -> dispatchSilentSuccess("Menghapus catatan riwayat") {
                navigationCallback?.onDeleteHistoryItem(command.oneBasedIndex)
            }
            is VoiceCommand.RequestClearHistory -> dispatchSilentSuccess("Konfirmasi bersihkan riwayat") {
                navigationCallback?.onRequestClearHistory()
            }
            is VoiceCommand.FilterWardrobeCategory -> dispatchSilentSuccess("Memfilter koleksi lemari") {
                navigationCallback?.onFilterWardrobeCategory(command.category)
            }
            is VoiceCommand.SearchWardrobe -> dispatchSilentSuccess("Mencari ${command.query}") {
                navigationCallback?.onSearchWardrobe(command.query)
            }
            is VoiceCommand.ClearWardrobeSearch -> dispatchSilentSuccess("Menghapus pencarian lemari") {
                navigationCallback?.onClearWardrobeSearch()
            }
            is VoiceCommand.WardrobeItemAction -> dispatchSilentSuccess("Menjalankan aksi pakaian") {
                navigationCallback?.onWardrobeItemAction(command.action, command.oneBasedNumber, command.nameQuery)
            }
            is VoiceCommand.SeedStarterWardrobe -> dispatchSilentSuccess("Memuat koleksi pakaian contoh") {
                navigationCallback?.onSeedStarterWardrobe()
            }
            is VoiceCommand.AddClothing -> dispatchSilentSuccess("Membuka 3 opsi tambah pakaian") {
                if (navigationCallback != null) navigationCallback.onAddClothing()
                else ttsManager.speak("Buka halaman Kelola Pakaian untuk menambahkan baju.")
            }
            is VoiceCommand.StartAddVideoAi -> dispatchSilentSuccess("Memulai tambah baju dengan Video AI") {
                navigationCallback?.onStartAddVideoAi()
            }
            is VoiceCommand.StartAddRfid -> dispatchSilentSuccess("Memulai tambah baju dengan Scan RFID") {
                navigationCallback?.onStartAddRfid()
            }
            is VoiceCommand.StartAddManual -> dispatchSilentSuccess("Membuka form tambah baju manual") {
                navigationCallback?.onStartAddManual()
            }
            is VoiceCommand.PickFromGallery -> dispatchSilentSuccess("Membuka galeri foto") {
                navigationCallback?.onPickFromGallery()
            }
            is VoiceCommand.OpenClothingCamera -> dispatchSilentSuccess("Membuka kamera pakaian") {
                if (navigationCallback != null) navigationCallback.onOpenClothingCamera()
                else ttsManager.speak("Buka form tambah pakaian untuk menggunakan kamera.")
            }
            is VoiceCommand.CapturePhotoNow -> dispatchSilentSuccess("Mengambil foto pakaian") {
                navigationCallback?.onCapturePhotoNow()
            }
            is VoiceCommand.UseCapturedPhoto -> dispatchSilentSuccess("Menggunakan foto untuk analisis AI") {
                navigationCallback?.onUseCapturedPhoto()
            }
            is VoiceCommand.RetakePhoto -> dispatchSilentSuccess("Mengambil ulang foto") {
                navigationCallback?.onRetakePhoto()
            }
            is VoiceCommand.ToggleTorch -> dispatchSilentSuccess("Mengatur lampu senter") {
                navigationCallback?.onToggleTorch(command.enable)
            }
            is VoiceCommand.FlipCamera -> dispatchSilentSuccess("Membalik kamera") {
                navigationCallback?.onFlipCamera()
            }
            is VoiceCommand.ToggleScannerVoice -> dispatchSilentSuccess("Mengatur suara kamera") {
                navigationCallback?.onToggleScannerVoice(command.enable)
            }
            is VoiceCommand.ScanRfid -> dispatchSilentSuccess("Membuka pemindai RFID dan AI") {
                if (navigationCallback != null) navigationCallback.onScanRfid()
                else ttsManager.speak("Membuka pemindai RFID untuk mengenali pakaian Anda.")
            }
            is VoiceCommand.BindRfidTag -> dispatchSilentSuccess("Menunggu tag RFID") {
                navigationCallback?.onBindRfidTag()
            }
            is VoiceCommand.ClearRfidTag -> dispatchSilentSuccess("Melepas tag RFID") {
                navigationCallback?.onClearRfidTag()
            }
            is VoiceCommand.SimulateRfidDemo -> dispatchSilentSuccess("Simulasi tap tag RFID") {
                navigationCallback?.onSimulateRfidDemo()
            }
            is VoiceCommand.RecognizeViaCameraAi -> dispatchSilentSuccess("Membuka kamera pengenalan") {
                navigationCallback?.onRecognizeViaCameraAi()
            }
            is VoiceCommand.SetFormName -> dispatchSilentSuccess("Mengisi nama pakaian") {
                navigationCallback?.onSetFormName(command.name)
            }
            is VoiceCommand.SetFormMaterial -> dispatchSilentSuccess("Mengisi bahan pakaian") {
                navigationCallback?.onSetFormMaterial(command.material)
            }
            is VoiceCommand.SetFormCategory -> dispatchSilentSuccess("Memilih kategori ${command.category.displayName}") {
                navigationCallback?.onSetFormCategory(command.category)
            }
            is VoiceCommand.SetFormColor -> dispatchSilentSuccess("Memilih warna ${command.colorName}") {
                navigationCallback?.onSetFormColor(command.colorName)
            }
            is VoiceCommand.SetFormPattern -> dispatchSilentSuccess("Memilih motif ${command.pattern.displayName}") {
                navigationCallback?.onSetFormPattern(command.pattern)
            }
            is VoiceCommand.SetFormOccasion -> dispatchSilentSuccess("Memilih aktivitas ${command.occasion.displayName}") {
                navigationCallback?.onSetFormOccasion(command.occasion)
            }
            is VoiceCommand.RemoveFormPhoto -> dispatchSilentSuccess("Menghapus foto formulir") {
                navigationCallback?.onRemoveFormPhoto()
            }
            is VoiceCommand.SaveClothingForm -> dispatchSilentSuccess("Menyimpan pakaian") {
                navigationCallback?.onSaveClothingForm()
            }
            is VoiceCommand.MarkWornToday -> dispatchSilentSuccess("Menandai dipakai hari ini") {
                navigationCallback?.onMarkWornToday()
            }
            is VoiceCommand.EditCurrentClothing -> dispatchSilentSuccess("Membuka mode edit pakaian") {
                navigationCallback?.onEditCurrentClothing()
            }
            is VoiceCommand.DeleteCurrentClothing -> dispatchSilentSuccess("Menghapus pakaian") {
                navigationCallback?.onDeleteCurrentClothing()
            }
            is VoiceCommand.CancelAction -> {
                cancelListening()
            }
            is VoiceCommand.Unknown -> {
                hapticManager.performError()
                val response = buildContextualUnknownMessage(screenContext)
                ttsManager.speak(response, recordInHistory = false)
                _state.update {
                    it.copy(
                        status = VoiceSessionStatus.ERROR,
                        feedbackMessage = response
                    )
                }
            }
        }
    }

    private fun dispatchSuccess(banner: String, speech: String, action: () -> Unit) {
        hapticManager.performSuccess()
        ttsManager.speak(speech)
        _state.update { it.copy(status = VoiceSessionStatus.SUCCESS, feedbackMessage = banner) }
        action()
    }

    private fun dispatchSilentSuccess(banner: String, action: () -> Unit) {
        hapticManager.performSuccess()
        _state.update { it.copy(status = VoiceSessionStatus.SUCCESS, feedbackMessage = banner) }
        action()
    }

    private fun buildContextualUnknownMessage(context: VoiceScreenContext): String {
        if (context.hasConfirmationDialog) {
            return "Maaf, saya belum memahami perintah Anda. Di dialog konfirmasi, katakan Ya untuk melanjutkan atau Batal."
        }
        val hint = when (context.page) {
            VoicePage.ONBOARDING -> "selanjutnya, sebelumnya, dengarkan, atau lewati"
            VoicePage.HOME -> "Kerja, Nongkrong, Olahraga, Streetwear, Buka Lemari, atau Buka Riwayat"
            VoicePage.PICK_CATEGORY -> "pilih atasan, bawahan, pakaian luar, semua kategori, lanjutkan, atau kembali"
            VoicePage.PICK_RECOMMENDATIONS -> "pilih nomor 1, lihat detail nomor 2, berikutnya, dengarkan, atau kembali"
            VoicePage.PICK_RESULT -> "pakai dan simpan, dengarkan deskripsi, atau kembali"
            VoicePage.HISTORY -> "dengarkan riwayat, riwayat nomor 1, rotasi nomor 1, hapus nomor 1, atau hapus semua"
            VoicePage.WARDROBE_LIST -> "tambah baju, pindai RFID, filter atasan, detail nomor 1, edit nomor 1, atau cari kemeja"
            VoicePage.ADD_GUIDE -> "opsi 1 video, opsi 2 RFID, opsi 3 manual, galeri, atau dengarkan panduan"
            VoicePage.RFID_ADD -> "dengarkan langkah, simulasi tap, lanjut tanpa tag, atau kembali"
            VoicePage.CAMERA_LIVE -> "ambil foto, nyalakan senter, balik kamera, galeri, atau kembali"
            VoicePage.CAMERA_PREVIEW -> "gunakan foto, ambil ulang, atau kembali"
            VoicePage.CLOTHING_FORM -> "warna biru, polos, kerja, atasan, tautkan RFID, simpan pakaian, atau kembali"
            VoicePage.CLOTHING_DETAIL -> "dengarkan deskripsi, pakai hari ini, edit, hapus, atau kembali"
            VoicePage.SCAN_FIND -> "kamera, galeri, simulasi tap, dengarkan deskripsi, atau buka detail"
            VoicePage.GLOBAL -> "Kerja, Nongkrong, Olahraga, Streetwear, Buka Lemari, Ulangi, atau Kembali"
        }
        return "Maaf, saya belum memahami perintah Anda. Di halaman ini Anda bisa mengatakan: $hint."
    }

    private fun handleSpeechError(error: VoiceRecognitionError) {
        hapticManager.performError()
        val userMsg = error.toUserMessage()
        _state.update {
            it.copy(
                status = VoiceSessionStatus.ERROR,
                isListening = false,
                feedbackMessage = userMsg
            )
        }
        ttsManager.speak(userMsg, recordInHistory = false)
    }

    fun cleanup() {
        ++listenSessionToken
        speechRecognizerManager.destroy()
        ttsManager.stop()
    }
}
