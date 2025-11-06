package com.example.appnotes

import android.app.Application
import com.example.appnotes.data.AppContainer
import com.example.appnotes.data.AppDataContainer

class NotesApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
