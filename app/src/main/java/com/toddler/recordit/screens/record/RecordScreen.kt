package com.toddler.recordit.screens.record

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toFile
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.animation.crossfade.CrossfadePlugin
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.transformation.blur.BlurTransformationPlugin
import com.toddler.recordit.Dashboard
import com.toddler.recordit.R
import com.toddler.recordit.ui.theme.DarkGrayHalfTransparent
import com.toddler.recordit.ui.theme.NavyDark
import com.toddler.recordit.ui.theme.OffWhite
import com.toddler.recordit.ui.theme.Red
import com.toddler.recordit.ui.theme.White
import com.toddler.recordit.utils.getImagesFromAssets
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale

@Composable
fun RecordScreen(viewModel: ImageRecordingViewModel, goBack: () -> Unit) {
    val context = LocalContext.current
    Log.i("RecordScreen", "COMPOSED RECORD SCREEN")

//    Scaffold(Modifier.fillMaxSize()) {
    ScreenContent(context, viewModel, goBack)
//    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenContent(
    context: Context,
    viewModel: ImageRecordingViewModel,
    goBack: () -> Unit
) {

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

//    val item by rememberSaveable(stateSaver = RecordItemSaver) { mutableStateOf(viewModel.currentItem.value!!) }

    val item = viewModel.currentItem.collectAsState().value!!
    val drawable by lazy {
        Drawable.createFromStream(context.assets.open("${item.imagePath}"), null)

//        val bitmap = BitmapFactory.decodeStream(context.assets.open(item.imagePath))
//        val outputStream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
//        outputStream.toByteArray()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        drawable?.let {
            BlurredImage(
                imageDrawable = it,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
//                .background(androidx.compose.ui.graphics.Color.White)
        ) {
            Box(
                modifier = Modifier
                    .weight(2f, true)
                    .fillMaxWidth()
//                    .background(Color.White)
            ) {
                GlideImage(
                    imageModel = { drawable },
                    component = rememberImageComponent {
                        +CrossfadePlugin(duration = 1000)
//                        +BlurTransformationPlugin(radius = 300) // between 0 to Int.MAX_VALUE.
//                        // shows a shimmering effect when loading an image.
//                        +ShimmerPlugin(
//                            baseColor = Color.DarkGray,
//                            highlightColor = Color.LightGray,
//                            durationMillis = 2000,
//                        )

                    },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Fit,
                        contentDescription = item.description,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(12.dp))
//                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(8.dp), clip = true)
                        .graphicsLayer(
                            alpha = 1.0f,
//                            shadowElevation = 12f,
                        ),
//                    .background(Color.Gray),
                    // shows a progress indicator when loading an image.
                    loading = {
                        Box(Modifier.fillMaxSize()) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center))
                        }
                    },
                    // shows an error text message when request failed.
                    failure = @Composable {
                        Text(text = "image request failed.")
                    },
                )

                ElevatedButton(
                    modifier = Modifier
                        .padding(14.dp)
                        .size(60.dp) //.background(Navy, shape = androidx.compose.foundation.shape.CircleShape)
                        .align(Alignment.TopStart),
                    contentPadding = PaddingValues(16.dp),
                    elevation = ButtonDefaults.buttonElevation(12.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color.White,
                        contentColor = NavyDark,
                    ),
                    onClick = goBack,
                ) {
                    Icon(
                        modifier = Modifier
                            .size(60.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_back),
                        contentDescription = "Go Back",
                        tint = NavyDark,
                    )
                }

                Text(
                    text = item.title,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(14.dp)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
            Box(
                // Lower Body Container
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Card(

                    colors = CardDefaults.cardColors(
                        containerColor = DarkGrayHalfTransparent,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(0.dp)
                        .align(Alignment.Center),
                    shape = RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp),
//                .shadow(
//                    elevation = 28.dp,
//                    shape = RoundedCornerShape(0.dp),
//                    clip = true // Allow shadow to extend beyond the Box
//                )

//                        .graphicsLayer {
//                            alpha = 0.5f
//                        },
                ) {

                    var canGoNext = viewModel.canNavigateToNextItem()
                    var canGoPrevious = viewModel.canNavigateToPreviousItem()
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        ) {
                            if (canGoPrevious) {
                                IconButton(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    onClick = { viewModel.navigateToPreviousItem() },
                                    content = {
                                        Icon(
                                            modifier = Modifier
                                                .size(30.dp),
                                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_left),
                                            contentDescription = "Previous-item icon",
                                            tint = OffWhite,
                                        )
                                    },
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(4f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceAround,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            val interactionSource = remember { MutableInteractionSource() }
                            val pressed by interactionSource.collectIsPressedAsState()
                            var recorded by remember { mutableStateOf(item.recorded) }
//                        val isRecording by remember { mutableStateOf(viewModel.isRecording.value) }
                            var isRecordingLocal by remember { mutableStateOf(false) }
//                        val audioFile = File(context.cacheDir, "audio.mp3")

                            var isPlaying by rememberSaveable { mutableStateOf(viewModel.isPlaying()) }
                            var buttonIcon by rememberSaveable { mutableIntStateOf(R.drawable.ic_play) }


                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isRecordingLocal) Color.Red else Color.Transparent)
                            )

                            ElevatedButton(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(72.dp), //.background(Navy, shape = androidx.compose.foundation.shape.CircleShape)
//                                .align(Alignment.TopCenter)
//                                .pointerInput(Unit) {
//                                    Log.i("RecordScreen", "I'm pointerInput")
//                                    detectTapGestures(
//                                        onPress = {
//                                            Log.i("RecordScreen", "I'm pressed")
//                                            try {
//                                                if (!isPlaying) {
//                                                    isRecordingLocal = true
//                                                    viewModel.startRecording(viewModel.returnFile())
//
//                                                    Log.i(
//                                                        "RecordScreen",
//                                                        "I'm pressed and am recording"
//                                                    )
//                                                }
//                                                awaitRelease()
//                                            } finally {
//                                                if (viewModel.isRecording()) {
//                                                    //released
//                                                    isRecordingLocal = false
//                                                    Log.i(
//                                                        "RecordScreen",
//                                                        "I'm not pressed and I stopped recording"
//                                                    )
//                                                    viewModel.stopRecording()
//
//                                                    item.recorded = true
//                                                    viewModel.updateItemList(item)
//                                                    recorded = true
//
//                                                    viewModel.saveItemListToJson()
//                                                }
//                                            }
//                                        },
//                                        onTap = {
//                                            Log.i("RecordScreen", "I'm tapped")
//                                        },
//                                    )
//                                }
                                contentPadding = PaddingValues(16.dp),
                                elevation = ButtonDefaults.buttonElevation(12.dp),
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = NavyDark,
                                ),
                                interactionSource = interactionSource, // remember to pass the source, the source is used to collect the interaction state from the button and can be aquired from the interactionSource.collectIsPressedAsState() method
                                onClick = { },
                            ) {
                                if (pressed && !isPlaying) {
                                    LaunchedEffect(Unit) {
                                        isRecordingLocal = true
                                        viewModel.startRecording(viewModel.returnFile())
//                    viewModel.startRecording(audioFile)
                                        Log.i("RecordScreen", "I'm pressed and am recording")
                                    }

                                    DisposableEffect(Unit) {
                                        onDispose {
                                            if (viewModel.isRecording()) {
                                                //released
                                                isRecordingLocal = false
                                                Log.i(
                                                    "RecordScreen",
                                                    "I'm not pressed and I stopped recording"
                                                )
                                                viewModel.stopRecording()

                                                item.recorded = true
                                                viewModel.updateItemList(item)
                                                recorded = true

                                                canGoNext = viewModel.canNavigateToNextItem()
                                                canGoPrevious =
                                                    viewModel.canNavigateToPreviousItem()

                                                viewModel.saveItemListToJson()
                                            }
                                        }
                                    }
                                }

                                Icon(
                                    modifier = Modifier
                                        .size(60.dp),
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_mic_filled_54),
                                    contentDescription = "Record icon",
                                    tint = NavyDark,
                                )

                            }



                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(72.dp), //.background(Navy, shape = androidx.compose.foundation.shape.CircleShape)
