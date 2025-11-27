package com.example.appnotes.data

import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun getAllNotes(): Flow<List<NoteWithDetails>>
    fun getNote(id: Int): Flow<NoteWithDetails?>
    suspend fun insertNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun addReminder(reminder: Reminder): Long
    suspend fun addAttachment(attachment: Attachment)
    suspend fun deleteAttachment(attachment: Attachment)
    suspend fun deleteRemindersByNoteId(noteId: Int)
    suspend fun deleteAttachmentsByNoteId(noteId: Int)
}