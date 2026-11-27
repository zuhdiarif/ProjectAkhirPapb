package com.zuhdiarifazizi.projectakhirpapb.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zuhdiarifazizi.projectakhirpapb.data.repository.InMemoryAuthRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.HapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.TextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.domain.model.User
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = InMemoryAuthRepository(),
    private val ttsManager: TextToSpeechManager? = null,
    private val hapticManager: HapticFeedbackManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { authRepository.restoreSession() }
            _uiState.update { it.copy(isSessionLoading = false) }
        }
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update {
        it.copy(name = value, nameError = null, errorMessage = null, statusMessage = null)
    }

    fun onEmailChange(value: String) = _uiState.update {
        it.copy(email = value, emailError = null, errorMessage = null, statusMessage = null)
    }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(
            password = value,
            passwordError = null,
            confirmPasswordError = null,
            errorMessage = null,
            statusMessage = null
        )
    }

    fun onConfirmPasswordChange(value: String) = _uiState.update {
        it.copy(confirmPassword = value, confirmPasswordError = null, errorMessage = null, statusMessage = null)
    }

    fun togglePasswordVisibility() = _uiState.update {
        it.copy(isPasswordVisible = !it.isPasswordVisible)
    }

    fun toggleConfirmPasswordVisibility() = _uiState.update {
        it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible)
    }

    fun showRegister() = switchForm(isRegister = true)

    fun showLogin() = switchForm(isRegister = false)

    fun speakAuthGuidance() {
        hapticManager?.performButtonClick()
        val isReg = _uiState.value.isRegister
        val guidance = if (isReg) {
            "Anda berada di halaman Buat Akun Lokal Aura. Isi nama, email, dan password minimal delapan karakter, " +
                "lalu tekan Register. Anda juga bisa kembali ke halaman Login atau menggunakan tombol Masuk Cepat Tanpa Ketik."
        } else {
            "Selamat datang di Aura, asisten outfit untuk tunanetra. Anda berada di halaman Masuk. " +
                "Untuk langsung menggunakan aplikasi tanpa mengetik email dan password, tekan tombol Masuk Cepat Tanpa Ketik, " +
                "atau gunakan tombol perintah suara dan ucapkan Masuk cepat."
        }
        ttsManager?.speak(guidance)
    }

    private fun switchForm(isRegister: Boolean) {
        _uiState.update {
            it.copy(
                isRegister = isRegister,
                nameError = null,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null,
                errorMessage = null,
                statusMessage = null
            )
        }
        hapticManager?.performButtonClick()
        ttsManager?.speak(
            if (isRegister) "Halaman buat akun lokal dibuka." else "Halaman login dibuka."
        )
    }

    fun submitForm(onAuthenticated: (User) -> Unit = {}) {
        val state = _uiState.value
        val emailIsValid = EMAIL_PATTERN.matches(state.email.trim())
        val nameError = if (state.isRegister && state.name.isBlank()) "Nama wajib diisi." else null
        val emailError = if (state.email.isBlank()) {
            "Email wajib diisi."
        } else if (!emailIsValid) {
            "Masukkan alamat email yang valid."
        } else null
        val passwordError = when {
            state.password.isBlank() -> "Password wajib diisi."
            state.isRegister && state.password.length < MIN_PASSWORD_LENGTH ->
                "Password minimal $MIN_PASSWORD_LENGTH karakter."
            else -> null
        }
        val confirmPasswordError = if (state.isRegister && state.confirmPassword.isBlank()) {
            "Konfirmasi password wajib diisi."
        } else if (state.isRegister && state.confirmPassword != state.password) {
            "Konfirmasi password belum sama."
        } else null

        if (nameError != null || emailError != null || passwordError != null || confirmPasswordError != null) {
            val combinedError = listOfNotNull(nameError, emailError, passwordError, confirmPasswordError)
                .joinToString(" ")
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError,
                    errorMessage = combinedError,
                    statusMessage = null
                )
            }
            hapticManager?.performError()
            ttsManager?.speak(combinedError)
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null, statusMessage = null) }
        viewModelScope.launch {
            val result = if (state.isRegister) {
                authRepository.register(state.name, state.email, state.password)
            } else {
                authRepository.login(state.email, state.password)
            }
            result.fold(
                onSuccess = { user ->
                    val successText = if (state.isRegister) {
                        "Akun ${user.name} berhasil dibuat dengan koleksi pakaian siap pakai."
                    } else {
                        "Berhasil masuk sebagai ${user.name}."
                    }
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            currentUser = user,
                            password = "",
                            confirmPassword = "",
                            errorMessage = null,
                            statusMessage = successText
                        )
                    }
                    hapticManager?.performSuccess()
                    ttsManager?.speak(successText)
                    onAuthenticated(user)
                },
                onFailure = { err ->
                    val errText = err.message ?: "Autentikasi gagal. Silakan periksa kembali data Anda."
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = errText,
                            statusMessage = null
                        )
                    }
                    hapticManager?.performError()
                    ttsManager?.speak(errText)
                }
            )
        }
    }

    fun loginQuickAccess(onAuthenticated: (User) -> Unit = {}) {
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            authRepository.loginAsQuickLocal().fold(
                onSuccess = { user ->
                    val msg = "Masuk cepat aktif sebagai ${user.name}."
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            currentUser = user,
                            errorMessage = null,
                            statusMessage = msg
                        )
                    }
                    hapticManager?.performSuccess()
                    ttsManager?.speak("$msg Membuka Beranda Aura.")
                    onAuthenticated(user)
                },
                onFailure = {
                    val err = "Gagal masuk ke akun lokal."
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = err) }
                    hapticManager?.performError()
                    ttsManager?.speak(err)
                }
            )
        }
    }

    fun logout(onLoggedOut: () -> Unit = {}) {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update {
                AuthUiState(
                    isSessionLoading = false,
                    currentUser = null,
                    statusMessage = "Anda telah keluar dari akun. Anda dapat masuk kembali atau gunakan Masuk Cepat Tanpa Ketik."
                )
            }
            hapticManager?.performButtonClick()
            ttsManager?.speak("Anda telah keluar dari akun.")
            onLoggedOut()
        }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 8
        val EMAIL_PATTERN = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
