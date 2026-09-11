package dev.jdtech.jellyfin.film.presentation.audiobook

sealed interface AudiobookAction {
    /** Play from the resume position (or start if none). */
    data object Play : AudiobookAction

    data object OnBackClick : AudiobookAction

    data object OnHomeClick : AudiobookAction
}
