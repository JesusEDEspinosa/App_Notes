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
        if (isEditMode && _noteUiState.value.id == noteId) return

        viewModelScope.launch {
            notesRepository.getNote(noteId).collect { noteWithDetails ->
                noteWithDetails?.let { safeNoteWithDetails ->
                    val note = safeNoteWithDetails.note
                    
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

    fun updateReminder(oldReminder: Reminder, newRemindAt: Long) {
        val currentReminders = _noteUiState.value.reminders.toMutableList()
        val index = currentReminders.indexOf(oldReminder)
        if (index != -1) {
            currentReminders[index] = oldReminder.copy(remindAt = newRemindAt)
            _noteUiState.value = _noteUiState.value.copy(reminders = currentReminders)
        }
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
                // Cancelar alarmas de recordatorios que van a ser reemplazados
                noteUi.reminders.forEach { reminder ->
                    if (reminder.id != 0) {
                        alarmScheduler.cancel(reminder)
                    }
                }

                notesRepository.updateNote(note)
                noteId = note.id
                
                notesRepository.deleteRemindersByNoteId(noteId)
                alarmScheduler.cancel(note)
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