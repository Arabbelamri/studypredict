package com.example.studypredict.audio

import android.media.MediaPlayer
import java.io.File

class VoiceMemoPlayer {
    private var player: MediaPlayer? = null
    private var currentPath: String? = null

    fun play(file: File, onComplete: () -> Unit) {
        stop()
        currentPath = file.absolutePath
        player = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            start()
            setOnCompletionListener {
                stop()
                onComplete()
            }
        }
    }

    fun stop() {
        try { player?.stop() } catch (_: Exception) {}
        try { player?.release() } catch (_: Exception) {}
        player = null
        currentPath = null
    }

    fun isPlaying(file: File): Boolean =
        player != null && currentPath == file.absolutePath
}