package com.toddler.recordit

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.toddler.recordit.ui.theme.Abel
import com.toddler.recordit.ui.theme.OffWhite
import com.toddler.recordit.ui.theme.Orange
import com.toddler.recordit.ui.theme.Red
import com.toddler.recordit.ui.theme.Russo
import java.io.IOException
import java.io.InputStream


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen() {
    // this val should be an argument passed not initiated here
    val value: FirebaseAuth

    val context = LocalContext.current
    val itemList = getImagesFromAssets(context = context).mapIndexed { index, imageMap ->
        val imageName = imageMap.entries.first().key
        RecordItem(
            id = index,
            title = imageName, //imageMap.toString().substring(7),
            description = "Description ${imageName}",
            image = imageMap.entries.first().value
        )
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val assetManager = context.assets
        val files = assetManager.list("myimages")!!.toList()
        val numberOfImages = files.size

        val pageCount = numberOfImages - 1
        val pagerState = rememberPagerState(
            initialPage = 0,
            initialPageOffsetFraction = 0.0f //within the range -0.5 to 0.5
        ) {
            // provide pageCount
            pageCount
        }
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(it),) {
            Column(){
                Column(
                    modifier = Modifier
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
                    Column(modifier = Modifier
                        .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,){

                        Text(
                            text = "$numberOfImages",
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
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(4.dp))
                        Text(
                            text = "Images left to record",
                            fontFamily = Abel,
                            style = TextStyle(
                                fontSize = 24.sp,
                                color = OffWhite,),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(4.dp))
                    }
                }
                HorizontalPager( modifier = Modifier.padding( 0.dp, 24.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp),
                    pageSpacing = 16.dp, state = pagerState
                ) { page ->
                    CarouselItem(context, itemList[page])
                }
            }

            val imageVector = Icons.Filled.PlayArrow
            val buttonText = "Start"
            Box(modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)){
                val iconSize = 24.dp
                Button(modifier = Modifier.fillMaxWidth(0.4f),
                    shape = MaterialTheme.shapes.medium,
                    onClick = { /*TODO*/ }) {
                    Icon(
                        modifier = Modifier.size(iconSize),
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Start slideshow icon")
                    Text(text = "Start")
                }
            }

            ExtendedFloatingActionButton(
                text = { Text(text = getString(context, R.string.start_slideshow)) },
                icon = { Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Start slideshow icon")},
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onClick = { /*TODO*/ },)

            DotIndicators(
                pageCount = pageCount,
                pagerState = pagerState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}


// fun to return list of myimages from assets/myimages path using Glide
fun getImagesFromAssets(context: Context): MutableList<Map<String, Drawable>> {
    val assetManager = context.assets
    val files = assetManager.list("myimages")!!.toList()
    val images = mutableListOf<String>()
    val mapImages = mutableListOf<Map<String, Drawable>>()
//    val defaultImage = R.drawable.i20170914_by_ra_lilium_dbnsypi.toString()
    val defaultImage = ResourcesCompat.getDrawable(
        context.resources,
        R.drawable.i20170914_by_ra_lilium_dbnsypi,
        null
    )
    val drawableImages = mutableListOf<Drawable>()
    try {
        files.forEach {
            if(validFile(it)){
                val image: InputStream = assetManager.open("myimages/$it")
                val d = Drawable.createFromStream(image, null)
                images.add("myimages/$it")
                drawableImages.add(d ?: defaultImage!!)
                mapImages.add(mapOf(it to (d ?: defaultImage!!)))

            } else {
                Log.d("getImagesFromAssets","File $it is not an image")
            }
        }

    } catch (ex: IOException) {
        ex.printStackTrace()
        drawableImages.fill(defaultImage!!)
    }

    return mapImages
}

fun validFile(fileName: String): Boolean {
// check if fileName ends with .jpg or .png
    val regex = Regex(pattern = "(.jpg|.png)$")
    val result = regex.find(input = fileName)
//    if (result == null) {
//        throw IOException("File $fileName is not an image")
//    }
    return result != null
}

