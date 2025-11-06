package com.example.appnotes.ui.note

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.appnotes.R
import com.example.appnotes.data.NoteWithDetails
import com.example.appnotes.ui.NoteDetailsViewModelProvider
import com.example.appnotes.ui.navigation.HomeDestination
import com.example.appnotes.ui.navigation.NavigationDestination
import com.example.appnotes.ui.navigation.NoteEditDestination
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Int,
    navController: NavController,
    viewModel: NoteDetailsViewModel = viewModel(factory = NoteDetailsViewModelProvider.Factory)
) {
    val noteWithDetails by viewModel.noteUiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { NoteEditDestination.titleRes },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.btn_volver))
                    }
                },
                actions = {
                    IconButton(onClick = { noteWithDetails?.note?.let {
                        navController.navigate("${NoteEditDestination.route}/${it.id}")
                    } }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.btn_editar))
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.btn_eliminar))
                    }
                }
            )
        },
        floatingActionButton = {
            noteWithDetails?.let {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.toggleCompleted() },
                    text = { Text(if (it.note.isCompleted) stringResource(R.string.marcar_pendiente) else stringResource(
                        R.string.marcar_completada
                    )) },
                    icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = null) }
                )
            }
        }
    ) { innerPadding ->
        noteWithDetails?.let {
            NoteDetailContent(
                note = it,
                modifier = Modifier.padding(innerPadding)
            )
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.confirmar_eliminar)) },
                text = { Text(stringResource(R.string.eliminar_definitivo)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteNote {
                                navController.navigateUp()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.btn_eliminar), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(R.string.btn_cancelar))
                    }
                }
            )
        }
    }
}

@Composable
fun NoteDetailContent(
    note: NoteWithDetails,
    modifier: Modifier = Modifier
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = note.note.title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = note.note.description,
                style = MaterialTheme.typography.bodyLarge
            )
            if (note.note.isTask && note.note.dueDateTime != null) {
                Text(
                    text = stringResource(
                        R.string.fecha_limite,
                        sdf.format(Date(note.note.dueDateTime))
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            if (note.note.isCompleted) {
                Text(
                    text = stringResource(R.string.tarea_completada),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (note.reminders.isNotEmpty()) {
            item { Text(stringResource(R.string.recordatorios), style = MaterialTheme.typography.titleMedium) }
            items(note.reminders) { reminder ->
                Text("- ${sdf.format(Date(reminder.remindAt))}")
            }
        }

        if (note.attachments.isNotEmpty()) {
            item { Text(stringResource(R.string.archivos_adjuntos), style = MaterialTheme.typography.titleMedium) }
            items(note.attachments) { att ->
                when (att.type) {
                    "image" -> {
                        Image(
                            painter = rememberAsyncImagePainter(att.uri),
                            contentDescription = att.caption ?: "imagen adjunta",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    "video" -> {
                        Text(context.getString(R.string.video_adjunto, att.caption ?: att.uri))
                    }
                    "audio" -> {
                        Text(context.getString(R.string.audio_adjunto, att.caption ?: att.uri))
                    }
                    else -> {
                        Text(context.getString(R.string.archivo, att.caption ?: att.uri))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
