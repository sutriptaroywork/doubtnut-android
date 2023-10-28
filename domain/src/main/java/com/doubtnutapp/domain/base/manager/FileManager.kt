package com.doubtnutapp.domain.base.manager

import java.io.InputStream

interface FileManager {

    fun isFilePresent(filePath: String): Boolean

    fun createDirectory(dirPath: String): Boolean

    fun saveFileToDirectory(fileInputStream: InputStream, desPath: String)

    fun fileNameFromUrl(url: String): String

    fun deleteFile(filePath: String): Boolean
}
