package com.toddler.recordit.screens

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.toddler.recordit.MyApplication
import com.toddler.recordit.R
import com.toddler.recordit.playback.AndroidAudioPlayer
import com.toddler.recordit.recorder.AndroidAudioRecorder
import com.toddler.recordit.screens.record.RecordItem
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


    val applicationContext = context

    val application = applicationContext as MyApplication

    val sharedPreferences = application.getSharedPreferences(application.packageName, MODE_PRIVATE)


    private val _itemList = MutableStateFlow(listOf<RecordItem>())
    val itemList: StateFlow<List<RecordItem>> = _itemList

    private val _currentItem = MutableStateFlow<RecordItem?>(null)
    val currentItem: StateFlow<RecordItem?> = _currentItem

    private val _currentItemIndex = MutableStateFlow(0)
    val currentItemIndex: StateFlow<Int> = _currentItemIndex

    private val audioRecorder = AndroidAudioRecorder(context)
    private val audioPlayer = AndroidAudioPlayer(context)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _buttonIcon = MutableStateFlow(R.drawable.ic_play)
    val buttonIcon: StateFlow<Int> = _buttonIcon

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
        initializeItemList()

        _numberOfImages.value = itemList.value.size
        determineNumberOfImagesRecorded()
        determineNumberOfImagesNotRecorded()

        updateCurrentItem()
    }

    private fun updateCurrentItem() {
        // set current item as first item not recorded
        _currentItem.value = _itemList.value.firstOrNull { !it.recorded } ?: _itemList.value.last()
        _currentItemIndex.value = _itemList.value.indexOf(_currentItem.value)

        /**
         * If no matching element is found, it returns null instead of throwing an exception.
         * This allows for safer and more graceful handling of cases where no suitable element exists.
         *
         * */

        _audioFile.value = returnFile()
        Log.i("RecordScreen", "updateCurrentItem() called | ${_currentItem.value}")
    }

    fun navigateToNextItem(){
        if(isPlaying()){
            stopPlayback()
        }
        /**
         *لا تنسي أخاك, ترعاه يداك
         * لا تنسي أخاك, ترعاه يداك
         *
         * */
        _currentItem.value = _itemList.value[_currentItemIndex.value + 1] // أنت
        _audioFile.value = returnFile()                                   // أخوك
    }

    fun navigateToPreviousItem(){
        if(isPlaying()){
            stopPlayback()
        }
        /**
         *لا تنسي أخاك, ترعاه يداك
         * لا تنسي أخاك, ترعاه يداك
         *
         * */
        _currentItem.value = _itemList.value[_currentItemIndex.value - 1] // أنت
        _audioFile.value = returnFile()                                   // أخوك
    }

    fun canNavigateToNextItem(): Boolean {
        _currentItemIndex.value = _itemList.value.indexOf(_currentItem.value)
        // check if it's the last item and if it is not recorded (both conditions prevent going to next item)
        return _currentItemIndex.value != _itemList.value.lastIndex && _itemList.value[_currentItemIndex.value].recorded
    }

    fun canNavigateToPreviousItem(): Boolean {
        _currentItemIndex.value = _itemList.value.indexOf(_currentItem.value)
        // check if it's the first item
        return _currentItemIndex.value != 0
    }

    private fun initializeItemList() {
        if (checkIfFileExists(JSON_FILE_NAME)) {
            loadItemListFromJson()
        } else {
            createItemList()
        }
    }

    private fun createItemList() {
        _itemList.value = getImagesFromAssets(context = context).mapIndexed { index, imageMap ->
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
    }

    fun updateItemList(item: RecordItem) {
        val updatedItemList = _itemList.value.map {
            if (it.id == item.id) {
                it.copy(recorded = item.recorded)
            } else {
                it
            }
        }
        _itemList.value = updatedItemList
        Log.i("RecordScreen", "itemList updated !!")
        determineNumberOfImagesRecorded()
        determineNumberOfImagesNotRecorded()
    }


    fun determineNumberOfImagesRecorded() {
        _numberOfImagesRecorded.value = _itemList.value.filter { it.recorded }.size
        Log.i("RecordScreen", "determineNumberOfImagesRecorded() called | ${_numberOfImagesRecorded.value}")
    }

    fun determineNumberOfImagesNotRecorded() {
        _numberOfImagesNotRecorded.value = _itemList.value.filter { !it.recorded }.size
        Log.i("RecordScreen", "determineNumberOfImagesNotRecorded() called | ${_numberOfImagesNotRecorded.value}")
    }

    fun startRecording(outputFile: File) {
        try {
            audioRecorder.start(outputFile)
            // check if audioRecorder started successfully



            Log.i("RecordScreen", "Started Recording")
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
//        Thread.sleep(500) // instead, checked the press duration and added coroutine delay
        audioRecorder.stop()
        _isRecording.value = false
        Log.i("RecordScreen", "Stopped Recording | Recording: ${_recordedFilePath.value}")
    }

    fun startPlayback() {
        try {
            audioPlayer.playFile(_audioFile.value ?: return)
            _buttonIcon.value = R.drawable.ic_stop
            audioPlayer.triggerWhenFinished {
                Log.i("RecordScreen", "Finished Playing")
                _buttonIcon.value = R.drawable.ic_play
                updateIsPlaying()
            }
        } catch (e: Exception) {
            // Handle playback errors
        }
    }

    fun isPlaying(): Boolean {
        return audioPlayer.isPlaying()
    }

    private fun updateIsPlaying(){
        _isPlaying.value = audioPlayer.isPlaying()
    }

    fun triggerWhenFinished(callback: () -> Unit) {
        audioPlayer.triggerWhenFinished(callback)
        updateIsPlaying()
    }

    fun stopPlayback() {
        audioPlayer.stop()
        _buttonIcon.value = R.drawable.ic_play
        updateIsPlaying()
    }

    fun returnFile(): File{
        var file: File? = null
        returnUri(_currentItem.value!!.title).path?.let {
            File(
                it
            )
        }?.let { file = it }
        return file!!
    }

    private fun returnUri(imageTitle: String): Uri {
//        viewModelScope.launch{
//        val pathConfig = pathConfig(suffix = imageTitle.replace(" ", "_"))
//        // set the Uri of the file from the csvConfig hostPath and fileName
//        val uri = Uri.parse("${pathConfig.hostPath}/${pathConfig.fileName}")
//        Log.i("AssessViewModel", "Uri: $uri")

        // instead, access the directory that the system provides for my app
        val uidDir = "${context.filesDir}/${application.firebaseAuth.currentUser?.uid ?: "default"}"

        return Uri.parse("${context.filesDir}/${imageTitle.replace(" ", "_")}.mp3")
//        }
    }

    // onCleared() for resource release

    // note that I'm using gson
    fun saveItemListToJson(){
        val gson = Gson()
        val json = gson.toJson(_itemList.value)
        val uidDir = "${context.filesDir}/${application.firebaseAuth.currentUser?.uid ?: "default"}"
        val file = File(context.filesDir, JSON_FILE_NAME)
        file.writeText(json)
        Log.i("RecordScreen", "itemList saved to json | ${file.absolutePath}")
    }


    fun loadItemListFromJson(){
        val gson = Gson()
        val uidDir = "${context.filesDir}/${application.firebaseAuth.currentUser?.uid ?: "default"}"
        val file = File(context.filesDir, JSON_FILE_NAME)
        val json = file.readText()
        val itemListFromJson = gson.fromJson(json, Array<RecordItem>::class.java).toList()
        Log.i("RecordScreen", "itemList loaded from json | ${file.absolutePath}")
//        Log.i("RecordScreen", "itemList loaded from json | ${itemListFromJson}")
        _itemList.value = itemListFromJson
    }

    private fun checkIfFileExists(fileName: String): Boolean {
        val uidDir = "${context.filesDir}/${application.firebaseAuth.currentUser?.uid ?: "default"}"
        val file = File(context.filesDir, fileName)
        return file.exists()
    }

    // function to take a string, replace underscores with spaces, and capitalize each word
    private fun String.capitalizeWords(): String = split("_").joinToString(" ") {
        it.replaceFirstChar { firstChar ->
            if (firstChar.isLowerCase()) firstChar.titlecase(
                Locale.ROOT
            ) else firstChar.toString()
        }
    }

    fun logOut() {
        application.firebaseAuth.signOut()
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        const val JSON_FILE_NAME = "itemList.json"
    }

}
