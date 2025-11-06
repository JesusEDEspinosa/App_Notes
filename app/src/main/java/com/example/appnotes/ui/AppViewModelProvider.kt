package com.example.appnotes.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.appnotes.NotesApplication
import com.example.appnotes.ui.home.HomeViewModel
import com.example.appnotes.ui.note.NoteDetailsViewModel
import com.example.appnotes.ui.note.NoteEntryViewModel

object HomeViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NotesApplication
            val repository = application.container.notesRepository
            HomeViewModel(repository)
        }
    }
}

object NoteEntryViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NotesApplication
            val repository = application.container.notesRepository
            NoteEntryViewModel(repository)
        }
    }
}

object NoteDetailsViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NotesApplication
            val repository = application.container.notesRepository
            NoteDetailsViewModel(repository)
        }
    }
}