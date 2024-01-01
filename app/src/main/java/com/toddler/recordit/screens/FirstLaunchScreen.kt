package com.toddler.recordit.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.toddler.recordit.Dashboard
import com.toddler.recordit.R
import com.toddler.recordit.ui.theme.Orange
import com.toddler.recordit.ui.theme.RecordItTheme
import com.toddler.recordit.ui.theme.Red


@Composable
fun FirstLaunchScreen(navController: NavHostController) {
    RecordItTheme(darkTheme = false, dynamicColor = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Orange, Red
                        )
                    )
                ),
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
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    onClick = {
                        navController.navigate(Dashboard.route)
                    },
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 10.dp
                    )
                ) {
                    Text(text = "Sign in")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {},
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
