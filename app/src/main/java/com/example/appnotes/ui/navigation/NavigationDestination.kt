package com.example.appnotes.ui.navigation

interface NavigationDestination {
    val route: String
    val titleRes: String
}

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = "Notes"
}

object NoteEntryDestination : NavigationDestination {
    override val route = "note_entry"
    override val titleRes = "Add Note"
}

object NoteEditDestination : NavigationDestination {
    override val route = "note_edit"
    override val titleRes = "Edit Note"
}

object NoteDetailDestination : NavigationDestination {
    override val route = "note_detail"
    override val titleRes = "Note Details"
}
