package dev.jdtech.jellyfin.film.presentation.audiobook

import dev.jdtech.jellyfin.models.FindroidAudiobook
import dev.jdtech.jellyfin.models.FindroidItemPerson

data class AudiobookState(
    val audiobook: FindroidAudiobook? = null,
    val authors: List<FindroidItemPerson> = emptyList(),
    val narrators: List<FindroidItemPerson> = emptyList(),
    val error: Exception? = null,
)
