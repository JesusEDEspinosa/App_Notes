package com.example.appnotes.data

import android.adservices.adid.AdId
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val noteId: Int,
    val remindAt: Long,
    val status: String = "PENDING"
)