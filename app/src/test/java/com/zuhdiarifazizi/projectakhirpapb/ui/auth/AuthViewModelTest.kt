package com.zuhdiarifazizi.projectakhirpapb.ui.auth

import com.zuhdiarifazizi.projectakhirpapb.data.repository.InMemoryAuthRepository
import com.zuhdiarifazizi.projectakhirpapb.ui.home.FakeHapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.ui.home.FakeTextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: InMemoryAuthRepository
    private lateinit var tts: FakeTextToSpeechManager
    private lateinit var haptics: FakeHapticFeedbackManager
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        repository = InMemoryAuthRepository()
        tts = FakeTextToSpeechManager()
        haptics = FakeHapticFeedbackManager()
        viewModel = AuthViewModel(
            authRepository = repository,
            ttsManager = tts,
            hapticManager = haptics
        )
    }

    @Test
    fun submitForm_withInvalidInputs_speaksErrorAndTriggersErrorHaptic() = runTest {
        viewModel.showRegister()
        viewModel.onNameChange("")
        viewModel.onEmailChange("tidak-valid")
        viewModel.onPasswordChange("123")
        viewModel.submitForm()

        val state = viewModel.uiState.value
        assertNotNull(state.nameError)
        assertNotNull(state.emailError)
        assertNotNull(state.passwordError)
        assertEquals(1, haptics.errorCount)
        assertTrue(tts.lastSpeech.orEmpty().contains("wajib diisi", ignoreCase = true))
    }

    @Test
    fun registerLogoutAndLoginRoundTrip_succeedsAndAnnouncesViaTts() = runTest {
        viewModel.showRegister()
        viewModel.onNameChange("Budi")
        viewModel.onEmailChange("budi@aura.id")
        viewModel.onPasswordChange("Rahasia123")
        viewModel.onConfirmPasswordChange("Rahasia123")

        var callbackUserCount = 0
        viewModel.submitForm { callbackUserCount++ }

        assertEquals(1, callbackUserCount)
        assertEquals("Budi", viewModel.uiState.value.currentUser?.name)
        assertTrue(tts.lastSpeech.orEmpty().contains("Budi"))

        viewModel.logout()
        assertNull(viewModel.uiState.value.currentUser)

        viewModel.showLogin()
        viewModel.onEmailChange("budi@aura.id")
        viewModel.onPasswordChange("Rahasia123")
        viewModel.submitForm { callbackUserCount++ }

        assertEquals(2, callbackUserCount)
        assertEquals("budi@aura.id", viewModel.uiState.value.currentUser?.email)
    }

    @Test
    fun loginQuickAccess_signsInLocalAccountWithoutTyping() = runTest {
        var entered = false
        viewModel.loginQuickAccess { entered = true }

        assertTrue(entered)
        assertNotNull(viewModel.uiState.value.currentUser)
        assertTrue(tts.lastSpeech.orEmpty().contains("Beranda Aura", ignoreCase = true))
    }
}
