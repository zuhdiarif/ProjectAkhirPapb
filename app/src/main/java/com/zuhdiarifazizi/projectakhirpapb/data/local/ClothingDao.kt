package com.zuhdiarifazizi.projectakhirpapb.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingDao {
    @Query("SELECT * FROM clothing_items WHERE userId = :userId ORDER BY createdAtEpochMillis DESC, name COLLATE NOCASE")
    fun observeByUser(userId: String): Flow<List<ClothingEntity>>

    @Query("SELECT * FROM clothing_items WHERE userId = :userId ORDER BY createdAtEpochMillis DESC, name COLLATE NOCASE")
    suspend fun getByUser(userId: String): List<ClothingEntity>

    @Query("SELECT * FROM clothing_items WHERE id = :itemId LIMIT 1")
    suspend fun getById(itemId: String): ClothingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ClothingEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<ClothingEntity>)

    @Query("DELETE FROM clothing_items WHERE id = :itemId")
    suspend fun deleteById(itemId: String)
}

@Dao
interface AppMetadataDao {
    @Query("SELECT value FROM app_metadata WHERE `key` = :key LIMIT 1")
    suspend fun getValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: AppMetadataEntity)
}
