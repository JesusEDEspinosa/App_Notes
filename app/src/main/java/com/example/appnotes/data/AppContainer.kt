package com.example.appnotes.data

import android.content.Context

interface AppContainer {
    val notesRepository: NotesRepository
}


class AppDataContainer(private val context: Context) : AppContainer{
    override val notesRepository: NotesRepository by lazy {
        val database = NotesDatabase.getDatabase(context)
        OfflineNotesRepository(database.notesDao())

    }
}