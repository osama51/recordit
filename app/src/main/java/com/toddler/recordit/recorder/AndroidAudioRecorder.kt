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
//            setAudioSource(MediaRecorder.AudioSource.MIC)
//            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//            setOutputFile(FileOutputStream(outputFile).fd) // works for lower api levels

            /** for m4a audio files (actually not bad) */
//            setAudioSource(MediaRecorder.AudioSource.MIC)
//            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//            setAudioEncodingBitRate(16*44100)
//            setAudioSamplingRate(44100)
//            setOutputFile(FileOutputStream(outputFile).fd) // works for lower api levels


            /** for wav audio files */

            /**
             * Although WAV isn't directly available, using THREE_GPP with the correct encoder and settings will create a lossless WAV file.
             *
             * AMR_NB is the encoder for Adaptive Multi-Rate Narrowband audio, which produces uncompressed PCM data for WAV files.
             *
             * The standard sampling rate for high-quality audio is 44100 Hz.
             *
             *  As for EncodingBitRate, WAV is a lossless format and doesn't involve bit rate compression
             *
             * */
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setAudioSamplingRate(44100)
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