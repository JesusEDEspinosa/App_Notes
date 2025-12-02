
package com.example.appnotes

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.FileOutputStream

interface AudioRecorder {
    fun start(outputFile: File)
    fun stop()
}

class AudioRecorderImpl(
    private val context: Context
): AudioRecorder {
    private var recorder: MediaRecorder? = null

    override fun start(outputFile: File) {
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC_ELD)
            setOutputFile(FileOutputStream(outputFile).fd)
            prepare()
            start()
        }
    }

    override fun stop() {
        recorder?.stop()
        recorder?.reset()
        recorder = null
    }
}
