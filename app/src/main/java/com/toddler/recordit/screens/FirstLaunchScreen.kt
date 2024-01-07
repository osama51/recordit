package com.toddler.recordit.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.toddler.recordit.Dashboard
import com.toddler.recordit.MainActivity.Companion.sharedPreferences
import com.toddler.recordit.R
import com.toddler.recordit.ui.theme.Orange
import com.toddler.recordit.ui.theme.RecordItTheme
import com.toddler.recordit.ui.theme.Red


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstLaunchScreen(startDashboard: () -> Unit) {
    RecordItTheme(darkTheme = false, dynamicColor = false) {

        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
//        BackHandler(true){focusManager.clearFocus() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Orange, Red
                        )
                    )
                )
//                .clickable(onClick = { focusManager.clearFocus() }) // contains ripple effect
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            focusManager.clearFocus()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val imageVector = ImageVector.vectorResource(id = R.drawable.ic_mic_54)
                Image(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth(),

                    imageVector = imageVector,
                    contentDescription = "RecordIt Logo"
                )
                Text(
                    text = "RecordIt",
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = MaterialTheme.typography.displayMedium.fontSize,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Cursive,
                        letterSpacing = 10.sp,
                        shadow = Shadow(
                            color = MaterialTheme.colorScheme.onPrimary,
                            blurRadius = 50f,
                            offset = Offset(0f, 5f)
                        )
                    ),
                )
                Spacer(modifier = Modifier.height(32.dp))
//                Button(
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.onPrimary,
//                        contentColor = MaterialTheme.colorScheme.primary
//                    ),
//                    onClick = {},
//                    elevation = ButtonDefaults.buttonElevation(
//                        defaultElevation = 10.dp
//                    )
//                ) {
//                    Text(text = "Sign in")
//                }
                var text by rememberSaveable { mutableStateOf("") }

                TextField(
                    modifier = Modifier.focusRequester(focusRequester),
                    value = text,
                    onValueChange = {
                        text = it
                    },
                    label = { Text("Your Name") },
                    shape = MaterialTheme.shapes.medium,
                    colors = TextFieldDefaults.textFieldColors(
                        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = MaterialTheme.colorScheme.onPrimary,
                        errorIndicatorColor = MaterialTheme.colorScheme.onPrimary,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                        focusedTrailingIconColor = MaterialTheme.colorScheme.onPrimary,
                        cursorColor = MaterialTheme.colorScheme.onPrimary,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary,
                        disabledTextColor = MaterialTheme.colorScheme.onPrimary,
                        errorTextColor = MaterialTheme.colorScheme.onPrimary,
                        focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        errorLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (text.isNotBlank() && text.isNotEmpty()) {
                            // set text to userName in shared preferences
                            sharedPreferences.edit().putString("userName", text).apply()

                            startDashboard()
                        }
                    },
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 10.dp
                    )
                ) {
                    Text(text = "Sign up")
                }
            }


        }
    }
}
