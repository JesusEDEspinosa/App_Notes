package com.example.appnotes.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appnotes.data.NoteWithDetails
import com.example.appnotes.data.NotesRepository
import com.example.appnotes.notification.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteDetailsViewModel (
    private val notesRepository: NotesRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {
    private val _noteUiState = MutableStateFlow<NoteWithDetails?>(null)
    val noteUiState: StateFlow<NoteWithDetails?> = _noteUiState.asStateFlow()

    fun loadNote(noteId: Int) {
            viewModelScope.launch {
                notesRepository.getNote(noteId).collect { result ->
                    _noteUiState.value = result
                }
            }
    }

    fun deleteNote(onDeleted: () -> Unit) {
        _noteUiState.value?.note?.let { note ->
            viewModelScope.launch {
                alarmScheduler.cancel(note)
                notesRepository.deleteNote(note)
                _noteUiState.value = null
                onDeleted()
            }
        }
    }

    fun toggleCompleted() {
        _noteUiState.value?.note?.let { note ->
            val updated = note.copy(isCompleted = !note.isCompleted)
            viewModelScope.launch {
                notesRepository.updateNote(updated)
                alarmScheduler.schedule(updated)
            }
            _noteUiState.value = _noteUiState.value?.copy(note = updated)
        }
    }

}