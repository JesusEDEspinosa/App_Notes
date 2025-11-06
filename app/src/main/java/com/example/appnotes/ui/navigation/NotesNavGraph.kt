package com.example.appnotes.ui.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appnotes.ui.home.HomeScreen
import com.example.appnotes.ui.note.NoteDetailScreen
import com.example.appnotes.ui.note.NoteEntryScreen

@Composable
fun NotesNavGraph(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(navController = navController, windowSizeClass = windowSizeClass)
        }

        composable(route = NoteEntryDestination.route) {
            NoteEntryScreen(
                navigateBack = { navController.navigateUp() },
            )
        }

        composable(
            route = "${NoteEditDestination.route}/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId")
            NoteEntryScreen(
                noteId = noteId,
                navigateBack = { navController.navigateUp() }
            )
        }

        composable(
            route = "${NoteDetailDestination.route}/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId")
            if (noteId != null) {
                NoteDetailScreen(
                    noteId = noteId,
                    navController = navController,
                )
            }
        }
    }
}
