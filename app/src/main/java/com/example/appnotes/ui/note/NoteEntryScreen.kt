package com.example.appnotes.ui.note

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.appnotes.R
import com.example.appnotes.data.Attachment
import com.example.appnotes.data.Reminder
import com.example.appnotes.ui.NoteEntryViewModelProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/*
    Unidad 7 - Datos
    -CRUD Nota/Tarea: Implementado en el ViewModel (saveNote, loadNote) y UI (NoteEntryScreen)
    -CRUD n Multimedia: Implementado en AttachmentsCard y ViewModel (addAttachment, removeAttachment)
    -CRUD n Recordatorio: Implementado en RemindersCard y ViewModel (addReminder, removeReminder)
    -Archivos: Gestión de archivos locales mediante URIs (rememberLauncherForActivityResult)
    -Informe técnico final: Estructura del proyecto y documentación.

    Unidad 8 - Multimedia
    Foto n: Implementado con rememberCameraLauncher y CameraX/Intent implícito.
    Audio n: Implementado con rememberAudioLauncher y MediaStore Intent.
    Video n: Implementado con rememberVideoLauncher y ActivityResultContracts.CaptureVideo.
    Recurso del sistema System n: Uso de Intents para abrir archivos, cámara, galería.
    Notificaciones/Reprogramación n: Implementado en AlarmScheduler y BootReceiver.
    Permisos en tiempo de ejecución n: Manejo dinámico de permisos (CAMERA, RECORD_AUDIO, POST_NOTIFICATIONS).
*/

