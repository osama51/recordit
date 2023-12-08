package com.toddler.recordit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(value: FirebaseAuth) {

    Box(Modifier.background(color = Color.Green))
}