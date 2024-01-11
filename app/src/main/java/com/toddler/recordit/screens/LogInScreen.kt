package com.toddler.recordit.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toddler.recordit.R
import com.toddler.recordit.ui.theme.MyBlack
import com.toddler.recordit.ui.theme.Orange
import com.toddler.recordit.ui.theme.RecordItTheme
import com.toddler.recordit.ui.theme.Red
import com.toddler.recordit.ui.theme.RobotoMedium


@Composable
fun LoginScreen(
    googleSignIn: () -> Unit
) {
    val context = LocalContext.current

    RecordItTheme(darkTheme = false, dynamicColor = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary
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
                Spacer(modifier = Modifier.height(40.dp))
//                Button(
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.onPrimary,
//                        contentColor = MaterialTheme.colorScheme.primary
//                    ),
//                    onClick = {  },
//                    elevation = ButtonDefaults.buttonElevation(
//                        defaultElevation = 10.dp
//                    )
//                ) {
//                    Text(text = "Sign in")
//                }
                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    modifier = Modifier.height(40.dp),
                    onClick = { googleSignIn() },
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MyBlack
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 10.dp
                    )
                ) {
                    Image(
                        modifier = Modifier
                            .padding(start = 12.dp, end = 10.dp)
                            .size(20.dp),
                        painter = painterResource(id = R.drawable.ic_google_g),
                        contentDescription = "Google Icon"
                    )
                    Text(modifier = Modifier
                        .padding(end = 12.dp),
                        text = "Sign in with Google",
                        fontFamily = RobotoMedium
                        )
                }
            }
        }
    }
}

fun Modifier.vectorShadow(
    path: Path,
    x: Dp,
    y: Dp,
    radius: Dp
) = composed(
    inspectorInfo = {
        name = "vectorShadow"
        value = path
        value = x
        value = y
        value = radius
    },
    factory = {

        val paint = remember {
            Paint()
        }

        val frameworkPaint = remember {
            paint.asFrameworkPaint()
        }

        val color = Color.DarkGray
        val dx: Float
        val dy: Float
        val radiusInPx: Float

        with(LocalDensity.current) {
            dx = x.toPx()
            dy = y.toPx()
            radiusInPx = radius.toPx()
        }

        drawBehind {
            this.drawIntoCanvas {
                val transparent = color
                    .copy(alpha = 0f)
                    .toArgb()

                frameworkPaint.color = transparent

                frameworkPaint.setShadowLayer(
                    radiusInPx,
                    dx,
                    dy,
                    color
                        .copy(alpha = .7f)
                        .toArgb()
                )
                it.drawPath(path, paint)
            }
        }
    }
)
