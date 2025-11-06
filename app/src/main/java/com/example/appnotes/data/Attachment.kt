package com.example.appnotes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attachments")
data class Attachment (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val noteId: Int,
    val uri: String,
    val type: String,
    val caption: String? = null
)