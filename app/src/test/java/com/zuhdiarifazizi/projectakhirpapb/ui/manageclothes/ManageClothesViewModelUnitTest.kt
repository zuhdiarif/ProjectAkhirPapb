package com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes

import app.cash.turbine.test
import com.zuhdiarifazizi.projectakhirpapb.data.repository.DemoClothingCatalog
import com.zuhdiarifazizi.projectakhirpapb.data.repository.InMemoryClothesRepository
import com.zuhdiarifazizi.projectakhirpapb.data.storage.ClothingPhotoStorage
import com.zuhdiarifazizi.projectakhirpapb.domain.classifier.ClassificationResult
import com.zuhdiarifazizi.projectakhirpapb.domain.classifier.ClothingClassifier
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.nfc.NfcTagReader
import com.zuhdiarifazizi.projectakhirpapb.domain.scanner.ScannerFrameGuidance
import com.zuhdiarifazizi.projectakhirpapb.domain.scanner.ScannerHapticCue
import com.zuhdiarifazizi.projectakhirpapb.domain.scanner.ScannerReadiness
import com.zuhdiarifazizi.projectakhirpapb.ui.home.FakeHapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.ui.home.FakeTextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.util.MainDispatcherRule
import java.io.ByteArrayInputStream
import java.io.File
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ManageClothesViewModelUnitTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var repository: InMemoryClothesRepository
    private lateinit var photoStorage: ClothingPhotoStorage
    private lateinit var tts: FakeTextToSpeechManager
    private lateinit var haptics: FakeHapticFeedbackManager
    private lateinit var nfcReader: FakeNfcTagReader
    private var classifierResult: Result<ClassificationResult> = Result.success(
        ClassificationResult(
            suggestedName = "Kemeja Katun Biru Navy",
            category = ClothingCategory.TOP_WEAR,
            pattern = ClothingPattern.SOLID,
            occasion = Occasion.WORK,
            dominantColor = HsvColor(220f, 0.82f, 0.25f, "Biru dongker"),
            confidence = 0.94f,
            material = "Katun Halus"
        )
    )
    private lateinit var viewModel: ManageClothesViewModel

    @Before
    fun setUp() {
        repository = InMemoryClothesRepository(DemoClothingCatalog.items)
        val photosDir = tempFolder.newFolder("clothing-images")
        photoStorage = ClothingPhotoStorage(
            photosDirectory = photosDir,
            ioDispatcher = mainDispatcherRule.testDispatcher
        ) { ByteArrayInputStream(byteArrayOf(1, 2, 3)) }
        tts = FakeTextToSpeechManager()
        haptics = FakeHapticFeedbackManager()
        nfcReader = FakeNfcTagReader()
        viewModel = ManageClothesViewModel(
            clothesRepository = repository,
            photoStorage = photoStorage,
            ttsManager = tts,
            hapticManager = haptics,
            classifier = object : ClothingClassifier {
                override suspend fun classify(imageBytes: ByteArray): Result<ClassificationResult> =
                    classifierResult
            },
            nfcTagReader = nfcReader,
            photoByteReader = { byteArrayOf(0x01, 0x02, 0x03) },
            ioDispatcher = mainDispatcherRule.testDispatcher
        )
    }

    @Test
    fun initialCollection_loadsFiltersAndSpeaksOverview() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(13, state.items.size)
            assertFalse(state.isLoading)
            assertTrue(state.isNfcSupported)
        }

        viewModel.setCategoryFilter(ClothingCategory.OUTER_WEAR)
        assertTrue(viewModel.uiState.value.visibleItems.all { it.category == ClothingCategory.OUTER_WEAR })

        viewModel.setCategoryFilter(null)
        viewModel.onSearchChange("Denim")
        assertTrue(viewModel.uiState.value.visibleItems.isNotEmpty())
        assertTrue(viewModel.uiState.value.visibleItems.all { it.name.contains("Denim", ignoreCase = true) })

        viewModel.speakWardrobeOverview()
        assertTrue(tts.lastSpeech.orEmpty().contains("13 pakaian"))
    }

    @Test
    fun guideToLiveCameraAutoCapture_analyzesPhotoAndPrefillsForm() = runTest {
        val dummyFile = File(tempFolder.root, "captured.jpg").apply { writeBytes(byteArrayOf(9, 8, 7)) }
        val photoUri = dummyFile.toURI().toString()

        viewModel.openAddGuide()
        assertEquals(ManageClothesPage.ADD_GUIDE, viewModel.uiState.value.page)

        viewModel.openCamera(findMode = false)
        assertEquals(ManageClothesPage.CAMERA, viewModel.uiState.value.page)

        val guidance = ScannerFrameGuidance(
            lightPercent = 88,
            needsTorch = false,
            readiness = ScannerReadiness.READY_HOLD_STEADY,
            stepNumber = 4,
            stepLabel = "Langkah 4 / 4",
            detectedTitle = "Pakaian Biru dongker",
            guidanceText = "Posisi terdeteksi sejajar.",
            spokenCue = "Posisi stabil. Mengambil foto sekarang.",
            hapticCue = ScannerHapticCue.LOCKED_CAPTURE,
            isReadyForAutoCapture = true,
            shouldTriggerAutoCapture = true
        )
        viewModel.onScannerFrameEvaluated(guidance)
        assertEquals(1, viewModel.uiState.value.captureRequestId)
        assertEquals(88, viewModel.uiState.value.scannerHud.lightPercent)

        viewModel.onCameraPhotoCaptured(photoUri)
        assertEquals(photoUri, viewModel.uiState.value.capturedCameraPhotoUri)

        viewModel.useCapturedPhoto()
        val formState = viewModel.uiState.value
        assertEquals(ManageClothesPage.FORM, formState.page)
        assertEquals("Kemeja Katun Biru Navy", formState.form.name)
        assertEquals("Biru dongker", formState.form.colorName)
        assertEquals("Katun Halus", formState.form.material)
        assertEquals(ClothingCategory.TOP_WEAR, formState.form.category)
        assertEquals(Occasion.WORK, formState.form.occasion)
    }

    @Test
    fun rfidBindingDuplicateGuardAndSave_persistsToWardrobeAndSupportsScanFind() = runTest {
        viewModel.openAddForm()
        viewModel.onNameChange("Batik Kerja Eksekutif")
        viewModel.onColorChange("Biru dongker")
        viewModel.onMaterialChange("Katun Batik")
        viewModel.startRfidBindingInForm()
        assertTrue(viewModel.uiState.value.isBindingRfidInForm)

        viewModel.simulateRfidTap("04ab127e")
        assertEquals("04:AB:12:7E", viewModel.uiState.value.form.rfidTagUid)
        assertFalse(viewModel.uiState.value.isBindingRfidInForm)

        viewModel.saveForm()
        assertEquals(ManageClothesPage.LIST, viewModel.uiState.value.page)
        assertEquals(14, viewModel.uiState.value.items.size)
        val savedItem = viewModel.uiState.value.items.first { it.name == "Batik Kerja Eksekutif" }
        assertEquals("04:AB:12:7E", savedItem.rfidTagUid)

        viewModel.openAddForm()
        viewModel.onNameChange("Kemeja Duplikat")
        viewModel.onColorChange("Putih")
        viewModel.startRfidBindingInForm()
        viewModel.simulateRfidTap("04:AB:12:7E")
        viewModel.saveForm()
        assertNotNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.errorMessage.orEmpty().contains("sudah terdaftar"))

        viewModel.openScanFind()
        assertEquals(ManageClothesPage.SCAN_FIND, viewModel.uiState.value.page)
        viewModel.simulateRfidTap("04:AB:12:7E")
        assertEquals(savedItem.id, viewModel.uiState.value.matchedScanItem?.id)
        assertTrue(tts.lastSpeech.orEmpty().contains("Batik Kerja Eksekutif"))

        viewModel.simulateRfidTap("AA:BB:CC:DD")
        assertNull(viewModel.uiState.value.matchedScanItem)
        assertTrue(viewModel.uiState.value.scanStatusMessage.orEmpty().contains("belum terdaftar"))
    }

    @Test
    fun findModeCameraAndListMenuDelete_workEndToEnd() = runTest {
        val dummyFile = File(tempFolder.root, "find.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }

        viewModel.openCamera(findMode = true)
        viewModel.onCameraPhotoCaptured(dummyFile.toURI().toString())
        assertEquals(ManageClothesPage.SCAN_FIND, viewModel.uiState.value.page)
        assertNotNull(viewModel.uiState.value.matchedScanItem)

        val target = viewModel.uiState.value.items.first()
        viewModel.openDetails(target)
        viewModel.markSelectedAsWornToday()
        assertNotNull(viewModel.uiState.value.selectedItem?.lastWornEpochMillis)
        assertTrue(tts.lastSpeech.orEmpty().contains("ditandai dipakai hari ini"))

        viewModel.openCamera(findMode = false)
        viewModel.requestVoiceCapture()
        assertEquals(1, viewModel.uiState.value.captureRequestId)
        viewModel.cancelCamera()

        viewModel.returnToList()
        viewModel.requestDeleteConfirmation(target)
        assertTrue(viewModel.uiState.value.showDeleteConfirmation)
        viewModel.confirmDelete()

        assertEquals(12, viewModel.uiState.value.items.size)
        assertFalse(viewModel.uiState.value.items.any { it.id == target.id })
    }

    private class FakeNfcTagReader : NfcTagReader {
        override val isSupported: Boolean = true
        override val isEnabled: StateFlow<Boolean> = MutableStateFlow(true)
        private val _tagScanned = MutableSharedFlow<String>(extraBufferCapacity = 4)
        override val tagScanned: SharedFlow<String> = _tagScanned

        override fun refreshAvailability() = Unit

        override fun simulateTagScan(uid: String) {
            _tagScanned.tryEmit(uid)
        }
    }
}
