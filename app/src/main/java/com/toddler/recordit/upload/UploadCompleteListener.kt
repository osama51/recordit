package com.toddler.recordit.upload

import java.io.File

interface UploadCompletionListener {
    fun onUploadComplete()
    fun onUploadFailed(file: File, exception: Exception)
}