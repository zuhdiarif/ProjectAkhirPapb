package com.zuhdiarifazizi.projectakhirpapb

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.zuhdiarifazizi.projectakhirpapb.data.classifier.DefaultClothingClassifier
import com.zuhdiarifazizi.projectakhirpapb.data.feedback.AndroidHapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.data.feedback.AndroidTextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.data.harmony.AndroidProjectHarmonyScorer
import com.zuhdiarifazizi.projectakhirpapb.data.local.AuraDatabase
import com.zuhdiarifazizi.projectakhirpapb.data.nfc.AndroidNfcTagReader
import com.zuhdiarifazizi.projectakhirpapb.data.repository.DefaultActivityCategoryRepository
import com.zuhdiarifazizi.projectakhirpapb.data.repository.DemoClothingCatalog
import com.zuhdiarifazizi.projectakhirpapb.data.repository.RoomAuthRepository
import com.zuhdiarifazizi.projectakhirpapb.data.repository.RoomClothesRepository
import com.zuhdiarifazizi.projectakhirpapb.data.repository.RoomOutfitRepository
import com.zuhdiarifazizi.projectakhirpapb.data.storage.ClothingPhotoStorage
import com.zuhdiarifazizi.projectakhirpapb.data.voice.AndroidSpeechRecognizerManager
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.usecase.CalculateOutfitScoreUseCase
import com.zuhdiarifazizi.projectakhirpapb.domain.voice.VoiceNavigationCallback
import com.zuhdiarifazizi.projectakhirpapb.ui.auth.AuthScreen
import com.zuhdiarifazizi.projectakhirpapb.ui.auth.AuthViewModel
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraBottomNavigation
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraProfileDialog
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
import com.zuhdiarifazizi.projectakhirpapb.ui.pickoutfit.PickOutfitScreen
import com.zuhdiarifazizi.projectakhirpapb.ui.pickoutfit.PickOutfitStep
import com.zuhdiarifazizi.projectakhirpapb.ui.pickoutfit.PickOutfitViewModel
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraIndigoSoft
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavyDeep
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSpacing
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.ProjectAkhirPapbTheme
import com.zuhdiarifazizi.projectakhirpapb.ui.voice.VoiceViewModel
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjectAkhirPapbTheme {
                AuraAppShell()
            }
        }
    }
}

