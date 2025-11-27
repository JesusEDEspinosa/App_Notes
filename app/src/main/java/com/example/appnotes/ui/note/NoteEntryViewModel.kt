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
        // Evitar recargar la nota si ya estamos en modo edición con la misma nota
        // Esto previene que se sobrescriban los cambios no guardados (como adjuntos) al rotar la pantalla
        if (isEditMode && _noteUiState.value.id == noteId) return

        viewModelScope.launch {
            notesRepository.getNote(noteId).collect { noteWithDetails ->
                noteWithDetails?.let { safeNoteWithDetails ->
                    val note = safeNoteWithDetails.note
                    
                    // Solo actualizamos el estado desde la BD si NO estamos ya editando o si es la carga inicial
                    // Sin embargo, como collect se queda escuchando, debemos tener cuidado.
                    // Para una pantalla de edición, lo ideal es cargar los datos una vez.
                    // Pero aquí, la protección principal es el chequeo al inicio de la función.
                    // Si la corrutina sigue viva tras rotar, y la BD no cambia, no pasa nada.
                    // Si la BD cambia externamente, esto sobrescribiría los cambios del usuario.
                    // Para este caso de uso, asumimos que la BD no cambia externamente mientras se edita.
                    
                    _noteUiState.value = NoteUiState(
                        id = note.id,
                        title = note.title,
                        description = note.description,
                        isTask = note.isTask,
                        dueDateTime = note.dueDateTime,
                        completed = note.isCompleted,
                        createdAt = note.createdAt,
                        reminders = safeNoteWithDetails.reminders,
                        attachments = safeNoteWithDetails.attachments
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
                 alarmScheduler.cancel(reminder)
             }
        }
    }

    fun addAttachment(attachment: Attachment) {
        val currentAttachments = _noteUiState.value.attachments.toMutableList()
        currentAttachments.add(attachment)
        _noteUiState.value = _noteUiState.value.copy(attachments = currentAttachments)
    }

    fun removeAttachment(attachment: Attachment) {
        val currentAttachments = _noteUiState.value.attachments.toMutableList()
        currentAttachments.remove(attachment)
        _noteUiState.value = _noteUiState.value.copy(attachments = currentAttachments)
        
        // Si ya existía en BD, lo borramos de inmediato para que sea "reactivo"
        if (attachment.id != 0) {
            viewModelScope.launch {
                notesRepository.deleteAttachment(attachment)
            }
        }
    }

    fun saveNote() {
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
                
                notesRepository.deleteRemindersByNoteId(noteId)
                alarmScheduler.cancel(note)
                // Ya no borramos todos los adjuntos para evitar borrar y recrear innecesariamente.
                // Los eliminados se manejaron en removeAttachment
            } else {
                val newId = notesRepository.insertNote(note)
                noteId = newId.toInt()
            }
            
             if (note.dueDateTime != null) {
                 alarmScheduler.schedule(note)
             }

            noteUi.reminders.forEach { reminder ->
                val newReminder = reminder.copy(noteId = noteId, id = 0)
                val generatedId = notesRepository.addReminder(newReminder)
                val savedReminder = newReminder.copy(id = generatedId.toInt())
                alarmScheduler.schedule(savedReminder, note.title)
            }

            noteUi.attachments.forEach { att ->
                // Solo insertamos los nuevos (id == 0)
                if (att.id == 0) {
                     notesRepository.addAttachment(att.copy(noteId = noteId))
                }
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
    val reminders: List<Reminder> = emptyList(),
    val attachments: List<Attachment> = emptyList()
)