package com.example.studypredict.view.notes

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.studypredict.controller.NotesController
import com.example.studypredict.model.NotesUiState
import com.example.studypredict.model.TextNote
import com.example.studypredict.model.VoiceNote
import com.example.studypredict.audio.VoiceMemoPlayer
import com.example.studypredict.audio.VoiceMemoRecorder
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class NoteMode {
    VOICE, TEXT
}

@Composable
fun NotesScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val notesController = remember { NotesController(context) }

    val bg = Color(0xFFF2F6FF)
    val card = Color(0xFFF7FAFF)
    val dark = Color(0xFF0B1220)
    val gray = Color(0xFF6B7280)
    val purple = Color(0xFF6D41FF)
    val purpleGrad = Brush.horizontalGradient(
        listOf(Color(0xFF4B3CFF), Color(0xFFB400FF))
    )

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var mode by remember { mutableStateOf(NoteMode.VOICE) }
    var uiState by remember { mutableStateOf(NotesUiState()) }

    val recorder = remember { VoiceMemoRecorder(context) }
    val player = remember { VoiceMemoPlayer() }

    var currentRecordingFile by remember { mutableStateOf<File?>(null) }

    val voiceNotes = remember { mutableStateListOf<VoiceNote>() }
    val textNotes = remember { mutableStateListOf<TextNote>() }

    fun reloadAll() {
        voiceNotes.clear()
        voiceNotes.addAll(notesController.loadVoiceNotes())

        textNotes.clear()
        textNotes.addAll(notesController.loadTextNotes())
    }

    LaunchedEffect(Unit) {
        reloadAll()
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            currentRecordingFile = recorder.startRecording()
            uiState = uiState.copy(isRecording = true)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Permission micro refusée.")
            }
        }
    }

    fun stopRecordingSafely() {
        runCatching { recorder.stopRecording() }
        uiState = uiState.copy(isRecording = false)
        currentRecordingFile = null
        reloadAll()
    }

    DisposableEffect(Unit) {
        onDispose {
            if (uiState.isRecording) {
                runCatching { recorder.stopRecording() }
            }
            player.stop()
        }
    }

    Scaffold(
        containerColor = bg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Retour",
                        tint = dark
                    )
                }
                Text(
                    text = "Retour",
                    fontWeight = FontWeight.SemiBold,
                    color = dark
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(10.dp, RoundedCornerShape(16.dp), clip = false)
                        .clip(RoundedCornerShape(16.dp))
                        .background(purpleGrad),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (mode == NoteMode.VOICE) {
                            Icons.Outlined.Mic
                        } else {
                            Icons.Outlined.EditNote
                        },
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Notes",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = dark
                    )
                    Text(
                        text = "Choisis entre note vocale et note textuelle",
                        color = gray
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = mode == NoteMode.VOICE,
                    onClick = { mode = NoteMode.VOICE },
                    label = { Text("Vocale") },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = purple,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    )
                )

                FilterChip(
                    selected = mode == NoteMode.TEXT,
                    onClick = { mode = NoteMode.TEXT },
                    label = { Text("Textuelle") },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.EditNote,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = purple,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    )
                )
            }

            Spacer(Modifier.height(14.dp))

            if (mode == NoteMode.VOICE) {
                VoiceNotesContent(
                    uiState = uiState,
                    onRecordToggle = {
                        if (!uiState.isRecording) {
                            val granted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (granted) {
                                currentRecordingFile = recorder.startRecording()
                                uiState = uiState.copy(isRecording = true)
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        } else {
                            stopRecordingSafely()
                        }
                    },
                    voiceNotes = voiceNotes,
                    player = player,
                    onDeleteVoice = { note ->
                        if (player.isPlaying(File(note.filePath))) {
                            player.stop()
                        }

                        val deleted = notesController.deleteVoiceNote(note)
                        reloadAll()

                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (deleted) "Note vocale supprimée"
                                else "Impossible de supprimer la note"
                            )
                        }
                    }
                )
            } else {
                TextNotesContent(
                    uiState = uiState,
                    onTitleChange = { uiState = uiState.copy(title = it) },
                    onContentChange = { uiState = uiState.copy(content = it) },
                    textNotes = textNotes,
                    onSaveText = {
                        val saved = notesController.saveTextNote(
                            title = uiState.title,
                            content = uiState.content
                        )

                        if (saved) {
                            uiState = uiState.copy(title = "", content = "")
                            reloadAll()
                        }

                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (saved) "Note textuelle enregistrée ✅"
                                else "Remplis le titre et le contenu."
                            )
                        }
                    },
                    onDeleteText = { note ->
                        val deleted = notesController.deleteTextNote(note)
                        reloadAll()

                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (deleted) "Note textuelle supprimée"
                                else "Impossible de supprimer la note"
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun VoiceNotesContent(
    uiState: NotesUiState,
    onRecordToggle: () -> Unit,
    voiceNotes: List<VoiceNote>,
    player: VoiceMemoPlayer,
    onDeleteVoice: (VoiceNote) -> Unit
) {
    val card = Color(0xFFF7FAFF)
    val dark = Color(0xFF0B1220)
    val purple = Color(0xFF6D41FF)

    Column(modifier = Modifier.fillMaxSize()) {
        val shape = RoundedCornerShape(22.dp)

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, shape, clip = false),
            shape = shape,
            color = card
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Enregistrement vocal",
                    fontWeight = FontWeight.ExtraBold,
                    color = dark
                )

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = onRecordToggle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isRecording) Color(0xFFEF4444) else purple,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (uiState.isRecording) Icons.Outlined.Stop else Icons.Outlined.Mic,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (uiState.isRecording) "Stop" else "Enregistrer",
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.isRecording) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "● Enregistrement en cours…",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Mes notes vocales",
            fontWeight = FontWeight.ExtraBold,
            color = dark,
            fontSize = 18.sp
        )

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(voiceNotes, key = { it.id }) { note ->
                VoiceNoteRow(
                    note = note,
                    isPlaying = player.isPlaying(File(note.filePath)),
                    onPlay = {
                        player.play(File(note.filePath)) {}
                    },
                    onStop = { player.stop() },
                    onDelete = { onDeleteVoice(note) }
                )
            }
        }
    }
}

