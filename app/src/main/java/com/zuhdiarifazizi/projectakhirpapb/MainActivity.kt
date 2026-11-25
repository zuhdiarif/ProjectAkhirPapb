package com.zuhdiarifazizi.projectakhirpapb

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.zuhdiarifazizi.projectakhirpapb.data.classifier.DefaultClothingClassifier
import com.zuhdiarifazizi.projectakhirpapb.data.feedback.AndroidHapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.data.feedback.AndroidTextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.data.repository.DefaultActivityCategoryRepository
import com.zuhdiarifazizi.projectakhirpapb.data.repository.InMemoryAuthRepository
import com.zuhdiarifazizi.projectakhirpapb.data.repository.InMemoryClothesRepository
import com.zuhdiarifazizi.projectakhirpapb.data.repository.InMemoryOutfitRepository
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

    val authRepository = remember { InMemoryAuthRepository() }
    val clothesRepository = remember { InMemoryClothesRepository() }
    val outfitRepository = remember { InMemoryOutfitRepository() }
    val classifier = remember { DefaultClothingClassifier() }
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

    val authViewModel: AuthViewModel = viewModel { AuthViewModel(authRepository) }
    val manageViewModel: ManageClothesViewModel = viewModel {
        ManageClothesViewModel(clothesRepository, classifier)
    }
    val pickOutfitViewModel: PickOutfitViewModel = viewModel {
        PickOutfitViewModel(clothesRepository, outfitRepository)
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

    var currentDestination by remember { mutableStateOf(AuraDestination.AUTH) }

    val voiceNavigationCallback = remember {
        object : VoiceNavigationCallback {
            override fun onSelectOccasion(occasion: Occasion) {
                homeViewModel.selectCategoryByOccasion(occasion)
                currentDestination = AuraDestination.PICK_OUTFIT
            }

            override fun onOpenHistory() {
                currentDestination = AuraDestination.HISTORY
            }

            override fun onOpenWardrobe() {
                currentDestination = AuraDestination.MANAGE_CLOTHES
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentDestination != AuraDestination.AUTH && currentDestination != AuraDestination.HOME) {
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
            if (currentDestination != AuraDestination.AUTH) {
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
            AuraDestination.AUTH -> AuthScreen(
                onContinueDevStub = { currentDestination = AuraDestination.HOME },
                modifier = screenModifier
            )
            AuraDestination.HOME -> HomeScreen(
                uiState = homeState,
                onCategoryClick = { category ->
                    homeViewModel.selectCategory(category) {
                        currentDestination = AuraDestination.PICK_OUTFIT
                    }
                },
                onVoiceButtonClick = {
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
                },
                onRepeatGuidance = {
                    homeViewModel.repeatGuidance()
                },
                onDismissBanner = {
                    homeViewModel.dismissFeedbackBanner()
                },
                modifier = screenModifier
            )
            AuraDestination.MANAGE_CLOTHES -> ManageClothesScreen(
                uiState = manageState,
                modifier = screenModifier
            )
            AuraDestination.PICK_OUTFIT -> PickOutfitScreen(
                uiState = pickState,
                modifier = screenModifier
            )
            AuraDestination.HISTORY -> HistoryScreen(
                uiState = historyState,
                modifier = screenModifier
            )
        }
    }
}
