package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ChannelRepository(private val dao: KivuDao) {
    private val TAG = "ChannelRepository"
    private val m3uUrl = "https://raw.githubusercontent.com/djmete/KivuTv/refs/heads/master/channel.m3u"

    val channels: Flow<List<ChannelEntity>> = dao.getAllChannels()
    val favorites: Flow<List<FavoriteEntity>> = dao.getAllFavorites()
    val settings: Flow<List<SettingEntity>> = dao.getAllSettings()

    suspend fun refreshChannels() = withContext(Dispatchers.IO) {
        Log.d(TAG, "Refreshing channels from network...")
        try {
            val parsed = M3uParser.fetchAndParse(m3uUrl)
            if (parsed.isNotEmpty()) {
                // Clear and save updated channels
                dao.clearAllChannels()
                dao.insertChannels(parsed)
                Log.d(TAG, "Successfully refreshed and saved ${parsed.size} channels.")
            } else {
                Log.w(TAG, "Parsed channel list is empty, skipping local update.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed refreshing channels", e)
        }
    }

    suspend fun toggleFavorite(channel: ChannelEntity) {
        val isFav = dao.isFavoriteState(channel.url)
        if (isFav) {
            dao.deleteFavorite(channel.url)
        } else {
            dao.insertFavorite(
                FavoriteEntity(
                    url = channel.url,
                    name = channel.name,
                    logo = channel.logo,
                    groupTitle = channel.groupTitle,
                    userAgent = channel.userAgent,
                    tvgId = channel.tvgId,
                    tvgName = channel.tvgName
                )
            )
        }
    }

    suspend fun isFavorite(url: String): Boolean {
        return dao.isFavoriteState(url)
    }

    suspend fun saveSetting(key: String, value: String) {
        dao.insertSetting(SettingEntity(key, value))
    }

    suspend fun getSettingValue(key: String): String? {
        return dao.getSettingByKey(key)?.value
    }
}