@Composable
fun NoteEntryScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit = navigateBack,
    noteId: Int? = null,
    viewModel: NoteEntryViewModel = viewModel(factory = NoteEntryViewModelProvider.Factory)
) {
    val noteUiState by viewModel.noteUiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(noteId) {
        if (noteId != null) viewModel.loadNote(noteId)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                NotesTopBar(noteId, onNavigateUp)
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.btn_guardar)) },
                    icon = { Icon(Icons.Default.Check, contentDescription = stringResource(R.string.btn_guardar)) },
                    onClick = {
                        if (viewModel.isValidNote()) {
                            viewModel.saveNote()
                            navigateBack()
                        } else {
                            Toast.makeText(
                                context,
                                "El título y la descripción no pueden estar vacíos",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        ) { innerPadding ->
            NoteEntryForm(
                noteUiState,
                onValueChange = viewModel::updateUiState,
                onAddReminder = viewModel::addReminder,
                onRemoveReminder = viewModel::removeReminder,
                onAddAttachment = viewModel::addAttachment,
                onRemoveAttachment = viewModel::removeAttachment,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesTopBar(
    noteId: Int? = null,
    onNavigateUp: () -> Unit
) {
    TopAppBar(
        title = { Text(if (noteId == null) stringResource(R.string.nueva_nota) else stringResource(R.string.editar_nota)) },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.btn_volver))
            }
        },
        modifier = Modifier
            .statusBarsPadding()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEntryForm(
    noteUiState: NoteUiState,
    onValueChange: (NoteUiState) -> Unit,
    onAddReminder: (Long) -> Unit,
    onRemoveReminder: (Reminder) -> Unit,
    onAddAttachment: (Attachment) -> Unit,
    onRemoveAttachment: (Attachment) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TitleCard(
            noteUiState.title,
            onValueChange,
            noteUiState,
            stringResource(R.string.titulo_nota),
            stringResource(R.string.titulo_placeholder),
            lines = 1,
            single = true,
            modifier = Modifier
        )

        DescriptionCard(
            noteUiState.description,
            onValueChange,
            noteUiState,
            stringResource(R.string.descripcion_nota),
            stringResource(R.string.descripcion_placeholder),
            lines = 5,
            single = false,
        )

        ConvertToTaskCard(noteUiState, onValueChange)

        if (noteUiState.isTask) {
            RemindersCard(
                reminders = noteUiState.reminders,
                onAddReminder = onAddReminder,
                onRemoveReminder = onRemoveReminder
            )
        }

        val fileLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri: Uri? ->
                uri?.let {
                    val mime = context.contentResolver.getType(uri) ?: ""
                    val type = when {
                        mime.startsWith("image/") -> "image"
                        mime.startsWith("video/") -> "video"
                        mime.startsWith("audio/") -> "audio"
                        else -> "file"
                    }
                    val newAttachment = Attachment(
                        noteId = 0,
                        uri = uri.toString(),
                        type = type
                    )
                    onAddAttachment(newAttachment)
                }
            }
        )

        val cameraLauncher = rememberCameraLauncher(onAddAttachment = onAddAttachment)
        val audioLauncher = rememberAudioLauncher(onAddAttachment = onAddAttachment)
        val videoLauncher = rememberVideoLauncher(onAddAttachment = onAddAttachment)

        AttachmentsCard(
            attachments = noteUiState.attachments,
            onAddFile = { fileLauncher.launch("*/*") },
            onAddCamera = { cameraLauncher.captureImage() },
            onAddVideo = { videoLauncher.captureVideo() },
            onAddAudio = { audioLauncher.recordAudio() }
        )

        if (noteUiState.attachments.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.archivos_adjuntos),
                style = MaterialTheme.typography.titleMedium
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(noteUiState.attachments) { att ->
                    Box(modifier = Modifier
                        .size(80.dp)
                    ) {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    val uri = Uri.parse(att.uri)
                                    setDataAndType(uri, context.contentResolver.getType(uri))
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    Toast.makeText(
                                        context,
                                        "No hay aplicación disponible para abrir este archivo.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        ) {
                            when (att.type) {
                                "image" -> {
                                    Image(
                                        painter = rememberAsyncImagePainter(att.uri),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp)),
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
                                        contentDescription = "Miniatura de video",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                "audio" -> {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp))
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
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                                    ) {
                                        Icon(
                                            Icons.Default.Description,
                                            contentDescription = "Archivo",
                                            modifier = Modifier.size(40.dp),
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }
                        }
                        
                        IconButton(
                            onClick = { onRemoveAttachment(att) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .background(Color.White.copy(alpha = 0.7f), CircleShape)
                                .padding(2.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Eliminar adjunto",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun rememberCameraLauncher(onAddAttachment: (Attachment) -> Unit): CameraLauncher {
    val context = LocalContext.current
    var tempImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempImageUri?.let { uri ->
                    val newAttachment = Attachment(noteId = 0, uri = uri.toString(), type = "image")
                    onAddAttachment(newAttachment)
                }
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val tempImageFile = File.createTempFile("JPEG_", ".jpg", context.externalCacheDir)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", tempImageFile)
            tempImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    return remember {
        object : CameraLauncher {
            override fun captureImage() {
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                        val tempImageFile = File.createTempFile("JPEG_", ".jpg", context.externalCacheDir)
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", tempImageFile)
                        tempImageUri = uri
                        cameraLauncher.launch(uri)
                    }
                    else -> {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            }
        }
    }
}

interface CameraLauncher {
    fun captureImage()
}

@Composable
fun rememberAudioLauncher(onAddAttachment: (Attachment) -> Unit): AudioRecorderLauncher {
    val context = LocalContext.current
    var tempAudioUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            tempAudioUri?.let { uri ->
                val newAttachment = Attachment(noteId = 0, uri = uri.toString(), type = "audio")
                onAddAttachment(newAttachment)
            }
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val tempAudioFile = File.createTempFile("AAC_", ".aac", context.externalCacheDir)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", tempAudioFile)
                tempAudioUri = uri

                val intent = Intent(android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION)
                    .putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri)
                audioLauncher.launch(intent)
            } else {
                Toast.makeText(context, "Permiso de grabación de audio denegado.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    return remember {
        object : AudioRecorderLauncher {
            override fun recordAudio() {
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) -> {
                        val tempAudioFile = File.createTempFile("AAC_", ".aac", context.externalCacheDir)
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", tempAudioFile)
                        tempAudioUri = uri

                        val intent = Intent(android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION)
                            .putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri)
                        audioLauncher.launch(intent)
                    }
                    else -> {
                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            }
        }
    }
}

interface AudioRecorderLauncher {
    fun recordAudio()
}

@Composable
fun rememberVideoLauncher(onAddAttachment: (Attachment) -> Unit): VideoLauncher {
    val context = LocalContext.current
    var tempVideoUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo(),
        onResult = { success ->
            if (success) {
                tempVideoUri?.let { uri ->
                    val newAttachment = Attachment(noteId = 0, uri = uri.toString(), type = "video")
                    onAddAttachment(newAttachment)
                }
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val tempVideoFile = File.createTempFile("MP4_", ".mp4", context.externalCacheDir)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", tempVideoFile)
                tempVideoUri = uri
                videoLauncher.launch(uri)
            } else {
                Toast.makeText(context, "Permiso de cámara denegado.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    return remember {
        object : VideoLauncher {
            override fun captureVideo() {
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                        val tempVideoFile = File.createTempFile("MP4_", ".mp4", context.externalCacheDir)
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", tempVideoFile)
                        tempVideoUri = uri
                        videoLauncher.launch(uri)
                    }
                    else -> {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            }
        }
    }
}

interface VideoLauncher {
    fun captureVideo()
}


@Composable
fun TitleCard(
    value: String,
    onValueChange: (NoteUiState) -> Unit,
    noteUiState: NoteUiState,
    text: String,
    placeholder: String,
    lines: Int,
    single: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .padding(16.dp, 16.dp, 0.dp)
                    .fillMaxWidth()
            )
            OutlinedTextField(
                value = value,
                onValueChange = { onValueChange(noteUiState.copy(title = it)) },
                placeholder = { Text(placeholder) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                maxLines = lines,
                singleLine = single,
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )
        }
    }
}

@Composable
fun DescriptionCard(
    value: String,
    onValueChange: (NoteUiState) -> Unit,
    noteUiState: NoteUiState,
    text: String,
    placeholder: String,
    lines: Int,
    single: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .padding(16.dp, 16.dp, 0.dp)
                    .fillMaxWidth()
            )
            OutlinedTextField(
                value = value,
                onValueChange = { onValueChange(noteUiState.copy(description = it)) },
                placeholder = { Text(placeholder) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                maxLines = lines,
                singleLine = single,
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )
        }
    }
}

@Composable
fun ConvertToTaskCard(
    noteUiState: NoteUiState,
    onValueChange: (NoteUiState) -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.marcar_como_tarea), fontWeight = FontWeight.Bold)

            Checkbox(
                checked = noteUiState.isTask,
                onCheckedChange = {
                    onValueChange(noteUiState.copy(isTask = it))
                }
            )
        }
    }
}

