package com.zuhdiarifazizi.projectakhirpapb.ui.auth

import com.zuhdiarifazizi.projectakhirpapb.domain.model.User

data class AuthUiState(
    val isRegister: Boolean = false,
    val isSessionLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val currentUser: User? = null,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val errorMessage: String? = null,
    val statusMessage: String? = null
)
