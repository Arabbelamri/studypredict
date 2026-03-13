package com.example.studypredict.audio

import android.content.Context
import android.media.MediaRecorder
import java.io.File

class VoiceMemoRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null

    fun startRecording(): File {
        val dir = File(context.filesDir, "voice_memos").apply { mkdirs() }
        val file = File(dir, "memo_${System.currentTimeMillis()}.m4a")

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        return file
    }

    fun stopRecording(): File? {
        return try {
            recorder?.stop()
            recorder?.release()
            recorder = null
            // le dernier fichier est celui qui a été créé, mais on le gère côté UI
            null
        } catch (_: Exception) {
            try { recorder?.release() } catch (_: Exception) {}
            recorder = null
            null
        }
    }

    fun isRecording(): Boolean = recorder != null
}
