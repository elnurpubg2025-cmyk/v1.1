package com.example

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import com.example.ui.KivuAppContent
import com.example.ui.KivuViewModel
import com.example.ui.PlaybackState

class MainActivity : ComponentActivity() {
    private val viewModel: KivuViewModel by viewModels()
    private val isPipMode = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KivuAppContent(
                viewModel = viewModel,
                isActivityPipMode = isPipMode.value
            )
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
    }
}
