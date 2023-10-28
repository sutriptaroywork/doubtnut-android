package com.doubtnutapp.utils

import android.content.Context
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.Log
import java.io.File

object FileUtils {

    const val EMPTY_PATH = ""

    //region File Extensions
    const val EXT_PDF = "pdf"
    //endregion

    const val DIR_IMAGES = "images"

    fun isFilePresent(filePath: String?): Boolean {
        return filePath?.let {
            File(filePath).exists()
        } ?: false
    }

    fun createDirectory(parentDirectoryPath: String, directoryName: String): Boolean {
        val directory = File(parentDirectoryPath, directoryName)
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

    //return file name with extension
    fun fileNameFromUrl(url: String, fileSuffix: String? = null) = if (fileSuffix.isNullOrEmpty()) {
        url.substring(url.lastIndexOf("/") + 1)
    } else {
        val fileName = url.substring(url.lastIndexOf("/") + 1)
        val splitArr = fileName.split(".")
        splitArr.getOrNull(0) + fileSuffix + "." + splitArr.getOrNull(1)
    }

    fun getFileList(dirPath: String): Array<File>? = try {
        File(dirPath).listFiles()
    } catch (ex: SecurityException) {
        ex.printStackTrace()
        null
    }

    fun isDirectoryEmpty(dirPath: String): Boolean {
        val numberOfFiles = getFileList(dirPath)?.size
        return if (numberOfFiles == null || numberOfFiles == 0) return true else false
    }

    fun deleteFiles(desFilePath: String) {
        val someDir = File(desFilePath)
        someDir.deleteRecursively()
    }

    fun deleteCompressVideoFileDir(context: Context) {
        val desFilePath = context.getExternalFilesDir(null)?.path + File.separator + "doubtnut_video_upload"
        deleteFiles(desFilePath)
    }

    fun getCompressVideoDesPath(context: Context): String {
        deleteCompressVideoFileDir(context)
        val externalDirectoryPath = context.getExternalFilesDir(null)?.path
                ?: ""
        val isChildDirCreated = createDirectory(externalDirectoryPath, "doubtnut_video_upload")
        if (isChildDirCreated) {
            return context.getExternalFilesDir(null)?.path.orEmpty() +
                    File.separator +
                    "doubtnut_video_upload" + File.separator + "VID_" + System.currentTimeMillis() + ".mp4"
        }

        return EMPTY_PATH
    }

    fun getPdfFileDestinationPath(url: String): String {
        val context = DoubtnutApp.INSTANCE
        val externalDirectoryPath = context.getExternalFilesDir(null)?.path
            ?: ""
        val isChildDirCreated =
            createDirectory(externalDirectoryPath, AppUtils.PDF_DIR_NAME)

        if (isChildDirCreated) {
            val fileName = fileNameFromUrl(url)
            return AppUtils.getPdfDirectoryPath(context) + File.separator + fileName
        }
        return EMPTY_PATH
    }

    fun deleteCacheDir(context: Context, directory: String) {
        try {
            val desFilePath = context.cacheDir?.path + File.separator + directory
            deleteFiles(desFilePath)
        } catch (e: Exception) {
            Log.e(e)
        }
    }

}