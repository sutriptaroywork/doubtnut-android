package com.doubtnutapp.domain.base.manager

import io.reactivex.Single
import java.io.InputStream

interface DownloadManager {

    fun downloadFile(url: String): Single<InputStream>

    fun isIconFileDownloaded(url: String): Boolean

    fun isSampleQuestionDownloaded(url: String): Boolean
}
