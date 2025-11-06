package com.example.appnotes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Note::class, Reminder::class, Attachment::class],
    version = 1,
    exportSchema = false
)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun notesDao(): NoteDao

    companion object{
        @Volatile
        private var INSTANCE: NotesDatabase? = null
        fun getDatabase(context: Context): NotesDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, NotesDatabase::class.java, "note_database")
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}