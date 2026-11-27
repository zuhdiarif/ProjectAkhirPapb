package com.zuhdiarifazizi.projectakhirpapb.data.repository

import com.zuhdiarifazizi.projectakhirpapb.data.local.AppMetadataEntity
import com.zuhdiarifazizi.projectakhirpapb.data.local.AuraDatabase
import com.zuhdiarifazizi.projectakhirpapb.data.local.UserEntity
import com.zuhdiarifazizi.projectakhirpapb.data.local.toDomain
import com.zuhdiarifazizi.projectakhirpapb.domain.model.User
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.AuthRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.security.PasswordHasher
import com.zuhdiarifazizi.projectakhirpapb.domain.security.SecurityInputValidator
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RoomAuthRepository(private val database: AuraDatabase) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser.asStateFlow()

    override suspend fun restoreSession(): User? {
        val activeUserId = database.appMetadataDao().getValue(ACTIVE_USER_ID_KEY)?.trim()
        if (activeUserId.isNullOrBlank()) {
            _currentUser.value = null
            return null
        }
        val restored = if (activeUserId == DemoClothingCatalog.USER_ID) {
            QUICK_LOCAL_USER
        } else {
            database.userDao().getById(activeUserId)?.toDomain()
        }
        if (restored == null) {
            database.appMetadataDao().deleteByKey(ACTIVE_USER_ID_KEY)
        }
        _currentUser.value = restored
        return restored
    }

    override suspend fun login(email: String, password: String): Result<User> {
        val normalizedEmail = email.trim().lowercase()
        val entity = database.userDao().getByEmail(normalizedEmail)
            ?: return Result.failure(
                IllegalArgumentException(
                    "Email atau password belum sesuai. Coba periksa kembali atau gunakan Masuk Cepat Tanpa Ketik."
                )
            )
        if (!PasswordHasher.verify(password, entity.passwordSalt, entity.passwordHash)) {
            return Result.failure(
                IllegalArgumentException(
                    "Email atau password belum sesuai. Coba periksa kembali atau gunakan Masuk Cepat Tanpa Ketik."
                )
            )
        }
        val user = entity.toDomain()
        database.appMetadataDao().upsert(AppMetadataEntity(ACTIVE_USER_ID_KEY, user.id))
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> {
        val cleanName = SecurityInputValidator.sanitizeText(name, maxLength = 60)
        val normalizedEmail = email.trim().lowercase()
        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("Nama pengguna wajib diisi."))
        }
        val existing = database.userDao().getByEmail(normalizedEmail)
        if (existing != null) {
            return Result.failure(
                IllegalStateException("Email ini sudah terdaftar. Silakan pindah ke halaman Login untuk masuk.")
            )
        }
        val salted = PasswordHasher.hashPassword(password)
        val userId = "user_${UUID.randomUUID()}"
        val entity = UserEntity(
            id = userId,
            name = cleanName,
            email = normalizedEmail,
            passwordHash = salted.hashHex,
            passwordSalt = salted.saltHex,
            createdAtEpochMillis = System.currentTimeMillis()
        )
        database.userDao().insert(entity)
        database.appMetadataDao().upsert(AppMetadataEntity(ACTIVE_USER_ID_KEY, userId))
        val user = entity.toDomain()
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun loginAsQuickLocal(): Result<User> {
        database.appMetadataDao().upsert(AppMetadataEntity(ACTIVE_USER_ID_KEY, DemoClothingCatalog.USER_ID))
        _currentUser.value = QUICK_LOCAL_USER
        return Result.success(QUICK_LOCAL_USER)
    }

    override suspend fun logout() {
        database.appMetadataDao().deleteByKey(ACTIVE_USER_ID_KEY)
        _currentUser.value = null
    }

    companion object {
        const val ACTIVE_USER_ID_KEY = "active_user_id"
        val QUICK_LOCAL_USER = InMemoryAuthRepository.QUICK_LOCAL_USER
    }
}