//                                .align(Alignment.BottomCenter)
                            ) {

                                if (recorded) {
                                    ElevatedButton(
                                        modifier = Modifier
                                            .padding(8.dp) //.background(Navy, shape = androidx.compose.foundation.shape.CircleShape)
                                            .align(Alignment.BottomCenter),
                                        contentPadding = PaddingValues(12.dp),
//                                    elevation = ButtonDefaults.buttonElevation(12.dp),
                                        colors = ButtonDefaults.elevatedButtonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = NavyDark,
                                        ),
                                        onClick = {
//                            val inputStream = context.contentResolver.openInputStream(
//                                viewModel.returnUri(item.title)
//                            )
                                            viewModel.apply {
                                                buttonIcon = if (!isPlaying) {
                                                    startPlayback()
                                                    R.drawable.ic_stop
                                                } else {
                                                    stopPlayback()
                                                    R.drawable.ic_play
                                                }
                                            }
                                            isPlaying = !isPlaying

                                            viewModel.triggerWhenFinished {
                                                buttonIcon = R.drawable.ic_play
                                                isPlaying = false
                                            }
                                        },
                                    ) {
                                        Icon(
                                            modifier = Modifier
                                                .size(60.dp),
                                            imageVector = ImageVector.vectorResource(id = buttonIcon),
                                            contentDescription = "Play/Stop Icon",
                                            tint = White,
                                        )
                                    }
                                }

                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        ) {
                            if(canGoNext) {
                                IconButton(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    onClick = { viewModel.navigateToNextItem() },
                                    content = {
                                        Icon(
                                            modifier = Modifier
                                                .size(30.dp),
                                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_right),
                                            contentDescription = "Next-item icon",
                                            tint = OffWhite,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dispose the Composition when viewLifecycleOwner is destroyed
    DisposableEffect(lifecycleOwner) {
        onDispose {
            viewModel.saveItemListToJson()

            if (viewModel.isRecording()) {
                viewModel.stopRecording()
            }
            if (viewModel.isPlaying()) {
                viewModel.stopPlayback()
            }
        }
    }
}


@Composable
private fun BlurredImage(imageDrawable: Drawable, modifier: Modifier = Modifier) {
    return GlideImage(
        imageModel = { imageDrawable },
        component = rememberImageComponent {
            +CrossfadePlugin(duration = 1000)
            +BlurTransformationPlugin(radius = 20) // between 0 to Int.MAX_VALUE.
//                        // shows a shimmering effect when loading an image.
//                        +ShimmerPlugin(
//                            baseColor = Color.DarkGray,
//                            highlightColor = Color.LightGray,
//                            durationMillis = 2000,
//                        )

        },
        imageOptions = ImageOptions(
            contentScale = ContentScale.Crop,
            alpha = 0.5f
        ),
        modifier = modifier,
//        // shows a progress indicator when loading an image.
//        loading = {
//            Box(Modifier.fillMaxSize()) {
//                CircularProgressIndicator(Modifier.align(Alignment.Center))
//            }
//        },
//        // shows an error text message when request failed.
//        failure = @Composable {
//            Text(text = "image request failed.")
//        },
    )
}

