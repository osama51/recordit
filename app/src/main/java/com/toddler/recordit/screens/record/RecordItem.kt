package com.toddler.recordit.screens.record

import android.content.Intent
import android.graphics.drawable.Drawable

data class RecordItem(
    val id: Int,
    val title: String,
    val description: String,
    val image: Drawable
) {
}