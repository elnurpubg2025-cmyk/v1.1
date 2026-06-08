package com.example.data

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.StringReader
import java.util.concurrent.TimeUnit

object M3uParser {
    private const val TAG = "M3uParser"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun fetchAndParse(url: String): List<ChannelEntity> {
        val channels = mutableListOf<ChannelEntity>()
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) IPTVStreamPlayer/1.0")
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Unsuccessful response fetching M3U: ${response.code}")
                    return emptyList()
                }
                
                val bodyString = response.body?.string() ?: return emptyList()
                return parse(bodyString)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching and parsing M3U list", e)
        }
        return channels
    }

    fun parse(m3uContent: String): List<ChannelEntity> {
        val channels = mutableListOf<ChannelEntity>()
        val reader = BufferedReader(StringReader(m3uContent))
        
        var line: String? = reader.readLine()
        
        var name = ""
        var logo = ""
        var groupTitle = "General"
        var currentProgram: String? = null
        
        while (line != null) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#EXTINF:")) {
                // Parse EXTINF metadata
                name = ""
                logo = ""
                groupTitle = "General"
                currentProgram = null
                
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
                
                // Parse tvg-name="..." if present or other attributes
                val tvgNameMatcher = "tvg-name=\"([^\"]+)\"".toRegex().find(trimmed)
                
                // Find comma separating attributes from display name
                val lastCommaIndex = trimmed.lastIndexOf(',')
                if (lastCommaIndex != -1) {
                    name = trimmed.substring(lastCommaIndex + 1).trim()
                }
                
                if (name.isEmpty() && tvgNameMatcher != null) {
                    name = tvgNameMatcher.groupValues[1]
                }
                
                // See if we can parse some program info or dummy data
                // Some lines have tvg-id or description
                if (trimmed.contains("current-program=")) {
                    currentProgram = "current-program=\"([^\"]+)\"".toRegex().find(trimmed)?.groupValues?.get(1)
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
                        currentProgram = currentProgram
                    )
                )
                
                // Reset for next channel
                name = ""
                logo = ""
                groupTitle = "General"
                currentProgram = null
            }
            line = reader.readLine()
        }
        
        return channels
    }
}
