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
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import java.util.Locale

@Composable
fun RecordScreen(navController: NavHostController) {
    val context = LocalContext.current
    val itemList = getImagesFromAssets(context = context).mapIndexed { index, imageMap ->
        var imageName = imageMap.entries.first().key
        imageName = imageName.dropLast(4).capitalizeWords()
        Log.i("RecordScreen", "itemList re-occupied !!")
        RecordItem(
            id = index,
            title = imageName, //imageMap.toString().substring(7),
            description = "Description ${imageName}",
            image = imageMap.entries.first().value
        )
    }

//    Scaffold(Modifier.fillMaxSize()) {
    ScreenContent(navController, itemList)
//    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenContent(navController: NavHostController, itemList: List<RecordItem>) {


    var item by remember { mutableStateOf(itemList[0]) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        BlurredImage(
            imageDrawable = item.image,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
        )
//        val bitmap = BitmapFactory
//            .decodeResource(
//                LocalContext.current.resources,
//                R.drawable.i20170914_by_ra_lilium_dbnsypi
//            )
//
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
//            LegacyBlurImage(bitmap, 25f) // 0 < radius <= 25
//        } else {
//            BlurImage(
//                bitmap,
//                Modifier
//                    .fillMaxSize()
//                    .blur(radiusX = 15.dp, radiusY = 15.dp)
//            )
////                LegacyBlurImage(bitmap, 25f) // 0 < radius <= 25


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
                val img = GlideImage(
                    imageModel = { item.image },
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
                    onClick = {
                        navController.navigate(Dashboard.route)
                    },
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
                        Log.i("RecordScreen", "item.id = ${item.id}")
                        Log.i("RecordScreen", "item.last = ${item == itemList.last()}")
                        Log.i("RecordScreen", "item.last = ${itemList.last()}")
                        Log.i("RecordScreen", "item.current = ${item}")

                        if(item == itemList.last()) {
                            Log.i("RecordScreen", "SET ITEM TO FIRST")
                            item = itemList.first()
                        } else {
                            Log.i("RecordScreen", "Still PRINTING")
                            item = itemList[item.id + 1]
                        }
//                        val cacheDir = context.cacheDir
                              if(pressed) {
//                                  File(cacheDir, "audio.mp3").also {
//                                  recorder.start(it)
//                                      audioFile = it
//                                  }
                              }
                    },
                    interactionSource = interactionSource, // remember to pass the source, the source is used to collect the interaction state from the button and can be aquired from the interactionSource.collectIsPressedAsState() method
                ) {
                    Icon(
                        modifier = Modifier
                            .size(60.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_mic_filled_54),
                        contentDescription = "Record",
                        tint = NavyDark,
                    )
                }
            }
        }
    }
}


@Composable
private fun BlurredImage(imageDrawable: Drawable, modifier: Modifier = Modifier) {
    val img = GlideImage(
        imageModel = { imageDrawable },
        component = rememberImageComponent {
            +CrossfadePlugin(duration = 2000)
            +BlurTransformationPlugin(radius = 100) // between 0 to Int.MAX_VALUE.
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
        modifier = modifier
    )
    return img
}

// function to take a string, replace underscores with spaces, and capitalize each word
fun String.capitalizeWords(): String = split("_").joinToString(" ") {
    it.replaceFirstChar { firstChar ->
        if (firstChar.isLowerCase()) firstChar.titlecase(
            Locale.ROOT
        ) else firstChar.toString()
    }
}

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

////////////////////////////////////////

@Composable
private fun LegacyBlurImage(
    bitmap: Bitmap,
    blurRadio: Float,
    modifier: Modifier = Modifier.fillMaxSize()
) {

    val renderScript = RenderScript.create(LocalContext.current)
    val bitmapAlloc = Allocation.createFromBitmap(renderScript, bitmap)
    ScriptIntrinsicBlur.create(renderScript, bitmapAlloc.element).apply {
        setRadius(blurRadio)
        setInput(bitmapAlloc)
        forEach(bitmapAlloc)
    }
    bitmapAlloc.copyTo(bitmap)
    renderScript.destroy()

    BlurImage(bitmap, modifier)
}

@Composable
fun BlurImage(
    bitmap: Bitmap,
    modifier: Modifier = Modifier,
) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}