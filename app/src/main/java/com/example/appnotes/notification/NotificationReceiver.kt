package com.example.appnotes.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.appnotes.MainActivity
import com.example.appnotes.NotesApplication
import com.example.appnotes.R

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationReceiver", "onReceive called")
        
        val noteId = intent.getIntExtra("NOTE_ID", -1)
        val realNoteId = intent.getIntExtra("REAL_NOTE_ID", -1)
        val title = intent.getStringExtra("TITLE") ?: "Tarea Pendiente"
        val message = intent.getStringExtra("MESSAGE") ?: "Tienes una tarea por completar"

        Log.d("NotificationReceiver", "Note ID: $noteId, Real Note ID: $realNoteId, Title: $title")

        if (noteId != -1) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val contentIntent = Intent(context, MainActivity::class.java).apply {
                if (realNoteId != -1) {
                    putExtra("NOTE_ID_TO_OPEN", realNoteId)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            
            val pendingContentIntent = PendingIntent.getActivity(
                context, 
                noteId,
                contentIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, NotesApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(pendingContentIntent)
                .build()

            try {
                notificationManager.notify(noteId, notification)
                Log.d("NotificationReceiver", "Notification sent successfully")
            } catch (e: Exception) {
                Log.e("NotificationReceiver", "Failed to send notification", e)
            }
        }
    }
}
