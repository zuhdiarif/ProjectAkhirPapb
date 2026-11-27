package com.zuhdiarifazizi.projectakhirpapb.data.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.User
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.AuthRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.security.PasswordHasher
import com.zuhdiarifazizi.projectakhirpapb.domain.security.SecurityInputValidator
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryAuthRepository : AuthRepository {

    private data class StoredCredential(
        val user: User,
        val saltHex: String,
        val hashHex: String
    )

    private val usersByEmail = mutableMapOf<String, StoredCredential>()
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser.asStateFlow()

    override suspend fun restoreSession(): User? = _currentUser.value

    override suspend fun login(email: String, password: String): Result<User> {
        val normalizedEmail = email.trim().lowercase()
        val stored = usersByEmail[normalizedEmail]
            ?: return Result.failure(IllegalArgumentException("Email atau password salah. Silakan coba lagi."))
        if (!PasswordHasher.verify(password, stored.saltHex, stored.hashHex)) {
            return Result.failure(IllegalArgumentException("Email atau password salah. Silakan coba lagi."))
        }
        _currentUser.value = stored.user
        return Result.success(stored.user)
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> {
        val cleanName = SecurityInputValidator.sanitizeText(name, maxLength = 60)
        val normalizedEmail = email.trim().lowercase()
        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("Nama pengguna wajib diisi."))
        }
        if (usersByEmail.containsKey(normalizedEmail)) {
            return Result.failure(IllegalStateException("Email sudah terdaftar. Silakan masuk di halaman Login."))
        }
        val salted = PasswordHasher.hashPassword(password)
        val user = User(
            id = "user_${UUID.randomUUID()}",
            name = cleanName,
            email = normalizedEmail
        )
        usersByEmail[normalizedEmail] = StoredCredential(user, salted.saltHex, salted.hashHex)
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun loginAsQuickLocal(): Result<User> {
        _currentUser.value = QUICK_LOCAL_USER
        return Result.success(QUICK_LOCAL_USER)
    }

    override suspend fun logout() {
        _currentUser.value = null
    }

    companion object {
        val QUICK_LOCAL_USER = User(
            id = DemoClothingCatalog.USER_ID,
            name = "Pengguna Lokal Aura",
            email = "lokal@aura.id"
        )
    }
}
