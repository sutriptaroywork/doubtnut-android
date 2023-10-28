package com.doubtnutapp.videoPage.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.BuildConfig
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.plus
import com.doubtnut.core.utils.toast
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.FileUtils
import com.doubtnutapp.utils.showApiErrorToast
import com.downloader.*
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.DaggerDialogFragment
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_share_video.*
import java.io.File
import java.net.URL
import java.net.URLConnection
import javax.inject.Inject

class DialogShareVideo : DaggerDialogFragment() {

    private val compositeDisposable = CompositeDisposable()
    private var permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val RC_PERMISSIONS = 987
    private val config: PRDownloaderConfig by lazy {
        PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .setReadTimeout(30_000)
                .setConnectTimeout(30_000)
                .build()
    }
    var onShareLink: () -> Unit = {}
    var onPermissionBocked: () -> Unit = {}
    var url = ""
    var downloadId: Int = 0
    var sharingMessage: String = ""


    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        isCancelable = false
        return inflater.inflate(R.layout.dialog_share_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PRDownloader.initialize(requireContext(), config)
        progressView.isVisible = true
        mainView.isInvisible = true
        setListener()
        fetchLength()
    }

    private fun configureNotificationEducationView() {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels - 100
        val height = (displayMetrics.heightPixels * .50).toInt()
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun setListener() {
        btnLinkShare.setOnClickListener {
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_VIDEO_CONTENT_SHARE_CLICKED_ON_LINK_SHARE, hashMapOf()))
            onShareLink()
            dismiss()
        }
        btnClose.setOnClickListener {
            val status = PRDownloader.getStatus(downloadId)
            if (status == Status.RUNNING)
                toast(getString(R.string.downloading_cancelled))
            PRDownloader.cancel(url)
            dismiss()
        }
        btnDownloadShare.setOnClickListener {
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_VIDEO_CONTENT_SHARE_CLICKED_ON_DOWNLOAD_AND_SHARE, hashMapOf()))
            checkPermissionNDownload()
        }
    }

    private fun fetchLength() {
        val url = URL(url)
        var conn: URLConnection? = null
        compositeDisposable + Single.fromCallable {
            conn = url.openConnection()
            conn?.contentLength
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    if (it != null) {
                        progressView?.isVisible = false
                        mainView?.isVisible = true
                        buttonView?.isVisible = true
                        downloadView?.isVisible = false
                        tvDesc?.text = getString(R.string.size_of_video_is, it / (1024 * 1024))
                    } else {
                        showApiErrorToast(requireContext())
                        dismiss()
                    }
                    conn?.inputStream?.close()
                }, {
                    showApiErrorToast(requireContext())
                    dismiss()
                    conn?.inputStream?.close()
                })
    }

    override fun onStart() {
        super.onStart()
        configureNotificationEducationView()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    private fun checkPermissionNDownload() {
        if (isPermissionGranted()) {
            startVideoDownloading()
        } else
            requestPermission()
    }

    private fun isPermissionGranted() =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED

    private fun requestPermission() {
        requestPermissions(permissions, RC_PERMISSIONS)
    }

    private fun checkDenial() {
        var denyForever = false
        permissions.forEach {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), it)) {
                denyForever = true
                return@forEach
            }
        }
        if (denyForever) {
            onPermissionBocked()
            dismiss()
        } else
            toast(getString(R.string.permission_denied))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_PERMISSIONS) {
            if (isPermissionGranted()) {
                startVideoDownloading()
            } else
                checkDenial()
        }
    }

    private fun startVideoDownloading() {
        val file = File(Environment.getExternalStoragePublicDirectory(AppUtils.DOWNLOADED_VIDEOS_DIR), FileUtils.fileNameFromUrl(url))
        if (file.exists()) {
            shareOnWhatsApp(file)
            return
        }
        progressBar?.max = 100
        val req = PRDownloader.download(url, Environment.getExternalStoragePublicDirectory(AppUtils.DOWNLOADED_VIDEOS_DIR).path, FileUtils.fileNameFromUrl(url))
                .build()
                .setOnStartOrResumeListener {
                    downloadView?.isVisible = true
                    buttonView?.isVisible = false
                }
                .setOnProgressListener {
                    if (it != null) {
                        progressBar?.progress = ((it.currentBytes * 100) / it.totalBytes).toInt()
                        tvDownloadText?.text = getString(R.string.downloaded_mb_out_of_mb, it.currentBytes.toMb(), it.totalBytes.toMb())
                    }
                }
        req.tag = url
        downloadId = req.start(object : OnDownloadListener {
            override fun onDownloadComplete() {
                if (context != null) {
                    shareOnWhatsApp(file)
                    dismiss()
                }
            }

            override fun onError(error: Error?) {
                if (context != null) {
                    showApiErrorToast(requireContext())
                    dismiss()
                }
            }
        })
    }

    private fun shareOnWhatsApp(videoFile: File) {
        Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            `package` = "com.whatsapp"
            putExtra(Intent.EXTRA_TEXT, sharingMessage)
            type = "video/*"
            val uri = FileProvider.getUriForFile(requireContext(), BuildConfig.AUTHORITY, videoFile)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, uri)
        }.also {
            if (AppUtils.isCallable(requireContext(), it)) {
                startActivity(it)
            } else {
                toast(getString(R.string.string_install_whatsApp), Toast.LENGTH_SHORT)
            }
        }
    }

    private fun Long.toMb() = (this / (1024 * 1024))

    companion object {

        val TAG = "DialogShareVideo"

        fun getInstance(url: String, sharingMessage: String, onPermissionBocked: () -> Unit, onShareLink: () -> Unit) =
                DialogShareVideo().apply {
                    this.onShareLink = onShareLink
                    this.url = url
                    this.onPermissionBocked = onPermissionBocked
                    this.sharingMessage = sharingMessage
                }

    }

}