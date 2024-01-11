package com.toddler.recordit.utils


import java.io.*
import java.util.zip.ZipFile

interface UnzipListener {
    fun onUnzipComplete()
    fun onUnzipFailed(error: Exception)
}

object UnzipUtilsWithListeners {

    fun unzip(zipFilePath: File, destDirectory: String, listener: UnzipListener? = null) {
        try {
            File(destDirectory).run {
                if (!exists()) {
                    mkdirs()
                }
            }

            ZipFile(zipFilePath).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    zip.getInputStream(entry).use { input ->
                        val filePath = destDirectory + File.separator + entry.name

                        if (!entry.isDirectory) {
                            extractFile(input, filePath)
                        } else {
                            val dir = File(filePath)
                            dir.mkdir()
                        }
                    }
                }
            }

            listener?.onUnzipComplete()

        } catch (e: Exception) {
            listener?.onUnzipFailed(e)
        }
    }

    private fun extractFile(inputStream: InputStream, destFilePath: String) {
        val bos = BufferedOutputStream(FileOutputStream(destFilePath))
        val bytesIn = ByteArray(BUFFER_SIZE)
        var read: Int
        while (inputStream.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
    }

    private const val BUFFER_SIZE = 4096
}
