package com.doubtnutapp.data.base.manager

import com.doubtnutapp.domain.base.manager.FileManager
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileManagerImpl @Inject constructor() : FileManager {

    override fun isFilePresent(filePath: String) = File(filePath).exists()

    override fun createDirectory(dirPath: String): Boolean {
        val directory = File(dirPath)
        return if (directory.exists()) {
            true
        } else {
            try {
                directory.mkdirs()
            } catch (securityException: SecurityException) {
                securityException.printStackTrace()
                false
            }
        }
    }

    override fun saveFileToDirectory(fileInputStream: InputStream, desPath: String) {
        val bufferedOutputStream = BufferedOutputStream(FileOutputStream(desPath))
        val bufferSize = 1024
        bufferedOutputStream.use { op ->
            fileInputStream.use { ip ->
                ip.copyTo(op, bufferSize)
            }
        }
    }

    override fun fileNameFromUrl(url: String) = url.substring(url.lastIndexOf("/") + 1)

    override fun deleteFile(filePath: String): Boolean = File(filePath).delete()
}
