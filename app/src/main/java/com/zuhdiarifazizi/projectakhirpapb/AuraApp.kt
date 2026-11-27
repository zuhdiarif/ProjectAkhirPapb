package com.zuhdiarifazizi.projectakhirpapb

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zuhdiarifazizi.projectakhirpapb.data.device.AndroidHapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.data.device.AndroidNfcTagReader
import com.zuhdiarifazizi.projectakhirpapb.data.device.AndroidSpeechRecognizerManager
import com.zuhdiarifazizi.projectakhirpapb.data.device.AndroidTextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.data.device.CameraXGarmentAnalyzer
import com.zuhdiarifazizi.projectakhirpapb.data.device.ClothingPhotoStorage
import com.zuhdiarifazizi.projectakhirpapb.data.device.DeviceTiltMonitor
import com.zuhdiarifazizi.projectakhirpapb.data.device.IndonesianIntentRecognizer
import com.zuhdiarifazizi.projectakhirpapb.data.harmony.AndroidProjectHarmonyScorer
import com.zuhdiarifazizi.projectakhirpapb.data.local.AuraDatabase
import com.zuhdiarifazizi.projectakhirpapb.data.remote.DefaultClothingClassifier
import com.zuhdiarifazizi.projectakhirpapb.data.repository.DefaultActivityCategoryRepository
import com.zuhdiarifazizi.projectakhirpapb.data.repository.RoomClothesRepository
import com.zuhdiarifazizi.projectakhirpapb.data.repository.RoomOnboardingRepository
import com.zuhdiarifazizi.projectakhirpapb.data.repository.RoomOutfitRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.model.LumaChromaGrid
import com.zuhdiarifazizi.projectakhirpapb.domain.model.TiltState
import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoicePage
import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoiceScreenContext
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ClothesRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.usecase.CalculateOutfitScoreUseCase
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraBottomNavigation
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraSpeakingBanner
import com.zuhdiarifazizi.projectakhirpapb.ui.components.FeedbackBanner
import com.zuhdiarifazizi.projectakhirpapb.ui.history.HistoryScreen
import com.zuhdiarifazizi.projectakhirpapb.ui.history.HistoryViewModel
import com.zuhdiarifazizi.projectakhirpapb.ui.home.HomeScreen
import com.zuhdiarifazizi.projectakhirpapb.ui.home.HomeViewModel
import com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes.ManageClothesPage
import com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes.ManageClothesScreen
import com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes.ManageClothesViewModel
import com.zuhdiarifazizi.projectakhirpapb.ui.navigation.AuraDestination
import com.zuhdiarifazizi.projectakhirpapb.ui.onboarding.OnboardingScreen
import com.zuhdiarifazizi.projectakhirpapb.ui.onboarding.OnboardingViewModel
import com.zuhdiarifazizi.projectakhirpapb.ui.pickoutfit.PickOutfitScreen
import com.zuhdiarifazizi.projectakhirpapb.ui.pickoutfit.PickOutfitStep
import com.zuhdiarifazizi.projectakhirpapb.ui.pickoutfit.PickOutfitViewModel
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraIndigoSoft
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavyDeep
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSpacing
import com.zuhdiarifazizi.projectakhirpapb.ui.voice.VoiceViewModel
import com.zuhdiarifazizi.projectakhirpapb.ui.voice.createAuraVoiceNavigationCallback
import java.io.ByteArrayOutputStream

