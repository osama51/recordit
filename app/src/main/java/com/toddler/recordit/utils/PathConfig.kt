package com.toddler.recordit.utils

import android.content.Context
import android.net.Uri
import android.os.Environment

data class pathConfig(
    private val prefix: String = "recordit",
    val context: Context,
//    private val suffix: String = DateFormat
//        .getDateTimeInstance()
//        .format(System.currentTimeMillis())
//        .toString()
//        .replace(",","")
//        .replace(":","")
//        .replace(" ", "_"),
    val suffix: String,
    val extension: String,

    val fileName: String = "${prefix}_$suffix.$extension",
//    val hostPath: String = Environment
//        .getExternalStorageDirectory()?.absolutePath?.plus("/Recordings/RecordIt") ?: ""

    // hostPath using the app's files directory instead of external storage
    val hostPath: String = Uri.parse("${context.filesDir}/data").path ?: ""
    ){}