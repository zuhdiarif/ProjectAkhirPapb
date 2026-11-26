package com.zuhdiarifazizi.projectakhirpapb.ui.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Handles form validation only. Authentication is intentionally not simulated. */
class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update {
        it.copy(name = value, nameError = null, statusMessage = null)
    }

    fun onEmailChange(value: String) = _uiState.update {
        it.copy(email = value, emailError = null, statusMessage = null)
    }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(password = value, passwordError = null, confirmPasswordError = null, statusMessage = null)
    }

    fun onConfirmPasswordChange(value: String) = _uiState.update {
        it.copy(confirmPassword = value, confirmPasswordError = null, statusMessage = null)
    }

    fun togglePasswordVisibility() = _uiState.update {
        it.copy(isPasswordVisible = !it.isPasswordVisible)
    }

    fun toggleConfirmPasswordVisibility() = _uiState.update {
        it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible)
    }

    fun showRegister() = switchForm(isRegister = true)

    fun showLogin() = switchForm(isRegister = false)

    private fun switchForm(isRegister: Boolean) {
        _uiState.update {
            it.copy(
                isRegister = isRegister,
                nameError = null,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null,
                statusMessage = null
            )
        }
    }

    fun submitForm() {
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
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError,
                    statusMessage = null
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                nameError = null,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null,
                statusMessage = if (it.isRegister) {
                    "Form valid. Layanan akun belum terhubung; akun belum dibuat."
                } else {
                    "Form valid. Layanan autentikasi belum terhubung; Anda belum login."
                }
            )
        }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 8
        val EMAIL_PATTERN = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
