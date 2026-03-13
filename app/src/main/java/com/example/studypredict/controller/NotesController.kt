package com.example.studypredict.controller

import android.content.Context
import com.example.studypredict.model.TextNote
import com.example.studypredict.model.VoiceNote
import java.io.File

class NotesController(
    private val context: Context
) {
    private val voiceDir = File(context.filesDir, "voice_memos").apply { mkdirs() }
    private val textDir = File(context.filesDir, "text_notes").apply { mkdirs() }

    fun loadVoiceNotes(): List<VoiceNote> {
        return voiceDir.listFiles()
            ?.filter { it.extension.lowercase() == "m4a" }
            ?.sortedByDescending { it.lastModified() }
            ?.map {
                VoiceNote(
                    id = it.nameWithoutExtension,
                    filePath = it.absolutePath,
                    createdAt = it.lastModified()
                )
            }
            ?: emptyList()
    }

    fun loadTextNotes(): List<TextNote> {
        return textDir.listFiles()
            ?.filter { it.extension.lowercase() == "txt" }
            ?.sortedByDescending { it.lastModified() }
            ?.map { file ->
                TextNote(
                    id = file.nameWithoutExtension,
                    title = extractTitleFromFileName(file.nameWithoutExtension),
                    content = runCatching { file.readText() }.getOrDefault(""),
                    createdAt = file.lastModified()
                )
            }
            ?: emptyList()
    }

    fun saveTextNote(title: String, content: String): Boolean {
        if (title.isBlank() || content.isBlank()) return false

        val safeName = title
            .trim()
            .replace(Regex("[^a-zA-Z0-9-]"), "")
            .take(30)

        val file = File(
            textDir,
            "${System.currentTimeMillis()}_$safeName.txt"
        )

        return runCatching {
            file.writeText(content.trim())
            true
        }.getOrDefault(false)
    }

    fun deleteTextNote(note: TextNote): Boolean {
        val file = textDir.listFiles()?.firstOrNull { it.nameWithoutExtension == note.id } ?: return false
        return file.delete()
    }

    fun deleteVoiceNote(note: VoiceNote): Boolean {
        val file = File(note.filePath)
        return if (file.exists()) file.delete() else false
    }

    private fun extractTitleFromFileName(name: String): String {
        val parts = name.split("_", limit = 2)
        return if (parts.size == 2) parts[1] else name
    }
}