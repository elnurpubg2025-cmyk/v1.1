package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "channel_cache")
data class ChannelEntity(
    @PrimaryKey val url: String,
    val name: String,
    val logo: String?,
    val groupTitle: String,
    val currentProgram: String? = null,
    val userAgent: String? = null,
    val tvgId: String? = null,
    val tvgName: String? = null
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val url: String,
    val name: String,
    val logo: String?,
    val groupTitle: String,
    val userAgent: String? = null,
    val tvgId: String? = null,
    val tvgName: String? = null
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Dao
interface KivuDao {
    @Query("SELECT * FROM channel_cache")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Query("DELETE FROM channel_cache")
    suspend fun clearAllChannels()

    @Delete
    suspend fun deleteChannels(channels: List<ChannelEntity>)

    @Query("DELETE FROM channel_cache WHERE url = :url")
    suspend fun deleteChannelByUrl(url: String)

    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(fav: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE url = :url")
    suspend fun deleteFavorite(url: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE url = :url)")
    suspend fun isFavoriteState(url: String): Boolean

    @Query("SELECT * FROM settings")
    fun getAllSettings(): Flow<List<SettingEntity>>

    @Query("SELECT * FROM settings WHERE `key` = :key")
    suspend fun getSettingByKey(key: String): SettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: SettingEntity)
}

@Database(entities = [ChannelEntity::class, FavoriteEntity::class, SettingEntity::class], version = 1, exportSchema = false)
abstract class KivuDatabase : RoomDatabase() {
    abstract fun kivuDao(): KivuDao

    companion object {
        @Volatile
        private var INSTANCE: KivuDatabase? = null

        fun getDatabase(context: Context): KivuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KivuDatabase::class.java,
                    "kivu_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
