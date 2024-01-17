package com.toddler.recordit.screens

import android.Manifest
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.toddler.recordit.LoginActivity
import com.toddler.recordit.MyApplication
import com.toddler.recordit.R
import com.toddler.recordit.listeners.DownloadCompletionListener
import com.toddler.recordit.playback.AndroidAudioPlayer
import com.toddler.recordit.recorder.AndroidAudioRecorder
import com.toddler.recordit.screens.record.RecordItem
import com.toddler.recordit.listeners.UploadCompletionListener
import com.toddler.recordit.utils.UnzipListener
import com.toddler.recordit.utils.UnzipUtilsWithListeners
import com.toddler.recordit.utils.getImagesFromFilesDir
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt


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

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _downloadProgress = MutableStateFlow(0)
    val downloadProgress: StateFlow<Int> = _downloadProgress

    private val _uploadProgress = MutableStateFlow(0)
    val uploadProgress: StateFlow<Int> = _uploadProgress

    private val _loadingState = MutableStateFlow<LoadingStates>(LoadingStates.NONE)
    val loadingState: StateFlow<LoadingStates> = _loadingState

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    var checkedNetwork = false

    // State flows and other properties remain the same

    init {
        viewModelScope.launch {

            checkIfUserIsConnected()
            /**
             * useless use of async and await, since the firebase functions have their own coroutines
             *
             * */
//            async { getImagesBasedOnVersion() }.await()

            try {
                /**
                 * still useless for the same reason, but I'll keep it for now
                 *
                 * */
                getImagesBasedOnVersion()

            } finally {
                checkIfUserHasAudioFiles(object : DownloadCompletionListener {
                    override fun onDownloadComplete() {
                        updateItemListJson()
                        Log.i("RecordScreen", "checkIfUserHasAudioFiles() | onDownloadComplete()")
                    }

                    override fun onDownloadFailed(file: File, exception: Exception) {
                        Log.i("RecordScreen", "checkIfUserHasAudioFiles() | onDownloadFailed() | error: $exception")
                        updateItemListJson()
                    }

                    override fun onNoFilesToDownload() {
                        updateItemListJson()
                    }
                })
            }
        }

    }

    private fun updateCurrentItem() {
        if (_itemList.value.isEmpty()) return
        // set current item as first item not recorded
        _currentItem.value = _itemList.value.firstOrNull { !it.recorded } ?: _itemList.value.last()
        _currentItemIndex.value = _itemList.value.indexOf(_currentItem.value)

        /**
         * If no matching element is found, it returns null instead of throwing an exception.
         * This allows for safer and more graceful handling of cases where no suitable element exists.
         *
         * */

        _audioFile.value = returnFile()
        _loadingState.value = LoadingStates.DONE
        Log.i("RecordScreen", "updateCurrentItem() called | ${_currentItem.value}")
    }

    fun navigateToNextItem() {
        if (isPlaying()) {
            stopPlayback()
        }
        /**
         *لا تنسي أخاك, ترعاه يداك
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


    fun determineNumberOfImages() {
        _numberOfImages.value = _itemList.value.size
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
        returnUri(_currentItem.value!!.fileName).path?.let {
            File(it)
        }?.let { file = it }
        return file!!
    }

    private fun returnUri(fileName: String): Uri {
//        viewModelScope.launch{
//        val pathConfig = pathConfig(suffix = imageTitle.replace(" ", "_"))
//        // set the Uri of the file from the csvConfig hostPath and fileName
//        val uri = Uri.parse("${pathConfig.hostPath}/${pathConfig.fileName}")
//        Log.i("AssessViewModel", "Uri: $uri")

        // instead, access the directory that the system provides for my app
//        val uidDir = "${context.filesDir}/${application.firebaseAuth.currentUser?.uid ?: "default"}"
//        val uidDir = context.getDir(application.firebaseAuth.currentUser?.uid ?: "default", MODE_PRIVATE).absolutePath


        val recordsDir = context.getDir("records", MODE_PRIVATE).absolutePath
        val uidDir = File(recordsDir, application.firebaseAuth.currentUser?.uid ?: "default")
        checkAndCreateDir(uidDir)
        return Uri.parse("${uidDir}/${fileName}.wav")
//        }
    }

    // onCleared() for resource release

    /** note that I'm using gson */
    fun saveItemListToJson() {
        val gson = Gson()
        val json = gson.toJson(_itemList.value)
//        val uidDir = "${context.filesDir}/${application.firebaseAuth.currentUser?.uid ?: "default"}"
//        val uidDir = context.getDir(application.firebaseAuth.currentUser?.uid ?: "default", MODE_PRIVATE).absolutePath

        Log.i("RecordScreen", "saveItemListToJson() called | ${_itemList.value}")
        val recordsDir = context.getDir("records", MODE_PRIVATE).absolutePath
        val uidDir = File(recordsDir, application.firebaseAuth.currentUser?.uid ?: "default")
        checkAndCreateDir(uidDir)

        val file = File(uidDir, JSON_FILE_NAME)
        file.writeText(json)
        Log.i("RecordScreen", "itemList saved to json | ${file.absolutePath}")
    }

    private fun checkAndCreateDir(dir: File) {
        if (!dir.exists()) {
            if (dir.mkdir()) {
                Log.d("checkAndCreateDir", "Subfolder created: $dir")
            } else {
//                Log.e("checkAndCreateDir", "Failed to create subfolder: $dir")
            }
        } else {
//            Log.d("checkAndCreateDir", "Subfolder already exists: $dir")
        }
    }


    private fun loadItemListFromJson(jsonFileName: String): List<RecordItem> {
        val gson = Gson()
//        val uidDir = "${context.filesDir}/${application.firebaseAuth.currentUser?.uid ?: "default"}"
//        val uidDir = context.getDir(application.firebaseAuth.currentUser?.uid ?: "default", MODE_PRIVATE).absolutePath

        val recordsDir = context.getDir("records", MODE_PRIVATE).absolutePath
        val uidDir = File(recordsDir, application.firebaseAuth.currentUser?.uid ?: "default")
        if (!uidDir.exists()) return _itemList.value
        val file = File(uidDir, jsonFileName)
        val json = file.readText()
        val itemListFromJson = gson.fromJson(json, Array<RecordItem>::class.java).toList()
        Log.i("RecordScreen", "itemList loaded from json | ${file.absolutePath}")
//        Log.i("RecordScreen", "itemList loaded from json | ${itemListFromJson}")
        return itemListFromJson
    }

    private fun checkIfFileExists(fileName: String): Boolean {
//        val uidDir = "${context.filesDir}/${application.firebaseAuth.currentUser?.uid ?: "default"}"
//        val uidDir = context.getDir(application.firebaseAuth.currentUser?.uid ?: "default", MODE_PRIVATE).absolutePath

        val recordsDir = context.getDir("records", MODE_PRIVATE).absolutePath
        val uidDir = File(recordsDir, application.firebaseAuth.currentUser?.uid ?: "default")
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
            _itemList.value = loadItemListFromJson(JSON_FILE_NAME)
            if (newZip) {
                Log.i("RecordScreen", "initializeItemList() | newZip and will compare and merge")
                compareAndMergeItemList()
                newZip = false
            }
        } else {
            createItemList()
        }
    }

    private fun createItemList() {
        _itemList.value = getImagesFromFilesDir(context = context).mapIndexed { index, imageMap ->
            val fileName = imageMap.entries.first().key
            val imageName = fileName.capitalizeWords() // no need to drop the extension anymore, since I'm passing file name without extension
            Log.i("RecordScreen", "itemList re-occupied !!")
            RecordItem(
                id = index,
                fileName = fileName,
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
            val fileName = imageMap.entries.first().key
            val imageName = fileName.capitalizeWords()

            // check if the image already exists in the itemList by its title
            val imageExists = _itemList.value.any { it.fileName == fileName }
            Log.i("RecordScreen", "newList occupied~!")
            RecordItem(
                id = index,
                fileName = fileName,
                title = imageName, //imageMap.toString().substring(7),
                description = "Description ${imageName}",
                imagePath = imageMap.entries.first().value,
                recorded = if (imageExists) _itemList.value.first { it.fileName == fileName }.recorded else false
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
//        viewModelScope.launch {
//            // emit the changes in the _itemList
//            _itemList.emit(updatedItemList)
//        }
        Log.i("RecordScreen", "itemList updated !!")
//        determineNumberOfImagesRecorded()
//        determineNumberOfImagesNotRecorded()
    }

    // check internet connection
    fun checkInternetConnection(): Boolean {
        return application.checkInternetConnection()
    }

    // fun to get reference to .info/connected in firebase realtime database and check if the user is connected
    fun checkIfUserIsConnected() {
        val ref = application.database.getReference(".info/connected")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _connected.value = snapshot.getValue(Boolean::class.java) ?: false
                if (_connected.value) {
                    Log.d("checkIfUserIsConnected", "connected")
                } else {
                    Log.d("checkIfUserIsConnected", "not connected")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("checkIfUserIsConnected", "Listener was cancelled")
            }
        })
        setActiveListener()
    }

    /**
     * set any active listener to prevent Firebase from
     * closing the connection after 60 seconds of inactivity.
     *
     */

    private fun setActiveListener() {
        val connectedRef = application.database.getReference("connect")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        connectedRef.keepSynced(true)
    }

    fun getImagesBasedOnVersion(): Int {
        val storageRef = application.storage.reference
        val imagesVerRef = storageRef.child("images/version.txt")
        val localVerFile = File(context.filesDir, "version.txt")
        var version = sharedPreferences.getInt("imagesVersion", -1)

        if (application.checkInternetConnection() && _connected.value) {
            Log.i("RecordScreen", "getImagesBasedOnVersion() | internet connection available")
        } else {
            Log.i("RecordScreen", "getImagesBasedOnVersion() | no internet connection")
            prepareToDisplay()
        }
        imagesVerRef.getFile(localVerFile).addOnSuccessListener {
            Log.i(
                "RecordScreen",
                "getImagesBasedOnVersion() | version downloaded successfully"
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
            unzipImages()
//            prepareToDisplay()
            Log.i(
                "RecordScreen",
                "getImagesBasedOnVersion() | version download failed | error: $it"
            )
        }
        return version
    }

    private fun extractVersionFromTextFile(file: File): Int {
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
        _loadingState.value = LoadingStates.DOWNLOADING
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
//            Toast.makeText(context, "Images downloaded successfully", Toast.LENGTH_SHORT).show()
            newZip = true
            unzipImages()
            _downloadProgress.value = 0

        }.addOnFailureListener {
            _loadingState.value = LoadingStates.ERROR_DOWNLOADING
            _downloadProgress.value = 0
            Log.i(
                "RecordScreen",
                "fetchImagesFromFirebaseCloudStorage() | images download failed | error: $it"
            )
        }.addOnProgressListener {
            _downloadProgress.value = ((100.0 * it.bytesTransferred) / it.totalByteCount).roundToInt()
//            Log.i(
//                "RecordScreen",
//                "fetchImagesFromFirebaseCloudStorage() | progress: $progress"
//            )
        }.snapshot.task.addOnCompleteListener {
            _downloadProgress.value = 0
            Log.i(
                "RecordScreen",
                "fetchImagesFromFirebaseCloudStorage() | task completed"
            )
        }
    }

    // check if cloud storage has audio files for the current user
    private fun checkIfUserHasAudioFiles(completionListener: DownloadCompletionListener? = null): Boolean {
        val storageRef = application.storage.reference
        val recordsRef = storageRef.child("records/${application.firebaseAuth.currentUser?.uid}")
        var hasAudioFiles = false
        val recordsDir = context.getDir("records", MODE_PRIVATE).absolutePath
        val uidDir = File(recordsDir, application.firebaseAuth.currentUser?.uid ?: "default")
        checkAndCreateDir(uidDir)
        recordsRef.listAll().addOnSuccessListener { listResult ->
            hasAudioFiles = listResult.items.isNotEmpty()
            val numberOfFiles = listResult.items.size
            if (!hasAudioFiles) { completionListener?.onNoFilesToDownload() }
            var numberOfFilesDownloaded = 0
            var numberOfFilesFailed = 0
            var numberOfFilesSkipped = 0
            listResult.items.forEach { item ->
                // check if the record already exists in the local storage and if not, download listResult
                if (!checkIfFileExists(item.name) && item.name != "$JSON_FILE_NAME.json") {
                    val localFile = File(uidDir, item.name)
                    item.getFile(localFile).addOnSuccessListener {
                        Log.i("RecordScreen", "checkIfUserHasAudioFiles() | ${item.name} downloaded successfully")
                        numberOfFilesDownloaded++
                        if ((numberOfFilesDownloaded + numberOfFilesFailed + numberOfFilesSkipped) == numberOfFiles) {
                            completionListener?.onDownloadComplete()
                        }
                    }.addOnFailureListener {
                        Log.i("RecordScreen", "checkIfUserHasAudioFiles() | ${item.name} download failed | error: $it")
                        numberOfFilesFailed++
                        if ((numberOfFilesDownloaded + numberOfFilesFailed + numberOfFilesSkipped) == numberOfFiles) {
                            completionListener?.onDownloadFailed(localFile, it)
                        }
                    }
                } else {
//                    // check if the file is itemList.json
//                    if (item.name == JSON_FILE_NAME) {
//                        val cloudFile = File(uidDir, CLOUD_JSON_FILE_NAME)
//                        val localFile = File(uidDir, JSON_FILE_NAME)
//                        item.getFile(cloudFile).addOnSuccessListener {
//                            compareTwoItemListJsonFiles(item, localFile, cloudFile, completionListener)
//                        }.addOnFailureListener {
//                            Log.e("RecordScreen", "checkIfUserHasAudioFiles() | ${item.name} download failed | error: $it")
//                        }
//                    }
//
//
                    Log.i("RecordScreen", "checkIfUserHasAudioFiles() | ${item.name} already exists")
                    numberOfFilesSkipped++
                    if ((numberOfFilesDownloaded + numberOfFilesFailed + numberOfFilesSkipped) == numberOfFiles) {
                        completionListener?.onDownloadComplete()
                    }
                }
            }


            Log.i("RecordScreen", "checkIfUserHasAudioFiles() | hasAudioFiles: $hasAudioFiles")
        }.addOnFailureListener {
            Log.i("RecordScreen", "checkIfUserHasAudioFiles() | error: $it")
            completionListener?.onDownloadFailed(uidDir, it)
        }
        return hasAudioFiles
    }

    // update the itemList.json file based on the existing records in the uidDir
    private fun updateItemListJson() {
        Log.i("RecordScreen", "updateItemListJson() | called")
        val recordsDir = context.getDir("records", MODE_PRIVATE).absolutePath
        val uidDir = File(recordsDir, application.firebaseAuth.currentUser?.uid ?: "default")
        checkAndCreateDir(uidDir)

        val listOfRecords = uidDir.listFiles()

//        listOfRecords?.forEach {
//            val updatedItemList = _itemList.value.map { item ->
////                Log.i("RecordScreen", "updateItemListJson() | item.title: ${item.title}")
////                Log.i("RecordScreen", "updateItemListJson() | it.title: ${it.nameWithoutExtension}")
//                if (it.nameWithoutExtension == item.title) {
//                    item.copy(recorded = true)
//                } else {
//                    item
//                }
//            }
//            _itemList.value = updatedItemList
//            saveItemListToJson()
//        }

        listOfRecords?.let {
            val updatedItemList = _itemList.value.map { item ->
                if (listOfRecords.any {
//                        Log.i("RecordScreen", "updateItemListJson() | item.fileName: ${item.fileName}")
//                        Log.i("RecordScreen", "updateItemListJson() | it.fileName: ${it.nameWithoutExtension}")
                    it.nameWithoutExtension == item.fileName }) {
                    item.copy(recorded = true)
                } else {
                    item
                }
            }
            _itemList.value = updatedItemList
            saveItemListToJson()
        }
    }


    // function to compare the old itemList.json with the new one from cloud storage,
    // I might not need it though if I went with updating the itemList.json based on
    // the records existing, it will be cleaner that way. we'll see.
    private fun compareTwoItemListJsonFiles(
        item: StorageReference,
        localFile: File,
        cloudFile: File,
        completionListener: DownloadCompletionListener?
    ) {
        val localItemList = loadItemListFromJson(JSON_FILE_NAME)

    }


    /**
     *
     * we didn't use filesDir and used getDir() in creating a sub folder because it ensures
     * consistency with Android's internal storage structure and handles potential conflicts
     * or naming restrictions.
     *
     * and it works reliably across different Android versions, addressing potential compatibility
     * issues that might arise with manual string concatenation.
     *
     * the result is: /data/user/0/com.toddler.recordit/app_images
     *
     * Android automatically adds the "app_" prefix to user-created directories within
     * the app's private storage to avoid conflicts with system-defined folders like "cache," "databases," etc.
     *
     * It visually distinguishes app-specific folders from those managed by the system,
     * enhancing clarity and organization.
     *
     * While the prefix is visible in the file path, you typically interact with the directory
     * using its original name (e.g., "images") within your app's code.
     *
     * */
    private fun unzipImages() {
        if (!sharedPreferences.getBoolean("imagesUnzipped", false) || newZip) {
            _loadingState.value = LoadingStates.EXTRACTING
            val imagesZipDir = File(context.filesDir, "images.zip")
            val destDir = context.getDir("images", MODE_PRIVATE).absolutePath
            // we need first to delete the old images inside the images folder
            val imagesDir = File(destDir).listFiles()
            imagesDir?.forEach {
                it.delete()
                Log.i("RecordScreen", "unzipImages() | ${it.name} deleted")
            }

            UnzipUtilsWithListeners.unzip(imagesZipDir, destDir, object : UnzipListener {
                override fun onUnzipComplete() {
                    Log.i(
                        "RecordScreen",
                        "unzipImages() | images unzip complete | destDir: $destDir"
                    )
                    Log.i(
                        "RecordScreen",
                        "unzipImages() | images unzip complete | listFiles: ${
                            File(destDir).listFiles()
                        }"
                    )
//                    Toast.makeText(context, "Images unzip complete", Toast.LENGTH_SHORT).show()

                    sharedPreferences.edit().putBoolean("imagesUnzipped", true).apply()

//                    convertImagesToWebP()

                    prepareToDisplay()

                }

                override fun onUnzipFailed(error: Exception) {
                    _loadingState.value = LoadingStates.ERROR_EXTRACTING
                    Log.i(
                        "RecordScreen",
                        "unzipImages() | images unzip failed: $error"
                    )
//                    Toast.makeText(context, "Images unzip failed", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            prepareToDisplay()
        }
    }

    // function to convert jpeg and png images to webp format
    private fun convertImagesToWebP() {
        val imagesDir = context.getDir("images", MODE_PRIVATE).absolutePath
        val imagesDirFile = File(imagesDir)
        val images = imagesDirFile.listFiles()
        try {
            images?.forEach { file ->
                val bmp = BitmapFactory.decodeFile(file.absolutePath)
                FileOutputStream(file).use { out ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        bmp.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 50, out) // or WEBP_LOSSY
                        out.close()
                        Log.i(
                            "RecordScreen",
                            "convertImagesToWebP() | ${file.name} size: ${file.length() / 1024} kb"
                        )
                        Log.i(
                            "RecordScreen",
                            "convertImagesToWebP() | ${out} size: ${out.channel.size() / 1024} kb"
                        )

                    } else {
                        bmp.compress(Bitmap.CompressFormat.WEBP, 50, out)
                        out.close() // close the stream to ensure that the file is written?
                        Log.i(
                            "RecordScreen",
                            "convertImagesToWebP() | ${file.name} size: ${file.length() / 1024} kb"
                        )
                        Log.i(
                            "RecordScreen",
                            "convertImagesToWebP() | ${out.fd} size: ${out.channel.size() / 1024} kb"
                        )
                    }
                }
            }
            Log.i(
                "RecordScreen",
                "convertImagesToWebP() | images converted to webp"
            )
        } catch (e: Exception) {
            Log.i("RecordScreen", "convertImagesToWebP() | error: $e")
        }
    }

    private fun prepareToDisplay() {
//        _loadingState.value = LoadingStates.LOADING
        initializeItemList()
        updateItemListJson()

//        _numberOfImages.value = itemList.value.size
//        determineNumberOfImagesRecorded()
//        determineNumberOfImagesNotRecorded()

        updateCurrentItem()
    }

    // loop over the audio files in the uidDir and upload them to firebase storage
    fun uploadAudioFiles(completionListener: UploadCompletionListener? = null) {
        if (!_isUploading.value) {
//            val uidDir = context.getDir(application.firebaseAuth.currentUser?.uid ?: "default", MODE_PRIVATE).absolutePath

            val recordsDir = context.getDir("records", MODE_PRIVATE).absolutePath
            val uidDir = File(recordsDir, application.firebaseAuth.currentUser?.uid ?: "default")
//            val uidDirFile = File(uidDir)
            if (!uidDir.exists()) return
            val audioFiles = uidDir.listFiles()
            var successfulUploads = 0
            _isUploading.value = true
            audioFiles?.forEach { file ->
                val storageRef = application.storage.reference
                val audioRef =
                    storageRef.child("records/${application.firebaseAuth.currentUser?.uid}/${file.name}")
                audioRef.putFile(Uri.fromFile(file)).addOnSuccessListener {
                    successfulUploads++
                    _uploadProgress.value++
                    if (successfulUploads == _numberOfImagesRecorded.value) {
                        _isUploading.value = false
                        _uploadProgress.value = 0
                        completionListener?.onUploadComplete()
                    }
                    Log.i("RecordScreen", "uploadAudioFiles() | ${file.name} uploaded successfully")
                }.addOnFailureListener {
                    _isUploading.value = false
                    _uploadProgress.value = 0
                    Log.i(
                        "RecordScreen",
                        "uploadAudioFiles() | ${file.name} upload failed | error: $it"
                    )
                }
            }
        }
    }


    fun logOut() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(LoginActivity.SERVER_CLIENT_ID)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
        application.firebaseAuth.signOut()
        googleSignInClient.revokeAccess()
//        sharedPreferences.edit().clear().apply()
    }


    // function to take a string, replace underscores with spaces, and capitalize each word
    private fun String.capitalizeWords(): String = split("_", " ").joinToString(" ") {
        it.replaceFirstChar { firstChar ->
            if (firstChar.isLowerCase()) firstChar.titlecase(
                Locale.ROOT
            ) else firstChar.toString()
        }
    }


    companion object {
        const val JSON_FILE_NAME = "itemList.json"
        const val CLOUD_JSON_FILE_NAME = "cloudItemList.json"
    }

}
