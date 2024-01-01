package com.toddler.recordit.screens.dashboard

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.toddler.recordit.screens.record.RecordItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarouselItem(context: Context, item: RecordItem) {
    val cardHeight = 250.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .shadow(elevation = 6.dp)
//            .graphicsLayer(
//                alpha = 1.0f,
//                ambientShadowColor = Color.Red,
//            )
//            .coloredShadow(
//                color = Color.Red,
//                alpha = 0.2f,
//                borderRadius = 0.dp,
//                shadowRadius = 20.dp,
//                offsetY = 0.dp,
//                offsetX = 0.dp
//            )
        ,
        shape = MaterialTheme.shapes.medium,
        onClick = {
            val drawable = item.imagePath
            Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
        }
    ) {
//        val imgUri = Uri.parse(item.image)

        val drawable by lazy {
            Drawable.createFromStream(context.assets.open(item.imagePath), null)
        }

        GlideImage(
            imageModel = { drawable },
            imageOptions = ImageOptions(
                contentScale = ContentScale.Crop,
                contentDescription = item.description,),
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .fillMaxSize()
            ,
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