package com.doubtnutapp.data.base.manager

import com.doubtnutapp.data.base.di.qualifier.AppInternalDirPath
import com.doubtnutapp.data.base.di.qualifier.ApplicationCachePath
import com.doubtnutapp.data.base.di.qualifier.IconDirName
import com.doubtnutapp.data.base.service.DownloadService
import com.doubtnutapp.domain.base.manager.DownloadManager
import com.doubtnutapp.domain.base.manager.FileManager
import io.reactivex.Single
import java.io.File
import java.io.InputStream
import javax.inject.Inject

class DownloadManagerImp @Inject constructor(
    private val downloadService: DownloadService,
    private val fileManager: FileManager,
    @AppInternalDirPath private val appInternalDirPath: String?,
    @ApplicationCachePath private val appCacheDirPath: String,
    @IconDirName private val iconDirName: String
) : DownloadManager {

    override fun isIconFileDownloaded(url: String): Boolean {
        val iconLocalFileName = fileManager.fileNameFromUrl(url)
        val iconLocalFilePath =
            "$appInternalDirPath${File.separator}$iconDirName${File.separator}$iconLocalFileName"
        return fileManager.isFilePresent(iconLocalFilePath)
    }

    override fun isSampleQuestionDownloaded(url: String): Boolean {
        val sampleQuesImageFileName = fileManager.fileNameFromUrl(url)
        val sampleQuestionAbsolutePath = "$appCacheDirPath${File.separator}$sampleQuesImageFileName"
        return fileManager.isFilePresent(sampleQuestionAbsolutePath)
    }

    override fun downloadFile(url: String): Single<InputStream> {
        return downloadService.downloadSampleCropImage(url).map {
            it.byteStream()
        }
    }
}
