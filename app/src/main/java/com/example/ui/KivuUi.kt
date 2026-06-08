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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
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
import kotlinx.coroutines.flow.map

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

    // 1.2 Splash Preloading state
    val isSplashVisible = remember { mutableStateOf(true) }

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
                isSplashVisible.value -> {
                    KivuSplashScreen(
                        onFinished = { isSplashVisible.value = false }
                    )
                }
                !isOnline -> {
                    NoInternetScreen(
                        onRetry = { viewModel.checkInternet() },
                        localeCode = viewModel.currentLanguage.collectAsStateWithLifecycle().value
                    )
                }
                isPipMode -> {
                    PipPlayerOnlyScreen(viewModel)
                }
                else -> {
                    if (deviceType == DeviceType.WATCH) {
                        WatchView(viewModel)
                    } else {
                        KivuMainDashboard(viewModel, deviceType)
                    }
                }
            }
        }
    }
}

@Composable
fun KivuSplashScreen(onFinished: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    var loadingProgress by remember { mutableStateOf(0f) }
    var currentSubtext by remember { mutableStateOf("AI Auto-Optimization Matcher Engine active...") }

    LaunchedEffect(Unit) {
        // Stepwise loader progression showing authentic setup milestones
        delay(600)
        loadingProgress = 0.35f
        currentSubtext = "Predictive Stream Pipeline Cache Setup..."
        delay(700)
        loadingProgress = 0.72f
        currentSubtext = "Amoled Deep Dark Render Engines initialization..."
        delay(600)
        loadingProgress = 1.0f
        currentSubtext = "Zero-Lag Channel Indexing Completed!"
        delay(500)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF020617)) // Modern deep space midnight theme
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "KIVUTV",
                color = Color(0xFF38BDF8), // Cyan neon tint
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.scale(scale)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "V1.2 ULTIMATE EDITION",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            LinearProgressIndicator(
                progress = loadingProgress,
                color = Color(0xFF38BDF8),
                trackColor = Color(0xFF1E293B),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = currentSubtext,
                color = Color.Gray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
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
            retryTrigger = playbackRetryTrigger,
            channelUserAgent = currentChannel?.userAgent,
            viewModel = viewModel
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


data class Movie(
    val title: String,
    val genre: String,
    val rating: String,
    val duration: String,
    val description: String,
    val imageUrl: String,
    val streamUrl: String
)

val preLoadedMovies = listOf(
    Movie(
        title = "Dune: Part Two",
        genre = "Science Fiction",
        rating = "9.1",
        duration = "166 m",
        description = "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.",
        imageUrl = "https://images.unsplash.com/photo-1544447677768-be436bb09401?w=400",
        streamUrl = "http://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"
    ),
    Movie(
        title = "Interstellar",
        genre = "Sci-Fi / Drama",
        rating = "8.9",
        duration = "169 m",
        description = "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.",
        imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=400",
        streamUrl = "http://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"
    ),
    Movie(
        title = "Inception",
        genre = "Action / Sci-Fi",
        rating = "8.8",
        duration = "148 m",
        description = "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea.",
        imageUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400",
        streamUrl = "http://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"
    ),
    Movie(
        title = "Nefes: Vatan Sağolsun",
        genre = "Drama / War",
        rating = "8.5",
        duration = "120 m",
        description = "A military unit stationed at a remote Turkish border patrol station faces existential struggles against surrounding threats.",
        imageUrl = "https://images.unsplash.com/photo-1507608869274-d3177c8bb4c7?w=400",
        streamUrl = "http://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"
    ),
    Movie(
        title = "Kardeşim Benim",
        genre = "Comedy / Drama",
        rating = "7.4",
        duration = "120 m",
        description = "Two famous brothers reunite for their father's funeral and set off on an adventure in an old hearse.",
        imageUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=400",
        streamUrl = "http://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"
    )
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun KivuMainDashboard(viewModel: KivuViewModel, deviceType: DeviceType) {
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val strings = Translations.getStrings(viewModel.currentLanguage.collectAsStateWithLifecycle().value)

    val currentChannel by viewModel.currentChannel.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val isAdaptivePerformance by viewModel.isAdaptivePerformance.collectAsStateWithLifecycle()
    val playbackRetryTrigger by viewModel.playbackRetryTrigger.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sleepTimerRemaining by viewModel.sleepTimerMinutes.collectAsStateWithLifecycle()
    val isFirstLaunch by viewModel.isFirstLaunch.collectAsStateWithLifecycle()

    var isFullscreen by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("live") } // "live", "vod", "fav", "settings"
    var selectedMovie by remember { mutableStateOf<Movie?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Kids Mode pinning locked categories
    val lockedCategories = remember { mutableStateListOf("Adult", "18+", "Restricted") }
    var activePinPromptCategory by remember { mutableStateOf<String?>(null) }
    var pinInputValue by remember { mutableStateOf("") }
    var showPinError by remember { mutableStateOf(false) }

    // EQ Sound levels
    var isDialogueBoostActive by remember { mutableStateOf(true) }
    var isNightModeSoundLevelerActive by remember { mutableStateOf(false) }
    var speechEqFrequencyAmplificationState by remember { mutableStateOf(100f) }

    // Intercept hardware remote control/zapping
    val focusRequester = remember { FocusRequester() }

    BackHandler {
        if (isFullscreen) {
            isFullscreen = false
        } else if (selectedMovie != null) {
            selectedMovie = null
            // If VOD demo playing, restore last live channel!
            viewModel.retryCurrentChannel()
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

    // Kids Mode Prompt Dialog
    if (activePinPromptCategory != null) {
        Dialog(onDismissRequest = { activePinPromptCategory = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Kids Lock PIN Pad",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "KIDS MODE LOCKED",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter 4-Digit PIN to access $activePinPromptCategory",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = pinInputValue.padEnd(4, '•'),
                        fontSize = 24.sp,
                        letterSpacing = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3x4 Pin Pad Layout
                    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "X", "0", "OK")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (r in 0 until 4) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                for (c in 0 until 3) {
                                    val k = keys[r * 3 + c]
                                    Button(
                                        onClick = {
                                            if (k == "X") {
                                                if (pinInputValue.isNotEmpty()) pinInputValue = pinInputValue.dropLast(1)
                                            } else if (k == "OK") {
                                                if (pinInputValue == viewModel.kidsModePin.value) {
                                                    val cat = activePinPromptCategory
                                                    if (cat != null) {
                                                        viewModel.selectCategory(cat)
                                                    }
                                                    activePinPromptCategory = null
                                                    pinInputValue = ""
                                                    showPinError = false
                                                } else {
                                                    showPinError = true
                                                    pinInputValue = ""
                                                }
                                            } else {
                                                if (pinInputValue.length < 4) pinInputValue += k
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier.size(52.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(k, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }

                    if (showPinError) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Incorrect PIN! Try default '1234'", color = Color.Red, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { activePinPromptCategory = null; pinInputValue = "" }) {
                        Text(strings.cancel)
                    }
                }
            }
        }
    }

    // Intercept keyboard numeric keys on parent box
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
        LaunchedEffect(Unit) {
            try {
                focusRequester.requestFocus()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (isFullscreen) {
            // Fullscreen video container
            Box(modifier = Modifier.fillMaxSize()) {
                KivuPlayer(
                    channelUrl = if (selectedMovie != null) selectedMovie?.streamUrl else currentChannel?.url,
                    playbackState = playbackState,
                    isAdaptivePerformance = isAdaptivePerformance,
                    onStateStarted = { viewModel.onPlaybackStarted() },
                    onStateBuffering = { viewModel.onPlaybackBuffering() },
                    onStateFailed = { viewModel.onPlaybackFailed() },
                    modifier = Modifier.fillMaxSize(),
                    retryTrigger = playbackRetryTrigger,
                    channelUserAgent = if (selectedMovie != null) null else currentChannel?.userAgent,
                    viewModel = viewModel
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
            // Adaptive Grid (Phone: Vertical arrangement. Table/TV: Side-by-Side arrangement)
            val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // 1. Premium Header Component
                KivuPremiumHeader(
                    viewModel = viewModel,
                    strings = strings,
                    searchQuery = searchQuery,
                    onThemeToggle = {
                        val next = if (viewModel.currentTheme.value == "Premium White") "Amoled Slate" else "Premium White"
                        viewModel.changeTheme(next)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Split Screen Area
                val splitRatio = if (isLandscape || deviceType == DeviceType.TABLET || deviceType == DeviceType.TV) 0.40f else 1.0f
                
                if (isLandscape || deviceType == DeviceType.TABLET || deviceType == DeviceType.TV) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Left Splitted Pane (Channel selection / Tabs)
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f - splitRatio)
                        ) {
                            when (selectedTab) {
                                "live" -> LiveTvTabScreen(
                                    viewModel = viewModel,
                                    strings = strings,
                                    selectedCategory = selectedCategory,
                                    searchQuery = searchQuery,
                                    currentChannel = currentChannel,
                                    lockedCategories = lockedCategories,
                                    onCategorySelected = { cat ->
                                        if (lockedCategories.contains(cat)) {
                                            activePinPromptCategory = cat
                                        } else {
                                            viewModel.selectCategory(cat)
                                        }
                                    }
                                )
                                "vod" -> VodMoviesTabScreen(
                                    movies = preLoadedMovies,
                                    selectedMovie = selectedMovie,
                                    onMovieClick = { m ->
                                        selectedMovie = m
                                        viewModel.playMovie(VodMovie(id = m.title, title = m.title, coverUrl = m.imageUrl, category = "VOD", streamUrl = m.streamUrl, duration = m.duration, year = "2024", rating = m.rating, description = m.description))
                                    }
                                )
                                "fav" -> FavoritesTabScreen(
                                    viewModel = viewModel,
                                    strings = strings,
                                    currentChannel = currentChannel
                                )
                                "settings" -> SettingsTabScreen(
                                    viewModel = viewModel,
                                    strings = strings,
                                    lockedCategories = lockedCategories,
                                    isDialogueBoostActive = isDialogueBoostActive,
                                    onDialogueBoostChange = { isDialogueBoostActive = it },
                                    isNightModeSoundLevelerActive = isNightModeSoundLevelerActive,
                                    onNightModeSoundLevelerChange = { isNightModeSoundLevelerActive = it },
                                    speechEqFrequencyAmplificationState = speechEqFrequencyAmplificationState,
                                    onSpeechEqFrequencyChange = { speechEqFrequencyAmplificationState = it }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Right Splitted Pane (Video Player + Program detailed card)
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(splitRatio)
                        ) {
                            KivuTheatricalPlayerBox(
                                viewModel = viewModel,
                                currentChannel = currentChannel,
                                playbackState = playbackState,
                                isAdaptivePerformance = isAdaptivePerformance,
                                playbackRetryTrigger = playbackRetryTrigger,
                                sleepTimerRemaining = sleepTimerRemaining,
                                strings = strings,
                                onFullscreenRequest = { isFullscreen = true }
                            )
                        }
                    }
                } else {
                    // Mobile Portrait Mode (Player always at the top, Tabs contents directly below it)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        KivuTheatricalPlayerBox(
                            viewModel = viewModel,
                            currentChannel = currentChannel,
                            playbackState = playbackState,
                            isAdaptivePerformance = isAdaptivePerformance,
                            playbackRetryTrigger = playbackRetryTrigger,
                            sleepTimerRemaining = sleepTimerRemaining,
                            strings = strings,
                            onFullscreenRequest = { isFullscreen = true }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            when (selectedTab) {
                                "live" -> LiveTvTabScreen(
                                    viewModel = viewModel,
                                    strings = strings,
                                    selectedCategory = selectedCategory,
                                    searchQuery = searchQuery,
                                    currentChannel = currentChannel,
                                    lockedCategories = lockedCategories,
                                    onCategorySelected = { cat ->
                                        if (lockedCategories.contains(cat)) {
                                            activePinPromptCategory = cat
                                        } else {
                                            viewModel.selectCategory(cat)
                                        }
                                    }
                                )
                                "vod" -> VodMoviesTabScreen(
                                    movies = preLoadedMovies,
                                    selectedMovie = selectedMovie,
                                    onMovieClick = { m ->
                                        selectedMovie = m
                                        viewModel.playMovie(VodMovie(id = m.title, title = m.title, coverUrl = m.imageUrl, category = "VOD", streamUrl = m.streamUrl, duration = m.duration, year = "2024", rating = m.rating, description = m.description))
                                    }
                                )
                                "fav" -> FavoritesTabScreen(
                                    viewModel = viewModel,
                                    strings = strings,
                                    currentChannel = currentChannel
                                )
                                "settings" -> SettingsTabScreen(
                                    viewModel = viewModel,
                                    strings = strings,
                                    lockedCategories = lockedCategories,
                                    isDialogueBoostActive = isDialogueBoostActive,
                                    onDialogueBoostChange = { isDialogueBoostActive = it },
                                    isNightModeSoundLevelerActive = isNightModeSoundLevelerActive,
                                    onNightModeSoundLevelerChange = { isNightModeSoundLevelerActive = it },
                                    speechEqFrequencyAmplificationState = speechEqFrequencyAmplificationState,
                                    onSpeechEqFrequencyChange = { speechEqFrequencyAmplificationState = it }
                                )
                            }
                        }
                    }
                }

                // 2. High-End Footer Navigation Dock
                KivuPremiumFooterDock(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    strings = strings
                )
            }
        }

        if (isFirstLaunch) {
            var currentTutorialStep by remember { mutableStateOf(1) }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(enabled = true, onClick = {}, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "KivuTv V1.2 " + strings.aboutTitle,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = "$currentTutorialStep / 3",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        val stepText = when (currentTutorialStep) {
                            1 -> "💡 **Header & Controls**\n\nBranding, user profile, active latency and clock are on the top header. Toggle bright daylight theme or AMOLED midnight contrast theme anytime easily!"
                            2 -> "⭐ **Favorites (Sevimliler)**\n\nStarred / Favorited channels will automatically populate inside the dedicated 'Favorites (Seçilmişlər)' section for instant mouse-free access."
                            3 -> "🔄 **Smart Live Sync**\n\nWe pull remote M3U streams in real-time. New channels populate instantly with smart delta syndication - no-reload required!"
                            else -> ""
                        }

                        Text(
                            text = stepText,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { viewModel.dismissOnboarding() },
                                modifier = Modifier.testTag("skip_tutorial_button")
                            ) {
                                Text(text = "Keç (Skip)", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (currentTutorialStep < 3) {
                                        currentTutorialStep++
                                    } else {
                                        viewModel.dismissOnboarding()
                                    }
                                },
                                modifier = Modifier.testTag("next_tutorial_button")
                            ) {
                                Text(text = if (currentTutorialStep == 3) "Bitir" else "Növbəti >")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KivuPremiumHeader(
    viewModel: KivuViewModel,
    strings: LanguageStrings,
    searchQuery: String,
    onThemeToggle: () -> Unit
) {
    // Ticking Live Clock Logic
    var timeString by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val sdfTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        val sdfDate = java.text.SimpleDateFormat("EEE, dd MMM", java.util.Locale.getDefault())
        while (true) {
            val cal = java.util.Calendar.getInstance()
            timeString = sdfTime.format(cal.time)
            dateString = sdfDate.format(cal.time)
            delay(1000)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Branding & Wifi diagnostic Speed meter
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Letter avatar User Profile
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Column {
                Text(
                    text = "KivuTv 1.2",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Wifi,
                        contentDescription = "Gigabit Speed",
                        tint = Color.Green,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "142 Mbps [Ultra]",
                        color = Color.Green,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Center search textbox
        Box(
            modifier = Modifier
                .widthIn(max = 240.dp)
                .height(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search icon",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (searchQuery.isEmpty()) {
                Text(
                    text = strings.searchPlaceholder,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 22.dp)
                )
            }
        }

        // Right side: Localized ticking Clock & Theme Toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = timeString,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = dateString,
                    color = Color.Gray,
                    fontSize = 9.sp
                )
            }

            IconButton(
                onClick = onThemeToggle,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.BrightnessMedium,
                    contentDescription = "Toggle Bright theme",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun KivuTheatricalPlayerBox(
    viewModel: KivuViewModel,
    currentChannel: ChannelEntity?,
    playbackState: PlaybackState,
    isAdaptivePerformance: Boolean,
    playbackRetryTrigger: Int,
    sleepTimerRemaining: Int?,
    strings: LanguageStrings,
    onFullscreenRequest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
    ) {
        KivuPlayer(
            channelUrl = currentChannel?.url,
            playbackState = playbackState,
            isAdaptivePerformance = isAdaptivePerformance,
            onStateStarted = { viewModel.onPlaybackStarted() },
            onStateBuffering = { viewModel.onPlaybackBuffering() },
            onStateFailed = { viewModel.onPlaybackFailed() },
            modifier = Modifier.fillMaxSize(),
            retryTrigger = playbackRetryTrigger,
            channelUserAgent = currentChannel?.userAgent,
            viewModel = viewModel
        )

        PlayerControlsOverlay(
            channel = currentChannel,
            playbackState = playbackState,
            isFullscreen = false,
            strings = strings,
            isAdaptive = isAdaptivePerformance,
            sleepTimerMinutes = sleepTimerRemaining,
            onToggleFullscreen = onFullscreenRequest,
            onPrevChannel = { viewModel.playPreviousChannel() },
            onNextChannel = { viewModel.playNextChannel() },
            onFavoriteToggle = { currentChannel?.let { viewModel.toggleFavorite(it) } },
            isFav = currentChannel?.let { viewModel.isChannelFavorite(it.url).collectAsState(initial = false).value } ?: false,
            onRetry = { viewModel.retryCurrentChannel() }
        )
    }

    Spacer(modifier = Modifier.height(6.dp))

    // EPG Details Card directly beneath the player
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = currentChannel?.name ?: strings.appName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            
            // Channel program / EPG
            val activeProg = currentChannel?.currentProgram ?: "Prime Time Evening Show [Live]"
            Text(
                text = activeProg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Simulated live timeline countdown indicator
            LinearProgressIndicator(
                progress = 0.45f,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.dp))
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("19:00", fontSize = 9.sp, color = Color.Gray)
                Text("-2.4s live latency [Perfect Sync]", fontSize = 9.sp, color = Color.Green, fontWeight = FontWeight.Bold)
                Text("20:00", fontSize = 9.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun LiveTvTabScreen(
    viewModel: KivuViewModel,
    strings: LanguageStrings,
    selectedCategory: String,
    searchQuery: String,
    currentChannel: ChannelEntity?,
    lockedCategories: List<String>,
    onCategorySelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal category row
        Box(modifier = Modifier.fillMaxWidth()) {
            val categories = viewModel.getAllCategories()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (cat in categories) {
                    val isLocked = lockedCategories.contains(cat)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (selectedCategory == cat) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            )
                            .clickable { onCategorySelected(cat) }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isLocked) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Locked category",
                                    tint = if (selectedCategory == cat) Color.Black else Color.Gray,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                            Text(
                                text = cat,
                                color = if (selectedCategory == cat) Color.Black else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Channels Scroll list
        val filteredList = viewModel.getFilteredChannels()
        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No matching channels in this folder", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(filteredList) { index, ch ->
                    val isFavorite = viewModel.isChannelFavorite(ch.url).collectAsState(initial = false).value
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectChannel(ch) },
                        border = if (currentChannel?.url == ch.url) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentChannel?.url == ch.url) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(0.8f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Channel Number badge
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Column {
                                    Text(
                                        text = ch.name,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    
                                    // Simulated Live Program Timeline
                                    Text(
                                        text = ch.currentProgram ?: "Live HD Cable Stream",
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.toggleFavorite(ch) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Favorite Toggle",
                                    tint = if (isFavorite) Color.Red else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VodMoviesTabScreen(
    movies: List<Movie>,
    selectedMovie: Movie?,
    onMovieClick: (Movie) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "CINEMA VOD HD LIBRARY",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(movies) { m ->
                val isCurrent = selectedMovie?.title == m.title
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clickable { onMovieClick(m) },
                    border = if (isCurrent) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text("⭐ ${m.rating}", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(text = m.duration, fontSize = 9.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = m.title,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = m.genre,
                                color = Color.Gray,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }

                        Text(
                            text = m.description,
                            color = Color.Gray.copy(alpha = 0.8f),
                            fontSize = 8.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesTabScreen(
    viewModel: KivuViewModel,
    strings: LanguageStrings,
    currentChannel: ChannelEntity?
) {
    val favoritesList by remember(viewModel) {
        viewModel.favorites.map { list ->
            list.map { fav ->
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
        }
    }.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "SEVİMLİLƏR (FAVORITES)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp),
            letterSpacing = 1.sp
        )

        if (favoritesList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = "No Favorites",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sevimlilər siyahısı boşdur.\nKanalları bura əlavə etmək üçün 'Ürək' ikonuna toxunun.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(160.dp),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(favoritesList) { ch ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectChannel(ch) }
                            .testTag("favorite_item_${ch.name.replace(" ", "_")}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentChannel?.url == ch.url) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (currentChannel?.url == ch.url) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!ch.logo.isNullOrEmpty()) {
                                    coil.compose.AsyncImage(
                                        model = ch.logo,
                                        contentDescription = ch.name,
                                        modifier = Modifier.size(36.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Tv,
                                        contentDescription = "Channel logo placeholder",
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = ch.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = ch.groupTitle,
                                fontSize = 9.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = { viewModel.toggleFavorite(ch) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Favorite,
                                        contentDescription = "Remove favorite",
                                        tint = Color.Red,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsTabScreen(
    viewModel: KivuViewModel,
    strings: LanguageStrings,
    lockedCategories: List<String>,
    isDialogueBoostActive: Boolean,
    onDialogueBoostChange: (Boolean) -> Unit,
    isNightModeSoundLevelerActive: Boolean,
    onNightModeSoundLevelerChange: (Boolean) -> Unit,
    speechEqFrequencyAmplificationState: Float,
    onSpeechEqFrequencyChange: (Float) -> Unit
) {
    val scrollState = rememberScrollState()
    val isAutoPlayLastChannel by viewModel.autoPlayLastChannelEnabled.collectAsStateWithLifecycle()
    val isAdaptivePerformance by viewModel.isAdaptivePerformance.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // System Hardening Section
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "AUTO-BOOT SETTING",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(0.8f)) {
                        Text("Auto-Start Last Channel", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Instantly plays your last active channel on startup.", fontSize = 10.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isAutoPlayLastChannel,
                        onCheckedChange = { viewModel.toggleAutoPlayLastChannel() }
                    )
                }
            }
        }

        // Kids Mode Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Lock, contentDescription = "Security", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Text(
                        text = "KIDS MODE GATEWAYS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Default Access PIN: '1234'. Categories filtered: ${lockedCategories.joinToString()}", fontSize = 11.sp, color = Color.Gray)
            }
        }

        // Dialogue Equalizer Tuning Slider
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "DOLBY EQ SIGNAL MATCHING",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dialogue Voice Boost", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Switch(checked = isDialogueBoostActive, onCheckedChange = onDialogueBoostChange)
                }
                if (isDialogueBoostActive) {
                    Text("Speech Frequency Parameter: ${speechEqFrequencyAmplificationState.toInt()} Hz", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    Slider(
                        value = speechEqFrequencyAmplificationState,
                        onValueChange = onSpeechEqFrequencyChange,
                        valueRange = 50f .. 400f
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Night Mode Peak Limiter", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Switch(checked = isNightModeSoundLevelerActive, onCheckedChange = onNightModeSoundLevelerChange)
                }
            }
        }

        // AI Diagnostics Panel
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "AI AUTO-OPTIMIZATION ENGINE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Diagnostic Match", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (isAdaptivePerformance) "Lite Mode (Conserve CPU)" else "Ultra Mode (Aggressive Buffers)",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Detected Hardware Stats: 8 Core CPU Host, 4.09 GB Virtual Memory Allocated", fontSize = 10.sp, color = Color.Gray)
            }
        }

        // Language choosing
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Select Language", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("az" to "AZ", "tr" to "TR", "en" to "EN", "ru" to "RU").forEach { item ->
                        val isSel = viewModel.currentLanguage.collectAsStateWithLifecycle().value == item.first
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                .clickable { viewModel.changeLanguage(item.first) }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(item.second, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KivuPremiumFooterDock(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    strings: LanguageStrings
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            Triple("live", Icons.Filled.Tv, "Live TV"),
            Triple("vod", Icons.Filled.Movie, "VOD Movies"),
            Triple("fav", Icons.Filled.Favorite, strings.favorites),
            Triple("settings", Icons.Filled.Settings, strings.settings)
        ).forEach { tab ->
            val isCurrent = selectedTab == tab.first
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTabSelected(tab.first) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.second,
                        contentDescription = tab.third,
                        tint = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.third,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PhoneView(viewModel: KivuViewModel) {
    // Legacy mapping fallback - delegates directly to unified dashboard!
    KivuMainDashboard(viewModel, DeviceType.PHONE)
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
                    retryTrigger = playbackRetryTrigger,
                    channelUserAgent = currentChannel?.userAgent,
                    viewModel = viewModel
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
                            retryTrigger = playbackRetryTrigger,
                            channelUserAgent = currentChannel?.userAgent,
                            viewModel = viewModel
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
                retryTrigger = playbackRetryTrigger,
                channelUserAgent = currentChannel?.userAgent,
                viewModel = viewModel
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
                    retryTrigger = playbackRetryTrigger,
                    channelUserAgent = currentChannel?.userAgent,
                    viewModel = viewModel
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
                        retryTrigger = playbackRetryTrigger,
                        channelUserAgent = currentChannel?.userAgent,
                        viewModel = viewModel
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
            item {
                CategoryPill(
                    name = strings.favorites,
                    isSelected = selectedCategory == "__favorites__",
                    onClick = { onCategorySelected("__favorites__") }
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
