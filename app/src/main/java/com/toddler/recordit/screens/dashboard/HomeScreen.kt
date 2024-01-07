package com.toddler.recordit.screens.dashboard

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getString
import com.toddler.recordit.MainActivity
import com.toddler.recordit.R
import com.toddler.recordit.screens.record.ImageRecordingViewModel
import com.toddler.recordit.ui.theme.Abel
import com.toddler.recordit.ui.theme.OffWhite
import com.toddler.recordit.ui.theme.Orange
import com.toddler.recordit.ui.theme.Red
import com.toddler.recordit.ui.theme.Russo


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: ImageRecordingViewModel,
    startRecordScreen: () -> Unit,
    startLogInScreen: () -> Unit
) {
    // this val should be an argument passed not initiated here
//    val value: FirebaseAuth

    val context = LocalContext.current
    Log.i("HomeScreen", "HomeScreen RECOMPOSED !!")
//    val itemList = viewModel.itemList.collectAsState().value
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val numberOfImages = viewModel.numberOfImages.collectAsState().value
        viewModel.determineNumberOfImagesNotRecorded()
        val numberOfImagesNotRecorded = viewModel.numberOfImagesNotRecorded.collectAsState().value

        val pagerState = rememberPagerState(
            initialPage = 0,
            initialPageOffsetFraction = 0.0f //within the range -0.5 to 0.5
        ) {
            // provide pageCount
            numberOfImages
        }
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.White)
//                .padding(it),
//        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(it)
                .scrollable(
                    orientation = Orientation.Vertical,
                    state = rememberScrollableState { delta ->
                        Log.i("HomeScreen", "delta: $delta")
                        delta
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(shape = RoundedCornerShape(0.dp, 0.dp, 40.dp, 40.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Orange, Red
                            )
                        )
                    )
                    .fillMaxWidth()
                    .height(250.dp),
                verticalArrangement = Arrangement.Center,
            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth(),
//                    verticalArrangement = Arrangement.Center,
//                ) {

//                        Row(modifier = Modifier
//                            .align(Alignment.CenterHorizontally)
//                            .padding(4.dp),
//                            verticalAlignment = Alignment.Bottom,){
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(4.dp),
                    text = "$numberOfImagesNotRecorded",
                    fontFamily = Russo,
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = MaterialTheme.typography.displayLarge.fontSize,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        shadow = Shadow(
                            color = MaterialTheme.colorScheme.onPrimary,
                            blurRadius = 50f,
                            offset = Offset(0f, 5f)
                        )
                    ),
                )
//                            Text(
//                                text = "  /$numberOfImages",
//                                fontFamily = Russo,
//                                style = TextStyle(
//                                    fontSize = 24.sp,
//                                    color = MaterialTheme.colorScheme.onPrimary,
//                                    letterSpacing = 1.sp,
//                                    fontWeight = FontWeight.Light,
//                                ),
//                            )
//                        }
                Text(
                    text = "Images left to record",
                    fontFamily = Abel,
                    style = TextStyle(
                        fontSize = 24.sp,
                        color = OffWhite,
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(4.dp)
                )

//                }
            }
            HorizontalPager(
                modifier = Modifier
                    .padding(0.dp, 24.dp)
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 32.dp),
                pageSpacing = 16.dp, state = pagerState
            ) { page ->
                CarouselItem(context, viewModel.itemList.collectAsState().value[page])
            }
            Text(
                text = "Total Images: $numberOfImages",
                fontFamily = Russo,
                fontSize = 18.sp,
                style = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Light,
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .weight(0.3f)
            )
            val userName = MainActivity.sharedPreferences.getString("userName", null)
            Text(
                text = "User: $userName",
                fontFamily = Russo,
                fontSize = 18.sp,
                style = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Light,
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .weight(0.3f),

            )
            TextButton(onClick = { startLogInScreen() }) {
                Text(text = "Edit User")
            }
            
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                ExtendedFloatingActionButton(
                    text = {
                        Text(
                            text = getString(context, R.string.start_slideshow),
//                            fontFamily = Abel,
                        )
                    },
                    icon = {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Start slideshow icon"
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    onClick = startRecordScreen,
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }


//            DotIndicators(
//                pageCount = pageCount,
//                pagerState = pagerState,
//                modifier = Modifier.align(Alignment.BottomCenter)
//            )
//        }
    }
}


