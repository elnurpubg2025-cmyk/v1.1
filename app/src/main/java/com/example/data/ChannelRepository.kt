package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
            // Read currently cached channels list
            val currentCached = dao.getAllChannels().first()
            val etag = getSettingValue("m3u_etag")
            val lastModified = getSettingValue("m3u_last_modified")

            // Smart check via HTTP caching validation headers
            val fetchResult = M3uParser.fetchAndParseWithCache(m3uUrl, etag, lastModified)
            if (!fetchResult.isModified) {
                Log.d(TAG, "M3U has not changed (304 Not Modified / Cache fresh). Skipping delta updates.")
                return@withContext
            }

            // Save new cache tokens
            if (fetchResult.etag != null) saveSetting("m3u_etag", fetchResult.etag)
            if (fetchResult.lastModified != null) saveSetting("m3u_last_modified", fetchResult.lastModified)

            val remoteChannels = fetchResult.channels
            if (remoteChannels.isNotEmpty()) {
                val existingMap = currentCached.associateBy { it.url }
                val remoteMap = remoteChannels.associateBy { it.url }

                // 1. Detect URL updates/rotations to preserve favorites
                val rotatedUrls = mutableMapOf<String, String>() // oldUrl -> newUrl
                val leftOverCurrent = currentCached.filter { !remoteMap.containsKey(it.url) }
                val leftOverRemote = remoteChannels.filter { !existingMap.containsKey(it.url) }

                for (oldCh in leftOverCurrent) {
                    val matchByTvgId = if (!oldCh.tvgId.isNullOrEmpty()) {
                        leftOverRemote.find { it.tvgId == oldCh.tvgId }
                    } else null

                    val matchByName = if (matchByTvgId == null && oldCh.name.isNotEmpty()) {
                        leftOverRemote.find { it.name == oldCh.name }
                    } else null

                    val matchedNewCh = matchByTvgId ?: matchByName
                    if (matchedNewCh != null) {
                        rotatedUrls[oldCh.url] = matchedNewCh.url
                    }
                }

                // Morph favorites to the new updated URLs
                for ((oldUrl, newUrl) in rotatedUrls) {
                    val wasFav = dao.isFavoriteState(oldUrl)
                    if (wasFav) {
                        dao.deleteFavorite(oldUrl)
                        val remoteCh = remoteMap[newUrl]
                        if (remoteCh != null) {
                            dao.insertFavorite(
                                FavoriteEntity(
                                    url = remoteCh.url,
                                    name = remoteCh.name,
                                    logo = remoteCh.logo,
                                    groupTitle = remoteCh.groupTitle,
                                    userAgent = remoteCh.userAgent,
                                    tvgId = remoteCh.tvgId,
                                    tvgName = remoteCh.tvgName
                                )
                            )
                        }
                    }
                }

                // 2. Identify and perform delta deletes
                val channelsToDelete = currentCached.filter { !remoteMap.containsKey(it.url) }
                if (channelsToDelete.isNotEmpty()) {
                    dao.deleteChannels(channelsToDelete)
                    Log.d(TAG, "Delta sync delete: removed ${channelsToDelete.size} obsolete channels.")
                }

                // 3. Identify and perform delta upserts (inserts/updates)
                val channelsToInsertOrUpdate = remoteChannels.filter { remoteCh ->
                    val cachedCh = existingMap[remoteCh.url]
                    cachedCh == null || cachedCh != remoteCh
                }

                if (channelsToInsertOrUpdate.isNotEmpty()) {
                    dao.insertChannels(channelsToInsertOrUpdate)
                    Log.d(TAG, "Delta sync insert/update: completed on ${channelsToInsertOrUpdate.size} channels.")
                }

                Log.d(TAG, "Smart delta sync successfully completed.")
            } else {
                Log.w(TAG, "Parsed channel list was empty, skipping local catalog upgrade.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to complete background sync", e)
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
