package dev.jdtech.jellyfin.models

import dev.jdtech.jellyfin.database.ServerDatabaseDao
import dev.jdtech.jellyfin.repository.JellyfinRepository
import java.time.LocalDateTime
import java.util.UUID
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.PlayAccess

/**
 * Model for a Jellyfin AudioBook item.
 *
 * Mirrors [FindroidMovie] in structure. Audiobook-specific concerns are handled
 * through shared fields: [chapters] for chapter navigation (may be empty if the
 * source file has no embedded chapters), [playbackPositionTicks] for resume, and
 * [people] for author/narrator (Jellyfin maps author -> WRITER, narrator -> ACTOR
 * for audiobooks; the ViewModel filters these).
 */
data class FindroidAudiobook(
    override val id: UUID,
    override val name: String,
    override val originalTitle: String?,
    override val overview: String,
    override val sources: List<FindroidSource>,
    override val played: Boolean,
    override val favorite: Boolean,
    override val canPlay: Boolean,
    override val canDownload: Boolean,
    override val runtimeTicks: Long,
    override val playbackPositionTicks: Long,
    val people: List<FindroidItemPerson>,
    val premiereDate: LocalDateTime?,
    val genres: List<String>,
    val communityRating: Float?,
    val productionYear: Int?,
    override val unplayedItemCount: Int? = null,
    override val images: FindroidImages,
    override val chapters: List<FindroidChapter>,
) : FindroidItem, FindroidSources

suspend fun BaseItemDto.toFindroidAudiobook(
    jellyfinRepository: JellyfinRepository,
    serverDatabase: ServerDatabaseDao? = null,
): FindroidAudiobook {
    val sources = mutableListOf<FindroidSource>()
    sources.addAll(mediaSources?.map { it.toFindroidSource(jellyfinRepository, id) } ?: emptyList())
    if (serverDatabase != null) {
        sources.addAll(serverDatabase.getSources(id).map { it.toFindroidSource(serverDatabase) })
    }
    return FindroidAudiobook(
        id = id,
        name = name.orEmpty(),
        originalTitle = originalTitle,
        overview = overview.orEmpty(),
        sources = sources,
        played = userData?.played == true,
        favorite = userData?.isFavorite == true,
        canPlay = playAccess != PlayAccess.NONE,
        canDownload = canDownload == true,
        runtimeTicks = runTimeTicks ?: 0,
        playbackPositionTicks = userData?.playbackPositionTicks ?: 0,
        people = people?.map { it.toFindroidPerson(jellyfinRepository) } ?: emptyList(),
        premiereDate = premiereDate,
        genres = genres ?: emptyList(),
        communityRating = communityRating,
        productionYear = productionYear,
        images = toFindroidImages(jellyfinRepository),
        chapters = toFindroidChapters(),
    )
}
