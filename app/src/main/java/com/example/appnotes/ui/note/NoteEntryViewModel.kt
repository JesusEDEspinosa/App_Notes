package com.example.appnotes.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appnotes.data.Attachment
import com.example.appnotes.data.Note
import com.example.appnotes.data.NotesRepository
import com.example.appnotes.data.Reminder
import com.example.appnotes.notification.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteEntryViewModel(
    private val notesRepository: NotesRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {
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
                        createdAt = note.createdAt,
                        reminders = safeNoteWithDetais.reminders
                    )
                    isEditMode = true
                }
            }
        }
    }

    fun updateUiState(newState: NoteUiState) {
        _noteUiState.value = newState
    }
    
    fun addReminder(remindAt: Long) {
        val currentReminders = _noteUiState.value.reminders.toMutableList()
        currentReminders.add(Reminder(noteId = 0, remindAt = remindAt))
        _noteUiState.value = _noteUiState.value.copy(reminders = currentReminders)
    }

    fun removeReminder(reminder: Reminder) {
        val currentReminders = _noteUiState.value.reminders.toMutableList()
        currentReminders.remove(reminder)
        _noteUiState.value = _noteUiState.value.copy(reminders = currentReminders)
        
        if (reminder.id != 0) {
             viewModelScope.launch {
                 // Como la UI no se actualiza instantáneamente desde BD en este flujo,
                 // necesitamos borrarlo "virtualmente" hasta guardar, pero
                 // como ya lo quitamos de la lista en memoria, si el usuario guarda,
                 // la lógica de "borrar todos y reinsertar" funcionará.
                 // Sin embargo, si quiere cancelar la alarma YA:
                 alarmScheduler.cancel(reminder)
             }
        }
    }

    fun saveNote(attachments: List<Attachment> = emptyList()) {
        viewModelScope.launch {
            val noteUi = noteUiState.value
            val note = Note(
                id = noteUi.id,
                title = noteUi.title,
                description = noteUi.description,
                isTask = noteUi.isTask,
                dueDateTime = noteUi.dueDateTime,
                isCompleted = noteUi.completed,
                createdAt = noteUi.createdAt
            )
            
            val noteId: Int
            
            if (isEditMode) {
                notesRepository.updateNote(note)
                noteId = note.id
                
                // Limpiar recordatorios antiguos para evitar duplicados o inconsistencias
                notesRepository.deleteRemindersByNoteId(noteId)
                alarmScheduler.cancel(note)
            } else {
                val newId = notesRepository.insertNote(note)
                noteId = newId.toInt()
            }
            
            // Guardar alarma principal (dueDateTime)
             if (note.dueDateTime != null) {
                 alarmScheduler.schedule(note)
             }

            // Guardar y programar recordatorios adicionales
            noteUi.reminders.forEach { reminder ->
                val newReminder = reminder.copy(noteId = noteId, id = 0) // Reset ID para autogenerar
                val generatedId = notesRepository.addReminder(newReminder)
                
                // Programar la alarma usando el ID real generado
                val savedReminder = newReminder.copy(id = generatedId.toInt())
                alarmScheduler.schedule(savedReminder, note.title)
            }

            attachments.forEach { att ->
                notesRepository.addAttachment(att.copy(noteId = noteId))
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
    val reminders: List<Reminder> = emptyList()
)
