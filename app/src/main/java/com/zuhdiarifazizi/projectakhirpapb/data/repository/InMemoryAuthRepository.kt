package com.zuhdiarifazizi.projectakhirpapb.data.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.User
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryAuthRepository : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser.asStateFlow()

    override suspend fun login(email: String, password: String): Result<User> {
        return Result.failure(NotImplementedError("Login not implemented"))
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> {
        return Result.failure(NotImplementedError("Registration not implemented"))
    }

    override suspend fun logout() {
        _currentUser.value = null
    }
}
