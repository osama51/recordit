package com.toddler.recordit

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(value: FirebaseAuth) {

    val context = LocalContext.current
    val itemList = getImagesFromAssets(context = context).mapIndexed { index, image ->
        RecordItem(
            id = index,
            title = image.substring(7),
            description = "Description ${image.substring(7)}",
            image = image
        )
    }
    Scaffold(
        modifier = Modifier.background(Color.White)
    ) {
        val pageCount = 30
        val pagerState = rememberPagerState(
            initialPage = 0,
            initialPageOffsetFraction = 2f
        ) {
            // provide pageCount
            pageCount
        }
        Box(modifier = Modifier.background(Color.White)
            .padding(it)) {
            Text(text = "Home Screen")
        }
        Box {
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
 fun getImagesFromAssets(context: Context): List<String> {
     val assetManager = context.assets
        val files = assetManager.list("images")!!.toList()
        val images = mutableListOf<String>()
        files.forEach {
            images.add("images/$it")
        }
        return images
    }
