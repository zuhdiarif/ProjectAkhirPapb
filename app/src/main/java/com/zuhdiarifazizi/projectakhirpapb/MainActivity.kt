package com.zuhdiarifazizi.projectakhirpapb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zuhdiarifazizi.projectakhirpapb.data.classifier.DefaultClothingClassifier
import com.zuhdiarifazizi.projectakhirpapb.data.repository.InMemoryAuthRepository
import com.zuhdiarifazizi.projectakhirpapb.data.repository.InMemoryClothesRepository
import com.zuhdiarifazizi.projectakhirpapb.data.repository.InMemoryOutfitRepository
import com.zuhdiarifazizi.projectakhirpapb.ui.auth.AuthScreen
import com.zuhdiarifazizi.projectakhirpapb.ui.auth.AuthViewModel
import com.zuhdiarifazizi.projectakhirpapb.ui.history.HistoryScreen
import com.zuhdiarifazizi.projectakhirpapb.ui.history.HistoryViewModel
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
    val authRepository = remember { InMemoryAuthRepository() }
    val clothesRepository = remember { InMemoryClothesRepository() }
    val outfitRepository = remember { InMemoryOutfitRepository() }
    val classifier = remember { DefaultClothingClassifier() }

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

    val manageState by manageViewModel.uiState.collectAsState()
    val pickState by pickOutfitViewModel.uiState.collectAsState()
    val historyState by historyViewModel.uiState.collectAsState()

    var currentDestination by remember { mutableStateOf(AuraDestination.AUTH) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentDestination != AuraDestination.AUTH) {
                TopAppBar(
                    title = { Text(currentDestination.label) },
                    navigationIcon = {
                        if (currentDestination != AuraDestination.HOME) {
                            TextButton(onClick = { currentDestination = AuraDestination.HOME }) {
                                Text("Back")
                            }
                        }
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
            AuraDestination.HOME -> HomePlaceholderMenu(
                onNavigate = { currentDestination = it },
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

@Composable
private fun HomePlaceholderMenu(
    onNavigate: (AuraDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "AURA — Navigation Shell",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onNavigate(AuraDestination.PICK_OUTFIT) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pick Outfit (Fungsi 1)")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { onNavigate(AuraDestination.MANAGE_CLOTHES) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Manage Clothes (Fungsi 3)")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { onNavigate(AuraDestination.HISTORY) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("History & Rotation (Fungsi 2)")
        }
    }
}
