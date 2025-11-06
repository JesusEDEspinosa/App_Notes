package com.example.appnotes.ui.note

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.appnotes.R
import com.example.appnotes.data.Attachment
import com.example.appnotes.ui.NoteEntryViewModelProvider
import java.util.Calendar

@Composable
fun NoteEntryScreen (
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit = navigateBack,
    noteId: Int? = null,
    viewModel: NoteEntryViewModel = viewModel(factory = NoteEntryViewModelProvider.Factory)
)
{
    val noteUiState by viewModel.noteUiState.collectAsState()
    val context = LocalContext.current
    var attachments by remember { mutableStateOf<List<Attachment>>(emptyList()) }


    LaunchedEffect(
        noteId
    ) {
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
                            viewModel.saveNote(attachments)
                            navigateBack()
                        }
                    }
                )
            }
        ) { innerPadding ->
            NoteEntryForm(
                noteUiState,
                attachments = attachments,
                onAttachmentsChange = {attachments = it},
                onValueChange = viewModel::updateUiState,
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
        title = { Text( if(noteId == null) stringResource(R.string.nueva_nota) else stringResource(R.string.editar_nota))  },
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
fun NoteEntryForm (
    noteUiState: NoteUiState,
    attachments: List<Attachment>,
    onAttachmentsChange: (List<Attachment>) -> Unit,
    onValueChange: (NoteUiState) -> Unit,

    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    Column (
        modifier = modifier.fillMaxSize(),
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
            val date = remember { mutableStateOf("") }
            val time = remember { mutableStateOf("") }

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    onClick = {
                        val datePicker = DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                calendar.set(year, month, day)
                                date.value = "$day/${month + 1}/$year"
                                onValueChange(noteUiState.copy(dueDateTime = calendar.timeInMillis))
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        datePicker.show()
                    }
                ) {
                    Text(if (date.value.isEmpty()) stringResource(R.string.seleccionar_fecha) else stringResource(
                        R.string.texto_fecha, date.value
                    ))
                }

                Button(
                    onClick = {
                        val timePicker = TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                calendar.set(Calendar.HOUR_OF_DAY, hour)
                                calendar.set(Calendar.MINUTE, minute)
                                time.value = "%02d:%02d".format(hour, minute)
                                onValueChange(noteUiState.copy(dueDateTime = calendar.timeInMillis))
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        )
                        timePicker.show()
                    }
                ) {
                    Text(if (time.value.isEmpty()) stringResource(R.string.seleccionar_hora) else stringResource(
                        R.string.texto_hora, time.value
                    ))
                }
            }
        }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                val mime = context.contentResolver.getType(uri) ?: ""
                val type = when {
                    mime.startsWith("image/") -> "image"
                    mime.startsWith("video/") -> "video"
                    mime.startsWith("audio/") -> "audio"
                    else -> "file"
                }
                val newAttachment = Attachment(
                    noteId = 0, // luego se actualiza tras guardar la nota
                    uri = uri.toString(),
                    type = type
                )
                onAttachmentsChange(attachments + newAttachment)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { launcher.launch("*/*") }) {
            Text(stringResource(R.string.agregar_archivo))
        }

        if (attachments.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.archivos_adjuntos), style = MaterialTheme.typography.titleMedium)

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(attachments) { att ->
                    when (att.type) {
                        "image" -> {
                            Image(
                                painter = rememberAsyncImagePainter(att.uri),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        "video" -> {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                        "audio" -> {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                        else -> {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                }
            }
        }

    }
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
){
    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {

        Column (
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
){
    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {

        Column (
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
fun RemindersCard() {
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
                    modifier = Modifier.clickable {  }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.recordatorios_vacios), color = Color.Gray, fontSize = 13.sp)
        }
    }
}

@Composable
fun AttachmentsCard() {
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
                Text(
                    stringResource(R.string.agregar_archivo),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.archivos_vacios), color = Color.Gray, fontSize = 13.sp)
        }
    }
}

//@Preview
//@Composable
//fun CreateEditPreview(){
//    AppNotesTheme {
//
//    }
//}