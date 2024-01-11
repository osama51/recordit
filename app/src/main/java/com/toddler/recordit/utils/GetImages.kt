package com.toddler.recordit.utils

import android.content.Context
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import com.toddler.recordit.R
import java.io.IOException

// fun to return list of myimages from assets/myimages path using Glide
fun getImagesFromAssets(context: Context): MutableList<Map<String, String>> {
    val assetManager = context.assets
    val files = assetManager.list("myimages")!!.toList()
//    val images = mutableListOf<String>()
//    val mapImages = mutableListOf<Map<String, Drawable>>()
//    val drawableImages = mutableListOf<Drawable>()
//    val defaultImage = R.drawable.i20170914_by_ra_lilium_dbnsypi.toString()
    val mapImages = mutableListOf<Map<String, String>>()

    val defaultImage = ResourcesCompat.getDrawable(
        context.resources,
        R.drawable.i20170914_by_ra_lilium_dbnsypi,
        null
    )
    try {
        files.forEach {
            if(validFile(it)){
//                val image: InputStream = assetManager.open("myimages/$it")
//                val d = Drawable.createFromStream(image, null)
//                images.add("myimages/$it")
//                drawableImages.add(d ?: defaultImage!!)
//                mapImages.add(mapOf(it to (d ?: defaultImage!!)))
                mapImages.add(mapOf(it to "myimages/$it"))
            } else {
                Log.d("getImagesFromAssets","File $it is not an image")
            }
        }

    } catch (ex: IOException) {
        ex.printStackTrace()
//        drawableImages.fill(defaultImage!!)
    }
    return mapImages
}

// get images from filesDir
fun getImagesFromFilesDir(context: Context): MutableList<Map<String, String>> {

    // get files in images folder inside filesDir
     val files = context.getDir("images", Context.MODE_PRIVATE).listFiles()
    Log.i("getImagesFromFilesDir", "getDir: ${context.getDir("images", Context.MODE_PRIVATE)}")
    Log.i("getImagesFromFilesDir", "files: $files")
    val mapImages = mutableListOf<Map<String, String>>()
    context.getDir("images", Context.MODE_PRIVATE).listFiles()?.forEach {
        if (validFile(it.name)) {
            mapImages.add(mapOf(it.name to it.absolutePath))
        } else {
            Log.d("getImagesFromFilesDir", "File ${it.name} is not an image")
        }
    }
    return mapImages
}




fun validFile(fileName: String): Boolean {
// check if fileName ends with .jpg or .png
    val regex = Regex(pattern = "(.jpeg|.jpg|.png)$")
    val result = regex.find(input = fileName)
//    if (result == null) {
//        throw IOException("File $fileName is not an image")
//    }
    return result != null
}

