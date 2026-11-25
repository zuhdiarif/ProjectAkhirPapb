package com.zuhdiarifazizi.projectakhirpapb.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ClothingEntity::class, AppMetadataEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun clothingDao(): ClothingDao
    abstract fun appMetadataDao(): AppMetadataDao

    companion object {
        @Volatile private var instance: AuraDatabase? = null

        fun getInstance(context: Context): AuraDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AuraDatabase::class.java,
                "aura_local.db"
            ).build().also { instance = it }
        }
    }
}
