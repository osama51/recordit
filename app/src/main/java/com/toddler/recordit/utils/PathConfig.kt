package com.toddler.recordit.utils

import android.os.Environment

data class pathConfig(
    private val prefix: String = "recordit",
//    private val suffix: String = DateFormat
//        .getDateTimeInstance()
//        .format(System.currentTimeMillis())
//        .toString()
//        .replace(",","")
//        .replace(":","")
//        .replace(" ", "_"),
    val suffix: String,

    val fileName: String = "$prefix-$suffix.mp3",
    val hostPath: String = Environment
        .getExternalStorageDirectory()?.absolutePath?.plus("/Recordings/RecordIt") ?: ""
){}