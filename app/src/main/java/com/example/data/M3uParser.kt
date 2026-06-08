package com.example.data

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.StringReader
import java.util.concurrent.TimeUnit

data class M3uFetchResult(
    val isModified: Boolean,
    val channels: List<ChannelEntity>,
    val etag: String?,
    val lastModified: String?
)

object M3uParser {
    private const val TAG = "M3uParser"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun fetchAndParse(url: String): List<ChannelEntity> {
        val result = fetchAndParseWithCache(url, null, null)
        return result.channels
    }

    fun fetchAndParseWithCache(url: String, etag: String?, lastModified: String?): M3uFetchResult {
        try {
            val requestBuilder = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) IPTVStreamPlayer/1.0")
            
            if (!etag.isNullOrEmpty()) {
                requestBuilder.header("If-None-Match", etag)
            }
            if (!lastModified.isNullOrEmpty()) {
                requestBuilder.header("If-Modified-Since", lastModified)
            }
            
            val request = requestBuilder.build()
            client.newCall(request).execute().use { response ->
                if (response.code == 304) {
                    Log.d(TAG, "Server returned 304 Not Modified. Cache is fresh.")
                    return M3uFetchResult(
                        isModified = false,
                        channels = emptyList(),
                        etag = etag,
                        lastModified = lastModified
                    )
                }
                
                if (!response.isSuccessful) {
                    Log.e(TAG, "Unsuccessful response fetching M3U: ${response.code}")
                    return M3uFetchResult(false, emptyList(), etag, lastModified)
                }
                
                val bodyString = response.body?.string() ?: return M3uFetchResult(false, emptyList(), etag, lastModified)
                val newEtag = response.header("ETag")
                val newLastModified = response.header("Last-Modified")
                val parsed = parse(bodyString)
                
                Log.d(TAG, "Fetched M3U successfully. Parsed ${parsed.size} channels. ETag: $newEtag, Last-Modified: $newLastModified")
                return M3uFetchResult(
                    isModified = true,
                    channels = parsed,
                    etag = newEtag,
                    lastModified = newLastModified
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching and parsing M3U list with cache", e)
        }
        return M3uFetchResult(false, emptyList(), etag, lastModified)
    }

    fun parse(m3uContent: String): List<ChannelEntity> {
        val channels = mutableListOf<ChannelEntity>()
        val reader = BufferedReader(StringReader(m3uContent))
        
        var line: String? = reader.readLine()
        
        var name = ""
        var logo = ""
        var groupTitle = "General"
        var currentProgram: String? = null
        var userAgent: String? = null
        var tvgId: String? = null
        var tvgName: String? = null
        
        while (line != null) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#EXTINF:")) {
                // Parse EXTINF metadata
                name = ""
                logo = ""
                groupTitle = "General"
                currentProgram = null
                userAgent = null
                tvgId = null
                tvgName = null
                
                // Parse group-title="..."
                val groupMatcher = "group-title=\"([^\"]+)\"".toRegex().find(trimmed)
                if (groupMatcher != null) {
                    groupTitle = groupMatcher.groupValues[1]
                }
                
                // Parse tvg-logo="..."
                val logoMatcher = "tvg-logo=\"([^\"]+)\"".toRegex().find(trimmed)
                if (logoMatcher != null) {
                    logo = logoMatcher.groupValues[1]
                }
                
                // Parse tvg-id="..."
                val idMatcher = "tvg-id=\"([^\"]+)\"".toRegex().find(trimmed)
                if (idMatcher != null) {
                    tvgId = idMatcher.groupValues[1]
                }
                
                // Parse tvg-name="..."
                val tvgNameMatcher = "tvg-name=\"([^\"]+)\"".toRegex().find(trimmed)
                if (tvgNameMatcher != null) {
                    tvgName = tvgNameMatcher.groupValues[1]
                }
                
                // Find comma separating attributes from display name
                val lastCommaIndex = trimmed.lastIndexOf(',')
                if (lastCommaIndex != -1) {
                    name = trimmed.substring(lastCommaIndex + 1).trim()
                }
                
                if (name.isEmpty() && tvgNameMatcher != null) {
                    name = tvgNameMatcher.groupValues[1]
                }
                
                // Parse program info or dummy data
                if (trimmed.contains("current-program=")) {
                    currentProgram = "current-program=\"([^\"]+)\"".toRegex().find(trimmed)?.groupValues?.get(1)
                }
            } else if (trimmed.startsWith("#EXTVLCOPT:")) {
                if (trimmed.contains("http-user-agent=")) {
                    userAgent = trimmed.substringAfter("http-user-agent=").trim()
                }
            } else if (trimmed.startsWith("http") || trimmed.startsWith("https")) {
                // This is a stream URL line
                val cleanUrl = trimmed
                if (name.isEmpty()) {
                    name = "Channel " + (channels.size + 1)
                }
                
                channels.add(
                    ChannelEntity(
                        url = cleanUrl,
                        name = name,
                        logo = if (logo.isNotEmpty()) logo else null,
                        groupTitle = groupTitle,
                        currentProgram = currentProgram,
                        userAgent = userAgent,
                        tvgId = tvgId,
                        tvgName = tvgName
                    )
                )
                
                // Reset for next channel
                name = ""
                logo = ""
                groupTitle = "General"
                currentProgram = null
                userAgent = null
                tvgId = null
                tvgName = null
            }
            line = reader.readLine()
        }
        
        return channels
    }
}
