package com.toddler.recordit.screens.record

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.toddler.recordit.playback.AndroidAudioPlayer
import com.toddler.recordit.recorder.AndroidAudioRecorder
import com.toddler.recordit.utils.getImagesFromAssets
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject


@HiltViewModel
class ImageRecordingViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : ViewModel() {

//    private val context = WeakReference(application.applicationContext)

    val itemList = getImagesFromAssets(context = context).mapIndexed { index, imageMap ->
        var imageName = imageMap.entries.first().key
        imageName = imageName.dropLast(4).capitalizeWords()
        Log.i("RecordScreen", "itemList re-occupied !!")
        RecordItem(
            id = index,
            title = imageName, //imageMap.toString().substring(7),
            description = "Description ${imageName}",
            imagePath = imageMap.entries.first().value
        )
    }


    private val audioRecorder = AndroidAudioRecorder(context)
    private val audioPlayer = AndroidAudioPlayer(context)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _recordedFilePath = MutableStateFlow("")
    val recordedFilePath: StateFlow<String> = _recordedFilePath

    private val _numberOfImages = MutableStateFlow(0)
    val numberOfImages: StateFlow<Int> = _numberOfImages

    // State flows and other properties remain the same

    init {
        _numberOfImages.value = itemList.size
    }
    fun startRecording(outputFile: File) {
        try {
            audioRecorder.start(outputFile)
            _isRecording.value = true
            _recordedFilePath.value =
                outputFile.absolutePath
        } catch (e: Exception) {
            // Handle recording errors
        }
    }

    fun stopRecording() {
        audioRecorder.stop()
        _isRecording.value = false
    }

    fun startPlayback(file: File) {
        try {
            audioPlayer.playFile(file)
        } catch (e: Exception) {
            // Handle playback errors
        }
    }

    fun stopPlayback() {
        audioPlayer.stop()
    }

    // onCleared() for resource release
}
