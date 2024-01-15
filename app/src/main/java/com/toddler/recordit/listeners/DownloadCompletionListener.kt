package com.toddler.recordit.listeners

import java.io.File

interface DownloadCompletionListener {
    fun onDownloadComplete()
    fun onDownloadFailed(file: File, exception: Exception)
    fun onNoFilesToDownload()
}