package com.zuhdiarifazizi.projectakhirpapb.ui.auth

import com.zuhdiarifazizi.projectakhirpapb.domain.model.User

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val authenticatedUser: User? = null
)
