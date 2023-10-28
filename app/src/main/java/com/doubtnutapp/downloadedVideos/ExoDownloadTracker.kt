package com.doubtnutapp.downloadedVideos

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.FragmentManager
import androidx.room.ColumnInfo
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.Log
import com.doubtnutapp.RemoteConfigUtils
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.VideoDownloadResponse
import com.doubtnutapp.data.remote.models.VideoLicenseResponse
import com.doubtnutapp.data.remote.repository.VideoDownloadRepository
import com.doubtnutapp.exoplayer.extensions.okhttp.OkHttpDataSourceFactory
import com.doubtnutapp.ui.mediahelper.ExoDownloadService
import com.doubtnutapp.ui.mediahelper.ExoUtils
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.drm.DrmSessionEventListener
import com.google.android.exoplayer2.drm.OfflineLicenseHelper
import com.google.android.exoplayer2.offline.*
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.util.Util
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Protocol
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.util.*

class ExoDownloadTracker private constructor(private val context: Context) {

    private val TAG = "ExoDownloadTracker"

    interface Listener {
        fun onFailure() {}
        fun onDownloadRemoved(url: String) {}
        fun onDownloadingComplete(url: String) {}
        fun onDownloadingStart(url: String) {}
    }

    private var videoDownloadRepository: VideoDownloadRepository = DataHandler.INSTANCE.videoDownloadRepository

    private var downloadManager: DownloadManager = ExoUtils.getInstance(context).getDownloadManager()

    private var downloadIndex: DownloadIndex = downloadManager.downloadIndex

    private val downloads = hashMapOf<Uri, Download>()

    private val downloadingList = mutableListOf<String>()

    private var listener: Listener? = null

    private var fragmentManager: FragmentManager? = null

    private var offlineMediaDao: OfflineMediaDao? = (context.applicationContext as? DoubtnutApp)?.getDatabase()?.offlineMediaDao()

    private var dialog: ProgressDialog? = null

    init {
        downloadManager.addListener(DownloadManagerListener())
        loadDownloads()
    }

    fun addListener(listener: Listener) {
        this.listener = listener
    }

    fun removeListener() {
        this.listener = null
    }

    fun getDownloadRequest(uri: Uri): DownloadRequest? {
        val download = downloads[uri] ?: return null
        return if (download.state != Download.STATE_FAILED)
            download.request
        else
            null
    }

    fun isMediaDownloaded(videoUrl: String): Boolean {
        val download = downloads[videoUrl.toUri()]
        return download?.state == Download.STATE_COMPLETED
    }

    private fun loadDownloads() {
        try {
            val downloadCursor = downloadIndex.getDownloads()
            while (downloadCursor.moveToNext()) {
                val download = downloadCursor.download
                downloads[download.request.uri] = downloadCursor.download
            }
        } catch (e: IOException) {
            Log.e(e)
        }
    }

    fun removeDownload(videoUrl: String) {
        DownloadService.sendRemoveDownload(context, ExoDownloadService::class.java, videoUrl, false)
    }

    fun clearAllDownloads() {
        downloads.clear()
        DownloadService.sendRemoveAllDownloads(context, ExoDownloadService::class.java, true)
        offlineMediaDao?.deleteAllVideos()
    }

    private fun showDialog(context: Context) {
        if (dialog == null) {
            dialog = ProgressDialog(context).apply {
                setMessage("Preparing video to download")
                setCanceledOnTouchOutside(false)
            }
        }
        dialog?.show()
    }

