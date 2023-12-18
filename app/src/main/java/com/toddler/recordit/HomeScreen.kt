package com.toddler.recordit

import android.content.Context
import android.graphics.drawable.Drawable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getString
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.toddler.recordit.ui.theme.Navy
import com.toddler.recordit.ui.theme.fontFamily
import java.io.IOException
import java.io.InputStream


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen() {
    // this val should be an argument passed not initiated here
    val value: FirebaseAuth

    val context = LocalContext.current
    val itemList = getImagesFromAssets(context = context).mapIndexed { index, image ->
        RecordItem(
            id = index,
            title = image.toString().substring(7),
            description = "Description ${image.toString().substring(7)}",
            image = image
        )
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val pageCount = 18
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
            Column(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(0.dp, 0.dp, 20.dp, 20.dp))
                    .background(Navy)
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Column(modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Top,){
                    val assetManager = context.assets
                    val files = assetManager.list("images")!!.toList()
                    val numberOfImages = files.size
                    Text(
                        text = "$numberOfImages",
                        fontFamily = fontFamily,
                        style = TextStyle(
                            fontSize = 48.sp,
                            color = Color.White
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    )
                    Text(
                        text = "Swipe to see your memories",
                        style = TextStyle(
                            fontSize = 24.sp,
                            color = Color.White
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    )
                }
                HorizontalPager(
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
                        contentDescription = "Start slideshow icon"
                    )
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


// fun to return list of images from assets/images path using Glide
fun getImagesFromAssets(context: Context): List<Drawable> {
    val assetManager = context.assets
    val files = assetManager.list("images")!!.toList()
    val images = mutableListOf<String>()
//    val defaultImage = R.drawable.i20170914_by_ra_lilium_dbnsypi.toString()
    val defaultImage = ResourcesCompat.getDrawable(
        context.resources,
        R.drawable.i20170914_by_ra_lilium_dbnsypi,
        null
    )
    val drawableImages = mutableListOf<Drawable>()
    try {
        files.forEach {
            val ims: InputStream = assetManager.open("images/$it")
            val d = Drawable.createFromStream(ims, null)
            images.add("images/$it")
            drawableImages.add(d ?: defaultImage!!)
        }

    } catch (ex: IOException) {
        ex.printStackTrace()
        drawableImages.fill(defaultImage!!)
    }

    return drawableImages

}

