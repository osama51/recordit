package com.toddler.recordit

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val recorder by lazy {
        AndroidAudioRecorder(applicationContext) }
    private val player by lazy {
        AndroidAudioPlayer(applicationContext) }
    private var audioFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("com.toddler.recordit", MODE_PRIVATE)
        setContent {
            RecordItTheme {
                // A surface container using the 'background' color from the theme
                MyApp()
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

//enum class Screen(val route: String) {
//    Dashboard("dashboard"),
//    Record("record"),
//}

@Composable
fun MyApp() {
    val navController = rememberNavController()
//    val currentScreen = rememberSaveable{ mutableIntStateOf(Screen.Dashboard.ordinal) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        var firstScreenRoute by remember { mutableStateOf("") }

        firstScreenRoute = if (isFirstLaunch()) {
            noMoreFirstLaunch()
            LogIn.route
        } else {
            Dashboard.route
        }
        Box(modifier = Modifier.padding(it)) {
            NavHost(navController = navController, startDestination = firstScreenRoute) {
                composable(Dashboard.route) {
                    HomeScreen(navController)
//                    currentScreen.intValue = Screen.Dashboard.ordinal
                }
                composable(Record.route) {
                    RecordScreen(navController, viewModel = hiltViewModel<ImageRecordingViewModel>())
//                    currentScreen.intValue = Screen.Record.ordinal
                }
                composable(LogIn.route) {
                    FirstLaunchScreen(navController)
                }
            }
        }
    }
}

