package com.example.appnotes.ui.note

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.appnotes.R
import com.example.appnotes.data.Attachment
import com.example.appnotes.data.NoteWithDetails
import com.example.appnotes.ui.NoteDetailsViewModelProvider
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
                title = { Text(stringResource(id = R.string.detalle_nota)) },
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
                if (it.note.isTask) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.toggleCompleted() },
                        text = { Text(if (it.note.isCompleted) stringResource(R.string.marcar_pendiente) else stringResource(R.string.marcar_completada)) },
                        icon = { Icon(Icons.Default.Check, contentDescription = null) }
                    )
                }
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
    var viewingAttachment by remember { mutableStateOf<Attachment?>(null) }

    if (viewingAttachment != null) {
        Dialog(onDismissRequest = { viewingAttachment = null }) {
            MediaViewer(uri = viewingAttachment!!.uri, type = viewingAttachment!!.type)
        }
    }

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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.note.description,
                style = MaterialTheme.typography.bodyLarge
            )
            if (note.note.isTask && note.note.dueDateTime != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.fecha_limite,
                        sdf.format(Date(note.note.dueDateTime))
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            if (note.note.isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
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
                Box(modifier = Modifier.clickable { viewingAttachment = att }) {
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
                            val painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(att.uri)
                                    .build()
                            )
                            Image(
                                painter = painter,
                                contentDescription = "Video thumbnail",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        "audio" -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "Audio",
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        else -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = "File",
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
