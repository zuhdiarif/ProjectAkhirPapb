package com.zuhdiarifazizi.projectakhirpapb.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ClothingEntity::class, AppMetadataEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun clothingDao(): ClothingDao
    abstract fun appMetadataDao(): AppMetadataDao

    companion object {
        @Volatile private var instance: AuraDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE clothing_items ADD COLUMN material TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE clothing_items ADD COLUMN rfidTagUid TEXT")
                db.execSQL("ALTER TABLE clothing_items ADD COLUMN lastWornEpochMillis INTEGER")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_clothing_items_rfidTagUid " +
                        "ON clothing_items(rfidTagUid)"
                )
            }
        }

        fun getInstance(context: Context): AuraDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AuraDatabase::class.java,
                "aura_local.db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
                .also { instance = it }
        }
    }
}
