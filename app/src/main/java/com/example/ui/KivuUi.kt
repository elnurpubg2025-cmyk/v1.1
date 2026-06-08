package com.example.ui

import android.app.Activity
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.ChannelEntity
import com.example.data.FavoriteEntity
import com.example.ui.theme.KivuTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class DeviceType {
    PHONE, TABLET, TV, WATCH
}

@Composable
fun detectDeviceType(): DeviceType {
    val context = LocalContext.current
    val config = LocalConfiguration.current
    
    // Watch detection
    val isWatch = (config.uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_WATCH || config.screenWidthDp < 300
    if (isWatch) return DeviceType.WATCH
    
    // TV detection
    val isTv = (config.uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION || 
               context.packageManager.hasSystemFeature("android.hardware.type.television") ||
               context.packageManager.hasSystemFeature("android.software.leanback")
    if (isTv) return DeviceType.TV
    
    // Tablet detection
    val isTablet = config.screenWidthDp >= 600
    if (isTablet) return DeviceType.TABLET
    
    return DeviceType.PHONE
}

@Composable
fun KivuAppContent(viewModel: KivuViewModel, isActivityPipMode: Boolean) {
    val context = LocalContext.current
    val deviceType = detectDeviceType()
    
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val isPipMode by viewModel.isPipMode.collectAsStateWithLifecycle()

    // Sync activity-level PiP state with viewmodel
    LaunchedEffect(isActivityPipMode) {
        viewModel.setPipMode(isActivityPipMode)
    }

    KivuTheme(themeName = currentTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                !isOnline -> {
                    NoInternetScreen(
                        onRetry = { viewModel.checkInternet() },
                        localeCode = viewModel.currentLanguage.collectAsStateWithLifecycle().value
                    )
                }
                isPipMode -> {
                    // Stripped down player screen for Picture-in-Picture mode
                    PipPlayerOnlyScreen(viewModel)
                }
                else -> {
                    when (deviceType) {
                        DeviceType.WATCH -> WatchView(viewModel)
                        DeviceType.TV -> TvView(viewModel)
                        DeviceType.TABLET -> TabletView(viewModel)
                        DeviceType.PHONE -> PhoneView(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun PipPlayerOnlyScreen(viewModel: KivuViewModel) {
    val currentChannel by viewModel.currentChannel.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val isAdaptivePerformance by viewModel.isAdaptivePerformance.collectAsStateWithLifecycle()
    val playbackRetryTrigger by viewModel.playbackRetryTrigger.collectAsStateWithLifecycle()
    val strings = Translations.getStrings(viewModel.currentLanguage.collectAsStateWithLifecycle().value)

    Box(modifier = Modifier.fillMaxSize()) {
        KivuPlayer(
            channelUrl = currentChannel?.url,
            playbackState = playbackState,
            isAdaptivePerformance = isAdaptivePerformance,
            onStateStarted = { viewModel.onPlaybackStarted() },
            onStateBuffering = { viewModel.onPlaybackBuffering() },
            onStateFailed = { viewModel.onPlaybackFailed() },
            modifier = Modifier.fillMaxSize(),
            retryTrigger = playbackRetryTrigger
        )

        // Overlay small control labels for PiP user context
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f))
                .padding(4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = currentChannel?.name ?: "",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.playPreviousChannel() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = "Prev",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Text(
                    text = "PTS Mode",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { viewModel.playNextChannel() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PhoneView(viewModel: KivuViewModel) {
    val context = LocalContext.current
    var isFullscreen by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val strings = Translations.getStrings(viewModel.currentLanguage.collectAsStateWithLifecycle().value)

    val currentChannel by viewModel.currentChannel.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val isAdaptivePerformance by viewModel.isAdaptivePerformance.collectAsStateWithLifecycle()
    val playbackRetryTrigger by viewModel.playbackRetryTrigger.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sleepTimerRemaining by viewModel.sleepTimerMinutes.collectAsStateWithLifecycle()

    var showExitDialog by remember { mutableStateOf(false) }

    // Intercept back button
    BackHandler {
        if (isFullscreen) {
            isFullscreen = false
        } else {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        ExitConfirmationDialog(
            strings = strings,
            onConfirmExit = { (context as? Activity)?.finish() },
            onDismiss = { showExitDialog = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Player Area (Top half)
        val playerHeight = if (isFullscreen) Modifier.fillMaxSize() else Modifier
            .fillMaxWidth()
            .height(240.dp)
        
        Box(modifier = playerHeight) {
            KivuPlayer(
                channelUrl = currentChannel?.url,
                playbackState = playbackState,
                isAdaptivePerformance = isAdaptivePerformance,
                onStateStarted = { viewModel.onPlaybackStarted() },
                onStateBuffering = { viewModel.onPlaybackBuffering() },
                onStateFailed = { viewModel.onPlaybackFailed() },
                modifier = Modifier.fillMaxSize(),
                retryTrigger = playbackRetryTrigger
            )

            // Custom Player Overlays
            PlayerControlsOverlay(
                channel = currentChannel,
                playbackState = playbackState,
                isFullscreen = isFullscreen,
                strings = strings,
                isAdaptive = isAdaptivePerformance,
                sleepTimerMinutes = sleepTimerRemaining,
                onToggleFullscreen = { isFullscreen = !isFullscreen },
                onPrevChannel = { viewModel.playPreviousChannel() },
                onNextChannel = { viewModel.playNextChannel() },
                onFavoriteToggle = { currentChannel?.let { viewModel.toggleFavorite(it) } },
                isFav = currentChannel?.let { viewModel.isChannelFavorite(it.url).collectAsState(initial = false).value } ?: false,
                onRetry = { viewModel.retryCurrentChannel() }
            )
        }

        // Channel list (Bottom half, only visible when not fullscreen)
        if (!isFullscreen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
            ) {
                // Header + Settings Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.channels,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    IconButton(
                        onClick = { showSettings = true },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = strings.settings,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Filter & Search Box
                SearchAndFilterBar(
                    searchQuery = searchQuery,
                    onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
                    selectedCategory = selectedCategory,
                    categories = viewModel.getAllCategories(),
                    onCategorySelected = { viewModel.selectCategory(it) },
                    strings = strings
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable channels list
                val filteredList = viewModel.getFilteredChannels()
                if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No Channels Found",
                            color = Color.Gray,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredList) { channel ->
                            val isFavorite = viewModel.isChannelFavorite(channel.url).collectAsState(initial = false).value
                            ChannelRowItem(
                                channel = channel,
                                isSelected = currentChannel?.url == channel.url,
                                isFavorite = isFavorite,
                                isAdaptive = isAdaptivePerformance,
                                onChannelSelected = { viewModel.selectChannel(channel) },
                                onFavoriteToggle = { viewModel.toggleFavorite(channel) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSettings) {
        SettingsSheet(
            viewModel = viewModel,
            strings = strings,
            onDismiss = { showSettings = false }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TvView(viewModel: KivuViewModel) {
    val context = LocalContext.current
    var isFullscreen by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val strings = Translations.getStrings(viewModel.currentLanguage.collectAsStateWithLifecycle().value)

    val currentChannel by viewModel.currentChannel.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val isAdaptivePerformance by viewModel.isAdaptivePerformance.collectAsStateWithLifecycle()
    val playbackRetryTrigger by viewModel.playbackRetryTrigger.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sleepTimerRemaining by viewModel.sleepTimerMinutes.collectAsStateWithLifecycle()
    val pressedDigits by viewModel.pressedDigits.collectAsStateWithLifecycle()

    var showExitDialog by remember { mutableStateOf(false) }

    // D-Pad navigation keys support on the parent Box
    val focusRequester = remember { FocusRequester() }

    BackHandler {
        if (isFullscreen) {
            isFullscreen = false
        } else {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        ExitConfirmationDialog(
            strings = strings,
            onConfirmExit = { (context as? Activity)?.finish() },
            onDismiss = { showExitDialog = false }
        )
    }

    // Capture hardware Remote Control buttons (0-9, up, down, back)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.Zero -> { viewModel.onNumericKeyPressed(0); true }
                        Key.One -> { viewModel.onNumericKeyPressed(1); true }
                        Key.Two -> { viewModel.onNumericKeyPressed(2); true }
                        Key.Three -> { viewModel.onNumericKeyPressed(3); true }
                        Key.Four -> { viewModel.onNumericKeyPressed(4); true }
                        Key.Five -> { viewModel.onNumericKeyPressed(5); true }
                        Key.Six -> { viewModel.onNumericKeyPressed(6); true }
                        Key.Seven -> { viewModel.onNumericKeyPressed(7); true }
                        Key.Eight -> { viewModel.onNumericKeyPressed(8); true }
                        Key.Nine -> { viewModel.onNumericKeyPressed(9); true }
                        
                        Key.DirectionLeft -> { viewModel.playPreviousChannel(); true }
                        Key.DirectionRight -> { viewModel.playNextChannel(); true }
                        Key.DirectionUp -> { viewModel.playPreviousChannel(); true }
                        Key.DirectionDown -> { viewModel.playNextChannel(); true }
                        Key.Enter, Key.NumPadEnter, Key.DirectionCenter -> {
                            isFullscreen = !isFullscreen
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
    ) {
        // Run TV focus initializer
        LaunchedEffect(Unit) {
            try {
                focusRequester.requestFocus()
            } catch (e: Exception) {
                Log.w("TvView", "Failed to focus Dpad container", e)
            }
        }

        if (isFullscreen) {
            // Fullscreen content
            Box(modifier = Modifier.fillMaxSize()) {
                KivuPlayer(
                    channelUrl = currentChannel?.url,
                    playbackState = playbackState,
                    isAdaptivePerformance = isAdaptivePerformance,
                    onStateStarted = { viewModel.onPlaybackStarted() },
                    onStateBuffering = { viewModel.onPlaybackBuffering() },
                    onStateFailed = { viewModel.onPlaybackFailed() },
                    modifier = Modifier.fillMaxSize(),
                    retryTrigger = playbackRetryTrigger
                )

                PlayerControlsOverlay(
                    channel = currentChannel,
                    playbackState = playbackState,
                    isFullscreen = true,
                    strings = strings,
                    isAdaptive = isAdaptivePerformance,
                    sleepTimerMinutes = sleepTimerRemaining,
                    onToggleFullscreen = { isFullscreen = false },
                    onPrevChannel = { viewModel.playPreviousChannel() },
                    onNextChannel = { viewModel.playNextChannel() },
                    onFavoriteToggle = { currentChannel?.let { viewModel.toggleFavorite(it) } },
                    isFav = currentChannel?.let { viewModel.isChannelFavorite(it.url).collectAsState(initial = false).value } ?: false,
                    onRetry = { viewModel.retryCurrentChannel() }
                )

                // TV Box visual numeric sequence digits buffer
                if (pressedDigits.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(24.dp)
                            .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "CHANNEL",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = pressedDigits,
                                color = Color.White,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            // Horizontal split for TV layout: Channels List (Left), Player & settings (Right)
            Row(modifier = Modifier.fillMaxSize()) {
                // Channels Sidebar (Left, 42% width)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.42f)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.appName,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif
                        )
                        
                        IconButton(onClick = { showSettings = true }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = strings.settings,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    SearchAndFilterBar(
                        searchQuery = searchQuery,
                        onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
                        selectedCategory = selectedCategory,
                        categories = viewModel.getAllCategories(),
                        onCategorySelected = { viewModel.selectCategory(it) },
                        strings = strings
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val filteredList = viewModel.getFilteredChannels()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredList) { channel ->
                            val isFavorite = viewModel.isChannelFavorite(channel.url).collectAsState(initial = false).value
                            TvChannelRowItem(
                                channel = channel,
                                isSelected = currentChannel?.url == channel.url,
                                isFavorite = isFavorite,
                                onChannelSelected = { viewModel.selectChannel(channel) }
                            )
                        }
                    }
                }

                // Player Preview Box (Right, 58% width)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.58f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.8f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        KivuPlayer(
                            channelUrl = currentChannel?.url,
                            playbackState = playbackState,
                            isAdaptivePerformance = isAdaptivePerformance,
                            onStateStarted = { viewModel.onPlaybackStarted() },
                            onStateBuffering = { viewModel.onPlaybackBuffering() },
                            onStateFailed = { viewModel.onPlaybackFailed() },
                            modifier = Modifier.fillMaxSize(),
                            retryTrigger = playbackRetryTrigger
                        )

                        PlayerControlsOverlay(
                            channel = currentChannel,
                            playbackState = playbackState,
                            isFullscreen = false,
                            strings = strings,
                            isAdaptive = isAdaptivePerformance,
                            sleepTimerMinutes = sleepTimerRemaining,
                            onToggleFullscreen = { isFullscreen = true },
                            onPrevChannel = { viewModel.playPreviousChannel() },
                            onNextChannel = { viewModel.playNextChannel() },
                            onFavoriteToggle = { currentChannel?.let { viewModel.toggleFavorite(it) } },
                            isFav = currentChannel?.let { viewModel.isChannelFavorite(it.url).collectAsState(initial = false).value } ?: false,
                            onRetry = { viewModel.retryCurrentChannel() }
                        )
                    }

                    // Program Details or Instructions Footer
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = currentChannel?.name ?: strings.appName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentChannel?.currentProgram ?: (strings.appName + " Live HD Source"),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSettings) {
        SettingsSheet(
            viewModel = viewModel,
            strings = strings,
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
fun TabletView(viewModel: KivuViewModel) {
    val context = LocalContext.current
    var isFullscreen by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val strings = Translations.getStrings(viewModel.currentLanguage.collectAsStateWithLifecycle().value)

    val currentChannel by viewModel.currentChannel.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val isAdaptivePerformance by viewModel.isAdaptivePerformance.collectAsStateWithLifecycle()
    val playbackRetryTrigger by viewModel.playbackRetryTrigger.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sleepTimerRemaining by viewModel.sleepTimerMinutes.collectAsStateWithLifecycle()

    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (isFullscreen) {
            isFullscreen = false
        } else {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        ExitConfirmationDialog(
            strings = strings,
            onConfirmExit = { (context as? Activity)?.finish() },
            onDismiss = { showExitDialog = false }
        )
    }

    if (isFullscreen) {
        // Fullscreen Mode
        Box(modifier = Modifier.fillMaxSize()) {
            KivuPlayer(
                channelUrl = currentChannel?.url,
                playbackState = playbackState,
                isAdaptivePerformance = isAdaptivePerformance,
                onStateStarted = { viewModel.onPlaybackStarted() },
                onStateBuffering = { viewModel.onPlaybackBuffering() },
                onStateFailed = { viewModel.onPlaybackFailed() },
                modifier = Modifier.fillMaxSize(),
                retryTrigger = playbackRetryTrigger
            )

            PlayerControlsOverlay(
                channel = currentChannel,
                playbackState = playbackState,
                isFullscreen = true,
                strings = strings,
                isAdaptive = isAdaptivePerformance,
                sleepTimerMinutes = sleepTimerRemaining,
                onToggleFullscreen = { isFullscreen = false },
                onPrevChannel = { viewModel.playPreviousChannel() },
                onNextChannel = { viewModel.playNextChannel() },
                onFavoriteToggle = { currentChannel?.let { viewModel.toggleFavorite(it) } },
                isFav = currentChannel?.let { viewModel.isChannelFavorite(it.url).collectAsState(initial = false).value } ?: false,
                onRetry = { viewModel.retryCurrentChannel() }
            )
        }
    } else {
        // Large screen Side-by-side Layout (Tablet optimization)
        Row(modifier = Modifier.fillMaxSize()) {
            // List left pane
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.45f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.appName,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    IconButton(onClick = { showSettings = true }) {
                        Icon(imageVector = Icons.Filled.Settings, contentDescription = strings.settings)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                SearchAndFilterBar(
                    searchQuery = searchQuery,
                    onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
                    selectedCategory = selectedCategory,
                    categories = viewModel.getAllCategories(),
                    onCategorySelected = { viewModel.selectCategory(it) },
                    strings = strings
                )

                Spacer(modifier = Modifier.height(12.dp))

                val filteredList = viewModel.getFilteredChannels()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList) { channel ->
                        val isFavorite = viewModel.isChannelFavorite(channel.url).collectAsState(initial = false).value
                        ChannelRowItem(
                            channel = channel,
                            isSelected = currentChannel?.url == channel.url,
                            isFavorite = isFavorite,
                            isAdaptive = isAdaptivePerformance,
                            onChannelSelected = { viewModel.selectChannel(channel) },
                            onFavoriteToggle = { viewModel.toggleFavorite(channel) }
                        )
                    }
                }
            }

            // Preview right pane
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.55f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    KivuPlayer(
                        channelUrl = currentChannel?.url,
                        playbackState = playbackState,
                        isAdaptivePerformance = isAdaptivePerformance,
                        onStateStarted = { viewModel.onPlaybackStarted() },
                        onStateBuffering = { viewModel.onPlaybackBuffering() },
                        onStateFailed = { viewModel.onPlaybackFailed() },
                        modifier = Modifier.fillMaxSize(),
                        retryTrigger = playbackRetryTrigger
                    )

                    PlayerControlsOverlay(
                        channel = currentChannel,
                        playbackState = playbackState,
                        isFullscreen = false,
                        strings = strings,
                        isAdaptive = isAdaptivePerformance,
                        sleepTimerMinutes = sleepTimerRemaining,
                        onToggleFullscreen = { isFullscreen = true },
                        onPrevChannel = { viewModel.playPreviousChannel() },
                        onNextChannel = { viewModel.playNextChannel() },
                        onFavoriteToggle = { currentChannel?.let { viewModel.toggleFavorite(it) } },
                        isFav = currentChannel?.let { viewModel.isChannelFavorite(it.url).collectAsState(initial = false).value } ?: false,
                        onRetry = { viewModel.retryCurrentChannel() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(0.7f)) {
                            Text(
                                text = currentChannel?.name ?: strings.appName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentChannel?.currentProgram ?: (strings.appName + " Adaptive Source"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(0.3f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentChannel?.groupTitle ?: "IPTV",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSettings) {
        SettingsSheet(
            viewModel = viewModel,
            strings = strings,
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
fun WatchView(viewModel: KivuViewModel) {
    val currentChannel by viewModel.currentChannel.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val isAdaptivePerformance by viewModel.isAdaptivePerformance.collectAsStateWithLifecycle()
    val playbackRetryTrigger by viewModel.playbackRetryTrigger.collectAsStateWithLifecycle()
    val strings = Translations.getStrings(viewModel.currentLanguage.collectAsStateWithLifecycle().value)
    
    var showCompactList by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showCompactList) {
            // Small watch channel selector list
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(viewModel.getFilteredChannels()) { ch ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .background(
                                if (currentChannel?.url == ch.url) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                else Color.DarkGray, RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                viewModel.selectChannel(ch)
                                showCompactList = false
                            }
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ch.name,
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        } else {
            // Watch player with tiny control buttons
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TV screen container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    KivuPlayer(
                        channelUrl = currentChannel?.url,
                        playbackState = playbackState,
                        isAdaptivePerformance = isAdaptivePerformance,
                        onStateStarted = { viewModel.onPlaybackStarted() },
                        onStateBuffering = { viewModel.onPlaybackBuffering() },
                        onStateFailed = { viewModel.onPlaybackFailed() },
                        modifier = Modifier.fillMaxSize(),
                        retryTrigger = playbackRetryTrigger
                    )

                    if (playbackState == PlaybackState.BUFFERING) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.Center)
                        )
                    }
                }

                Text(
                    text = currentChannel?.name ?: "Kivu TV",
                    color = Color.White,
                    fontSize = 10.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis
                )

                // Swap buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.playPreviousChannel() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = "Prev",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { showCompactList = true },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.List,
                            contentDescription = "Channels",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.playNextChannel() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerControlsOverlay(
    channel: ChannelEntity?,
    playbackState: PlaybackState,
    isFullscreen: Boolean,
    strings: LanguageStrings,
    isAdaptive: Boolean,
    sleepTimerMinutes: Int?,
    onToggleFullscreen: () -> Unit,
    onPrevChannel: () -> Unit,
    onNextChannel: () -> Unit,
    onFavoriteToggle: () -> Unit,
    isFav: Boolean,
    onRetry: () -> Unit = {}
) {
    var showOverlays by remember { mutableStateOf(true) }

    // Auto-hide overlays after 5 seconds to provide seamless theatrical stream experience
    LaunchedEffect(showOverlays, playbackState) {
        if (showOverlays && playbackState == PlaybackState.PLAYING) {
            delay(5000)
            showOverlays = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { showOverlays = !showOverlays }
    ) {
        // Buffering/Loading Overlay State
        if (playbackState == PlaybackState.BUFFERING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = strings.buffering,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Connection failure or weak internet state overlays
        if (playbackState == PlaybackState.ERROR_WEAK_INTERNET || playbackState == PlaybackState.ERROR_UNAVAILABLE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.82f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (playbackState == PlaybackState.ERROR_WEAK_INTERNET) Icons.Filled.WifiOff else Icons.Filled.Error,
                        contentDescription = "Playback Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (playbackState == PlaybackState.ERROR_WEAK_INTERNET) strings.internetWeak else strings.channelUnavailable,
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Retry",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.retry,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Interactive control overlay panel
        AnimatedVisibility(
            visible = showOverlays,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -20 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -20 })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            ) {
                // Top control status bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(0.7f)) {
                        Text(
                            text = channel?.name ?: strings.appName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (channel?.currentProgram != null) {
                            Text(
                                text = channel.currentProgram,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (sleepTimerMinutes != null) {
                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                Text(
                                    text = "$sleepTimerMinutes m",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(2.dp)
                                )
                            }
                        }

                        IconButton(onClick = onFavoriteToggle) {
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Fav",
                                tint = if (isFav) Color.Red else Color.White
                            )
                        }
                    }
                }

                // Middle Navigation Swappers (Left/Right buttons)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingActionButton(
                        onClick = onPrevChannel,
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        contentColor = Color.White,
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape
                    ) {
                        Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = "Prev")
                    }

                    FloatingActionButton(
                        onClick = onNextChannel,
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        contentColor = Color.White,
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape
                    ) {
                        Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = "Next")
                    }
                }

                // Bottom Fullscreen controller
                IconButton(
                    onClick = onToggleFullscreen,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                        contentDescription = "Toggle Fullscreen",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryPill(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    selectedCategory: String,
    categories: List<String>,
    onCategorySelected: (String) -> Unit,
    strings: LanguageStrings
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Search text field
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text(strings.searchPlaceholder, fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "Search Icon", modifier = Modifier.size(18.dp)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal scrolling category pills (Dynamic, quick selection of IPTV streams)
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp)
        ) {
            item {
                CategoryPill(
                    name = strings.allCategories,
                    isSelected = selectedCategory.isEmpty(),
                    onClick = { onCategorySelected("") }
                )
            }
            items(categories) { category ->
                CategoryPill(
                    name = category,
                    isSelected = selectedCategory == category,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@Composable
fun ChannelRowItem(
    channel: ChannelEntity,
    isSelected: Boolean,
    isFavorite: Boolean,
    isAdaptive: Boolean,
    onChannelSelected: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChannelSelected() }
            .then(
                if (isSelected) {
                    Modifier.border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant 
            else Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(0.85f)
            ) {
                // Left border accent line for selected/active indicator
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                // Small Logo design
                if (!isAdaptive && !channel.logo.isNullOrEmpty()) {
                    AsyncImage(
                        model = channel.logo,
                        contentDescription = channel.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tv,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = channel.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isSelected && !channel.currentProgram.isNullOrEmpty()) channel.currentProgram!! else channel.groupTitle,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (isSelected) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val translationY by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = -6f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(400, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    Box(
                        modifier = Modifier
                            .offset(y = translationY.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                IconButton(
                    onClick = onFavoriteToggle
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Fav",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TvChannelRowItem(
    channel: ChannelEntity,
    isSelected: Boolean,
    isFavorite: Boolean,
    onChannelSelected: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val bgStateColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isFocused -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    }

    val borderStroke = when {
        isFocused -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        isSelected -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        else -> BorderStroke(0.dp, Color.Transparent)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChannelSelected() }
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .border(borderStroke, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = bgStateColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFavorite) Color.Red.copy(alpha = 0.15f) 
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.Tv,
                    contentDescription = null,
                    tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = channel.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = channel.groupTitle,
                    color = Color.Gray,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun NoInternetScreen(onRetry: () -> Unit, localeCode: String) {
    val strings = Translations.getStrings(localeCode)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B11)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.WifiOff,
                contentDescription = strings.noInternet,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(76.dp)
                    .padding(bottom = 12.dp)
            )

            Text(
                text = strings.noInternet,
                fontSize = 22.sp,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Please check your network settings and try again.",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(46.dp)
            ) {
                Text(text = strings.retry, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ExitConfirmationDialog(
    strings: LanguageStrings,
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.ExitToApp,
                    contentDescription = strings.exitConfirmation,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = strings.exitConfirmation, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Text(text = strings.exitPrompt, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        },
        confirmButton = {
            Button(
                onClick = onConfirmExit,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = strings.exit, color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = strings.cancel, fontWeight = FontWeight.SemiBold)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsSheet(
    viewModel: KivuViewModel,
    strings: LanguageStrings,
    onDismiss: () -> Unit
) {
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val isAdaptivePerformance by viewModel.isAdaptivePerformance.collectAsStateWithLifecycle()
    val sleepTimerValue by viewModel.sleepTimerMinutes.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(4.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Exit configurations top
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.settings,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close dashboard")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Sleep Timer selection box
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = strings.sleepTimer,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val options = listOf(null, 5, 15, 30, 60, 120)
                                    FlowRow(
                                        maxItemsInEachRow = 3,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        options.forEach { minutes ->
                                            val isSelected = sleepTimerValue == minutes
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.primary 
                                                        else MaterialTheme.colorScheme.surface
                                                    )
                                                    .clickable { viewModel.setSleepTimer(minutes) }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (minutes == null) strings.sleepTimerDisabled else "${minutes}m",
                                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Theme selector box
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = strings.selectTheme,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                val themes = listOf("Elegant Dark", "Cosmic Midnight", "Ocean Breeze", "Emerald Shine", "Amoled Slate")
                                FlowRow(
                                    maxItemsInEachRow = 2,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    themes.forEach { theme ->
                                        val isSelected = currentTheme == theme
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary 
                                                    else MaterialTheme.colorScheme.surface
                                                )
                                                .clickable { viewModel.changeTheme(theme) }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = theme,
                                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Language Selection Box
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = strings.selectLanguage,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                val languagesList = Translations.languages.toList()
                                FlowRow(
                                    maxItemsInEachRow = 3,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    languagesList.forEach { (code, name) ->
                                        val isSelected = currentLanguage == code
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary 
                                                    else MaterialTheme.colorScheme.surface
                                                )
                                                .clickable { viewModel.changeLanguage(code) }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = name,
                                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Adaptive Optimization option
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(0.75f)) {
                                    Text(
                                        text = strings.adaptivePerformance,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = strings.adaptivePerformanceDesc,
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                                Switch(
                                    checked = isAdaptivePerformance,
                                    onCheckedChange = { viewModel.toggleAdaptivePerformance() },
                                    modifier = Modifier.weight(0.25f)
                                )
                            }
                        }
                    }

                    // About section
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = strings.aboutTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = strings.aboutText,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    // Channel Owners section
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = strings.ownersTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = strings.ownersText,
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
