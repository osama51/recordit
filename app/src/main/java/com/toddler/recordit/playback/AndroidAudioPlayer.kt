package com.toddler.recordit.playback

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.core.net.toUri
import java.io.File

class AndroidAudioPlayer(
    private val context: Context
) : AudioPlayer {


    private var player: MediaPlayer? = null

    override fun playFile(file: File) {
        Log.i("AndroidAudioPlayer", "Playing file: ${file.absolutePath}")
        MediaPlayer.create(context, file.toUri()).apply {
            player = this
            start()
        }
    }

    fun isPlaying(): Boolean {
        return player?.isPlaying ?: false
    }

    fun triggerWhenFinished(callback: () -> Unit) {
        player?.setOnCompletionListener {
            Log.i("AndroidAudioPlayer", "Finished Playing")
            callback()
        }
    }

    override fun stop() {
        player?.stop()
        player?.release()
        player = null
    }
}