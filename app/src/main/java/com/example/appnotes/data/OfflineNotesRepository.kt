package com.example.appnotes.data

import kotlinx.coroutines.flow.Flow

class OfflineNotesRepository (private val noteDao: NoteDao) : NotesRepository {
    override fun getAllNotes(): Flow<List<NoteWithDetails>> =
        noteDao.getAllNotesWithDetails()

    override fun getNote(id: Int): Flow<NoteWithDetails?> =
        noteDao.getNoteWithDetails(id)

    override suspend fun insertNote(note: Note): Long =
        noteDao.insertNote(note)

    override suspend fun updateNote(note: Note) =
        noteDao.updateNote(note)

    override suspend fun deleteNote(note: Note) {
        noteDao.deleteRemindersByNoteId(note.id)
        noteDao.deleteAttachmentsByNoteId(note.id)
        noteDao.deleteNote(note)
    }

    override suspend fun addReminder(reminder: Reminder): Long =
        noteDao.insertReminder(reminder)

    override suspend fun addAttachment(attachment: Attachment) =
        noteDao.insertAttachment(attachment)
        
    override suspend fun deleteAttachment(attachment: Attachment) =
        noteDao.deleteAttachment(attachment)

    override suspend fun deleteRemindersByNoteId(noteId: Int) =
        noteDao.deleteRemindersByNoteId(noteId)

    override suspend fun deleteAttachmentsByNoteId(noteId: Int) =
        noteDao.deleteAttachmentsByNoteId(noteId)
}