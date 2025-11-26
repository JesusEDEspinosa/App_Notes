package com.example.appnotes.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.appnotes.data.AppDataContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val alarmScheduler = AlarmScheduler(context)
            val appContainer = AppDataContainer(context)
            val notesRepository = appContainer.notesRepository

            
            val pendingResult = goAsync()
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val allNotes = notesRepository.getAllNotes().first()
                    
                    for (noteWithDetails in allNotes) {
                        val note = noteWithDetails.note
                        
                        if (note.dueDateTime != null && !note.isCompleted && note.dueDateTime > System.currentTimeMillis()) {
                            alarmScheduler.schedule(note)
                        }
                        
                        for (reminder in noteWithDetails.reminders) {
                            if (reminder.remindAt > System.currentTimeMillis()) {
                                alarmScheduler.schedule(reminder, note.title)
                            }
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
