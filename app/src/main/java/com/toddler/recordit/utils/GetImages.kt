package com.toddler.recordit.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import com.toddler.recordit.R
import java.io.IOException
import java.io.InputStream

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

