package com.toddler.recordit

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.toddler.recordit.screens.FirstLaunchScreen
import com.toddler.recordit.screens.dashboard.HomeScreen
import com.toddler.recordit.screens.ImageRecordingViewModel
import com.toddler.recordit.screens.record.RecordScreen
import com.toddler.recordit.ui.theme.RecordItTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    var startSlideShow: Boolean = false
    var shouldShowRationale: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val packageName = applicationContext.packageName
        sharedPreferences = getSharedPreferences(packageName, MODE_PRIVATE)
        setContent {
            RecordItTheme(dynamicColor = true) {
                // A surface container using the 'background' color from the theme
                MyApp(hiltViewModel<ImageRecordingViewModel>())
            }
        }
        requestRecordPermissions() {}
    }

    companion object {
        private const val MY_PERMISSION_REQUEST_RECORD_AUDIO = 1

        //        @Inject
        lateinit var sharedPreferences: SharedPreferences
        fun isFirstLaunch(): Boolean {
            // Check if a flag indicating first launch is absent in SharedPreferences
            return sharedPreferences.getBoolean("isFirstLaunch", true)
        }

        fun checkIfUserNameExists(): Boolean {
            return sharedPreferences.getString("userName", null) != null
        }

        fun noMoreFirstLaunch() {
            // Mark first launch as complete
            sharedPreferences.edit().putBoolean("isFirstLaunch", false).apply()
        }
    }


    fun requestRecordPermissions(proceed: () -> Unit) {
        val recordPermissionCheck = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )
        if (recordPermissionCheck == PERMISSION_GRANTED
        ) {
            // you have the permission, proceed to record audio
            proceed()
        } else {
            // you don't have permission, try requesting for it
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(
                    Manifest.permission.RECORD_AUDIO,
                ),
                MY_PERMISSION_REQUEST_RECORD_AUDIO
            )
            //check if user denied the permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                // user denied the permission, show rationale
                Log.i("MainActivity", "requestRecordPermission: user denied the permission")
                // show snackbar to request permission
                shouldShowRationale = true
            } else {
                // user denied the permission and checked "never ask again"
                Log.i("MainActivity", "requestRecordPermission: user denied the permission and checked \"never ask again\"")
            }
//            Toast.makeText(this, "You need to allow RecordIt to record audio to use this app", Toast.LENGTH_LONG).show()
        }
    }


    // Handle permission request result
    @Deprecated("Not deprecated itself?")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSION_REQUEST_RECORD_AUDIO) {
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

//fun determineInitialRoute(): String {
//    return if (isFirstLaunch() || !checkIfUserNameExists()) {
//        noMoreFirstLaunch()
//        LogIn.route
//    } else {
//        Dashboard.route
//    }
//}

@Composable
fun MyApp(hiltViewModel: ImageRecordingViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = context as MainActivity
//    val initialRoute = remember { determineInitialRoute() }

    NavHost(navController = navController, startDestination = Dashboard.route) {
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
            HomeScreen(
                viewModel = hiltViewModel,
                startRecordScreen = {
                    navController.navigate(Record.route) {}
                },
                logOut = {
                    hiltViewModel.logOut()
                    context.startActivity(Intent(context, LoginActivity::class.java))
                    activity.finish()
                    // here we use "as" keyword to cast the context to MainActivity
                    // because without it, the finish() function will not be available
                }
            )
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
                    navController.navigate(Dashboard.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                })
//                    currentScreen.intValue = Screen.Record.ordinal
        }
        composable(LogIn.route) {
            Log.i("MainActivity", "LogIn route")
            FirstLaunchScreen(
                startDashboard = {
                    navController.navigate(Dashboard.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }

    }
}

/**
 * Navigation compose version "2.7.6" adds that AnimatedContentScope thing to the composable
 * also it appears the the multiple recomposition is occurring only with that version
 * now I'm running version "2.5.2" and does not have that issue, at least not in the layout inspector.
 * although by observation, the recomposition is occurring slightly (if you pay attention).
 * also it's worth mentioning that the current multiple-recomposition issue is as of its current state:
 * 1. recompose current screen
 * 2. recompose destination screen
 * 3. recompose destination screen
 * this pattern is the best so far and was sadly existing in the ComposableUI project that was created during coursera course.
 *
 * */

