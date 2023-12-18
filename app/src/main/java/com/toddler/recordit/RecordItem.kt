package com.toddler.recordit

import android.graphics.drawable.Drawable

data class RecordItem(
    val id: Int,
    val title: String,
    val description: String,
    val image: Drawable
)