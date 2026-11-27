package com.zuhdiarifazizi.projectakhirpapb.ui.voice

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ItemVoiceAction
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ClothesRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.TextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.ui.history.HistoryViewModel
import com.zuhdiarifazizi.projectakhirpapb.ui.home.HomeViewModel
import com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes.ManageClothesPage
import com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes.ManageClothesViewModel
import com.zuhdiarifazizi.projectakhirpapb.ui.navigation.AuraDestination
import com.zuhdiarifazizi.projectakhirpapb.ui.onboarding.OnboardingViewModel
import com.zuhdiarifazizi.projectakhirpapb.ui.pickoutfit.PickOutfitStep
import com.zuhdiarifazizi.projectakhirpapb.ui.pickoutfit.PickOutfitViewModel

fun createAuraVoiceNavigationCallback(
    voiceViewModel: VoiceViewModel,
    onboardingViewModel: OnboardingViewModel,
    homeViewModel: HomeViewModel,
    pickOutfitViewModel: PickOutfitViewModel,
    historyViewModel: HistoryViewModel,
    manageViewModel: ManageClothesViewModel,
    ttsManager: TextToSpeechManager,
    onRequestCameraAccess: (Boolean) -> Unit,
    onOpenClothingGallery: () -> Unit
): VoiceNavigationCallback = object : VoiceNavigationCallback {
    override fun onSelectOccasion(occasion: Occasion) {
        homeViewModel.selectCategoryByOccasion(occasion, announceTts = false)
        pickOutfitViewModel.beginSelection(occasion, queueFollowUpSpeech = true)
        voiceViewModel.navigateTo(AuraDestination.PICK_OUTFIT)
    }

    override fun onOpenHome() {
        voiceViewModel.navigateTo(AuraDestination.HOME)
    }

    override fun onOpenHistory() {
        voiceViewModel.navigateTo(AuraDestination.HISTORY)
    }

    override fun onOpenWardrobe() {
        if (manageViewModel.uiState.value.page != ManageClothesPage.LIST) {
            manageViewModel.returnToList()
        }
        voiceViewModel.navigateTo(AuraDestination.MANAGE_CLOTHES)
    }

    override fun onOpenOnboarding() {
        onboardingViewModel.reopenOnboarding()
        voiceViewModel.navigateTo(AuraDestination.ONBOARDING)
    }

    override fun onNavigateBack() {
        when (voiceViewModel.currentDestination.value) {
            AuraDestination.ONBOARDING -> {
                if (!onboardingViewModel.previousPage()) {
                    ttsManager.speak("Anda berada di halaman pengenalan pertama.")
                }
            }
            AuraDestination.MANAGE_CLOTHES -> {
                val activePage = manageViewModel.uiState.value.page
                if (activePage != ManageClothesPage.LIST) {
                    manageViewModel.navigateBack()
                    if (activePage == ManageClothesPage.ADD_GUIDE ||
                        activePage == ManageClothesPage.SCAN_FIND ||
                        activePage == ManageClothesPage.DETAIL
                    ) {
                        ttsManager.speak("Kembali ke daftar pakaian.")
                    }
                } else {
                    voiceViewModel.navigateTo(AuraDestination.HOME)
                    ttsManager.speak("Kembali ke halaman Beranda.")
                }
            }
            AuraDestination.PICK_OUTFIT -> {
                if (!pickOutfitViewModel.goToPreviousStep()) {
                    voiceViewModel.navigateTo(AuraDestination.HOME)
                    ttsManager.speak("Kembali ke halaman Beranda.")
                }
            }
            AuraDestination.HISTORY -> {
                voiceViewModel.navigateTo(AuraDestination.HOME)
                ttsManager.speak("Kembali ke halaman Beranda.")
            }
            AuraDestination.HOME -> {
                ttsManager.speak("Anda sudah berada di halaman awal.")
            }
        }
    }

    override fun onConfirmPositive() {
        val dest = voiceViewModel.currentDestination.value
        val manage = manageViewModel.uiState.value
        val history = historyViewModel.uiState.value
        when {
            dest == AuraDestination.MANAGE_CLOTHES && manage.showDeleteConfirmation ->
                manageViewModel.confirmDelete()
            dest == AuraDestination.HISTORY && history.showClearConfirmation ->
                historyViewModel.confirmClearHistory()
            else -> ttsManager.speak("Tidak ada dialog konfirmasi yang aktif.")
        }
    }

    override fun onReadPageGuidance() {
        onListenRecommendations(null)
    }

    override fun onOnboardingNext() {
        onboardingViewModel.nextPage {
            voiceViewModel.navigateTo(AuraDestination.HOME)
        }
    }

    override fun onOnboardingPrevious() {
        if (!onboardingViewModel.previousPage()) {
            ttsManager.speak("Anda sudah berada di halaman pengenalan pertama.")
        }
    }

    override fun onOnboardingFinish() {
        onboardingViewModel.skipOnboarding {
            voiceViewModel.navigateTo(AuraDestination.HOME)
        }
    }

    override fun onToggleOutfitCategory(category: ClothingCategory, selected: Boolean?) {
        pickOutfitViewModel.toggleCategoryByVoice(category, selected)
    }

    override fun onSelectAllOutfitCategories() {
        pickOutfitViewModel.selectAllCategoriesByVoice()
    }

    override fun onContinueRecommendation() {
        val activeDest = voiceViewModel.currentDestination.value
        val pickStep = pickOutfitViewModel.uiState.value.step
        val capturedUri = manageViewModel.uiState.value.capturedCameraPhotoUri
        when {
            activeDest == AuraDestination.PICK_OUTFIT && pickStep == PickOutfitStep.CATEGORY_SELECTION -> {
                pickOutfitViewModel.generateRecommendations()
            }
            activeDest == AuraDestination.MANAGE_CLOTHES &&
                manageViewModel.uiState.value.page == ManageClothesPage.CAMERA &&
                capturedUri != null -> {
                manageViewModel.useCapturedPhoto()
            }
            else -> {
                ttsManager.speak("Pilih tujuan aktivitas terlebih dahulu untuk membuat rekomendasi outfit.")
            }
        }
    }

    override fun onNextRecommendation() {
        if (pickOutfitViewModel.uiState.value.candidates.isNotEmpty()) {
            voiceViewModel.navigateTo(AuraDestination.PICK_OUTFIT)
            pickOutfitViewModel.nextRecommendation()
        } else {
            ttsManager.speak("Belum ada daftar rekomendasi. Pilih tujuan aktivitas terlebih dahulu.")
        }
    }

    override fun onPreviousRecommendation() {
        if (pickOutfitViewModel.uiState.value.candidates.isNotEmpty()) {
            voiceViewModel.navigateTo(AuraDestination.PICK_OUTFIT)
            pickOutfitViewModel.previousRecommendation()
        } else {
            ttsManager.speak("Belum ada daftar rekomendasi.")
        }
    }

    override fun onSelectFirstRecommendation() {
        if (pickOutfitViewModel.selectFirstRecommendationByVoice()) {
            voiceViewModel.navigateTo(AuraDestination.PICK_OUTFIT)
        }
    }

    override fun onSelectCurrentRecommendation() {
        if (pickOutfitViewModel.selectCurrentRecommendationByVoice()) {
            voiceViewModel.navigateTo(AuraDestination.PICK_OUTFIT)
        }
    }

    override fun onSelectNumberedRecommendation(zeroBasedIndex: Int) {
        if (pickOutfitViewModel.selectNumberedRecommendationByVoice(zeroBasedIndex)) {
            voiceViewModel.navigateTo(AuraDestination.PICK_OUTFIT)
        }
    }

    override fun onViewRecommendationDetail(zeroBasedIndex: Int?) {
        val dest = voiceViewModel.currentDestination.value
        val manage = manageViewModel.uiState.value
        if (dest == AuraDestination.MANAGE_CLOTHES && manage.page == ManageClothesPage.SCAN_FIND) {
            val matched = manage.matchedScanItem
            if (matched != null) {
                manageViewModel.openDetails(matched)
            } else {
                ttsManager.speak("Belum ada pakaian yang cocok dari pemindai.")
            }
            return
        }
        if (pickOutfitViewModel.viewRecommendationDetailsByVoice(zeroBasedIndex)) {
            voiceViewModel.navigateTo(AuraDestination.PICK_OUTFIT)
        }
    }

    override fun onListenRecommendations(zeroBasedIndex: Int?) {
        val activeDest = voiceViewModel.currentDestination.value
        val activePickState = pickOutfitViewModel.uiState.value
        val activeManageState = manageViewModel.uiState.value
        when {
            activeDest == AuraDestination.ONBOARDING -> {
                onboardingViewModel.speakCurrentSlide()
            }
            activeDest == AuraDestination.HOME -> {
                homeViewModel.repeatGuidance()
            }
            activeDest == AuraDestination.HISTORY -> {
                historyViewModel.speakHistoryAndRotationSummary()
            }
            activeDest == AuraDestination.PICK_OUTFIT && activePickState.step == PickOutfitStep.RESULT -> {
                pickOutfitViewModel.speakSelectedResult()
            }
            activeDest == AuraDestination.PICK_OUTFIT && activePickState.step == PickOutfitStep.RECOMMENDATIONS -> {
                if (zeroBasedIndex != null) {
                    pickOutfitViewModel.focusRecommendation(zeroBasedIndex)
                } else {
                    pickOutfitViewModel.speakCurrentRecommendationByVoice()
                }
            }
            activeDest == AuraDestination.PICK_OUTFIT && activePickState.step == PickOutfitStep.CATEGORY_SELECTION -> {
                val occ = activePickState.selectedOccasion?.displayName ?: "belum dipilih"
                val selected = activePickState.selectedCategories.joinToString(", ") { it.displayName }
                    .ifBlank { "belum ada kategori dipilih" }
                ttsManager.speak(
                    "Pilih kategori pakaian untuk $occ. Saat ini terpilih: $selected. " +
                        "Ucapkan Atasan, Bawahan, Pakaian Luar, atau Lanjutkan."
                )
            }
            activeDest == AuraDestination.MANAGE_CLOTHES -> when (activeManageState.page) {
                ManageClothesPage.LIST -> manageViewModel.speakWardrobeOverview()
                ManageClothesPage.ADD_GUIDE -> manageViewModel.speakAddGuide()
                ManageClothesPage.RFID_ADD -> manageViewModel.speakRfidAddSteps()
                ManageClothesPage.DETAIL -> activeManageState.selectedItem?.let(manageViewModel::speakItemSummary)
                ManageClothesPage.SCAN_FIND -> activeManageState.matchedScanItem?.let(manageViewModel::speakItemSummary)
                    ?: ttsManager.speak("Belum ada pakaian yang dipindai. Tempelkan tag RFID atau gunakan kamera AI.")
                ManageClothesPage.CAMERA -> manageViewModel.speakCurrentScannerInstruction()
                ManageClothesPage.FORM -> ttsManager.speak(
                    activeManageState.statusMessage
                        ?: "Form detail pakaian aktif. Anda dapat menyebut nama, warna, motif, kategori, aktivitas, atau Simpan."
                )
            }
            activePickState.candidates.isNotEmpty() -> {
                voiceViewModel.navigateTo(AuraDestination.PICK_OUTFIT)
                pickOutfitViewModel.speakCurrentRecommendationByVoice()
            }
            else -> {
                ttsManager.speak("Buka halaman rekomendasi, riwayat, atau lemari pakaian untuk mendengarkan deskripsi.")
            }
        }
    }

    override fun onConfirmWearOutfit() {
        if (!pickOutfitViewModel.confirmSelectedCandidate()) {
            ttsManager.speak("Belum ada outfit yang sedang ditinjau.")
        }
    }

    override fun onSpeakHistoryItem(oneBasedIndex: Int) {
        historyViewModel.speakHistoryByIndex(oneBasedIndex)
    }

    override fun onSpeakRotationItem(oneBasedIndex: Int) {
        historyViewModel.speakRotationByIndex(oneBasedIndex)
    }

    override fun onDeleteHistoryItem(oneBasedIndex: Int) {
        historyViewModel.deleteHistoryByIndex(oneBasedIndex)
    }

    override fun onRequestClearHistory() {
        historyViewModel.requestClearHistory()
        ttsManager.speak("Konfirmasi hapus semua riwayat pemakaian? Katakan Ya atau Batal.")
    }

    override fun onFilterWardrobeCategory(category: ClothingCategory?) {
        manageViewModel.setCategoryFilter(category)
        val label = category?.displayName ?: "Semua kategori"
        ttsManager.speak("Menampilkan $label.")
    }

    override fun onSearchWardrobe(query: String) {
        manageViewModel.onSearchChange(query)
        val count = manageViewModel.uiState.value.visibleItems.size
        ttsManager.speak("Mencari $query. Ditemukan $count pakaian.")
    }

    override fun onClearWardrobeSearch() {
        manageViewModel.onSearchChange("")
        manageViewModel.setCategoryFilter(null)
        ttsManager.speak("Pencarian dihapus. Menampilkan seluruh koleksi.")
    }

    override fun onWardrobeItemAction(action: ItemVoiceAction, oneBasedNumber: Int?, nameQuery: String?) {
        manageViewModel.performVoiceItemAction(action, oneBasedNumber, nameQuery)
    }

    override fun onSeedStarterWardrobe() {
        manageViewModel.seedStarterWardrobe()
    }

    override fun onAddClothing() {
        manageViewModel.openAddGuide(queueFollowUpSpeech = true)
        voiceViewModel.navigateTo(AuraDestination.MANAGE_CLOTHES)
    }

    override fun onStartAddVideoAi() {
        voiceViewModel.navigateTo(AuraDestination.MANAGE_CLOTHES)
        onRequestCameraAccess(false)
    }

    override fun onStartAddRfid() {
        voiceViewModel.navigateTo(AuraDestination.MANAGE_CLOTHES)
        manageViewModel.openRfidAddFlow(queueFollowUpSpeech = true)
    }

    override fun onStartAddManual() {
        voiceViewModel.navigateTo(AuraDestination.MANAGE_CLOTHES)
        manageViewModel.openAddForm()
    }

    override fun onPickFromGallery() {
        voiceViewModel.navigateTo(AuraDestination.MANAGE_CLOTHES)
        onOpenClothingGallery()
    }

    override fun onOpenClothingCamera() {
        val activeDest = voiceViewModel.currentDestination.value
        val activeManageState = manageViewModel.uiState.value
        if (activeDest == AuraDestination.MANAGE_CLOTHES && activeManageState.page == ManageClothesPage.CAMERA) {
            if (activeManageState.capturedCameraPhotoUri != null) {
                manageViewModel.useCapturedPhoto()
            } else {
                manageViewModel.requestVoiceCapture()
            }
            return
        }
        if (activeManageState.page != ManageClothesPage.FORM && activeManageState.page != ManageClothesPage.ADD_GUIDE) {
            manageViewModel.openAddGuide()
        }
        voiceViewModel.navigateTo(AuraDestination.MANAGE_CLOTHES)
        onRequestCameraAccess(false)
    }

    override fun onCapturePhotoNow() {
        val state = manageViewModel.uiState.value
        if (state.page == ManageClothesPage.CAMERA && state.capturedCameraPhotoUri == null) {
            manageViewModel.requestVoiceCapture()
        } else {
            voiceViewModel.navigateTo(AuraDestination.MANAGE_CLOTHES)
            onRequestCameraAccess(false)
        }
    }

    override fun onUseCapturedPhoto() {
        manageViewModel.useCapturedPhoto()
    }

    override fun onRetakePhoto() {
        manageViewModel.retakePhoto()
    }

    override fun onToggleTorch(enable: Boolean?) {
        val isOn = manageViewModel.uiState.value.scannerHud.isTorchOn
        if (enable == null || enable != isOn) {
            manageViewModel.toggleScannerTorch()
        } else {
            ttsManager.speak(if (isOn) "Lampu senter sudah menyala." else "Lampu senter sudah mati.")
        }
    }

    override fun onFlipCamera() {
        manageViewModel.toggleScannerCameraFacing()
    }

    override fun onToggleScannerVoice(enable: Boolean?) {
        val isOn = manageViewModel.uiState.value.scannerHud.isVoiceGuidanceOn
        if (enable == null || enable != isOn) {
            manageViewModel.toggleScannerVoiceGuidance()
        } else {
            ttsManager.speak(if (isOn) "Panduan suara sudah aktif." else "Panduan suara sudah dimatikan.")
        }
    }

    override fun onScanRfid() {
        manageViewModel.openScanFind(queueFollowUpSpeech = true)
        voiceViewModel.navigateTo(AuraDestination.MANAGE_CLOTHES)
    }

    override fun onBindRfidTag() {
        manageViewModel.startRfidBindingInForm()
    }

    override fun onClearRfidTag() {
        manageViewModel.clearRfidInForm()
        ttsManager.speak("Tag RFID dilepas dari formulir.")
    }

    override fun onSimulateRfidDemo() {
        when (manageViewModel.uiState.value.page) {
            ManageClothesPage.RFID_ADD -> manageViewModel.simulateNewRfidAdd()
            ManageClothesPage.SCAN_FIND, ManageClothesPage.FORM ->
                manageViewModel.simulateRfidTap(ClothesRepository.DEMO_RFID_UID)
            else -> ttsManager.speak("Buka pemindai RFID atau form pakaian terlebih dahulu.")
        }
    }

    override fun onRecognizeViaCameraAi() {
        voiceViewModel.navigateTo(AuraDestination.MANAGE_CLOTHES)
        onRequestCameraAccess(true)
    }

    override fun onSetFormName(name: String) {
        manageViewModel.onNameChange(name)
        ttsManager.speak("Nama pakaian diisi: $name.")
    }

    override fun onSetFormMaterial(material: String) {
        manageViewModel.onMaterialChange(material)
        ttsManager.speak("Bahan diisi: $material.")
    }

    override fun onSetFormCategory(category: ClothingCategory) {
        manageViewModel.onCategoryChange(category)
        ttsManager.speak("Kategori dipilih: ${category.displayName}.")
    }

    override fun onSetFormColor(colorName: String) {
        manageViewModel.onColorChange(colorName)
        ttsManager.speak("Warna dipilih: $colorName.")
    }

    override fun onSetFormPattern(pattern: ClothingPattern) {
        manageViewModel.onPatternChange(pattern)
        ttsManager.speak("Motif dipilih: ${pattern.displayName}.")
    }

    override fun onSetFormOccasion(occasion: Occasion) {
        manageViewModel.onOccasionChange(occasion)
        ttsManager.speak("Aktivitas dipilih: ${occasion.displayName}.")
    }

    override fun onRemoveFormPhoto() {
        manageViewModel.removeSelectedPhoto()
    }

    override fun onSaveClothingForm() {
        manageViewModel.saveForm()
    }

    override fun onMarkWornToday() {
        manageViewModel.markSelectedAsWornToday()
    }

    override fun onEditCurrentClothing() {
        manageViewModel.uiState.value.selectedItem?.let(manageViewModel::openEditForm)
            ?: ttsManager.speak("Tidak ada pakaian yang sedang dibuka.")
    }

    override fun onDeleteCurrentClothing() {
        val item = manageViewModel.uiState.value.selectedItem
        if (item != null) {
            manageViewModel.requestDeleteConfirmation(item)
            ttsManager.speak("Apakah Anda yakin ingin menghapus ${item.name}? Katakan Ya atau Batal.")
        } else {
            ttsManager.speak("Tidak ada pakaian yang sedang dipilih.")
        }
    }
}
