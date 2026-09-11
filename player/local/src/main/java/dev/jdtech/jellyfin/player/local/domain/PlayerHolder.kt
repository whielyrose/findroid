package dev.jdtech.jellyfin.player.local.domain

import androidx.media3.common.Player
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A process-wide holder that lets the audiobook [MediaSessionService] reference the
 * exact same [Player] instance created and owned by the PlayerViewModel, without
 * moving player ownership out of the ViewModel.
 *
 * The ViewModel registers its player on creation and clears it on release. The
 * service reads [player] to build its MediaSession. This is intentionally a plain
 * reference container — it does not create, configure, or release the player.
 *
 * Only used for audiobook playback; video playback never touches this.
 */
@Singleton
class PlayerHolder @Inject constructor() {
    @Volatile
    var player: Player? = null
        private set

    fun register(player: Player) {
        this.player = player
    }

    fun clear() {
        this.player = null
    }
}
