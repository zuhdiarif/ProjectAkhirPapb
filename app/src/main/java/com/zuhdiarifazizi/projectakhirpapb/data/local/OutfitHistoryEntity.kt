package com.zuhdiarifazizi.projectakhirpapb.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitHistory
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "outfit_history",
    indices = [Index(value = ["userId", "wornAtEpochMillis"])]
)
data class OutfitHistoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val outfitId: String,
    val occasion: String,
    val score: Double,
    val wornAtEpochMillis: Long,
    val itemsSnapshot: String
)

fun OutfitHistory.toEntity(): OutfitHistoryEntity = OutfitHistoryEntity(
    id = id,
    userId = userId,
    outfitId = outfitId,
    occasion = occasion.name,
    score = score,
    wornAtEpochMillis = wornAtEpochMillis,
    itemsSnapshot = OutfitHistoryCodec.encodeItems(items)
)

fun OutfitHistoryEntity.toDomain(): OutfitHistory = OutfitHistory(
    id = id,
    userId = userId,
    outfitId = outfitId,
    items = OutfitHistoryCodec.decodeItems(itemsSnapshot, fallbackUserId = userId),
    occasion = runCatching { Occasion.valueOf(occasion) }.getOrDefault(Occasion.OTHER),
    score = score,
    wornAtEpochMillis = wornAtEpochMillis
)

/** Pure-Kotlin lossless codec for garment snapshots (avoids Android org.json stubs in JVM tests). */
object OutfitHistoryCodec {

    fun encodeItems(items: List<ClothingItem>): String =
        items.joinToString(separator = "\n") { item ->
            listOf(
                escape(item.id),
                escape(item.name),
                escape(item.category.name),
                escape(item.pattern.name),
                escape(item.occasion.name),
                item.color.hue.toString(),
                item.color.saturation.toString(),
                item.color.value.toString(),
                escape(item.color.colorName),
                escape(item.imageUri.orEmpty()),
                escape(item.material),
                escape(item.rfidTagUid.orEmpty()),
                item.createdAtEpochMillis.toString()
            ).joinToString(separator = "|")
        }

    fun decodeItems(encoded: String, fallbackUserId: String): List<ClothingItem> {
        if (encoded.isBlank()) return emptyList()
        return encoded.split("\n").mapNotNull { line ->
            if (line.isBlank()) return@mapNotNull null
            val fields = splitUnescaped(line)
            if (fields.size < 10) return@mapNotNull null
            val category = runCatching { ClothingCategory.valueOf(fields[2]) }.getOrNull() ?: return@mapNotNull null
            val pattern = runCatching { ClothingPattern.valueOf(fields[3]) }.getOrDefault(ClothingPattern.SOLID)
            val occasion = runCatching { Occasion.valueOf(fields[4]) }.getOrDefault(Occasion.OTHER)
            ClothingItem(
                id = fields[0],
                userId = fallbackUserId,
                name = fields[1],
                category = category,
                pattern = pattern,
                occasion = occasion,
                color = HsvColor(
                    hue = fields[5].toFloatOrNull() ?: 0f,
                    saturation = fields[6].toFloatOrNull() ?: 0f,
                    value = fields[7].toFloatOrNull() ?: 0.5f,
                    colorName = fields[8].ifBlank { "Netral" }
                ),
                imageUri = fields[9].ifBlank { null },
                createdAtEpochMillis = fields.getOrNull(12)?.toLongOrNull() ?: 0L,
                material = fields.getOrNull(10).orEmpty(),
                rfidTagUid = fields.getOrNull(11)?.ifBlank { null }
            )
        }
    }

    private fun escape(value: String): String = buildString(value.length) {
        for (ch in value) {
            when (ch) {
                '\\' -> append("\\\\")
                '|' -> append("\\p")
                '\n' -> append("\\n")
                '\r' -> append(' ')
                else -> append(ch)
            }
        }
    }

    private fun splitUnescaped(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var escaping = false
        for (ch in line) {
            if (escaping) {
                when (ch) {
                    '\\' -> current.append('\\')
                    'p' -> current.append('|')
                    'n' -> current.append('\n')
                    else -> current.append(ch)
                }
                escaping = false
            } else if (ch == '\\') {
                escaping = true
            } else if (ch == '|') {
                result += current.toString()
                current.setLength(0)
            } else {
                current.append(ch)
            }
        }
        result += current.toString()
        return result
    }
}

@Dao
interface OutfitHistoryDao {
    @Query("SELECT * FROM outfit_history WHERE userId = :userId ORDER BY wornAtEpochMillis DESC")
    fun observeByUser(userId: String): Flow<List<OutfitHistoryEntity>>

    @Query("SELECT * FROM outfit_history WHERE userId = :userId AND wornAtEpochMillis >= :fromEpochMillis ORDER BY wornAtEpochMillis DESC")
    suspend fun getSince(userId: String, fromEpochMillis: Long): List<OutfitHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: OutfitHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entries: List<OutfitHistoryEntity>)

    @Query("DELETE FROM outfit_history WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM outfit_history WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)
}
