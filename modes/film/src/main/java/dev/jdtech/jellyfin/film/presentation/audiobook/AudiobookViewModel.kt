package dev.jdtech.jellyfin.film.presentation.audiobook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jdtech.jellyfin.models.FindroidAudiobook
import dev.jdtech.jellyfin.models.FindroidItemPerson
import dev.jdtech.jellyfin.repository.JellyfinRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.model.api.PersonKind

@HiltViewModel
class AudiobookViewModel
@Inject
constructor(
    private val repository: JellyfinRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AudiobookState())
    val state = _state.asStateFlow()

    lateinit var audiobookId: UUID

    fun loadAudiobook(audiobookId: UUID) {
        this.audiobookId = audiobookId
        viewModelScope.launch {
            try {
                val audiobook = repository.getAudiobook(audiobookId)
                val authors = getAuthors(audiobook)
                val narrators = getNarrators(audiobook)
                _state.emit(
                    _state.value.copy(
                        audiobook = audiobook,
                        authors = authors,
                        narrators = narrators,
                    )
                )
            } catch (e: Exception) {
                _state.emit(_state.value.copy(error = e))
            }
        }
    }

    // Jellyfin maps audiobook authors to WRITER and narrators to ACTOR/READER.
    private suspend fun getAuthors(item: FindroidAudiobook): List<FindroidItemPerson> {
        return withContext(Dispatchers.Default) {
            item.people.filter { it.type == PersonKind.WRITER || it.type == PersonKind.AUTHOR }
        }
    }

    private suspend fun getNarrators(item: FindroidAudiobook): List<FindroidItemPerson> {
        return withContext(Dispatchers.Default) {
            item.people.filter { it.type == PersonKind.ACTOR }
        }
    }
}
