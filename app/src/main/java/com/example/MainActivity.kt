package com.example

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.media.AudioManager
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.ui.KivuAppContent
import com.example.ui.KivuViewModel
import com.example.ui.PlaybackState

class MainActivity : ComponentActivity() {
    private val viewModel: KivuViewModel by viewModels()
    private val isPipMode = mutableStateOf(false)
    private var isExitingFromPipWithClose = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1.2 Water-Tight Lifecycle Observer to terminate audio ghost threads on close
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                // If stopped while in PiP OR we marked an exit-with-close, release audio immediately
                if (isPipMode.value || isExitingFromPipWithClose) {
                    cleanupMediaAndRelease()
                }
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                cleanupMediaAndRelease()
            }
        })

        setContent {
            KivuAppContent(
                viewModel = viewModel,
                isActivityPipMode = isPipMode.value
            )
        }
    }

    private fun cleanupMediaAndRelease() {
        viewModel.stopAndReleasePlayer()
        abandonAudioFocus()
    }

    private fun abandonAudioFocus() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.abandonAudioFocus(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Automatically trigger PTS (Pip Tv Screen) PiP mode if channel streaming is playing
        if (viewModel.currentChannel.value != null && viewModel.playbackState.value != PlaybackState.IDLE) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val params = PictureInPictureParams.Builder().build()
                    enterPictureInPictureMode(params)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isPipMode.value = isInPictureInPictureMode
        
        // Exiting PiP: Detect if restoring to full screen or closed via 'X'
        if (!isInPictureInPictureMode) {
            // If exiting PiP and the activity is not starting/resumed in full screen, it's 'X' closed
            val isRestoring = lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)
            if (!isRestoring) {
                isExitingFromPipWithClose = true
                cleanupMediaAndRelease()
            }
        }
    }
}

