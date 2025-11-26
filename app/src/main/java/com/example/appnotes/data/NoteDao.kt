package com.example.appnotes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Transaction
    @Query("SELECT * from notes ORDER BY createdAt DESC")
    fun getAllNotesWithDetails(): Flow<List<NoteWithDetails>>

    @Transaction
    @Query("SELECT * from notes WHERE id = :id")
    fun getNoteWithDetails(id: Int): Flow<NoteWithDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)



    //Reminders
    @Insert
    suspend fun insertReminder(reminder: Reminder): Long

    @Query("DELETE FROM reminders WHERE noteId = :noteId")
    suspend fun deleteRemindersByNoteId(noteId: Int)

    //Attachments
    @Insert
    suspend fun insertAttachment(attachment: Attachment)

    @Query("DELETE FROM attachments WHERE noteId = :noteId")
    suspend fun deleteAttachmentsByNoteId(noteId: Int)

}