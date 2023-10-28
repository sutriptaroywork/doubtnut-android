package com.doubtnutapp.ui.mediahelper

import android.content.Context
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadProgress
import com.google.android.exoplayer2.ui.DownloadNotificationHelper
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import java.util.concurrent.Executors

class ExoUtils private constructor(private val context: Context) {

    private val DOWNLOAD_CONTENT_DIRECTORY = "offline"

    private var downloadManager: DownloadManager? = null

    val downloadNotificationHelper: DownloadNotificationHelper by lazy {
        DownloadNotificationHelper(context, EXO_DOWNLOAD_NOTIFICATION_CHANNEL)
    }

    fun getDownloadManager(): DownloadManager {
        if (downloadManager == null) {
            downloadManager = DownloadManager(
                    context,
                    ExoDatabaseProvider(context),
                    ExoPlayerCacheManager.getInstance(context).cache,
                    DefaultDataSourceFactory(context),
                    Executors.newFixedThreadPool( /* nThreads= */6))
        }
        return downloadManager!!
    }

    fun buildRenderersFactory(preferExtensionRenderer: Boolean): RenderersFactory {
        val extensionRendererMode = if (preferExtensionRenderer)
            DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
        else DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF
        return DefaultRenderersFactory(context.applicationContext)
                .setExtensionRendererMode(extensionRendererMode)
    }

    fun getFirstFormatWithDrmInitData(helper: DownloadHelper): Format? {
        for (periodIndex in 0 until helper.periodCount) {
            val mappedTrackInfo = helper.getMappedTrackInfo(periodIndex)
            for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
                val trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex)
                for (trackGroupIndex in 0 until trackGroups.length) {
                    val trackGroup = trackGroups[trackGroupIndex]
                    for (formatIndex in 0 until trackGroup.length) {
                        val format = trackGroup.getFormat(formatIndex)
                        if (format.drmInitData != null) {
                            return format
                        }
                    }
                }
            }
        }
        return null
    }

    companion object {

        private var instance: ExoUtils? = null

        fun getInstance(context: Context): ExoUtils = instance ?: synchronized(ExoUtils::class) {
            instance ?: ExoUtils(context).apply {
                instance = this
            }
        }

    }

    class ExoDownloadInfo(val videoUrl: String, val percentage: Float, val downloadedBytes: Long, val totalBytes : Long)

}