@Composable
fun AuraApp() {
    val context = LocalContext.current

    val auraDatabase = remember { AuraDatabase.getInstance(context.applicationContext) }
    val photoStorage = remember { ClothingPhotoStorage(context.applicationContext) }
    val clothesRepository = remember(auraDatabase) { RoomClothesRepository(auraDatabase) }
    val outfitRepository = remember(auraDatabase) { RoomOutfitRepository(auraDatabase) }
    val onboardingRepository = remember(auraDatabase) { RoomOnboardingRepository(auraDatabase) }
    val categoryRepository = remember { DefaultActivityCategoryRepository() }
    val nfcTagReader = remember { AndroidNfcTagReader(context.applicationContext) }

    var voiceContextProvider by remember {
        mutableStateOf<() -> VoiceScreenContext>({ VoiceScreenContext() })
    }

    val voiceViewModel: VoiceViewModel = viewModel {
        val appContext = context.applicationContext
        VoiceViewModel(
            ttsManager = AndroidTextToSpeechManager(appContext),
            hapticManager = AndroidHapticFeedbackManager(appContext),
            speechRecognizerManager = AndroidSpeechRecognizerManager(appContext),
            nfcTagReader = nfcTagReader,
            intentRecognizer = IndonesianIntentRecognizer(),
            contextProvider = { voiceContextProvider() }
        )
    }
    val ttsManager = voiceViewModel.ttsManager
    val hapticManager = voiceViewModel.hapticManager
    val voiceInteractionManager = voiceViewModel.voiceInteractionManager

    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = remember(context) { context.findActivity() }
    val classifier = remember {
        DefaultClothingClassifier(apiKey = BuildConfig.GEMINI_API_KEY)
    }
    val harmonyScorer = remember {
        AndroidProjectHarmonyScorer(context.applicationContext)
    }
    DisposableEffect(harmonyScorer) {
        onDispose { harmonyScorer.close() }
    }

    val tiltMonitor = remember { DeviceTiltMonitor(context.applicationContext) }
    val tiltState by tiltMonitor.tiltState.collectAsState()
    val currentDestination by voiceViewModel.currentDestination.collectAsState()

    val onboardingViewModel: OnboardingViewModel = viewModel {
        OnboardingViewModel(
            onboardingRepository = onboardingRepository,
            ttsManager = ttsManager,
            hapticManager = hapticManager
        )
    }
    val manageViewModel: ManageClothesViewModel = viewModel {
        ManageClothesViewModel(
            clothesRepository = clothesRepository,
            photoStorage = photoStorage,
            ttsManager = ttsManager,
            hapticManager = hapticManager,
            classifier = classifier,
            nfcTagReader = nfcTagReader,
            photoByteReader = { uriString -> readSafeImageBytes(context.applicationContext, uriString) }
        )
    }
    val pickOutfitViewModel: PickOutfitViewModel = viewModel {
        PickOutfitViewModel(
            clothesRepository = clothesRepository,
            ttsManager = ttsManager,
            hapticManager = hapticManager,
            calculateOutfitScoreUseCase = CalculateOutfitScoreUseCase(harmonyScorer = harmonyScorer),
            outfitRepository = outfitRepository,
            userIdProvider = { ClothesRepository.DEFAULT_USER_ID }
        )
    }
    val historyViewModel: HistoryViewModel = viewModel {
        HistoryViewModel(
            clothesRepository = clothesRepository,
            outfitRepository = outfitRepository,
            ttsManager = ttsManager,
            hapticManager = hapticManager
        )
    }
    val homeViewModel: HomeViewModel = viewModel {
        HomeViewModel(
            activityCategoryRepository = categoryRepository,
            ttsManager = ttsManager,
            hapticManager = hapticManager,
            voiceInteractionManager = voiceInteractionManager
        )
    }

    val manageState by manageViewModel.uiState.collectAsState()
    val voiceState by voiceViewModel.state.collectAsState()
    val globalFeedbackBanner by voiceViewModel.globalFeedbackBanner.collectAsState()
    val onboardingState by onboardingViewModel.uiState.collectAsState()
    val pickState by pickOutfitViewModel.uiState.collectAsState()
    val historyState by historyViewModel.uiState.collectAsState()
    val homeState by homeViewModel.uiState.collectAsState()
    val isTtsSpeaking by ttsManager.isSpeakingFlow.collectAsState()
    val activeSpeechText by ttsManager.currentSpeechFlow.collectAsState()

    val isCameraActive = currentDestination == AuraDestination.MANAGE_CLOTHES &&
        manageState.page == ManageClothesPage.CAMERA &&
        manageState.capturedCameraPhotoUri == null
    DisposableEffect(tiltMonitor, isCameraActive) {
        if (isCameraActive) tiltMonitor.start() else tiltMonitor.stop()
        onDispose { tiltMonitor.stop() }
    }

    DisposableEffect(lifecycleOwner, activity, nfcTagReader) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> activity?.let(nfcTagReader::enableReader)
                Lifecycle.Event.ON_PAUSE -> activity?.let(nfcTagReader::disableReader)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            activity?.let(nfcTagReader::enableReader)
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            activity?.let(nfcTagReader::disableReader)
        }
    }

    voiceContextProvider = remember(voiceViewModel, manageViewModel, pickOutfitViewModel, historyViewModel) {
        {
            val dest = voiceViewModel.currentDestination.value
            val manage = manageViewModel.uiState.value
            val pick = pickOutfitViewModel.uiState.value
            val history = historyViewModel.uiState.value
            val hasDialog = (dest == AuraDestination.MANAGE_CLOTHES && manage.showDeleteConfirmation) ||
                (dest == AuraDestination.HISTORY && history.showClearConfirmation)

            val page = when (dest) {
                AuraDestination.ONBOARDING -> VoicePage.ONBOARDING
                AuraDestination.HOME -> VoicePage.HOME
                AuraDestination.HISTORY -> VoicePage.HISTORY
                AuraDestination.PICK_OUTFIT -> when (pick.step) {
                    PickOutfitStep.CATEGORY_SELECTION -> VoicePage.PICK_CATEGORY
                    PickOutfitStep.RECOMMENDATIONS -> VoicePage.PICK_RECOMMENDATIONS
                    PickOutfitStep.RESULT -> VoicePage.PICK_RESULT
                }
                AuraDestination.MANAGE_CLOTHES -> when (manage.page) {
                    ManageClothesPage.LIST -> VoicePage.WARDROBE_LIST
                    ManageClothesPage.ADD_GUIDE -> VoicePage.ADD_GUIDE
                    ManageClothesPage.RFID_ADD -> VoicePage.RFID_ADD
                    ManageClothesPage.CAMERA -> if (manage.capturedCameraPhotoUri != null) {
                        VoicePage.CAMERA_PREVIEW
                    } else {
                        VoicePage.CAMERA_LIVE
                    }
                    ManageClothesPage.FORM -> VoicePage.CLOTHING_FORM
                    ManageClothesPage.DETAIL -> VoicePage.CLOTHING_DETAIL
                    ManageClothesPage.SCAN_FIND -> VoicePage.SCAN_FIND
                }
            }
            VoiceScreenContext(page = page, hasConfirmationDialog = hasDialog)
        }
    }

    var pendingFindCameraMode by remember { mutableStateOf(false) }
    var initialCheckHandled by remember { mutableStateOf(false) }

    LaunchedEffect(onboardingState.isCheckingStatus, onboardingState.isCompleted) {
        if (!initialCheckHandled && !onboardingState.isCheckingStatus) {
            initialCheckHandled = true
            if (onboardingState.isCompleted && currentDestination == AuraDestination.ONBOARDING) {
                voiceViewModel.navigateTo(AuraDestination.HOME)
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            manageViewModel.openCamera(findMode = pendingFindCameraMode)
        } else {
            manageViewModel.onCameraPermissionDenied()
        }
        pendingFindCameraMode = false
    }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> manageViewModel.selectGalleryPhoto(uri?.toString()) }

    val requestCameraAccess: (Boolean) -> Unit = { findMode ->
        val hasCamera = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        pendingFindCameraMode = findMode
        when {
            !hasCamera -> {
                pendingFindCameraMode = false
                manageViewModel.onCameraUnavailable()
            }
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                pendingFindCameraMode = false
                manageViewModel.openCamera(findMode = findMode)
            }
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    val latestRequestCameraAccess by rememberUpdatedState(requestCameraAccess)
    val latestOpenClothingGallery by rememberUpdatedState {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    BackHandler(
        enabled = (currentDestination == AuraDestination.ONBOARDING && onboardingState.currentPage > 0) ||
            (currentDestination == AuraDestination.PICK_OUTFIT) ||
            (currentDestination == AuraDestination.MANAGE_CLOTHES && manageState.page != ManageClothesPage.LIST)
    ) {
        when (currentDestination) {
            AuraDestination.ONBOARDING -> onboardingViewModel.previousPage()
            AuraDestination.MANAGE_CLOTHES -> manageViewModel.navigateBack()
            AuraDestination.PICK_OUTFIT -> if (!pickOutfitViewModel.goToPreviousStep()) {
                voiceViewModel.navigateTo(AuraDestination.HOME)
            }
            else -> Unit
        }
    }

    val voiceNavigationCallback = remember {
        createAuraVoiceNavigationCallback(
            voiceViewModel = voiceViewModel,
            onboardingViewModel = onboardingViewModel,
            homeViewModel = homeViewModel,
            pickOutfitViewModel = pickOutfitViewModel,
            historyViewModel = historyViewModel,
            manageViewModel = manageViewModel,
            ttsManager = ttsManager,
            onRequestCameraAccess = { findMode -> latestRequestCameraAccess(findMode) },
            onOpenClothingGallery = { latestOpenClothingGallery() }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        homeViewModel.onVoiceActionClicked(
            hasRecordAudioPermission = isGranted,
            navigationCallback = voiceNavigationCallback
        )
    }

    val onVoiceActionClick = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            homeViewModel.onVoiceActionClicked(
                hasRecordAudioPermission = true,
                navigationCallback = voiceNavigationCallback
            )
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentDestination != AuraDestination.ONBOARDING &&
                !(currentDestination == AuraDestination.MANAGE_CLOTHES && manageState.page != ManageClothesPage.LIST)
            ) {
                AuraBottomNavigation(
                    currentDestination = currentDestination,
                    onNavigate = { destination ->
                        voiceViewModel.navigateTo(destination)
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AuraSpeakingBanner(
                isSpeaking = isTtsSpeaking,
                currentSpeech = activeSpeechText,
                onStop = ttsManager::stop
            )
            if (voiceState.isListening && voiceState.lastTranscript.isNotBlank()) {
                VoiceLiveTranscriptBanner(transcript = voiceState.lastTranscript)
            }
            if (currentDestination != AuraDestination.HOME) {
                globalFeedbackBanner?.let { banner ->
                    FeedbackBanner(
                        feedback = banner,
                        onDismiss = voiceViewModel::dismissGlobalBanner,
                        modifier = Modifier.padding(horizontal = AuraSpacing.md, vertical = 4.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val screenModifier = Modifier.fillMaxSize()
                when (currentDestination) {
                    AuraDestination.ONBOARDING -> OnboardingScreen(
                        uiState = onboardingState,
                        isVoiceListening = voiceState.isListening,
                        partialTranscript = voiceState.lastTranscript,
                        onVoiceClick = onVoiceActionClick,
                        onSpeakSlide = onboardingViewModel::speakCurrentSlide,
                        onNext = {
                            onboardingViewModel.nextPage {
                                voiceViewModel.navigateTo(AuraDestination.HOME)
                            }
                        },
                        onPrevious = { onboardingViewModel.previousPage() },
                        onSkip = {
                            onboardingViewModel.skipOnboarding {
                                voiceViewModel.navigateTo(AuraDestination.HOME)
                            }
                        },
                        modifier = screenModifier
                    )
                    AuraDestination.HOME -> HomeScreen(
                        uiState = homeState,
                        onCategoryClick = { category ->
                            homeViewModel.selectCategory(category) {
                                pickOutfitViewModel.beginSelection(category.occasion)
                                voiceViewModel.navigateTo(AuraDestination.PICK_OUTFIT)
                            }
                        },
                        onVoiceButtonClick = onVoiceActionClick,
                        onRepeatGuidance = {
                            homeViewModel.repeatGuidance()
                        },
                        onScreenShown = {
                            homeViewModel.playWelcomeGuidance(force = false)
                        },
                        onDismissBanner = {
                            homeViewModel.dismissFeedbackBanner()
                        },
                        onOpenOnboarding = {
                            onboardingViewModel.reopenOnboarding()
                            voiceViewModel.navigateTo(AuraDestination.ONBOARDING)
                        },
                        partialTranscript = voiceState.lastTranscript,
                        modifier = screenModifier
                    )
                    AuraDestination.MANAGE_CLOTHES -> ManageClothesScreen(
                        uiState = manageState,
                        isVoiceListening = voiceState.isListening,
                        partialTranscript = voiceState.lastTranscript,
                        onVoiceClick = onVoiceActionClick,
                        onAddGuide = { manageViewModel.openAddGuide() },
                        onSpeakAddGuide = manageViewModel::speakAddGuide,
                        onStartRfidAdd = { manageViewModel.openRfidAddFlow() },
                        onSpeakRfidAddSteps = manageViewModel::speakRfidAddSteps,
                        onSimulateNewRfidTag = manageViewModel::simulateNewRfidAdd,
                        onAddManual = manageViewModel::openAddForm,
                        onScanFind = { manageViewModel.openScanFind() },
                        onSpeakOverview = { manageViewModel.speakWardrobeOverview() },
                        onSpeakItem = manageViewModel::speakItemSummary,
                        onItemDetails = manageViewModel::openDetails,
                        onEdit = manageViewModel::openEditForm,
                        onSearchChange = manageViewModel::onSearchChange,
                        onCategoryFilter = manageViewModel::setCategoryFilter,
                        onRequestDelete = { manageViewModel.requestDeleteConfirmation(it) },
                        onCancelDelete = manageViewModel::cancelDelete,
                        onConfirmDelete = manageViewModel::confirmDelete,
                        onFormBack = manageViewModel::cancelForm,
                        onNameChange = manageViewModel::onNameChange,
                        onMaterialChange = manageViewModel::onMaterialChange,
                        onCategoryChange = manageViewModel::onCategoryChange,
                        onPatternChange = manageViewModel::onPatternChange,
                        onOccasionChange = manageViewModel::onOccasionChange,
                        onColorChange = manageViewModel::onColorChange,
                        onCameraClick = { requestCameraAccess(false) },
                        onFindCameraClick = { requestCameraAccess(true) },
                        onGalleryClick = latestOpenClothingGallery,
                        onRemovePhoto = manageViewModel::removeSelectedPhoto,
                        onBindRfidClick = manageViewModel::startRfidBindingInForm,
                        onCancelRfidBind = manageViewModel::cancelRfidBindingInForm,
                        onClearRfid = manageViewModel::clearRfidInForm,
                        onSimulateRfidTap = manageViewModel::simulateRfidTap,
                        onSave = manageViewModel::saveForm,
                        onBackToList = manageViewModel::returnToList,
                        onEditSelected = { manageState.selectedItem?.let(manageViewModel::openEditForm) },
                        onMarkWornToday = manageViewModel::markSelectedAsWornToday,
                        onToggleTorch = manageViewModel::toggleScannerTorch,
                        onToggleCameraFacing = manageViewModel::toggleScannerCameraFacing,
                        onToggleVoiceGuidance = manageViewModel::toggleScannerVoiceGuidance,
                        onSpeakCameraInstruction = manageViewModel::speakCurrentScannerInstruction,
                        onScannerFrameEvaluated = manageViewModel::onScannerFrameEvaluated,
                        onCameraReady = manageViewModel::onCameraReady,
                        onCameraUnavailable = manageViewModel::onCameraUnavailable,
                        onCameraCaptureFailed = manageViewModel::onCameraCaptureFailed,
                        onCameraPhotoCaptured = manageViewModel::onCameraPhotoCaptured,
                        onUseCapturedPhoto = manageViewModel::useCapturedPhoto,
                        onRetakePhoto = manageViewModel::retakePhoto,
                        onCancelCamera = manageViewModel::cancelCamera,
                        createCameraFile = photoStorage::createCameraOutputFile,
                        tiltState = if (isCameraActive) tiltState else TiltState(),
                        createFrameAnalyzer = { onGridReady: (LumaChromaGrid) -> Unit ->
                            CameraXGarmentAnalyzer(onGridReady)
                        },
                        onSeedStarterWardrobe = manageViewModel::seedStarterWardrobe,
                        modifier = screenModifier
                    )
                    AuraDestination.PICK_OUTFIT -> PickOutfitScreen(
                        uiState = pickState,
                        isVoiceListening = voiceState.isListening,
                        partialTranscript = voiceState.lastTranscript,
                        onBackToHome = { voiceViewModel.navigateTo(AuraDestination.HOME) },
                        onBackStep = { pickOutfitViewModel.goToPreviousStep() },
                        onVoiceClick = onVoiceActionClick,
                        onCategoryChanged = pickOutfitViewModel::setCategorySelected,
                        onContinue = { pickOutfitViewModel.generateRecommendations() },
                        onNextRecommendation = pickOutfitViewModel::nextRecommendation,
                        onSpeakRecommendation = pickOutfitViewModel::speakRecommendation,
                        onViewDetails = pickOutfitViewModel::viewRecommendationDetails,
                        onChooseRecommendation = { pickOutfitViewModel.chooseRecommendation(it) },
                        onConfirmWearResult = { pickOutfitViewModel.confirmSelectedCandidate() },
                        onSpeakResult = pickOutfitViewModel::speakSelectedResult,
                        modifier = screenModifier
                    )
                    AuraDestination.HISTORY -> HistoryScreen(
                        uiState = historyState,
                        isVoiceListening = voiceState.isListening,
                        partialTranscript = voiceState.lastTranscript,
                        onVoiceClick = onVoiceActionClick,
                        onSpeakSummary = { historyViewModel.speakHistoryAndRotationSummary() },
                        onSpeakHistoryEntry = historyViewModel::speakHistoryEntry,
                        onDeleteHistoryEntry = historyViewModel::deleteHistoryEntry,
                        onSpeakRotationRecommendation = historyViewModel::speakRotationRecommendation,
                        onGoToPickOutfit = { voiceViewModel.navigateTo(AuraDestination.HOME) },
                        onRequestClearHistory = historyViewModel::requestClearHistory,
                        onCancelClearHistory = historyViewModel::cancelClearHistory,
                        onConfirmClearHistory = historyViewModel::confirmClearHistory,
                        modifier = screenModifier
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceLiveTranscriptBanner(transcript: String) {
    Surface(
        color = AuraIndigoSoft,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AuraSpacing.md, vertical = 4.dp)
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_mic),
                contentDescription = null,
                tint = AuraNavyDeep,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Terdengar: \"$transcript\"",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = AuraNavyDeep
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun readSafeImageBytes(context: Context, uriString: String): ByteArray {
    val parsed = Uri.parse(uriString)
    val scheme = parsed.scheme?.lowercase()
    if (scheme != "content" && scheme != "file") return ByteArray(0)
    return runCatching {
        context.contentResolver.openInputStream(parsed)?.use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            val out = ByteArrayOutputStream()
            var total = 0
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                total += read
                if (total > 15 * 1024 * 1024) return@use ByteArray(0)
                out.write(buffer, 0, read)
            }
            out.toByteArray()
        } ?: ByteArray(0)
    }.getOrDefault(ByteArray(0))
}
