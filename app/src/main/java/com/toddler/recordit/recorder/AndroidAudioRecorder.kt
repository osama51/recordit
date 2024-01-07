package com.toddler.recordit.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class AndroidAudioRecorder(
    private val context: Context
): AudioRecorder {

    private var recorder: MediaRecorder? = null

    private fun createRecorder(): MediaRecorder {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
    }

    override fun start(outputFile: File) {
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd) // works for lower api levels

            prepare()
            start()

            recorder = this
        }
    }

    override fun stop() {
        Log.i("AndroidAudioRecorder", "$recorder")
        recorder?.apply {
            try{
                stop()
                reset()
            } catch (e: Exception){
                Log.e("AndroidAudioRecorder", "Error stopping recorder", e)
                Thread.sleep(300)
                try {
                    stop()
                    reset()
                } catch (e: Exception){
                    Log.e("AndroidAudioRecorder", "Error stopping recorder again", e)
                }
            }
        }
        recorder = null

    }
}