@Composable
fun AuraAppShell() {
    val context = LocalContext.current

    val clothesDatabase = remember { AuraDatabase.getInstance(context.applicationContext) }
    val photoStorage = remember { ClothingPhotoStorage(context.applicationContext) }
    val clothesRepository = remember(clothesDatabase) { RoomClothesRepository(clothesDatabase) }
    val outfitRepository = remember(clothesDatabase) { RoomOutfitRepository(clothesDatabase) }
    val authRepository = remember(clothesDatabase) { RoomAuthRepository(clothesDatabase) }
    val categoryRepository = remember { DefaultActivityCategoryRepository() }
    val activeUserIdFlow = remember { MutableStateFlow(DemoClothingCatalog.USER_ID) }

    val voiceViewModel: VoiceViewModel = viewModel {
        val appContext = context.applicationContext
        VoiceViewModel(
            ttsManager = AndroidTextToSpeechManager(appContext),
            hapticManager = AndroidHapticFeedbackManager(appContext),
            speechRecognizerManager = AndroidSpeechRecognizerManager(appContext),
            nfcTagReader = AndroidNfcTagReader(appContext)
        )
    }
    val ttsManager = voiceViewModel.ttsManager
    val hapticManager = voiceViewModel.hapticManager
    val voiceInteractionManager = voiceViewModel.voiceInteractionManager
    val nfcTagReader = voiceViewModel.nfcTagReader

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

    val authViewModel: AuthViewModel = viewModel {
        AuthViewModel(
            authRepository = authRepository,
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
            photoByteReader = { uriString -> readSafeImageBytes(context.applicationContext, uriString) },
            userIdFlow = activeUserIdFlow
        )
    }
    val pickOutfitViewModel: PickOutfitViewModel = viewModel {
        PickOutfitViewModel(
            clothesRepository = clothesRepository,
            ttsManager = ttsManager,
            hapticManager = hapticManager,
            calculateOutfitScoreUseCase = CalculateOutfitScoreUseCase(harmonyScorer = harmonyScorer),
            outfitRepository = outfitRepository,
            userIdProvider = { activeUserIdFlow.value }
        )
    }
    val historyViewModel: HistoryViewModel = viewModel {
        HistoryViewModel(
            clothesRepository = clothesRepository,
            outfitRepository = outfitRepository,
            ttsManager = ttsManager,
            hapticManager = hapticManager,
            userIdFlow = activeUserIdFlow
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

    val currentDestination by voiceViewModel.currentDestination.collectAsState()
    val voiceState by voiceViewModel.state.collectAsState()
    val globalFeedbackBanner by voiceViewModel.globalFeedbackBanner.collectAsState()
    val manageState by manageViewModel.uiState.collectAsState()
    val pickState by pickOutfitViewModel.uiState.collectAsState()
    val historyState by historyViewModel.uiState.collectAsState()
    val homeState by homeViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val isTtsSpeaking by ttsManager.isSpeakingFlow.collectAsState()
    val activeSpeechText by ttsManager.currentSpeechFlow.collectAsState()

    var pendingFindCameraMode by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    LaunchedEffect(authState.isSessionLoading, authState.currentUser) {
        val signedInUser = authState.currentUser
        if (signedInUser != null) {
            activeUserIdFlow.value = signedInUser.id
            if (currentDestination == AuraDestination.AUTH || currentDestination == AuraDestination.REGISTER) {
                voiceViewModel.navigateTo(AuraDestination.HOME)
            }
        } else if (!authState.isSessionLoading) {
            activeUserIdFlow.value = DemoClothingCatalog.USER_ID
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
    val openClothingGallery = {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    BackHandler(
        enabled = currentDestination == AuraDestination.REGISTER ||
            (currentDestination == AuraDestination.PICK_OUTFIT && pickState.step != PickOutfitStep.CATEGORY_SELECTION) ||
            (currentDestination == AuraDestination.MANAGE_CLOTHES && manageState.page != ManageClothesPage.LIST)
    ) {
        when (currentDestination) {
            AuraDestination.REGISTER -> {
                authViewModel.showLogin()
                voiceViewModel.navigateTo(AuraDestination.AUTH)
            }
            AuraDestination.MANAGE_CLOTHES -> manageViewModel.navigateBack()
            AuraDestination.PICK_OUTFIT -> if (!pickOutfitViewModel.goToPreviousStep()) {
                voiceViewModel.navigateTo(AuraDestination.HOME)
            }
            else -> Unit
        }
    }

    val ensureSignedInForNavigation: () -> Boolean = {
        if (authViewModel.uiState.value.currentUser == null) {
            authViewModel.loginQuickAccess {
                voiceViewModel.navigateTo(AuraDestination.HOME)
            }
            false
        } else {
            true
        }
    }

    val voiceNavigationCallback = remember {
        object : VoiceNavigationCallback {
            override fun onQuickLogin() {
                authViewModel.loginQuickAccess {
                    voiceViewModel.navigateTo(AuraDestination.HOME)
                }
            }

            override fun onLogoutAccount() {
                showProfileDialog = false
                authViewModel.logout {
                    voiceViewModel.navigateTo(AuraDestination.AUTH)
                }
            }

            override fun onSelectOccasion(occasion: Occasion) {
                if (authViewModel.uiState.value.currentUser == null) {
                    authViewModel.loginQuickAccess()
                }
                homeViewModel.selectCategoryByOccasion(occasion, announceTts = false)
                pickOutfitViewModel.beginSelection(occasion, queueFollowUpSpeech = true)
                voiceViewModel.navigateTo(AuraDestination.PICK_OUTFIT)
            }

            override fun onOpenHome() {
                if (ensureSignedInForNavigation()) {
                    voiceViewModel.navigateTo(AuraDestination.HOME)
                }
            }

            override fun onOpenHistory() {
                if (authViewModel.uiState.value.currentUser == null) {
                    authViewModel.loginQuickAccess()
                }
                voiceViewModel.navigateTo(AuraDestination.HISTORY)
            }

            override fun onOpenWardrobe() {
                if (authViewModel.uiState.value.currentUser == null) {
                    authViewModel.loginQuickAccess()
                }
                if (manageViewModel.uiState.value.page != ManageClothesPage.LIST) {
                    manageViewModel.returnToList()
                }
                voiceViewModel.navigateTo(AuraDestination.MANAGE_CLOTHES)
            }

            override fun onNavigateBack() {
                when (voiceViewModel.currentDestination.value) {
                    AuraDestination.REGISTER -> {
                        authViewModel.showLogin()
                        voiceViewModel.navigateTo(AuraDestination.AUTH)
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
                    AuraDestination.HOME, AuraDestination.AUTH -> {
                        ttsManager.speak("Anda sudah berada di halaman awal.")
                    }
                }
            }

            override fun onAddClothing() {
                if (authViewModel.uiState.value.currentUser == null) {
                    authViewModel.loginQuickAccess()
                }
                manageViewModel.openAddGuide(queueFollowUpSpeech = true)
                voiceViewModel.navigateTo(AuraDestination.MANAGE_CLOTHES)
            }

            override fun onOpenClothingCamera() {
                if (authViewModel.uiState.value.currentUser == null) {
                    authViewModel.loginQuickAccess()
                }
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
                latestRequestCameraAccess(false)
            }

            override fun onScanRfid() {
                if (authViewModel.uiState.value.currentUser == null) {
                    authViewModel.loginQuickAccess()
                }
                manageViewModel.openScanFind(queueFollowUpSpeech = true)
                voiceViewModel.navigateTo(AuraDestination.MANAGE_CLOTHES)
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

            override fun onListenRecommendations() {
                val activeDest = voiceViewModel.currentDestination.value
                val activePickState = pickOutfitViewModel.uiState.value
                val activeManageState = manageViewModel.uiState.value
                when {
                    activeDest == AuraDestination.AUTH || activeDest == AuraDestination.REGISTER -> {
                        authViewModel.speakAuthGuidance()
                    }
                    activeDest == AuraDestination.HISTORY -> {
                        historyViewModel.speakHistoryAndRotationSummary()
                    }
                    activeDest == AuraDestination.PICK_OUTFIT && activePickState.step == PickOutfitStep.RESULT -> {
                        pickOutfitViewModel.speakSelectedResult()
                    }
                    activeDest == AuraDestination.PICK_OUTFIT && activePickState.step == PickOutfitStep.RECOMMENDATIONS -> {
                        pickOutfitViewModel.speakCurrentRecommendationByVoice()
                    }
                    activeDest == AuraDestination.PICK_OUTFIT && activePickState.step == PickOutfitStep.CATEGORY_SELECTION -> {
                        pickOutfitViewModel.generateRecommendations()
                    }
                    activeDest == AuraDestination.MANAGE_CLOTHES -> when (activeManageState.page) {
                        ManageClothesPage.LIST -> manageViewModel.speakWardrobeOverview()
                        ManageClothesPage.DETAIL -> activeManageState.selectedItem?.let(manageViewModel::speakItemSummary)
                        ManageClothesPage.SCAN_FIND -> activeManageState.matchedScanItem?.let(manageViewModel::speakItemSummary)
                            ?: ttsManager.speak("Belum ada pakaian yang dipindai. Tempelkan tag RFID atau gunakan kamera AI.")
                        ManageClothesPage.CAMERA -> manageViewModel.speakCurrentScannerInstruction()
                        else -> ttsManager.speak(activeManageState.statusMessage ?: "Halaman kelola pakaian aktif.")
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

            override fun onSelectCurrentRecommendation() {
                if (pickOutfitViewModel.selectCurrentRecommendationByVoice()) {
                    voiceViewModel.navigateTo(AuraDestination.PICK_OUTFIT)
                }
            }

            override fun onSelectFirstRecommendation() {
                if (pickOutfitViewModel.selectFirstRecommendationByVoice()) {
                    voiceViewModel.navigateTo(AuraDestination.PICK_OUTFIT)
                }
            }
        }
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

    val openProfileDialog = {
        hapticManager.performButtonClick()
        showProfileDialog = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentDestination != AuraDestination.AUTH &&
                currentDestination != AuraDestination.REGISTER &&
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
                    AuraDestination.AUTH, AuraDestination.REGISTER -> AuthScreen(
                        uiState = authState,
                        isVoiceListening = voiceState.isListening,
                        partialTranscript = voiceState.lastTranscript,
                        onVoiceClick = onVoiceActionClick,
                        onSpeakGuidance = authViewModel::speakAuthGuidance,
                        onNameChange = authViewModel::onNameChange,
                        onEmailChange = authViewModel::onEmailChange,
                        onPasswordChange = authViewModel::onPasswordChange,
                        onConfirmPasswordChange = authViewModel::onConfirmPasswordChange,
                        onTogglePasswordVisibility = authViewModel::togglePasswordVisibility,
                        onToggleConfirmPasswordVisibility = authViewModel::toggleConfirmPasswordVisibility,
                        onSubmit = {
                            authViewModel.submitForm { user ->
                                activeUserIdFlow.value = user.id
                                voiceViewModel.navigateTo(AuraDestination.HOME)
                            }
                        },
                        onShowRegister = {
                            authViewModel.showRegister()
                            voiceViewModel.navigateTo(AuraDestination.REGISTER)
                        },
                        onShowLogin = {
                            authViewModel.showLogin()
                            voiceViewModel.navigateTo(AuraDestination.AUTH)
                        },
                        onContinueAsDemo = {
                            authViewModel.loginQuickAccess { user ->
                                activeUserIdFlow.value = user.id
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
                        onProfileClick = openProfileDialog,
                        partialTranscript = voiceState.lastTranscript,
                        modifier = screenModifier
                    )
                    AuraDestination.MANAGE_CLOTHES -> ManageClothesScreen(
                        uiState = manageState,
                        isVoiceListening = voiceState.isListening,
                        partialTranscript = voiceState.lastTranscript,
                        onVoiceClick = onVoiceActionClick,
                        onProfileClick = openProfileDialog,
                        onAddGuide = { manageViewModel.openAddGuide() },
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
                        onGalleryClick = openClothingGallery,
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
                        onProfileClick = openProfileDialog,
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

    if (showProfileDialog) {
        val currentUser = authState.currentUser
        AuraProfileDialog(
            user = currentUser,
            wardrobeCount = manageState.items.size,
            historyCount = historyState.historyEntries.size,
            onSpeakSummary = {
                val name = currentUser?.name ?: "Pengguna Lokal Aura"
                val email = currentUser?.email ?: "lokal@aura.id"
                ttsManager.speak(
                    "Akun aktif: $name, email $email. Tersimpan ${manageState.items.size} pakaian di lemari " +
                        "dan ${historyState.historyEntries.size} catatan riwayat dalam tiga puluh hari terakhir."
                )
            },
            onSeedStarterWardrobe = {
                manageViewModel.seedStarterWardrobe()
                showProfileDialog = false
            },
            onLogout = {
                showProfileDialog = false
                authViewModel.logout {
                    activeUserIdFlow.value = DemoClothingCatalog.USER_ID
                    voiceViewModel.navigateTo(AuraDestination.AUTH)
                }
            },
            onDismiss = { showProfileDialog = false }
        )
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
