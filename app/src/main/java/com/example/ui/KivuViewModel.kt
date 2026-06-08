package com.example.ui

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class PlaybackState {
    IDLE,
    BUFFERING,
    PLAYING,
    ERROR_WEAK_INTERNET,
    ERROR_UNAVAILABLE
}

class KivuViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "KivuViewModel"
    private val repository: ChannelRepository
    
    // States
    val channels = MutableStateFlow<List<ChannelEntity>>(emptyList())
    val favorites = MutableStateFlow<List<FavoriteEntity>>(emptyList())
    
    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _currentTheme = MutableStateFlow("Elegant Dark")
    val currentTheme: StateFlow<String> = _currentTheme.asStateFlow()

    private val _isAdaptivePerformance = MutableStateFlow(false)
    val isAdaptivePerformance: StateFlow<Boolean> = _isAdaptivePerformance.asStateFlow()

    private val _sleepTimerMinutes = MutableStateFlow<Int?>(null)
    val sleepTimerMinutes: StateFlow<Int?> = _sleepTimerMinutes.asStateFlow()

    private val _currentChannel = MutableStateFlow<ChannelEntity?>(null)
    val currentChannel: StateFlow<ChannelEntity?> = _currentChannel.asStateFlow()

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _playbackRetryTrigger = MutableStateFlow(0)
    val playbackRetryTrigger: StateFlow<Int> = _playbackRetryTrigger.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // TV Remote numeric entry
    private val _pressedDigits = MutableStateFlow("")
    val pressedDigits: StateFlow<String> = _pressedDigits.asStateFlow()

    // Internet check
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // PiP Mode configuration
    private val _isPipMode = MutableStateFlow(false)
    val isPipMode: StateFlow<Boolean> = _isPipMode.asStateFlow()

    private var numericDelayTimer: Job? = null
    private var sleepTimerJob: Job? = null
    private var bufferTimeoutJob: Job? = null
    private var backgroundRefreshJob: Job? = null

    init {
        val database = KivuDatabase.getDatabase(application)
        repository = ChannelRepository(database.kivuDao())

        // Initial setup
        detectNetworkState()
        loadLocalSettingsAndData()
        startBackgroundSync()
    }

    private fun detectNetworkState() {
        val context = getApplication<Application>().applicationContext
        val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        if (connectivityManager != null) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            _isOnline.value = capabilities != null && (
                capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET)
            )
        } else {
            _isOnline.value = true
        }
    }

    fun checkInternet() {
        detectNetworkState()
    }

    private fun loadLocalSettingsAndData() {
        viewModelScope.launch {
            // Collect channels
            repository.channels.collectLatest { cached ->
                if (cached.isNotEmpty()) {
                    channels.value = cached
                    // Auto-select first channel on launch if not selected and online
                    if (_currentChannel.value == null && _isOnline.value) {
                        _currentChannel.value = cached.first()
                    }
                } else {
                    // Try to refresh if empty
                    if (_isOnline.value) {
                        repository.refreshChannels()
                    }
                }
            }
        }

        viewModelScope.launch {
            // Collect favorites
            repository.favorites.collectLatest { favs ->
                favorites.value = favs
            }
        }

        viewModelScope.launch {
            // Load and restore set preferences
            _currentLanguage.value = repository.getSettingValue("language") ?: Translations.detectDeviceLanguage()
            _currentTheme.value = repository.getSettingValue("theme") ?: "Elegant Dark"
            _isAdaptivePerformance.value = (repository.getSettingValue("adaptive_optimization") ?: "true").toBoolean()
            
            // Check performance optimization for old device (API level lower than 28 or RAM < 2GB is marked as a light optimization by default)
            val systemRam = getDeviceRamInGb()
            if (systemRam > 0 && systemRam < 2.5 || android.os.Build.VERSION.SDK_INT < 28) {
                // Force adaptive optimization on old/low RAM devices
                _isAdaptivePerformance.value = true
            }
        }
    }

    private fun getDeviceRamInGb(): Double {
        try {
            val activityManager = getApplication<Application>().getSystemService(android.content.Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
            if (activityManager != null) {
                val memoryInfo = android.app.ActivityManager.MemoryInfo()
                activityManager.getMemoryInfo(memoryInfo)
                return memoryInfo.totalMem.toDouble() / (1024.0 * 1024.0 * 1024.0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking memory", e)
        }
        return 0.0
    }

    private fun startBackgroundSync() {
        // Run AJAX-style background refresh every 45 seconds to update channels stream details
        backgroundRefreshJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                if (_isOnline.value) {
                    try {
                        repository.refreshChannels()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in background stream sync", e)
                    }
                }
                delay(45_000) // AJAX update interval
            }
        }
    }

    fun selectChannel(channel: ChannelEntity) {
        if (!_isOnline.value) return
        _pressedDigits.value = ""
        cancelPlaybackTimers()
        
        _currentChannel.value = channel
        _playbackState.value = PlaybackState.BUFFERING
        
        // Start a 60 second watchdog to show weak internet message if buffer doesn't stop
        startBufferWatchdog()
    }

    fun selectChannelByUrl(url: String) {
        val found = channels.value.find { it.url == url }
        if (found != null) {
            selectChannel(found)
        }
    }

    fun toggleFavorite(channel: ChannelEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(channel)
        }
    }

    fun isChannelFavorite(url: String): Flow<Boolean> {
        return repository.favorites.map { list -> list.any { it.url == url } }
    }

    // Handlers for Player View
    fun onPlaybackStarted() {
        cancelBufferWatchdog()
        _playbackState.value = PlaybackState.PLAYING
    }

    fun onPlaybackBuffering() {
        _playbackState.value = PlaybackState.BUFFERING
        startBufferWatchdog()
    }

    fun onPlaybackFailed() {
        cancelBufferWatchdog()
        _playbackState.value = PlaybackState.ERROR_UNAVAILABLE
    }

    fun retryCurrentChannel() {
        if (!_isOnline.value) {
            checkInternet()
            return
        }
        cancelPlaybackTimers()
        _playbackState.value = PlaybackState.BUFFERING
        _playbackRetryTrigger.value = _playbackRetryTrigger.value + 1
        startBufferWatchdog()
    }

    private fun startBufferWatchdog() {
        cancelBufferWatchdog()
        bufferTimeoutJob = viewModelScope.launch {
            delay(60_000) // 1 minute buffering timeout
            if (_playbackState.value == PlaybackState.BUFFERING) {
                _playbackState.value = PlaybackState.ERROR_WEAK_INTERNET
            }
        }
    }

    private fun cancelBufferWatchdog() {
        bufferTimeoutJob?.cancel()
        bufferTimeoutJob = null
    }

    private fun cancelPlaybackTimers() {
        cancelBufferWatchdog()
    }

    // Dynamic Swapping logic
    fun playNextChannel() {
        val filtered = getFilteredChannels()
        if (filtered.isEmpty()) return
        val currentIdx = filtered.indexOfFirst { it.url == _currentChannel.value?.url }
        if (currentIdx != -1) {
            val nextIdx = (currentIdx + 1) % filtered.size
            selectChannel(filtered[nextIdx])
        } else {
            selectChannel(filtered.first())
        }
    }

    fun playPreviousChannel() {
        val filtered = getFilteredChannels()
        if (filtered.isEmpty()) return
        val currentIdx = filtered.indexOfFirst { it.url == _currentChannel.value?.url }
        if (currentIdx != -1) {
            val prevIdx = if (currentIdx - 1 < 0) filtered.size - 1 else currentIdx - 1
            selectChannel(filtered[prevIdx])
        } else {
            selectChannel(filtered.first())
        }
    }

    fun getFilteredChannels(): List<ChannelEntity> {
        val category = _selectedCategory.value
        var baseList = if (category == "__favorites__") {
            favorites.value.map { fav ->
                ChannelEntity(
                    url = fav.url,
                    name = fav.name,
                    logo = fav.logo,
                    groupTitle = fav.groupTitle,
                    userAgent = fav.userAgent,
                    tvgId = fav.tvgId,
                    tvgName = fav.tvgName
                )
            }
        } else {
            var list = channels.value
            if (category.isNotEmpty()) {
                list = list.filter { it.groupTitle.equals(category, ignoreCase = true) }
            }
            list
        }

        // Filter by search query
        val query = _searchQuery.value
        if (query.isNotEmpty()) {
            baseList = baseList.filter { it.name.contains(query, ignoreCase = true) }
        }

        return baseList
    }

    fun getAllCategories(): List<String> {
        return channels.value.map { it.groupTitle }.distinct().sorted()
    }

    // Dynamic numeric selection for TV box D-pad or TV boxes
    fun onNumericKeyPressed(digit: Int) {
        cancelNumericDelay()
        _pressedDigits.value = _pressedDigits.value + digit.toString()

        // 4-5 seconds delay to confirm the sequence before opening
        numericDelayTimer = viewModelScope.launch {
            delay(4500)
            executeNumericSelection()
        }
    }

    private fun executeNumericSelection() {
        val digits = _pressedDigits.value
        if (digits.isNotEmpty()) {
            val index = digits.toIntOrNull()
            if (index != null && index > 0) {
                // Find channel at index (1-based index)
                val filtered = getFilteredChannels()
                if (index <= filtered.size) {
                    selectChannel(filtered[index - 1])
                } else if (channels.value.isNotEmpty()) {
                    // Fallback to absolute index in raw channels
                    val rawIndex = index % channels.value.size
                    val actualIdx = if (rawIndex == 0) channels.value.size - 1 else rawIndex - 1
                    selectChannel(channels.value[actualIdx])
                }
            }
            _pressedDigits.value = ""
        }
    }

    private fun cancelNumericDelay() {
        numericDelayTimer?.cancel()
        numericDelayTimer = null
    }

    // Search operations
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    // Configuration preferences setter
    fun changeLanguage(lang: String) {
        viewModelScope.launch {
            _currentLanguage.value = lang
            repository.saveSetting("language", lang)
        }
    }

    fun changeTheme(theme: String) {
        viewModelScope.launch {
            _currentTheme.value = theme
            repository.saveSetting("theme", theme)
        }
    }

    fun toggleAdaptivePerformance() {
        viewModelScope.launch {
            val newVal = !_isAdaptivePerformance.value
            _isAdaptivePerformance.value = newVal
            repository.saveSetting("adaptive_optimization", newVal.toString())
        }
    }

    // Sleep Timer countdown implementation
    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        _sleepTimerMinutes.value = minutes
        
        if (minutes != null && minutes > 0) {
            sleepTimerJob = viewModelScope.launch {
                var currentMin = minutes
                while (currentMin > 0) {
                    delay(60_000) // Count down every 1 minute
                    currentMin--
                    _sleepTimerMinutes.value = currentMin
                }
                
                // Sleep Timer triggered: exit playback
                _currentChannel.value = null
                _playbackState.value = PlaybackState.IDLE
                _sleepTimerMinutes.value = null
                
                // In a production IPTV app, we pause player or even exit the app.
                // We'll pause playback state to IDLE, protecting the device from resource drain.
                Log.d(TAG, "Sleep Timer triggered. Stopped playback.")
            }
        }
    }

    fun setPipMode(isPip: Boolean) {
        _isPipMode.value = isPip
    }

    override fun onCleared() {
        super.onCleared()
        cancelBufferWatchdog()
        cancelNumericDelay()
        sleepTimerJob?.cancel()
        backgroundRefreshJob?.cancel()
    }
}
