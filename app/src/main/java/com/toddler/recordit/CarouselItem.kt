package com.toddler.recordit

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import java.io.File


@Composable
fun CarouselItem(context: Context, item: RecordItem) {
    val cardHight = 250.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHight)
            .shadow(elevation = 6.dp),
        shape = MaterialTheme.shapes.medium
    ) {
//        val imgUri = Uri.parse(item.image)
        GlideImage(
            imageModel = { item.image },
            imageOptions = ImageOptions(
                contentScale = ContentScale.Crop,
                contentDescription = item.description,),
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHight),
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
    }
}