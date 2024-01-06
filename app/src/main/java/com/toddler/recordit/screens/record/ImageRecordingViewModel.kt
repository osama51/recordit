package com.toddler.recordit.screens.record

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.toddler.recordit.playback.AndroidAudioPlayer
import com.toddler.recordit.recorder.AndroidAudioRecorder
import com.toddler.recordit.utils.pathConfig
import com.toddler.recordit.utils.getImagesFromAssets
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class ImageRecordingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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
            imagePath = imageMap.entries.first().value,
            recorded = false
        )
    }

    private val audioRecorder = AndroidAudioRecorder(context)
    private val audioPlayer = AndroidAudioPlayer(context)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _recordedFilePath = MutableStateFlow("")
    val recordedFilePath: StateFlow<String> = _recordedFilePath

    private val _audioFile = MutableStateFlow<File?>(null)
    val audioFile: StateFlow<File?> = _audioFile


    private val _numberOfImages = MutableStateFlow(0)
    val numberOfImages: StateFlow<Int> = _numberOfImages

    private val _numberOfImagesRecorded = MutableStateFlow(0)
    val numberOfImagesRecorded: StateFlow<Int> = _numberOfImagesRecorded

    private val _numberOfImagesNotRecorded = MutableStateFlow(0)
    val numberOfImagesNotRecorded: StateFlow<Int> = _numberOfImagesNotRecorded

    // State flows and other properties remain the same

    init {
        _numberOfImages.value = itemList.size
        determineNumberOfImagesRecorded()
        determineNumberOfImagesNotRecorded()

    }


    fun determineNumberOfImagesRecorded() {
        _numberOfImagesRecorded.value = itemList.filter { it.recorded }.size
        Log.i("RecordScreen", "determineNumberOfImagesRecorded() called | ${_numberOfImagesRecorded.value}")
    }

    fun determineNumberOfImagesNotRecorded() {
        _numberOfImagesNotRecorded.value = itemList.filter { !it.recorded }.size
        Log.i("RecordScreen", "determineNumberOfImagesNotRecorded() called | ${_numberOfImagesNotRecorded.value}")
    }

    fun startRecording(outputFile: File) {
        try {
            audioRecorder.start(outputFile)
            Log.i("RecordScreen", "Started Recording | Recording: ${_recordedFilePath.value}")
            _recordedFilePath.value =
                outputFile.absolutePath
            _audioFile.value = outputFile
            _isRecording.value = true
        } catch (e: Exception) {
            // Handle recording errors
        }
    }

    fun isRecording(): Boolean {
        return _isRecording.value
    }

    fun stopRecording() {
        // wait for a delay to ensure the file is written
        Thread.sleep(500)
        audioRecorder.stop()
        _isRecording.value = false
        Log.i("RecordScreen", "Stopped Recording | Recording: ${_recordedFilePath.value}")
    }

    fun startPlayback(file: File) {
        try {
            audioPlayer.playFile(_audioFile.value ?: return)
            audioPlayer.triggerWhenFinished {
                Log.i("RecordScreen", "Finished Playing")
            }
        } catch (e: Exception) {
            // Handle playback errors
        }
    }

    fun isPlaying(): Boolean {
        return audioPlayer.isPlaying()
    }

    fun triggerWhenFinished(callback: () -> Unit) {
        audioPlayer.triggerWhenFinished(callback)
    }

    fun stopPlayback() {
        audioPlayer.stop()
    }


    fun returnUri(imageTitle: String): Uri {
//        viewModelScope.launch{
//        val pathConfig = pathConfig(suffix = imageTitle.replace(" ", "_"))
//        // set the Uri of the file from the csvConfig hostPath and fileName
//        val uri = Uri.parse("${pathConfig.hostPath}/${pathConfig.fileName}")
//        Log.i("AssessViewModel", "Uri: $uri")
        // instead, access the directory that the system provides for my app
        val uri = Uri.parse("${context.filesDir}/${imageTitle.replace(" ", "_")}.mp3")

        return uri
//        }
    }

    // onCleared() for resource release

    // save ItemList to json file using gson library
    fun saveItemListToJson(){
        val gson = Gson()
        val json = gson.toJson(itemList)
        val file = File(context.filesDir, "itemList.json")
        file.writeText(json)
        Log.i("RecordScreen", "itemList saved to json | ${file.absolutePath}")
    }

    // load ItemList from json file using gson library
    fun loadItemListFromJson(){
        val gson = Gson()
        val file = File(context.filesDir, "itemList.json")
        val json = file.readText()
        val itemListFromJson = gson.fromJson(json, Array<RecordItem>::class.java).toList()
        Log.i("RecordScreen", "itemList loaded from json | ${file.absolutePath}")
        Log.i("RecordScreen", "itemList loaded from json | ${itemListFromJson}")
    }

    // function to take a string, replace underscores with spaces, and capitalize each word
    private fun String.capitalizeWords(): String = split("_").joinToString(" ") {
        it.replaceFirstChar { firstChar ->
            if (firstChar.isLowerCase()) firstChar.titlecase(
                Locale.ROOT
            ) else firstChar.toString()
        }
    }

}
