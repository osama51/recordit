package com.toddler.recordit

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import java.io.IOException
import java.io.InputStream


@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
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
            .padding(0.dp, 10.dp)
            .background(Color.White)
    ) {
        val pageCount = 17
        val pagerState = rememberPagerState(
            initialPage = 0,
            initialPageOffsetFraction = 0.0f //within the range -0.5 to 0.5
        ) {
            // provide pageCount
            pageCount
        }
//        Box(modifier = Modifier
//            .background(Color.White)
//            .padding(it)) {
//            Text(text = "Home Screen")
//        }
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(it))  {
            HorizontalPager(contentPadding = PaddingValues(horizontal = 32.dp),
                pageSpacing = 16.dp, state = pagerState) { page ->
                CarouselItem(context, itemList[page])
            }

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
    val defaultImage = ResourcesCompat.getDrawable(context.resources, R.drawable.i20170914_by_ra_lilium_dbnsypi, null)
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
