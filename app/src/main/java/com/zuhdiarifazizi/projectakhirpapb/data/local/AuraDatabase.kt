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
        UserEntity::class,
        OutfitHistoryEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun clothingDao(): ClothingDao
    abstract fun appMetadataDao(): AppMetadataDao
    abstract fun userDao(): UserDao
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

        fun getInstance(context: Context): AuraDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AuraDatabase::class.java,
                "aura_local.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                .also { instance = it }
        }
    }
}
