package com.toddler.recordit

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    }

    companion object {
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

