package com.zuhdiarifazizi.projectakhirpapb.domain.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<User>
    suspend fun logout()
}
