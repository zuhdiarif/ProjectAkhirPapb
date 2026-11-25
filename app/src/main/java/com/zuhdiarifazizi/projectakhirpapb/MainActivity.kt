package com.zuhdiarifazizi.projectakhirpapb

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zuhdiarifazizi.projectakhirpapb.data.feedback.AndroidHapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.data.feedback.AndroidTextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.data.local.AuraDatabase
import com.zuhdiarifazizi.projectakhirpapb.data.repository.DefaultActivityCategoryRepository
import com.zuhdiarifazizi.projectakhirpapb.data.repository.InMemoryOutfitRepository
import com.zuhdiarifazizi.projectakhirpapb.data.repository.RoomClothesRepository
import com.zuhdiarifazizi.projectakhirpapb.data.storage.ClothingPhotoStorage
import com.zuhdiarifazizi.projectakhirpapb.data.voice.AndroidSpeechRecognizerManager
import com.zuhdiarifazizi.projectakhirpapb.data.voice.IndonesianIntentRecognizer
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.voice.VoiceInteractionManager
import com.zuhdiarifazizi.projectakhirpapb.domain.voice.VoiceNavigationCallback
import com.zuhdiarifazizi.projectakhirpapb.ui.auth.AuthScreen
import com.zuhdiarifazizi.projectakhirpapb.ui.auth.AuthViewModel
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraBottomNavigation
import com.zuhdiarifazizi.projectakhirpapb.ui.history.HistoryScreen
import com.zuhdiarifazizi.projectakhirpapb.ui.history.HistoryViewModel
import com.zuhdiarifazizi.projectakhirpapb.ui.home.HomeScreen
import com.zuhdiarifazizi.projectakhirpapb.ui.home.HomeViewModel
import com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes.ManageClothesScreen
import com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes.ManageClothesPage
import com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes.ManageClothesViewModel
import com.zuhdiarifazizi.projectakhirpapb.ui.navigation.AuraDestination
import com.zuhdiarifazizi.projectakhirpapb.ui.pickoutfit.PickOutfitScreen
import com.zuhdiarifazizi.projectakhirpapb.ui.pickoutfit.PickOutfitViewModel
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.ProjectAkhirPapbTheme
import com.zuhdiarifazizi.projectakhirpapb.ui.voice.VoiceViewModel

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraAppShell() {
    val context = LocalContext.current

    val clothesDatabase = remember { AuraDatabase.getInstance(context.applicationContext) }
    val photoStorage = remember { ClothingPhotoStorage(context.applicationContext) }
    val clothesRepository = remember(clothesDatabase) { RoomClothesRepository(clothesDatabase) }
    val outfitRepository = remember { InMemoryOutfitRepository() }
    val categoryRepository = remember { DefaultActivityCategoryRepository() }

    val ttsManager = remember { AndroidTextToSpeechManager(context) }
    val hapticManager = remember { AndroidHapticFeedbackManager(context) }
    val speechRecognizerManager = remember { AndroidSpeechRecognizerManager(context) }
    val intentRecognizer = remember { IndonesianIntentRecognizer() }
    val voiceInteractionManager = remember {
        VoiceInteractionManager(
            speechRecognizerManager = speechRecognizerManager,
            intentRecognizer = intentRecognizer,
            ttsManager = ttsManager,
            hapticManager = hapticManager
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceInteractionManager.cleanup()
        }
    }

    val authViewModel: AuthViewModel = viewModel { AuthViewModel() }
    val manageViewModel: ManageClothesViewModel = viewModel {
        ManageClothesViewModel(clothesRepository, photoStorage, ttsManager, hapticManager)
    }
    val pickOutfitViewModel: PickOutfitViewModel = viewModel {
        PickOutfitViewModel(
            clothesRepository = clothesRepository,
            ttsManager = ttsManager,
            hapticManager = hapticManager
        )
    }
    val historyViewModel: HistoryViewModel = viewModel {
        HistoryViewModel(clothesRepository, outfitRepository)
    }
    val voiceViewModel: VoiceViewModel = viewModel { VoiceViewModel() }
    val homeViewModel: HomeViewModel = viewModel {
        HomeViewModel(
            activityCategoryRepository = categoryRepository,
            ttsManager = ttsManager,
            hapticManager = hapticManager,
            voiceInteractionManager = voiceInteractionManager
        )
    }

    val manageState by manageViewModel.uiState.collectAsState()
    val pickState by pickOutfitViewModel.uiState.collectAsState()
    val historyState by historyViewModel.uiState.collectAsState()
    val homeState by homeViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()

    var currentDestination by remember { mutableStateOf(AuraDestination.AUTH) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) manageViewModel.openCamera() else manageViewModel.onCameraPermissionDenied()
    }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> manageViewModel.selectGalleryPhoto(uri?.toString()) }

    val requestCameraAccess = {
        val hasCamera = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        when {
            !hasCamera -> manageViewModel.onCameraUnavailable()
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ->
                manageViewModel.openCamera()
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    val openClothingGallery = {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    BackHandler(
        enabled = currentDestination == AuraDestination.REGISTER ||
            (currentDestination == AuraDestination.PICK_OUTFIT && pickState.step != com.zuhdiarifazizi.projectakhirpapb.ui.pickoutfit.PickOutfitStep.CATEGORY_SELECTION) ||
            (currentDestination == AuraDestination.MANAGE_CLOTHES && manageState.page != ManageClothesPage.LIST)
    ) {
        when (currentDestination) {
            AuraDestination.REGISTER -> {
                authViewModel.showLogin()
                currentDestination = AuraDestination.AUTH
            }
            AuraDestination.MANAGE_CLOTHES -> manageViewModel.navigateBack()
            AuraDestination.PICK_OUTFIT -> if (!pickOutfitViewModel.goToPreviousStep()) {
                currentDestination = AuraDestination.HOME
            }
            else -> Unit
        }
    }

    val voiceNavigationCallback = remember {
        object : VoiceNavigationCallback {
            override fun onSelectOccasion(occasion: Occasion) {
                homeViewModel.selectCategoryByOccasion(occasion)
                pickOutfitViewModel.beginSelection(occasion)
                currentDestination = AuraDestination.PICK_OUTFIT
            }

            override fun onOpenHistory() {
                currentDestination = AuraDestination.HISTORY
            }

            override fun onOpenWardrobe() {
                currentDestination = AuraDestination.MANAGE_CLOTHES
            }

            override fun onAddClothing() {
                manageViewModel.openAddForm()
                currentDestination = AuraDestination.MANAGE_CLOTHES
            }

            override fun onOpenClothingCamera() {
                if (manageState.page != ManageClothesPage.FORM) manageViewModel.openAddForm()
                currentDestination = AuraDestination.MANAGE_CLOTHES
                requestCameraAccess()
            }

            override fun onListenRecommendations() {
                pickOutfitViewModel.speakCurrentRecommendationByVoice()
            }

            override fun onSelectFirstRecommendation() {
                if (pickOutfitViewModel.selectFirstRecommendationByVoice()) {
                    currentDestination = AuraDestination.PICK_OUTFIT
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentDestination != AuraDestination.AUTH &&
                currentDestination != AuraDestination.REGISTER &&
                currentDestination != AuraDestination.HOME &&
                currentDestination != AuraDestination.PICK_OUTFIT &&
                currentDestination != AuraDestination.MANAGE_CLOTHES
            ) {
                TopAppBar(
                    title = { Text(currentDestination.label) },
                    navigationIcon = {
                        TextButton(onClick = { currentDestination = AuraDestination.HOME }) {
                            Text("Back")
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (currentDestination != AuraDestination.AUTH &&
                currentDestination != AuraDestination.REGISTER &&
                !(currentDestination == AuraDestination.MANAGE_CLOTHES && manageState.page == ManageClothesPage.CAMERA)
            ) {
                AuraBottomNavigation(
                    currentDestination = currentDestination,
                    onNavigate = { destination ->
                        currentDestination = destination
                    }
                )
            }
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)
        when (currentDestination) {
            AuraDestination.AUTH, AuraDestination.REGISTER -> AuthScreen(
                uiState = authState,
                onNameChange = authViewModel::onNameChange,
                onEmailChange = authViewModel::onEmailChange,
                onPasswordChange = authViewModel::onPasswordChange,
                onConfirmPasswordChange = authViewModel::onConfirmPasswordChange,
                onTogglePasswordVisibility = authViewModel::togglePasswordVisibility,
                onToggleConfirmPasswordVisibility = authViewModel::toggleConfirmPasswordVisibility,
                onSubmit = authViewModel::submitForm,
                onShowRegister = {
                    authViewModel.showRegister()
                    currentDestination = AuraDestination.REGISTER
                },
                onShowLogin = {
                    authViewModel.showLogin()
                    currentDestination = AuraDestination.AUTH
                },
                onContinueAsDemo = { currentDestination = AuraDestination.HOME },
                modifier = screenModifier
            )
            AuraDestination.HOME -> HomeScreen(
                uiState = homeState,
                onCategoryClick = { category ->
                    homeViewModel.selectCategory(category) {
                        pickOutfitViewModel.beginSelection(category.occasion)
                        currentDestination = AuraDestination.PICK_OUTFIT
                    }
                },
                onVoiceButtonClick = onVoiceActionClick,
                onRepeatGuidance = {
                    homeViewModel.repeatGuidance()
                },
                onDismissBanner = {
                    homeViewModel.dismissFeedbackBanner()
                },
                onProfileClick = {
                    authViewModel.showLogin()
                    currentDestination = AuraDestination.AUTH
                },
                modifier = screenModifier
            )
            AuraDestination.MANAGE_CLOTHES -> ManageClothesScreen(
                uiState = manageState,
                isVoiceListening = homeState.isVoiceListening,
                onVoiceClick = onVoiceActionClick,
                onAdd = manageViewModel::openAddForm,
                onItemDetails = manageViewModel::openDetails,
                onEdit = manageViewModel::openEditForm,
                onSearchChange = manageViewModel::onSearchChange,
                onCategoryFilter = manageViewModel::setCategoryFilter,
                onRequestDelete = manageViewModel::requestDeleteConfirmation,
                onCancelDelete = manageViewModel::cancelDelete,
                onConfirmDelete = manageViewModel::confirmDelete,
                onFormBack = manageViewModel::cancelForm,
                onNameChange = manageViewModel::onNameChange,
                onCategoryChange = manageViewModel::onCategoryChange,
                onPatternChange = manageViewModel::onPatternChange,
                onOccasionChange = manageViewModel::onOccasionChange,
                onColorChange = manageViewModel::onColorChange,
                onCameraClick = requestCameraAccess,
                onGalleryClick = openClothingGallery,
                onRemovePhoto = manageViewModel::removeSelectedPhoto,
                onSave = manageViewModel::saveForm,
                onBackToList = manageViewModel::returnToList,
                onEditSelected = { manageState.selectedItem?.let(manageViewModel::openEditForm) },
                onCameraReady = manageViewModel::onCameraReady,
                onCameraUnavailable = manageViewModel::onCameraUnavailable,
                onCameraCaptureFailed = manageViewModel::onCameraCaptureFailed,
                onCameraPhotoCaptured = manageViewModel::onCameraPhotoCaptured,
                onUseCapturedPhoto = manageViewModel::useCapturedPhoto,
                onRetakePhoto = manageViewModel::retakePhoto,
                onCancelCamera = manageViewModel::cancelCamera,
                createCameraFile = photoStorage::createCameraOutputFile,
                modifier = screenModifier
            )
            AuraDestination.PICK_OUTFIT -> PickOutfitScreen(
                uiState = pickState,
                isVoiceListening = homeState.isVoiceListening,
                onBackToHome = { currentDestination = AuraDestination.HOME },
                onBackStep = { pickOutfitViewModel.goToPreviousStep() },
                onVoiceClick = onVoiceActionClick,
                onCategoryChanged = pickOutfitViewModel::setCategorySelected,
                onContinue = { pickOutfitViewModel.generateRecommendations() },
                onNextRecommendation = pickOutfitViewModel::nextRecommendation,
                onSpeakRecommendation = pickOutfitViewModel::speakRecommendation,
                onViewDetails = pickOutfitViewModel::viewRecommendationDetails,
                onChooseRecommendation = { pickOutfitViewModel.chooseRecommendation(it) },
                onSpeakResult = pickOutfitViewModel::speakSelectedResult,
                modifier = screenModifier
            )
            AuraDestination.HISTORY -> HistoryScreen(
                uiState = historyState,
                modifier = screenModifier
            )
        }
    }
}
