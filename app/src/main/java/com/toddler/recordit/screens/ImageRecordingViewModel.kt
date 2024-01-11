package com.toddler.recordit.screens

import android.Manifest
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.toddler.recordit.MyApplication
import com.toddler.recordit.R
import com.toddler.recordit.playback.AndroidAudioPlayer
import com.toddler.recordit.recorder.AndroidAudioRecorder
import com.toddler.recordit.screens.record.RecordItem
import com.toddler.recordit.utils.UnzipListener
import com.toddler.recordit.utils.UnzipUtils
import com.toddler.recordit.utils.UnzipUtilsWithListeners
import com.toddler.recordit.utils.getImagesFromAssets
import com.toddler.recordit.utils.getImagesFromFilesDir
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

    var newZip = false

    private val _zipVersion = MutableStateFlow(sharedPreferences.getInt("imagesVersion", -1))
    val zipVersion: StateFlow<Int> = _zipVersion


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
        getImagesBasedOnVersion()
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

    fun navigateToNextItem() {
        if (isPlaying()) {
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

    fun navigateToPreviousItem() {
        if (isPlaying()) {
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


    fun determineNumberOfImagesRecorded() {
        _numberOfImagesRecorded.value = _itemList.value.filter { it.recorded }.size
        Log.i(
            "RecordScreen",
            "determineNumberOfImagesRecorded() called | ${_numberOfImagesRecorded.value}"
        )
    }

    fun determineNumberOfImagesNotRecorded() {
        _numberOfImagesNotRecorded.value = _itemList.value.filter { !it.recorded }.size
        Log.i(
            "RecordScreen",
            "determineNumberOfImagesNotRecorded() called | ${_numberOfImagesNotRecorded.value}"
        )
    }

    fun isPermissionGranted(): Boolean {
        val recordPermissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        )
        return recordPermissionCheck == PERMISSION_GRANTED
    }

    fun startRecording(outputFile: File) {
        try {
            audioRecorder.start(outputFile)
            // check if audioRecorder started successfully

            // TODO: Check what's causing the next and previous buttons to recompose
            //  when the recording and then again when stopping~!

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

    private fun updateIsPlaying() {
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

    fun returnFile(): File {
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
//        val uidDir = "${context.filesDir}/${application.firebaseAuth.currentUser?.uid ?: "default"}"
        val uidDir = context.getDir(application.firebaseAuth.currentUser?.uid ?: "default", MODE_PRIVATE).absolutePath

        return Uri.parse("${uidDir}/${imageTitle.replace(" ", "_")}.mp3")
//        }
    }

    // onCleared() for resource release

    // note that I'm using gson
    fun saveItemListToJson() {
        val gson = Gson()
        val json = gson.toJson(_itemList.value)
//        val uidDir = "${context.filesDir}/${application.firebaseAuth.currentUser?.uid ?: "default"}"
        val uidDir = context.getDir(application.firebaseAuth.currentUser?.uid ?: "default", MODE_PRIVATE).absolutePath
        val file = File(uidDir, JSON_FILE_NAME)
        file.writeText(json)
        Log.i("RecordScreen", "itemList saved to json | ${file.absolutePath}")
    }


    fun loadItemListFromJson() {
        val gson = Gson()
//        val uidDir = "${context.filesDir}/${application.firebaseAuth.currentUser?.uid ?: "default"}"
        val uidDir = context.getDir(application.firebaseAuth.currentUser?.uid ?: "default", MODE_PRIVATE).absolutePath
        val file = File(uidDir, JSON_FILE_NAME)
        val json = file.readText()
        val itemListFromJson = gson.fromJson(json, Array<RecordItem>::class.java).toList()
        Log.i("RecordScreen", "itemList loaded from json | ${file.absolutePath}")
//        Log.i("RecordScreen", "itemList loaded from json | ${itemListFromJson}")
        _itemList.value = itemListFromJson
    }

    private fun checkIfFileExists(fileName: String): Boolean {
//        val uidDir = "${context.filesDir}/${application.firebaseAuth.currentUser?.uid ?: "default"}"
        val uidDir = context.getDir(application.firebaseAuth.currentUser?.uid ?: "default", MODE_PRIVATE).absolutePath
        val file = File(uidDir, fileName)
        return file.exists()
    }

    // this will be used for the images.zip file only, since it is shared between all users
    private fun checkIfFileExistsInFilesDir(fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return file.exists()
    }


    private fun initializeItemList() {
        if (checkIfFileExists(JSON_FILE_NAME)) {
            loadItemListFromJson()
            if (newZip) {
                compareAndMergeItemList()
                newZip = false
            }

        } else {
            createItemList()
        }
    }

    private fun createItemList() {
        _itemList.value = getImagesFromFilesDir(context = context).mapIndexed { index, imageMap ->
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
        saveItemListToJson()
    }

    /**
     * Compare the old itemList with the newly unzipped images,
     * if there's a duplicate, keep the recorded property of the
     * old item otherwise, give it the default value of false,
     * fill the newImages then assign it to the itemList.
     *
     * */
    private fun compareAndMergeItemList() {
        val newImages = getImagesFromFilesDir(context = context).mapIndexed { index, imageMap ->
            var imageName = imageMap.entries.first().key
            imageName = imageName.dropLast(4).capitalizeWords()

            // check if the image already exists in the itemList by its title
            val imageExists = _itemList.value.any { it.title == imageName }
            Log.i("RecordScreen", "newList occupied~!")
            RecordItem(
                id = index,
                title = imageName, //imageMap.toString().substring(7),
                description = "Description ${imageName}",
                imagePath = imageMap.entries.first().value,
                recorded = if (imageExists) _itemList.value.first { it.title == imageName }.recorded else false
            )

        }
        _itemList.value = newImages
        saveItemListToJson()
    }

    /**
     * Update the recorded property of the item in the itemList
     * once the recording is done.
     * Re-evaluate the number of images recorded and not recorded.
     * @param item the item instance passed from the RecordScreen
     * */
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

    private fun getImagesBasedOnVersion(): Int{
        var version = 0
        val storageRef = application.storage.reference
        val imagesVerRef = storageRef.child("images/version.txt")
        val localVerFile = File(context.filesDir, "version.txt")
        imagesVerRef.getFile(localVerFile).addOnSuccessListener {
            Log.i(
                "RecordScreen",
                "fetchImagesFromFirebaseCloudStorage() | version downloaded successfully"
            )

            version = extractVersionFromTextFile(localVerFile)
            sharedPreferences.getInt("imagesVersion", -1).let {
                if (version > it || !checkIfFileExistsInFilesDir("images.zip")) {
                    sharedPreferences.edit().putInt("imagesVersion", version).apply()
                    fetchImagesFromFirebaseCloudStorage()
                    _zipVersion.value = version
                } else {
                    unzipImages()
                }
            }

        }.addOnFailureListener {
            prepareToDisplay()
            Log.i(
                "RecordScreen",
                "fetchImagesFromFirebaseCloudStorage() | version download failed | error: $it"
            )
        }
        return version
    }

    private fun extractVersionFromTextFile(file: File): Int{
        var version = 0
        try {
            val fileText = file.readText()
            // remove all non-digit characters
            val regex = Regex("[^0-9]")
            val versionString = regex.replace(fileText, "")
            version = versionString.toInt()
            Log.i("RecordScreen", "extractTextFromTextFile() | text: $version")
        } catch (e: Exception) {
            Log.i("RecordScreen", "extractTextFromTextFile() | error: $e")
        }
        return version
    }

    private fun fetchImagesFromFirebaseCloudStorage() {
        // Create a storage reference from my app
        val storageRef = application.storage.reference

        // downloading all files in a folder is not possible so instead we'll download a zip file
        val imagesRef = storageRef.child("images/images.zip")
        val localFile = File(context.filesDir, "images.zip")
        imagesRef.getFile(localFile).addOnSuccessListener {
            Log.i(
                "RecordScreen",
                "fetchImagesFromFirebaseCloudStorage() | images downloaded successfully"
            )
            Toast.makeText(context, "Images downloaded successfully", Toast.LENGTH_SHORT).show()
            newZip = true
            unzipImages()

        }.addOnFailureListener {
            Log.i(
                "RecordScreen",
                "fetchImagesFromFirebaseCloudStorage() | images download failed | error: $it"
            )
        }
    }


    private fun unzipImages() {
        if (!sharedPreferences.getBoolean("imagesUnzipped", false) || newZip) {
            val imagesZipDir = File(context.filesDir, "images.zip")
            val destDir = context.getDir("images", MODE_PRIVATE).absolutePath
            UnzipUtilsWithListeners.unzip(imagesZipDir, destDir, object : UnzipListener {
                override fun onUnzipComplete() {
                    Log.i(
                        "RecordScreen",
                        "fetchImagesFromFirebaseCloudStorage() | images unzip complete | destDir: $destDir"
                    )
                    Log.i(
                        "RecordScreen",
                        "fetchImagesFromFirebaseCloudStorage() | images unzip complete | listFiles: ${
                            File(destDir).listFiles()
                        }"
                    )
                    Toast.makeText(context, "Images unzip complete", Toast.LENGTH_SHORT).show()

                    sharedPreferences.edit().putBoolean("imagesUnzipped", true).apply()
                    prepareToDisplay()

                }

                override fun onUnzipFailed(error: Exception) {
                    Log.i(
                        "RecordScreen",
                        "fetchImagesFromFirebaseCloudStorage() | images unzip failed: $error"
                    )
                    Toast.makeText(context, "Images unzip failed", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            prepareToDisplay()
        }
    }

    private fun prepareToDisplay() {
        initializeItemList()

        _numberOfImages.value = itemList.value.size
        determineNumberOfImagesRecorded()
        determineNumberOfImagesNotRecorded()

        updateCurrentItem()
    }

    // loop over the audio files in the uidDir and upload them to firebase storage
    fun uploadAudioFiles() {
        val uidDir = context.getDir(application.firebaseAuth.currentUser?.uid ?: "default", MODE_PRIVATE).absolutePath
        val uidDirFile = File(uidDir)
        val audioFiles = uidDirFile.listFiles()
        audioFiles?.forEach { file ->
            val storageRef = application.storage.reference
            val audioRef = storageRef.child("${application.firebaseAuth.currentUser?.uid}/${file.name}")
            audioRef.putFile(Uri.fromFile(file)).addOnSuccessListener {
                Log.i("RecordScreen", "uploadAudioFiles() | ${file.name} uploaded successfully")
            }.addOnFailureListener {
                Log.i("RecordScreen", "uploadAudioFiles() | ${file.name} upload failed | error: $it")
            }
        }
    }


    fun logOut() {
        application.firebaseAuth.signOut()
//        sharedPreferences.edit().clear().apply()
    }


    // function to take a string, replace underscores with spaces, and capitalize each word
    private fun String.capitalizeWords(): String = split("_").joinToString(" ") {
        it.replaceFirstChar { firstChar ->
            if (firstChar.isLowerCase()) firstChar.titlecase(
                Locale.ROOT
            ) else firstChar.toString()
        }
    }


    companion object {
        const val JSON_FILE_NAME = "itemList.json"
    }

}
