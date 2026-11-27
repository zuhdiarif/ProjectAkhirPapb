package com.zuhdiarifazizi.projectakhirpapb.domain.repository

import java.io.File

interface PhotoStorageRepository {
    suspend fun importPhoto(sourceUri: String): String
    fun createCameraOutputFile(): File
    suspend fun deleteStoredPhoto(photoUri: String?)
}
