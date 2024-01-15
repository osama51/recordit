package com.toddler.recordit.listeners

import java.io.File

interface UploadCompletionListener {
    fun onUploadComplete()
    fun onUploadFailed(file: File, exception: Exception)
}