package dev.jdtech.jellyfin

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import dev.jdtech.jellyfin.player.local.domain.PlayerHolder
import javax.inject.Inject

/**
 * Foreground media service for audiobook playback.
 *
 * On Android (target SDK 36) the only sanctioned way to keep audio playing in the
 * background with lock-screen / notification controls is a [MediaSessionService]
 * declared with foregroundServiceType="mediaPlayback". Media3 automatically builds
 * and posts the media notification for the session this service returns.
 *
 * This service does NOT create or own the player. It attaches a MediaSession to the
 * existing player instance created by PlayerViewModel and shared via [PlayerHolder].
 * It is only ever started for audiobooks; video playback never starts this service
 * and is completely unaffected by it.
 */
@AndroidEntryPoint
class AudiobookPlaybackService : MediaSessionService() {

    @Inject lateinit var playerHolder: PlayerHolder

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = playerHolder.player
        if (player != null) {
            mediaSession = MediaSession.Builder(this, player).build()
        } else {
            // No player to attach to — nothing to play. Stop immediately so the OS does
            // not terminate us for failing to start in the foreground.
            stopSelf()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        // If the user swipes the app away and playback is paused (or no player),
        // stop the service so it doesn't linger. If still playing, let it continue.
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        // Release only the session wrapper, NOT the player — the player is owned by
        // the ViewModel and released there. Releasing it here would double-release.
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}
