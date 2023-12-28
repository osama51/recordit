package com.toddler.recordit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.toddler.recordit.screens.dashboard.HomeScreen
import com.toddler.recordit.screens.record.RecordScreen
import com.toddler.recordit.ui.theme.RecordItTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecordItTheme {
                // A surface container using the 'background' color from the theme
                MyApp()
            }
        }
    }
}

//enum class Screen(val route: String) {
//    Dashboard("dashboard"),
//    Record("record"),
//}

@Composable
fun MyApp(){
    val navController = rememberNavController()
//    val currentScreen = rememberSaveable{ mutableIntStateOf(Screen.Dashboard.ordinal) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ){
        Box(modifier = Modifier.padding(it)){
            NavHost(navController = navController, startDestination = Dashboard.route){
                composable(Dashboard.route){
                    HomeScreen(navController)
//                    currentScreen.intValue = Screen.Dashboard.ordinal
                }
                composable(Record.route){
                    RecordScreen(navController)
//                    currentScreen.intValue = Screen.Record.ordinal
                }
            }
        }
    }
}