    @SuppressLint("CheckResult")
    fun downloadVideo(context: Context, questionID: String, title: String) {
        try {
            showDialog(context)
        } catch (e: Exception) {
            ToastUtils.makeText(context, "Downloading will start soon", Toast.LENGTH_LONG).show()
        }
        val downloadedCount = offlineMediaDao?.getAllVideosData()?.size ?: 0
        if (downloadedCount >= RemoteConfigUtils.getMaxDownloadVideoCount()) {
            ToastUtils.makeText(context, "Maximum 5 videos can be downloaded", Toast.LENGTH_SHORT).show()
            dialog?.dismiss()
            listener?.onFailure()
            return
        }
        Observable.zip(
            videoDownloadRepository.getDownloadOptions(questionID).subscribeOn(Schedulers.io()),
            videoDownloadRepository.getLicense(questionID).subscribeOn(Schedulers.io()),
            BiFunction<VideoDownloadResponse, VideoLicenseResponse, OfflineMediaItem>
            { firstResonse: VideoDownloadResponse,
                secondResponse: VideoLicenseResponse ->
                combineResult(questionID, firstResonse, secondResponse)!!
            }
        )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val obj = it.copy(title = title)
                downloadMedia(obj)
            }, {
                dialog?.dismiss()
                Log.e(it, TAG)
                handleError(it)
            })
    }

    private fun handleError(error: Throwable) {
        if (error !is HttpException) {
            ToastUtils.makeText(context, "Unable to download, something went wrong", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val json = JSONObject(error.response()?.errorBody()?.string() ?: "")
            val message = json.getString("message")
            ToastUtils.makeText(context, message, Toast.LENGTH_LONG).show()
        } catch (e: java.lang.Exception) {
            ToastUtils.makeText(context, "Unable to download, something went wrong", Toast.LENGTH_SHORT).show()
        }
    }

    private fun combineResult(questionId: String?, firstResponse: VideoDownloadResponse, secondResponse: VideoLicenseResponse): OfflineMediaItem? {
        val susbcriptionDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 2)
        }.time

        return OfflineMediaItem(
            0, firstResponse.cdnUrl + firstResponse.playlist,
            questionId = questionId!!.toInt(),
            title = firstResponse.title,
            drmLicenseUrl = secondResponse.licenseUrl,
            thumbUrl = firstResponse.thumbnail,
            licenceExpireDate = secondResponse.validity!!.time,
            mediaType = firstResponse.mediaType,
            aspectRatio = firstResponse.aspectRatio,
            subscriptionExpireDate = susbcriptionDate.time
        )
    }

    @ColumnInfo(name = "created_at")
    fun downloadMedia(offlineMediaItem: OfflineMediaItem) {
        if (downloadingList.contains(offlineMediaItem.videoUrl)) {
            ToastUtils.makeText(context, "Already in download process", Toast.LENGTH_SHORT).show()
            dialog?.dismiss()
            return
        }
        val download = downloads[offlineMediaItem.videoUrl.toUri()]
        if (download != null && download.state == Download.STATE_COMPLETED) {
            if (offlineMediaDao?.isVideoDataExist(download.request.id) == true) {
                ToastUtils.makeText(context, "File already downloaded", Toast.LENGTH_SHORT).show()
                dialog?.dismiss()
                return
            }
        }
        if (download?.state == Download.STATE_DOWNLOADING || download?.state == Download.STATE_QUEUED) {
            ToastUtils.makeText(context, "Download in progress", Toast.LENGTH_SHORT).show()
            dialog?.dismiss()
            return
        }
        downloadingList.add(offlineMediaItem.videoUrl)
        val okhttpClient = OkHttpClient.Builder()
            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
            .build()
        val licenseDataSourceFactory: HttpDataSource.Factory = OkHttpDataSourceFactory(okhttpClient, Util.getUserAgent(DoubtnutApp.INSTANCE, "doubtnutapp"))
        val mediaitem = getMediaItem(offlineMediaItem)
        android.util.Log.e("exoTracker", mediaitem.mediaId)
        val helper = DownloadHelper.forMediaItem(context, getMediaItem(offlineMediaItem), DefaultRenderersFactory(context), licenseDataSourceFactory)
        downloadThumbnail(offlineMediaItem.thumbUrl.orEmpty())
        StartDownloading().startFileDownloading(helper, offlineMediaItem, listener)
    }

    private fun downloadThumbnail(thumbUrl: String) {
        Glide.with(DoubtnutApp.INSTANCE.applicationContext)
            .asBitmap()
            .load(thumbUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                }
            })
    }

    private fun getMediaItem(offlineMediaItem: OfflineMediaItem) =
        MediaItem.Builder()
            .setUri(offlineMediaItem.videoUrl)
            .setDrmUuid(C.WIDEVINE_UUID)
            .setMediaMetadata(MediaMetadata.Builder().setTitle(offlineMediaItem.title).build())
            .setDrmLicenseUri(offlineMediaItem.drmLicenseUrl)
            .build()

    inner class DownloadManagerListener : DownloadManager.Listener {

        override fun onDownloadChanged(downloadManager: DownloadManager, download: Download, finalException: Exception?) {
            val rxBus = DoubtnutApp.INSTANCE.bus()
            download.contentLength
            when (download.state) {
                Download.STATE_QUEUED -> {
                    Log.d("Queued ${download.request.id}", TAG)
                    listener?.onDownloadingStart(download.request.id)
                }
                Download.STATE_DOWNLOADING -> {
                    Log.d("Downloading ${download.request.id}", TAG)
                    rxBus?.send(ExoUtils.ExoDownloadInfo(download.request.id, download.percentDownloaded, download.bytesDownloaded, download.contentLength))
                    offlineMediaDao?.updateVideoStatus(download.request.id, OfflineMediaStatus.DOWNLOADING)
                }
                Download.STATE_COMPLETED -> {
                    downloadingList.remove(download.request.uri.toString())
                    Log.d("Completed ${download.request.id}", TAG)
                    downloads[download.request.uri] = download
                    listener?.onDownloadingComplete(download.request.id)
                    offlineMediaDao?.updateVideoStatus(download.request.id, OfflineMediaStatus.DOWNLOADED)
                }
                Download.STATE_FAILED -> {
                    downloadingList.remove(download.request.uri.toString())
                    Log.d("Failed ${download.request.id}", TAG)
                    listener?.onFailure()
                    ToastUtils.makeText(context, "Downloading failed", Toast.LENGTH_SHORT).show()
                    offlineMediaDao?.deleteOfflineVideo(download.request.id)
                    // offlineMediaDao?.updateVideoStatus(download.request.id, OfflineMediaStatus.FAILURE)
                }
                Download.STATE_REMOVING -> {
                    downloads.remove(download.request.uri)
                    downloadingList.remove(download.request.uri.toString())
                    Log.d("Removing ${download.request.id}", TAG)
                }
                else -> {
                }
            }
        }

        override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
            super.onDownloadRemoved(downloadManager, download)
            offlineMediaDao?.deleteOfflineVideo(download.request.id)
            listener?.onDownloadRemoved(download.request.id)
            downloads.remove(download.request.uri)
        }
    }

    inner class StartDownloading : DownloadHelper.Callback {

        private var listener: Listener? = null
        private var offlineMediaItem: OfflineMediaItem? = null

        override fun onPrepareError(helper: DownloadHelper, e: IOException) {
            listener?.onFailure()
        }

        override fun onPrepared(helper: DownloadHelper) {
            for (item in 0 until helper.periodCount) {
                helper.clearTrackSelections(item)
            }
            helper.addTrackSelection(0, DefaultTrackSelector.ParametersBuilder(context).build())
            downloadLicenseNVideo(helper)
        }

        fun startFileDownloading(downloadHelper: DownloadHelper, offlineMediaItem: OfflineMediaItem, listener: Listener?) {
            this.listener = listener
            this.offlineMediaItem = offlineMediaItem
            downloadHelper.prepare(this)
        }

        @SuppressLint("CheckResult")
        private fun downloadLicenseNVideo(downloadHelper: DownloadHelper) {
            Observable.fromCallable {
                val format = ExoUtils.getInstance(context).getFirstFormatWithDrmInitData(downloadHelper)
                if (format != null)
                    fetchWidevineOfflineDrm(format)
                else
                    null
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ keySetId ->
                    dialog?.dismiss()
                    if (keySetId != null) {
                        buildDownloadRequest(downloadHelper, keySetId)?.let {
                            DownloadService.sendAddDownload(context, ExoDownloadService::class.java, it, true)
                            offlineMediaDao?.addOfflineVideoData(offlineMediaItem!!)
                            ToastUtils.makeText(context, "Downloading started", Toast.LENGTH_SHORT).show()
                        }
                        downloadHelper.release()
                    }
                }, {
                    dialog?.dismiss()
                    Log.e(it, TAG)
                    ToastUtils.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    listener?.onFailure()
                })
        }

        private fun fetchWidevineOfflineDrm(format: Format): ByteArray {
            val okhttpClient = OkHttpClient.Builder()
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .build()
            val licenseDataSourceFactory: HttpDataSource.Factory = OkHttpDataSourceFactory(okhttpClient, Util.getUserAgent(DoubtnutApp.INSTANCE, "doubtnutapp"))
            val licence = OfflineLicenseHelper.newWidevineInstance(offlineMediaItem?.drmLicenseUrl.orEmpty(), licenseDataSourceFactory, DrmSessionEventListener.EventDispatcher())
            return licence.downloadLicense(format)
        }

        private fun buildDownloadRequest(downloadHelper: DownloadHelper, keySetId: ByteArray): DownloadRequest? {
            return downloadHelper
                .getDownloadRequest(Util.getUtf8Bytes(Assertions.checkNotNull(offlineMediaItem?.title.orEmpty())))
                .copyWithKeySetId(keySetId)
                .copyWithId(offlineMediaItem?.videoUrl!!)
        }
    }

    companion object {

        private var instance: ExoDownloadTracker? = null

        fun getInstance(context: Context) = instance
            ?: synchronized(ExoDownloadTracker::class) {
                instance ?: ExoDownloadTracker(context).also {
                    instance = it
                }
            }
    }
}
