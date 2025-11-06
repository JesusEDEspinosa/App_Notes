package com.example.appnotes.data

import androidx.room.Embedded
import androidx.room.Relation

data class NoteWithDetails(
    @Embedded val note: Note,

    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val reminders: List<Reminder> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val attachments: List<Attachment> = emptyList()
)