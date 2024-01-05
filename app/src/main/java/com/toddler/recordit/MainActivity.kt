package com.toddler.recordit

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.toddler.recordit.MainActivity.Companion.isFirstLaunch
import com.toddler.recordit.MainActivity.Companion.noMoreFirstLaunch
import com.toddler.recordit.playback.AndroidAudioPlayer
import com.toddler.recordit.recorder.AndroidAudioRecorder
import com.toddler.recordit.screens.FirstLaunchScreen
import com.toddler.recordit.screens.dashboard.HomeScreen
import com.toddler.recordit.screens.record.ImageRecordingViewModel
import com.toddler.recordit.screens.record.RecordScreen
import com.toddler.recordit.ui.theme.RecordItTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val recorder by lazy {
        AndroidAudioRecorder(applicationContext)
    }
    private val player by lazy {
        AndroidAudioPlayer(applicationContext)
    }
    private var audioFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("com.toddler.recordit", MODE_PRIVATE)
        setContent {
            RecordItTheme {
                // A surface container using the 'background' color from the theme
                MyApp(hiltViewModel<ImageRecordingViewModel>())
            }
        }
        checkStoragePermission()
        requestRecordPermission()
    }

    companion object {
        private const val STORAGE_PERMISSION_REQUEST_CODE = 1
        private const val MY_PERMISSION_REQUEST_RECORD_AUDIO = 2

        //        @Inject
        lateinit var sharedPreferences: SharedPreferences
        fun isFirstLaunch(): Boolean {
            // Check if a flag indicating first launch is absent in SharedPreferences
            return sharedPreferences.getBoolean("isFirstLaunch", true)
        }

        fun noMoreFirstLaunch() {
            // Mark first launch as complete
            sharedPreferences.edit().putBoolean("isFirstLaunch", false).apply()
        }
    }


    private fun requestRecordPermission(){
        val permissionCheck = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )
        if (permissionCheck === PERMISSION_GRANTED) {
            // you have the permission, proceed to record audio

            Log.i("SpeechActivity", "Speech Recognizer is listening")
        } else {

            // you don't have permission, try requesting for it
            ActivityCompat.requestPermissions(
                this, arrayOf<String>(Manifest.permission.RECORD_AUDIO),
                MY_PERMISSION_REQUEST_RECORD_AUDIO
            )
        }
    }

    // Check and request storage permission
    private fun checkStoragePermission() {
        // Check if the storage permission is already granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted
            // Perform your desired operations here
        } else {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                // Perform your desired operations here
            } else {
                // Permission denied
                // Handle permission denied scenario
            }
        }
    }

}

fun determineInitialRoute(): String {
    return if (isFirstLaunch()) {
        noMoreFirstLaunch()
        LogIn.route
    } else {
        Dashboard.route
    }
}

@Composable
fun MyApp(hiltViewModel: ImageRecordingViewModel) {
    val navController = rememberNavController()
    val initialRoute = remember { determineInitialRoute() }

    NavHost(navController = navController, startDestination = initialRoute) {
        /**
         * The NavController's navigate function modifies the NavController's internal state.
         * To comply with the single source of truth principle as much as possible,
         * only the composable function or state holder that hoists the NavController instance
         * and those composable functions that take the NavController as a parameter should make
         * navigation calls. Navigation events triggered from other composable functions lower
         * in the UI hierarchy need to expose those events to the caller appropriately using functions.
         *
         * */
        composable(Dashboard.route) {
//                    backStackEntry ->
//                    val parentEntry = remember(backStackEntry) {
//                        navController.getBackStackEntry(initialRoute)
//                    }
//                    val parentViewModel = hiltViewModel<ImageRecordingViewModel>(parentEntry)
            Log.i("MainActivity", "Dashboard route")
            HomeScreen(viewModel = hiltViewModel,
                startRecordScreen = {
                    navController.navigate(Record.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                })
//                    currentScreen.intValue = Screen.Dashboard.ordinal
        }
        composable(Record.route) {
//                    backStackEntry ->
//                    val parentEntry = remember(backStackEntry) {
//                        navController.getBackStackEntry(initialRoute)
//                    }
//                    val parentViewModel = hiltViewModel<ImageRecordingViewModel>(parentEntry)
            Log.i("MainActivity", "Record route")
            RecordScreen(viewModel = hiltViewModel,
                goBack = {
                    navController.navigate(Dashboard.route)
                })
//                    currentScreen.intValue = Screen.Record.ordinal
        }
        composable(LogIn.route) {
            Log.i("MainActivity", "LogIn route")
            FirstLaunchScreen(navController)
        }

    }
}

/**
* Navigation compose version "2.7.6" adds that AnimatedContentScrope thing to the composeable
 * also it appears the the multiple recomposition is occuring only with that version
 * now I'm running version "2.5.2" and does not have that issue, at least not in the layout inspector.
 * although by observation, the recomposition is occurring slightly (if you pay attention).
 * also it's worth mentioning that the current multiple-recomposition issue is as of its current state:
 * 1. recompose current screen
 * 2. recompose destination screen
 * 3. recompose destination screen
 * this patter is the best so far and is existing in the ComposableUI project that was created during coursera course.
*
* */

