package com.example.appnotes.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appnotes.data.Attachment
import com.example.appnotes.data.Note
import com.example.appnotes.data.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteEntryViewModel(private val notesRepository: NotesRepository) : ViewModel() {
    private val _noteUiState = MutableStateFlow(NoteUiState())
    val noteUiState: StateFlow<NoteUiState> = _noteUiState.asStateFlow()

    private var isEditMode = false

    fun loadNote(noteId: Int) {
        viewModelScope.launch {
            notesRepository.getNote(noteId).collect { noteWithDetails ->
                noteWithDetails?.let { safeNoteWithDetais ->
                    val note = noteWithDetails.note
                    _noteUiState.value = NoteUiState(
                        id = note.id,
                        title = note.title,
                        description = note.description,
                        isTask = note.isTask,
                        dueDateTime = note.dueDateTime,
                        completed = note.isCompleted,
                        createdAt = note.createdAt
                    )
                    isEditMode = true
                }
            }
        }
    }

    fun updateUiState(newState: NoteUiState) {
        _noteUiState.value = newState
    }

    fun saveNote(attachments: List<Attachment> = emptyList()) {
        viewModelScope.launch {
            val note = Note(
                id = noteUiState.value.id,
                title = noteUiState.value.title,
                description = noteUiState.value.description,
                isTask = noteUiState.value.isTask,
                dueDateTime = noteUiState.value.dueDateTime,
                isCompleted = noteUiState.value.completed,
                createdAt = noteUiState.value.createdAt
            )
            val noteId = if (isEditMode) {
                notesRepository.updateNote(note)
                note.id.toLong()
            } else {
                notesRepository.insertNote(note)
            }

            attachments.forEach { att ->
                notesRepository.addAttachment(att.copy(noteId = noteId.toInt()))
            }
        }
    }

    fun isValidNote(): Boolean {
        val note = noteUiState.value
        return note.title.isNotBlank() && note.description.isNotBlank()
    }
}

data class NoteUiState(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val isTask: Boolean = false,
    val dueDateTime: Long? = null,
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)