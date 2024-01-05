package com.toddler.recordit.screens.record

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toFile
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.animation.crossfade.CrossfadePlugin
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.transformation.blur.BlurTransformationPlugin
import com.toddler.recordit.Dashboard
import com.toddler.recordit.R
import com.toddler.recordit.ui.theme.NavyDark
import com.toddler.recordit.utils.getImagesFromAssets
import java.io.File
import java.util.Locale

@Composable
fun RecordScreen(viewModel: ImageRecordingViewModel, goBack: () -> Unit) {
    val context = LocalContext.current
    val itemList = viewModel.itemList
    Log.i("RecordScreen", "COMPOSED RECORD SCREEN")

//    Scaffold(Modifier.fillMaxSize()) {
    ScreenContent(context, itemList, viewModel, goBack)
//    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenContent(
    context: Context,
    itemList: List<RecordItem>,
    viewModel: ImageRecordingViewModel,
    goBack: () -> Unit
) {

    var item by rememberSaveable(stateSaver = RecordItemSaver) { mutableStateOf(itemList[0]) }

    val drawable by lazy {
        Drawable.createFromStream(context.assets.open("${item.imagePath}"), null)
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
                        contentDescription = "Record",
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
                    .weight(1f, true)
                    .fillMaxWidth(),
            ) {
                Card(
                    elevation = CardDefaults.cardElevation(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.DarkGray,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(0.dp)
                        .align(Alignment.Center)
//                .shadow(
//                    elevation = 28.dp,
//                    shape = RoundedCornerShape(0.dp),
//                    clip = true // Allow shadow to extend beyond the Box
//                )
                        .graphicsLayer {
                            alpha = 0.5f
                        },
                ) { }
                val interactionSource = remember { MutableInteractionSource() }
                val pressed by interactionSource.collectIsPressedAsState()
                var recorded by remember { mutableStateOf(item.recorded) }
                val audioFile = File(context.cacheDir, "audio.mp3")
                ElevatedButton(
                    modifier = Modifier
                        .padding(14.dp)
                        .size(72.dp) //.background(Navy, shape = androidx.compose.foundation.shape.CircleShape)
                        .align(Alignment.Center),
                    contentPadding = PaddingValues(16.dp),
                    elevation = ButtonDefaults.buttonElevation(12.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color.White,
                        contentColor = NavyDark,
                    ),
                    onClick = {

//                        item = if (item == itemList.last()) {
//                            itemList.first()
//                        } else {
//                            itemList[item.id + 1]
//                        }
//                        val cacheDir = context.cacheDir
                        if (!viewModel.isRecording()) {
//                                  File(cacheDir, "audio.mp3").also {
//                                  recorder.start(it)
//                                      audioFile = it
//                                  }

//                            viewModel.returnUri(item.title).path?.let {
//                                File(
//                                    it
//                                )
//                            }?.let { viewModel.startRecording(it) }
                            viewModel.startRecording(audioFile)
                        } else {
                            viewModel.stopRecording()
                            item.recorded = true
                            recorded = true
                        }
                    },
//                    interactionSource = interactionSource, // remember to pass the source, the source is used to collect the interaction state from the button and can be aquired from the interactionSource.collectIsPressedAsState() method
                ) {
                    Icon(
                        modifier = Modifier
                            .size(60.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_mic_filled_54),
                        contentDescription = "Record",
                        tint = NavyDark,
                    )
                }

                var isPlaying by rememberSaveable { mutableStateOf(viewModel.isPlaying()) }
                var buttonIcon by rememberSaveable { mutableIntStateOf(R.drawable.ic_play) }

                Box(modifier = Modifier
                    .padding(14.dp)
                    .size(72.dp) //.background(Navy, shape = androidx.compose.foundation.shape.CircleShape)
                    .align(Alignment.BottomCenter),){
                    if(recorded){
                        ElevatedButton(
                            modifier = Modifier
                                .padding(14.dp)
                                .size(40.dp) //.background(Navy, shape = androidx.compose.foundation.shape.CircleShape)
                                .align(Alignment.BottomCenter),
                            contentPadding = PaddingValues(8.dp),
                            elevation = ButtonDefaults.buttonElevation(12.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = Color.White,
                                contentColor = NavyDark,
                            ),
                            onClick = {
//                            val inputStream = context.contentResolver.openInputStream(
//                                viewModel.returnUri(item.title)
//                            )
                                viewModel.apply {
                                    buttonIcon = if (!isPlaying) {
                                        startPlayback(audioFile)
                                        R.drawable.ic_stop
                                    } else {
                                        stopPlayback()
                                        R.drawable.ic_play
                                    }
                                    isPlaying = !isPlaying
                                }

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
                                contentDescription = "Record",
                                tint = NavyDark,
                            )
                        }
                    }

                }

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
            +BlurTransformationPlugin(radius = 50) // between 0 to Int.MAX_VALUE.
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

