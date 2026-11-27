package com.zuhdiarifazizi.projectakhirpapb.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ClothingEntity::class,
        AppMetadataEntity::class,
        OutfitHistoryEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun clothingDao(): ClothingDao
    abstract fun appMetadataDao(): AppMetadataDao
    abstract fun outfitHistoryDao(): OutfitHistoryDao

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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `users` (" +
                        "`id` TEXT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`email` TEXT NOT NULL, " +
                        "`passwordHash` TEXT NOT NULL, " +
                        "`passwordSalt` TEXT NOT NULL, " +
                        "`createdAtEpochMillis` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_users_email` " +
                        "ON `users` (`email`)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `outfit_history` (" +
                        "`id` TEXT NOT NULL, " +
                        "`userId` TEXT NOT NULL, " +
                        "`outfitId` TEXT NOT NULL, " +
                        "`occasion` TEXT NOT NULL, " +
                        "`score` REAL NOT NULL, " +
                        "`wornAtEpochMillis` INTEGER NOT NULL, " +
                        "`itemsSnapshot` TEXT NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_outfit_history_userId_wornAtEpochMillis` " +
                        "ON `outfit_history` (`userId`, `wornAtEpochMillis`)"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `users`")
                db.execSQL("DELETE FROM `app_metadata` WHERE `key` = 'active_user_id'")
            }
        }

        fun getInstance(context: Context): AuraDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AuraDatabase::class.java,
                "aura_local.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                .build()
                .also { instance = it }
        }
    }
}
