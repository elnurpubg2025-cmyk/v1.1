package com.example.ui

import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun KivuPlayer(
    channelUrl: String?,
    playbackState: PlaybackState,
    isAdaptivePerformance: Boolean,
    onStateStarted: () -> Unit,
    onStateBuffering: () -> Unit,
    onStateFailed: () -> Unit,
    modifier: Modifier = Modifier,
    retryTrigger: Int = 0
) {
    val context = LocalContext.current
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    // Re-create or adjust player when adaptive condition or context shifts
    LaunchedEffect(isAdaptivePerformance) {
        // Build LoadControl to optimize buffering depending on hardware performance
        val loadControlBuilder = DefaultLoadControl.Builder()
        if (isAdaptivePerformance) {
            // Low RAM / Old box optimization: lower buffer allocation to conserve memory and start extremely fast
            loadControlBuilder.setBufferDurationsMs(
                10_000, // minBufferMs
                20_000, // maxBufferMs
                1_500,  // bufferForPlaybackMs
                2_500   // bufferForPlaybackAfterRebufferMs
            )
        } else {
            // High-end buffers: aggressive preloading similar to YouTube to avoid freezing on micro-drops
            loadControlBuilder.setBufferDurationsMs(
                20_000, // minBufferMs
                45_000, // maxBufferMs
                2_500,  // bufferForPlaybackMs
                5_000   // bufferForPlaybackAfterRebufferMs
            )
        }

        // Build a robust HTTP Data Source Factory to avoid stream failures due to user-agent restriction and redirect errors
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
        
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)

        val player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControlBuilder.build())
            .build()
            .apply {
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_OFF
            }

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> {
                        onStateBuffering()
                    }
                    Player.STATE_READY -> {
                        onStateStarted()
                    }
                    Player.STATE_ENDED -> {
                        // Restart or notify
                    }
                    Player.STATE_IDLE -> {
                        // Idle
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("KivuPlayer", "ExoPlayer exception encountered", error)
                onStateFailed()
            }
        })

        exoPlayer?.release()
        exoPlayer = player
    }

    // Load stream URL when it shifts or when retry is triggered
    LaunchedEffect(channelUrl, exoPlayer, retryTrigger) {
        val player = exoPlayer ?: return@LaunchedEffect
        if (!channelUrl.isNullOrEmpty()) {
            try {
                player.stop()
                player.clearMediaItems()
                
                val parsedUri = Uri.parse(channelUrl)
                val mediaItemBuilder = MediaItem.Builder().setUri(parsedUri)
                
                // Explicitly tell ExoPlayer this is an HLS (.m3u8) stream if the URL suggests it,
                // which bypasses failures when parameters (e.g. ?app=web) hide the file extension.
                if (channelUrl.contains(".m3u8", ignoreCase = true) || channelUrl.contains("m3u8", ignoreCase = true)) {
                    mediaItemBuilder.setMimeType(androidx.media3.common.MimeTypes.APPLICATION_M3U8)
                }
                
                player.setMediaItem(mediaItemBuilder.build())
                player.prepare()
                player.play()
            } catch (e: Exception) {
                Log.e("KivuPlayer", "Error loading media uri: $channelUrl", e)
                onStateFailed()
            }
        } else {
            player.stop()
            player.clearMediaItems()
        }
    }

    // Release player on disposal
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer?.let {
                it.stop()
                it.release()
            }
            exoPlayer = null
        }
    }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (channelUrl.isNullOrEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Select a Channel",
                    color = Color.Gray,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
            // Render Android video container
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false // We render custom polished Compose buttons on top
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { view ->
                    view.player = exoPlayer
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
