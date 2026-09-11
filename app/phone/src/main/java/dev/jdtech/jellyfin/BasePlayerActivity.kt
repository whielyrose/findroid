package dev.jdtech.jellyfin

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.media3.session.MediaSession
import dev.jdtech.jellyfin.player.local.presentation.PlayerViewModel

abstract class BasePlayerActivity : AppCompatActivity() {

    abstract val viewModel: PlayerViewModel

    private var mediaSession: MediaSession? = null
    private var wasPip: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    override fun onStart() {
        super.onStart()

        // Audiobooks: the AudiobookPlaybackService owns the MediaSession (for the media
        // notification + background survival). The activity must NOT create its own
        // session for audiobooks, or two sessions would wrap the same player and conflict.
        // Video: create the activity-owned session exactly as before.
        if (!viewModel.isAudiobook && mediaSession == null) {
            mediaSession = MediaSession.Builder(this, viewModel.player).build()
        }
    }

    override fun onResume() {
        super.onResume()

        if (wasPip) {
            wasPip = false
        } else {
            viewModel.player.playWhenReady = viewModel.playWhenReady
        }
        hideSystemUI()
    }

    override fun onPause() {
        super.onPause()

        if (isInPictureInPictureMode) {
            wasPip = true
        } else if (viewModel.isAudiobook) {
            // Audiobooks keep playing in the background (screen off / pocket).
            // Just checkpoint progress; do not pause.
            viewModel.updatePlaybackProgress()
        } else {
            viewModel.playWhenReady = viewModel.player.playWhenReady
            viewModel.player.playWhenReady = false
            viewModel.updatePlaybackProgress()
        }
    }

    override fun onStop() {
        super.onStop()

        // Only video uses an activity-owned session; release it as before.
        // Audiobooks never create one here (the service owns theirs), so nothing to do.
        if (!viewModel.isAudiobook) {
            mediaSession?.release()
            mediaSession = null
        }

        if (wasPip) {
            finish()
        }
    }

    protected fun hideSystemUI() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }

        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }

    protected fun configureInsets(playerControls: View) {
        playerControls.setOnApplyWindowInsetsListener { _, windowInsets ->
            val cutout = windowInsets.displayCutout
            playerControls.updatePadding(
                left = cutout?.safeInsetLeft ?: 0,
                top = cutout?.safeInsetTop ?: 0,
                right = cutout?.safeInsetRight ?: 0,
                bottom = cutout?.safeInsetBottom ?: 0,
            )
            return@setOnApplyWindowInsetsListener windowInsets
        }
    }
}