@Composable
fun RemindersCard(
    reminders: List<Reminder>,
    onAddReminder: (Long) -> Unit,
    onRemoveReminder: (Reminder) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.recordatorios), fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.agregar_recordatorio),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        val datePicker = DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                calendar.set(Calendar.YEAR, year)
                                calendar.set(Calendar.MONTH, month)
                                calendar.set(Calendar.DAY_OF_MONTH, day)

                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                                        calendar.set(Calendar.MINUTE, minute)
                                        calendar.set(Calendar.SECOND, 0)
                                        onAddReminder(calendar.timeInMillis)
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        datePicker.show()
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (reminders.isEmpty()) {
                Text(stringResource(R.string.recordatorios_vacios), color = Color.Gray, fontSize = 13.sp)
            } else {
                Column {
                    reminders.forEach { reminder ->
                        val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(reminder.remindAt)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(formattedDate, fontSize = 14.sp)
                            }
                            IconButton(onClick = { onRemoveReminder(reminder) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentsCard(
    attachments: List<Attachment>,
    onAddFile: () -> Unit,
    onAddCamera: () -> Unit,
    onAddVideo: () -> Unit,
    onAddAudio: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.archivos_adjuntos), fontWeight = FontWeight.Bold)

                Box {
                    Text(
                        stringResource(R.string.agregar_archivo),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { expanded = true }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cámara") },
                            leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                            onClick = {
                                expanded = false
                                onAddCamera()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Video") },
                            leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null) },
                            onClick = {
                                expanded = false
                                onAddVideo()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Audio") },
                            leadingIcon = { Icon(Icons.Default.Mic, contentDescription = null) },
                            onClick = {
                                expanded = false
                                onAddAudio()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Archivo") },
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                            onClick = {
                                expanded = false
                                onAddFile()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (attachments.isEmpty()) {
                Text(stringResource(R.string.archivos_vacios), color = Color.Gray, fontSize = 13.sp)
            }
        }
    }
}
