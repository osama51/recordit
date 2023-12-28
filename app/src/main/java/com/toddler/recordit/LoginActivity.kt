package com.toddler.recordit

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.toddler.recordit.screens.LoginScreen
import com.toddler.recordit.ui.theme.RecordItTheme


class LoginActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecordItTheme {
                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    Greeting("Android")
                    ToggleScreen()
//                }
            }
        }

    }
}

@Composable
fun ToggleScreen() {
    val firebaseAuth = remember { mutableStateOf(FirebaseAuth.getInstance()) }
    val isSignedIn = remember { mutableStateOf(firebaseAuth.value.currentUser != null) }

//    if (isSignedIn.value) {
//        Toast.makeText(LocalContext.current, "Signed In", Toast.LENGTH_SHORT).show()
//        HomeScreen(firebaseAuth.value)
//    } else {
        Toast.makeText(LocalContext.current, "Not Signed In", Toast.LENGTH_SHORT).show()
        LoginScreen(isSignedIn, firebaseAuth) // pass firebaseAuth and isSignedIn as parameters
//    }
}