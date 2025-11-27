package com.example.appnotes.ui.navigation

import android.app.Activity
import android.content.Intent
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    
    // Manejar la intención de notificación para navegar
    LaunchedEffect(Unit) {
        val activity = context as? Activity
        val intent = activity?.intent
        if (intent != null && intent.hasExtra("NOTE_ID_TO_OPEN")) {
            val noteId = intent.getIntExtra("NOTE_ID_TO_OPEN", -1)
            if (noteId != -1) {
                navController.navigate("${NoteDetailDestination.route}/$noteId")
                // Limpiar el extra para evitar re-navegación al rotar o recrear
                intent.removeExtra("NOTE_ID_TO_OPEN")
            }
        }
    }

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
