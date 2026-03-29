package com.example.studypredict.view.notes

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import java.io.File
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.studypredict.model.NoteItem
import com.example.studypredict.network.ApiResult
import com.example.studypredict.network.BackendApi
import kotlinx.coroutines.launch

private enum class NoteMode {
    Text, Voice
}

@Composable
fun NotesScreen(
    token: String?,
    onBack: () -> Unit,
    onUnauthorized: () -> Unit
) {
    val bg = Color(0xFFF2F6FF)
    val dark = Color(0xFF0B1220)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val recordingsDir = remember { File(context.cacheDir, "voice_memos").apply { mkdirs() } }
    val recorder = remember { MediaRecorder() }
    val localPlayer = remember { MediaPlayer() }

    var loading by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf<List<NoteItem>>(emptyList()) }
    var mode by remember { mutableStateOf(NoteMode.Text) }
    var title by remember { mutableStateOf("") }
    var textContent by remember { mutableStateOf("") }
    var voiceDescription by remember { mutableStateOf("") }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var tempRecordingFile by remember { mutableStateOf<File?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var isLocalPlaying by remember { mutableStateOf(false) }
    var isLocalPreparing by remember { mutableStateOf(false) }
    var isUploadingVoice by remember { mutableStateOf(false) }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasAudioPermission = granted }

    DisposableEffect(Unit) {
        localPlayer.setOnCompletionListener { isLocalPlaying = false }
        localPlayer.setOnErrorListener { _, _, _ ->
            isLocalPreparing = false
            isLocalPlaying = false
            true
        }
        onDispose {
            recorder.release()
            localPlayer.release()
        }
    }

    fun requestAudioPermission() {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    fun startRecording() {
        if (!hasAudioPermission) {
            requestAudioPermission()
            return
        }
        if (isRecording) return

        val targetFile = File(recordingsDir, "memo_${System.currentTimeMillis()}.m4a")
        try {
            recorder.reset()
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setOutputFile(targetFile.absolutePath)
            recorder.prepare()
            recorder.start()
            isRecording = true
            tempRecordingFile = targetFile
        } catch (_: Exception) {
            scope.launch { snackbarHostState.showSnackbar("Impossible de démarrer l'enregistrement.") }
        }
    }

    fun stopRecording() {
        if (!isRecording) return
        try {
            recorder.stop()
        } catch (_: Exception) {
        } finally {
            recorder.reset()
            isRecording = false
            recordedFile = tempRecordingFile?.takeIf { it.exists() }
            tempRecordingFile = null
        }
    }

    fun toggleLocalPlayback() {
        val file = recordedFile ?: return
        if (isLocalPlaying) {
            localPlayer.pause()
            isLocalPlaying = false
            return
        }
        try {
            isLocalPreparing = true
            localPlayer.reset()
            localPlayer.setDataSource(file.absolutePath)
            localPlayer.setOnPreparedListener { player ->
                player.start()
                isLocalPreparing = false
                isLocalPlaying = true
            }
            localPlayer.setOnCompletionListener { isLocalPlaying = false }
            localPlayer.setOnErrorListener { _, _, _ ->
                isLocalPreparing = false
                isLocalPlaying = false
                true
            }
            localPlayer.prepareAsync()
        } catch (_: Exception) {
            isLocalPreparing = false
            scope.launch { snackbarHostState.showSnackbar("Impossible de lire l'enregistrement.") }
        }
    }

    fun loadNotes() {
        if (token.isNullOrBlank()) {
            loading = false
            notes = emptyList()
            return
        }
        scope.launch {
            loading = true
            when (val result = BackendApi.getMyNotes(token)) {
                is ApiResult.Success -> {
                    notes = result.data.map {
                        NoteItem(
                            id = it.id.toString(),
                            noteType = it.noteType,
                            title = it.title,
                            content = it.content,
                            createdAt = parseIsoMillis(it.createdAt),
                            audioUrl = it.audioUrl
                        )
                    }
                    loading = false
                }

                is ApiResult.Failure -> {
                    loading = false
                    if (result.unauthorized) {
                        onUnauthorized()
                    } else {
                        snackbarHostState.showSnackbar(result.message)
                    }
                }
            }
        }
    }

    LaunchedEffect(token) { loadNotes() }

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
            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Retour")
                }
                Text("Retour", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(8.dp))
            Text("Notes", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = dark)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { mode = NoteMode.Text },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mode == NoteMode.Text) dark else Color.White,
                        contentColor = if (mode == NoteMode.Text) Color.White else dark
                    )
                ) {
                    Text("Texte")
                }
                Button(
                    onClick = { mode = NoteMode.Voice },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mode == NoteMode.Voice) Color(0xFF2563EB) else Color.White,
                        contentColor = if (mode == NoteMode.Voice) Color.White else dark
                    )
                ) {
                    Text("Vocal")
                }
            }

            Spacer(Modifier.height(12.dp))
            if (mode == NoteMode.Text) {
                OutlinedTextField(
                    value = textContent,
                    onValueChange = { textContent = it },
                    label = { Text("Contenu") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )

                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (token.isNullOrBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Connectez-vous pour enregistrer une note.") }
                            return@Button
                        }
                        if (title.isBlank() || textContent.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Titre et contenu obligatoires.") }
                            return@Button
                        }
                        scope.launch {
                            when (val result = BackendApi.createNote(token, title.trim(), textContent.trim())) {
                                is ApiResult.Success -> {
                                    title = ""
                                    textContent = ""
                                    loadNotes()
                                    snackbarHostState.showSnackbar("Note enregistrée")
                                }

                                is ApiResult.Failure -> {
                                    if (result.unauthorized) onUnauthorized() else snackbarHostState.showSnackbar(result.message)
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ajouter la note")
                }
            } else {
                OutlinedTextField(
                    value = voiceDescription,
                    onValueChange = { voiceDescription = it },
                    label = { Text("Contenu (optionnel)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(Modifier.height(10.dp))

                if (!hasAudioPermission) {
                    Text(
                        "Autorisez l'accès au micro pour créer un mémo vocal.",
                        color = Color(0xFFB91C1C),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(4.dp))
                }

                Button(
                    onClick = {
                        if (isRecording) stopRecording() else startRecording()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) Color(0xFFDC2626) else Color(0xFF2563EB),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Filled.MicNone else Icons.Filled.Mic,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (isRecording) "Arrêter l'enregistrement" else "Enregistrer un mémo vocal")
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    text = when {
                        isRecording -> "Enregistrement en cours..."
                        recordedFile != null -> "Enregistrement prêt : ${recordedFile?.name}"
                        else -> "Appuyez sur 'Enregistrer un mémo vocal' pour démarrer."
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF4B5563)
                )

                if (recordedFile != null) {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(onClick = ::toggleLocalPlayback) {
                            Icon(
                                imageVector = if (isLocalPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = null
                            )
                        }
                        Text(
                            if (isLocalPlaying) "Pause" else "Lire l'enregistrement",
                            color = Color(0xFF2563EB)
                        )
                        if (isLocalPreparing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (token.isNullOrBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Connectez-vous pour ajouter un mémo vocal.") }
                            return@Button
                        }
                        if (title.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Un titre est requis pour le mémo vocal.") }
                            return@Button
                        }
                        val file = recordedFile
                        if (file == null) {
                            scope.launch { snackbarHostState.showSnackbar("Enregistrement requis pour le mémo vocal.") }
                            return@Button
                        }
                        scope.launch {
                            isUploadingVoice = true
                            when (val result = BackendApi.createVoiceNote(
                                token,
                                title.trim(),
                                voiceDescription.trim().takeIf { it.isNotBlank() },
                                file.absolutePath
                            )) {
                                is ApiResult.Success -> {
                                    title = ""
                                    voiceDescription = ""
                                    recordedFile = null
                                    isLocalPlaying = false
                                    file.delete()
                                    loadNotes()
                                    snackbarHostState.showSnackbar("Mémo vocal enregistré")
                                }

                                is ApiResult.Failure -> {
                                    if (result.unauthorized) onUnauthorized() else snackbarHostState.showSnackbar(result.message)
                                }
                            }
                            isUploadingVoice = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploadingVoice && recordedFile != null && title.isNotBlank()
                ) {
                    if (isUploadingVoice) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text("Ajouter le mémo vocal")
                }
            }

            Spacer(Modifier.height(14.dp))

            if (loading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(notes, key = { it.id }) { note ->
                        NoteRow(note = note, onDelete = {
                            val noteId = note.id.toIntOrNull() ?: return@NoteRow
                            scope.launch {
                                when (val result = BackendApi.deleteNote(token.orEmpty(), noteId)) {
                                    is ApiResult.Success -> loadNotes()
                                    is ApiResult.Failure -> {
                                        if (result.unauthorized) onUnauthorized() else snackbarHostState.showSnackbar(result.message)
                                    }
                                }
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteRow(
    note: NoteItem,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val remotePlayer = remember { MediaPlayer() }
    var isRemotePlaying by remember { mutableStateOf(false) }
    var isRemoteLoading by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        remotePlayer.setOnCompletionListener { isRemotePlaying = false }
        remotePlayer.setOnErrorListener { _, _, _ ->
            isRemotePlaying = false
            isRemoteLoading = false
            true
        }
        onDispose {
            remotePlayer.reset()
            remotePlayer.release()
        }
    }

    fun toggleRemotePlayback() {
        val url = note.audioUrl ?: return
        if (isRemotePlaying) {
            remotePlayer.pause()
            isRemotePlaying = false
            return
        }
        try {
            isRemoteLoading = true
            remotePlayer.reset()
            remotePlayer.setDataSource(context, Uri.parse(url))
            remotePlayer.setOnPreparedListener { player ->
                player.start()
                isRemotePlaying = true
                isRemoteLoading = false
            }
            remotePlayer.prepareAsync()
        } catch (_: Exception) {
            isRemoteLoading = false
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp), clip = false),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(note.title, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0B1220))
                Spacer(Modifier.height(4.dp))
                Text(
                    if (note.content.isBlank()) {
                        if (note.noteType == "voice") "Mémo vocal" else ""
                    } else {
                        note.content
                    },
                    color = Color(0xFF374151),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (note.noteType == "voice") "Mémo vocal" else "Note texte",
                        color = Color(0xFF2563EB),
                        fontSize = 12.sp
                    )
                    if (note.audioUrl != null) {
                        IconButton(onClick = ::toggleRemotePlayback, enabled = !isRemoteLoading) {
                            Icon(
                                imageVector = if (isRemotePlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = null
                            )
                        }
                        if (isRemoteLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        }
                        Text("Lire", color = Color(0xFF2563EB), fontSize = 12.sp)
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Supprimer", tint = Color(0xFFEF4444))
            }
        }
    }
}

private fun parseIsoMillis(value: String): Long {
    return try {
        OffsetDateTime.parse(value).toInstant().toEpochMilli()
    } catch (_: DateTimeParseException) {
        System.currentTimeMillis()
    }
}
