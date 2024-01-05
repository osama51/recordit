package com.toddler.recordit.screens.record

import androidx.compose.runtime.saveable.Saver

data class RecordItem(
    val id: Int,
    val title: String,
    val description: String,
    val imagePath: String,
    var recorded: Boolean = false
) {}



val RecordItemSaver = Saver<RecordItem, Map<String, Any?>> (
    save = {
        mapOf(
            "id" to it.id,
            "title" to it.title,
            "description" to it.description,
            "imagePath" to it.imagePath, // Save the image path
             "recorded" to it.recorded
        )
    },
    restore = {
        RecordItem(
            id = it["id"] as Int,
            title = it["title"] as String,
            description = it["description"] as String,
            imagePath = it["imagePath"] as String, // Restore image path
            recorded = it["recorded"] as Boolean
        )
    }
)

