package com.example.appnotes.data

import android.content.Context
import com.example.appnotes.notification.AlarmScheduler

interface AppContainer {
    val notesRepository: NotesRepository
    val alarmScheduler: AlarmScheduler
}


class AppDataContainer(private val context: Context) : AppContainer{
    override val notesRepository: NotesRepository by lazy {
        val database = NotesDatabase.getDatabase(context)
        OfflineNotesRepository(database.notesDao())
    }

    override val alarmScheduler: AlarmScheduler by lazy {
        AlarmScheduler(context)
    }
}
