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
    retryTrigger: Int = 0,
    channelUserAgent: String? = null,
    viewModel: KivuViewModel
) {
    val context = LocalContext.current
    val player = remember(viewModel) { viewModel.getOrCreatePlayer() }

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
                    if (view.player != player) {
                        view.player = player
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
