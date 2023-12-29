package com.toddler.recordit.screens.record

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import androidx.compose.runtime.saveable.Saver
import kotlinx.parcelize.Parcelize

data class RecordItem(
    val id: Int,
    val title: String,
    val description: String,
    val image: Drawable
) {}



//val RecordItemSaver = Saver<RecordItem, Map<String, Any?>> (
//    save = {
//        mapOf(
//            "id" to it.id,
//            "title" to it.title,
//            "description" to it.description,
//            "image" to it.image.constantState?.toString()  // Handle Drawable saving
//        )
//    },
//    restore = {
//        RecordItem(
//            id = it["id"] as Int,
//            title = it["title"] as String,
//            description = it["description"] as String,
//            image = it["image"]?.let { bundle ->
//                Drawable.createFrom
//            }
//        )
//    }
//)
//

