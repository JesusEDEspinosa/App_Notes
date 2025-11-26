package com.example.appnotes.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.appnotes.data.Note
import com.example.appnotes.data.Reminder

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Mantenemos la firma anterior por compatibilidad, pero ahora solo programa la fecha de vencimiento principal si existe
    fun schedule(note: Note) {
        if (note.dueDateTime != null && !note.isCompleted) {
             // Usamos un ID único para el recordatorio principal basado en el ID de la nota
             scheduleReminder(note.id, note.dueDateTime, note.title, "Tu tarea vence pronto!")
        } else {
             cancel(note)
        }
    }

    fun schedule(reminder: Reminder, noteTitle: String) {
        scheduleReminder(reminder.id, reminder.remindAt, "Recordatorio: $noteTitle", "Tienes un recordatorio programado")
    }

    private fun scheduleReminder(reminderId: Int, remindAt: Long, title: String, message: String) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("NOTE_ID", reminderId) // Usamos el ID del recordatorio o nota como identificador único
            putExtra("TITLE", title)
            putExtra("MESSAGE", message)
        }

        // Usamos reminderId como requestCode para que sea único por cada recordatorio
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (remindAt > System.currentTimeMillis()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            remindAt,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            remindAt,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        remindAt,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                Log.e("AlarmScheduler", "Error scheduling exact alarm", e)
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    remindAt,
                    pendingIntent
                )
            }
        }
    }

    fun cancel(note: Note) {
        // Cancelar el recordatorio principal de la nota
        cancelReminder(note.id)
    }
    
    fun cancel(reminder: Reminder) {
        cancelReminder(reminder.id)
    }

    private fun cancelReminder(id: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
