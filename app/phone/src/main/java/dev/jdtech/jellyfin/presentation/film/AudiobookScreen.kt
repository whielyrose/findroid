package dev.jdtech.jellyfin.presentation.film

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.jdtech.jellyfin.PlayerActivity
import dev.jdtech.jellyfin.core.R as CoreR
import dev.jdtech.jellyfin.film.presentation.audiobook.AudiobookAction
import dev.jdtech.jellyfin.film.presentation.audiobook.AudiobookState
import dev.jdtech.jellyfin.film.presentation.audiobook.AudiobookViewModel
import dev.jdtech.jellyfin.presentation.film.components.ItemHeader
import dev.jdtech.jellyfin.presentation.film.components.ItemTopBar
import dev.jdtech.jellyfin.presentation.film.components.OverviewText
import dev.jdtech.jellyfin.presentation.theme.spacings
import dev.jdtech.jellyfin.presentation.utils.rememberSafePadding
import java.util.UUID
import org.jellyfin.sdk.model.api.BaseItemKind

@Composable
fun AudiobookScreen(
    audiobookId: UUID,
    navigateBack: () -> Unit,
    navigateHome: () -> Unit,
    viewModel: AudiobookViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(true) { viewModel.loadAudiobook(audiobookId = audiobookId) }

    AudiobookScreenLayout(
        state = state,
        onAction = { action ->
            when (action) {
                is AudiobookAction.Play -> {
                    val intent = Intent(context, PlayerActivity::class.java)
                    intent.putExtra("itemId", audiobookId.toString())
                    intent.putExtra("itemKind", BaseItemKind.AUDIO_BOOK.serialName)
                    intent.putExtra("startFromBeginning", false)
                    context.startActivity(intent)
                }
                is AudiobookAction.OnBackClick -> navigateBack()
                is AudiobookAction.OnHomeClick -> navigateHome()
            }
        },
    )
}

@Composable
private fun AudiobookScreenLayout(
    state: AudiobookState,
    onAction: (AudiobookAction) -> Unit,
) {
    val safePadding = rememberSafePadding()
    val paddingStart = safePadding.start + MaterialTheme.spacings.default
    val paddingEnd = safePadding.end + MaterialTheme.spacings.default
    val paddingBottom = safePadding.bottom + MaterialTheme.spacings.default

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        state.audiobook?.let { audiobook ->
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)) {
                ItemHeader(
                    item = audiobook,
                    scrollState = scrollState,
                    content = {
                        Column(
                            modifier =
                                Modifier.align(Alignment.BottomStart)
                                    .padding(start = paddingStart, end = paddingEnd)
                        ) {
                            Text(
                                text = audiobook.name,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 3,
                                style = MaterialTheme.typography.headlineMedium,
                            )
                        }
                    },
                )
                Column(modifier = Modifier.padding(start = paddingStart, end = paddingEnd)) {
                    Spacer(Modifier.height(MaterialTheme.spacings.small))

                    // Author line
                    if (state.authors.isNotEmpty()) {
                        Text(
                            text = state.authors.joinToString(", ") { it.name },
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    // Narrator line
                    if (state.narrators.isNotEmpty()) {
                        Text(
                            text = "Narrated by " + state.narrators.joinToString(", ") { it.name },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    Spacer(Modifier.height(MaterialTheme.spacings.small))

                    // Play / Resume button
                    Button(
                        onClick = { onAction(AudiobookAction.Play) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            painter = painterResource(CoreR.drawable.ic_play),
                            contentDescription = null,
                        )
                        Spacer(Modifier.width(MaterialTheme.spacings.small))
                        Text(
                            text =
                                if (audiobook.playbackPositionTicks > 0) "Resume" else "Play"
                        )
                    }

                    Spacer(Modifier.height(MaterialTheme.spacings.medium))

                    if (audiobook.overview.isNotBlank()) {
                        OverviewText(text = audiobook.overview, maxCollapsedLines = 3)
                        Spacer(Modifier.height(MaterialTheme.spacings.medium))
                    }

                    // Chapter list (album-style track list)
                    if (audiobook.chapters.isNotEmpty()) {
                        Text(
                            text = "Chapters",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.height(MaterialTheme.spacings.small))
                        audiobook.chapters.forEachIndexed { index, chapter ->
                            ChapterRow(
                                number = index + 1,
                                title = chapter.name ?: "Chapter ${index + 1}",
                                onClick = { onAction(AudiobookAction.Play) },
                            )
                            HorizontalDivider()
                        }
                    }

                    Spacer(Modifier.height(paddingBottom))
                }
            }
        } ?: run { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) }

        ItemTopBar(
            hasBackButton = true,
            hasHomeButton = true,
            onBackClick = { onAction(AudiobookAction.OnBackClick) },
            onHomeClick = { onAction(AudiobookAction.OnHomeClick) },
        )
    }
}

@Composable
private fun ChapterRow(
    number: Int,
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = MaterialTheme.spacings.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacings.default),
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(MaterialTheme.spacings.medium),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