@Composable
private fun TextNotesContent(
    uiState: NotesUiState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    textNotes: List<TextNote>,
    onSaveText: () -> Unit,
    onDeleteText: (TextNote) -> Unit
) {
    val card = Color(0xFFF7FAFF)
    val dark = Color(0xFF0B1220)
    val purple = Color(0xFF6D41FF)

    Column(modifier = Modifier.fillMaxSize()) {
        val shape = RoundedCornerShape(22.dp)

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, shape, clip = false),
            shape = shape,
            color = card
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Nouvelle note textuelle",
                    fontWeight = FontWeight.ExtraBold,
                    color = dark
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Titre") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.content,
                    onValueChange = onContentChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Contenu") },
                    minLines = 4,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onSaveText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = purple,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Outlined.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Sauvegarder", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Mes notes textuelles",
            fontWeight = FontWeight.ExtraBold,
            color = dark,
            fontSize = 18.sp
        )

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(textNotes, key = { it.id }) { note ->
                TextNoteRow(
                    note = note,
                    onDelete = { onDeleteText(note) }
                )
            }
        }
    }
}

@Composable
private fun VoiceNoteRow(
    note: VoiceNote,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val dark = Color(0xFF0B1220)
    val gray = Color(0xFF6B7280)

    val df = remember { SimpleDateFormat("dd/MM • HH:mm", Locale.getDefault()) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape, clip = false),
        shape = shape,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Note vocale", fontWeight = FontWeight.ExtraBold, color = dark)
                Text(df.format(Date(note.createdAt)), color = gray, fontSize = 13.sp)
            }

            OutlinedButton(
                onClick = { if (isPlaying) onStop() else onPlay() },
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF5B55FF)
                )
                Spacer(Modifier.width(6.dp))
                Text(if (isPlaying) "Stop" else "Lire")
            }

            Spacer(Modifier.width(8.dp))

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Supprimer",
                    tint = Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
private fun TextNoteRow(
    note: TextNote,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val dark = Color(0xFF0B1220)
    val gray = Color(0xFF6B7280)

    val preview = if (note.content.length > 90) {
        note.content.take(90) + "..."
    } else {
        note.content
    }

    val dateText = remember(note.createdAt) {
        SimpleDateFormat("dd/MM • HH:mm", Locale.getDefault()).format(Date(note.createdAt))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape, clip = false),
        shape = shape,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    fontWeight = FontWeight.ExtraBold,
                    color = dark
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = dateText,
                    color = gray,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = preview,
                    color = dark,
                    fontSize = 14.sp
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Supprimer",
                    tint = Color(0xFFEF4444)
                )
            }
        }
    }
}