package com.doubtnutapp.ui.pdfviewer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.DocumentsContract
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ActivityPdfViewerBinding
import com.doubtnutapp.ui.ToolbarActivity
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.FileUtils
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.showApiErrorToast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Lazy
import io.branch.referral.Defines
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class PdfViewerActivity : ToolbarActivity<PdfViewerViewModel, ActivityPdfViewerBinding>() {

    @Inject
    lateinit var analyticsPublisher: Lazy<AnalyticsPublisher>

    private var isPdfLoaded = false
    private var filePath: String = ""
    private var totalEngagementTime: Int = 0
    private var engagementTimeToSend: Number = 0

    private var engagementTimerTask: TimerTask? = null
    private var engageTimer: Timer? = null

    private var engagementHandler: Handler? = null

    private var timeFormatter = SimpleDateFormat("m:ss", Locale.getDefault())

    // variable used to reload page in case onPageStarted() is not called and page shows blank.
    private var isPageLoadingStarted = false

    // variable used to reload page while returning to this page after launching deeplink.
    private var shouldReloadPageWhenComesBack = false

    private val mQuestionId: String? by lazy {
        intent?.getStringExtra(INTENT_EXTRA_QUESTION_ID)
    }

    private val mSource: String? by lazy {
        intent?.getStringExtra(INTENT_EXTRA_SOURCE)
    }

    private fun shouldShowPreviewFromUrl() = intent.hasExtra(Constants.INTENT_EXTRA_PDF_URL)

    private fun getUrl() = intent?.getStringExtra(Constants.INTENT_EXTRA_PDF_URL)

    private val finalPdfUrl: String by lazy {
        Constants.PDF_VIEW_GVIEW_URL + getUrl()
    }

    companion object {
        private const val TAG = "PdfViewerActivity"
        private const val INTENT_EXTRA_SHOW_DOWNLOAD_BUTTON = "show_download_button"
        private const val INTENT_EXTRA_QUESTION_ID = "question_id"
        private const val INTENT_EXTRA_SOURCE = "source"

        fun previewPdfFromTheUrl(
            context: Context, url: String, questionId: String? = null,
            showDownloadButton: Boolean = false, source: String? = null
        ) {
            val intent = Intent(context, PdfViewerActivity::class.java).apply {
                putExtra(Constants.INTENT_EXTRA_PDF_URL, url)
                putExtra(INTENT_EXTRA_QUESTION_ID, questionId)
                putExtra(INTENT_EXTRA_SHOW_DOWNLOAD_BUTTON, showDownloadButton)
                putExtra(INTENT_EXTRA_SOURCE, source)
            }
            context.startActivity(intent)
        }
    }

    override fun provideViewBinding(): ActivityPdfViewerBinding {
        return ActivityPdfViewerBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String = TAG

    override fun provideViewModel(): PdfViewerViewModel = viewModelProvider(viewModelFactory)

    override fun getStatusBarColor(): Int = R.color.redTomato

    override fun setupView(savedInstanceState: Bundle?) {
        init()
        viewModel.publishPdfItemClickEvent()
    }

    override fun startActivity(intent: Intent?) {
        val isBranchLink = intent?.data?.host.equals(Constants.BRANCH_HOST)
        if (isBranchLink) {
            intent?.putExtra(Defines.IntentKeys.ForceNewBranchSession.key, true)
            intent?.action = Constants.IN_DEEP_LINK
            intent?.putExtra(Constants.SOURCE, mSource)
        }
        super.startActivity(intent)
    }

    private fun init() {
        FirebaseCrashlytics.getInstance().log("init()")
        binding.apply {
            tvToolbarTitle.text = getFileName()
            setUpWebView()
        }
        setListeners()
        loadPdfFromUrl()
    }

    override fun onResume() {
        super.onResume()
        // When comes back after launching deeplink, reload webview to original url
        // so that user need not to press back to reach the original page
        if (shouldReloadPageWhenComesBack) {
            if (binding.pdfViewer.canGoBack()) {
                binding.pdfViewer.goBack()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView() {
        binding.pdfViewer.apply {
            webViewClient = callback
            settings.setSupportZoom(true)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            clearCache(true)
        }
    }

    private fun moveToPreviousPage() {
        binding.pdfViewer.goBack()
    }

    private val callback = object : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            isPageLoadingStarted = true
            showLoadingState()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            if (isPageLoadingStarted.not() && url.isNotNullAndNotEmpty()) {
                view?.loadUrl(url!!)
            } else {
                hideLoadingState()
            }
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            try {
                if (request?.url != null) {
                    if (request.url.toString().contains(Constants.BRANCH_HOST)
                        && request.url.getQueryParameter("q").isNotNullAndNotEmpty()
                    ) {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(request.url.getQueryParameter("q"))
                        startActivity(intent)
                        return true
                    } else if (request.url?.host.equals(Constants.BRANCH_HOST)) {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(request.url.toString())
                        startActivity(intent)
                        return true
                    }
                }
            } catch (e: Exception) {
                Log.e(e, TAG)
            }
            shouldReloadPageWhenComesBack = true
            return super.shouldOverrideUrlLoading(view, request)
        }
    }

    private fun setListeners() {
        binding.buttonSharePdf.setOnClickListener {
            FirebaseCrashlytics.getInstance().log("sharePdf()")
            sharePdf()
        }
        binding.buttonPdfDownload.setOnClickListener {
            FirebaseCrashlytics.getInstance().log("saveFile()")
            saveFile()
        }
        binding.ivBack.setOnClickListener { onBackPressed() }
    }

    private fun loadPdfFromUrl() {
        if (shouldShowPreviewFromUrl()) {
            getUrl()?.let {
                // Store pdf in local file system, it will help in sharing pdfs
                viewModel.getPdfFilePath(it)
                binding.pdfViewer.apply {
                    loadUrl(finalPdfUrl)
                }
            } ?: onUrlEmpty()
        } else {
            onIntentExtraEmpty()
        }
        sendEventCleverTap(EventConstants.EVENT_NAME_PDF_OPEN)
    }

    private fun onIntentExtraEmpty() {
        showApiErrorToast(this@PdfViewerActivity)
        finish()
    }

    private fun showLoadingState() {
        binding.progressBar.show()
        binding.textViewLoadingState.show()
    }

    private fun hideLoadingState() {
        binding.progressBar.hide()
        binding.textViewLoadingState.hide()
    }

    private fun onUrlEmpty() {
        ToastUtils.makeText(this, R.string.pdf_url_not_present, Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.pdfUriLiveData.observe(this) { file ->
            binding.buttonSharePdf.show()
            binding.buttonPdfDownload.isVisible =
                FeaturesManager.isFeatureEnabled(this@PdfViewerActivity, Features.PDF_DOWNLOAD) ||
                        intent?.getBooleanExtra(INTENT_EXTRA_SHOW_DOWNLOAD_BUTTON, false) == true
            isPdfLoaded = true
            filePath = file.absolutePath
        }

        viewModel.pdfFromUriLiveData.observe(this) {
            if (it.first) {
                sendEvent(EventConstants.VIEW_PDF_FROM_DOWNLOAD_URL)
                sendEventWithParams(EventConstants.VIEW_PDF_FROM_DOWNLOAD_URL, it.second)
            } else {
                sendEvent(EventConstants.VIEW_PDF_FROM_FILE_PATH)
                sendEventWithParams(EventConstants.VIEW_PDF_FROM_FILE_PATH, it.second)
            }
        }
    }

    private fun getFileName(): String =
        when {
            intent.hasExtra(Constants.INTENT_EXTRA_PDF_URL) ->
                getUrl()?.let {
                    FileUtils.fileNameFromUrl(it)
                } ?: getString(R.string.myPdf)

            else -> getString(R.string.myPdf)

        } - FileUtils.EXT_PDF

    private fun sharePdf() {
        if (isPdfLoaded) {
            if (FileUtils.isFilePresent(filePath)) {
                val pdfUri = FileProvider.getUriForFile(this, BuildConfig.AUTHORITY, File(filePath))
                Intent(Intent.ACTION_SEND).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    type = "application/pdf"
                    `package` = "com.whatsapp"
                    putExtra(Intent.EXTRA_STREAM, pdfUri)
                }.also {
                    if (AppUtils.isCallable(this, it)) {
                        startActivity(it)
                    } else {
                        ToastUtils.makeText(
                            this,
                            R.string.string_install_whatsApp,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                toast(getString(R.string.pdf_file_not_present))
            }
        } else {
            toast(getString(R.string.pdf_loading))
        }
        sendEvent(EventConstants.EVENT_NAME_PDF_SHARE)
        sendEvent("${EventConstants.EVENT_NAME_PDF_SHARE}${getFileName()}")
        sendEventShareCleverTap(EventConstants.EVENT_NAME_PDF_SHARE_CLEVERTAP, getFileName())
    }

    private fun sendEvent(eventName: String) {
        (applicationContext as DoubtnutApp).getEventTracker()
            .addEventNames(eventName)
            .addStudentId(getStudentId())
            .track()
    }

    private fun sendEventWithParams(eventName: String, params: String) {
        (applicationContext as DoubtnutApp).getEventTracker()
            .addEventNames(eventName)
            .addStudentId(getStudentId())
            .addEventParameter(EventConstants.PDF_NAME_AS_PARAMS, params)
            .track()
    }

    private fun startEngagementTimer() {
        if (engageTimer == null) {
            engageTimer = Timer()
            timeFormatter.timeZone = TimeZone.getTimeZone("UTC")
        }

        engagementTimerTask = object : TimerTask() {
            override fun run() {
                engagementHandler?.post {
                    if (isAppInForeground) {
                        engagementTimeToSend = totalEngagementTime
                        totalEngagementTime++
                    }
                }
            }
        }

        totalEngagementTime = 0
        engageTimer!!.schedule(engagementTimerTask, 0, 1000)
    }

    private fun sendEventEngagement(
        @Suppress("SameParameterValue") eventName: String,
        engagementTime: Number
    ) {
        (applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(this).toString())
            .addStudentId(getStudentId())
            .addEventParameter(
                EventConstants.PDF_VIEWER_ENGAGEMENT_TOTAL_TIME_AS_PARAMS,
                engagementTime
            )
            .track()
    }

    override fun onStart() {
        super.onStart()
        startEngagementTimer()
        analyticsPublisher.get().publishEvent(
            AnalyticsEvent(
                EventConstants.PDF_ACTIVITY_OPEN,
                hashMapOf(EventConstants.SOURCE_ID to getFileName()),
                ignoreSnowplow = true
            )
        )
    }

    override fun onStop() {
        super.onStop()
        analyticsPublisher.get().publishEvent(
            AnalyticsEvent(
                EventConstants.PDF_ACTIVITY_CLOSE,
                hashMapOf(
                    EventConstants.SOURCE_ID to getFileName(),
                    EventConstants.ENGAGEMENT_TIME to engagementTimeToSend.toString()
                ), ignoreSnowplow = true
            )
        )
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        engagementTimerTask?.let { engagementHandler?.removeCallbacks(it) }
        sendEventEngagement(EventConstants.PDF_VIEWER_ENGAGEMENT_TOTAL_TIME, engagementTimeToSend)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1211 && data?.data != null) {
            alterFile(data.data!!)
        }
    }

    private fun alterFile(uri: Uri) {
        try {
            val pdfUri = FileProvider.getUriForFile(this, BuildConfig.AUTHORITY, File(filePath))
            val inputStream = contentResolver.openInputStream(pdfUri)
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { outStream ->
                    inputStream!!.copyTo(outStream)
                    Snackbar.make(
                        binding.bottomView, "Your PDF has been downloaded.\n" +
                                "You can also find it under My PDFs", Snackbar.LENGTH_LONG
                    )
                        .setAction("View") {
                            openFile(uri)
                        }.show()
                }
            }
            sendEvent(EventConstants.PDF_DOWNLOAD_CLICK)
            viewModel.sendEvent(
                EventConstants.PDF_DOWNLOADED_BY_USER, hashMapOf(
                    Constants.QUESTION_ID to mQuestionId.orEmpty()
                ), ignoreSnowplow = true
            )
        } catch (e: Exception) {
            DocumentsContract.deleteDocument(contentResolver, uri)
            Snackbar.make(binding.bottomView, "Unable to download file", Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    private fun openFile(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(Intent.createChooser(intent, "Open PDF"))
        } catch (e: Exception) {
            Log.e(e)
            toast("No pdf reader found")
        }
    }

    private fun saveFile() {
        val name = getFileName() + ".pdf"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, name)
        }
        try {
            startActivityForResult(intent, 1211)
        } catch (e: Exception) {
            Log.e(e)
            showApiErrorToast(this)
        }
    }

    private fun sendEventCleverTap(@Suppress("SameParameterValue") eventName: String) {
        (applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(this).toString())
            .addStudentId(getStudentId())
            .addEventParameter(EventConstants.PARAM_IS_EXPANDED, true)
            .cleverTapTrack()
    }

    private fun sendEventShareCleverTap(
        @Suppress("SameParameterValue") eventName: String,
        path: String
    ) {
        (applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(this).toString())
            .addStudentId(getStudentId())
            .addEventParameter(EventConstants.EVENT_NAME_PATH, path)
            .cleverTapTrack()
    }

    private fun destroyWebView() {
        binding.webViewContainer.removeAllViews()
        binding.pdfViewer.apply {
            clearHistory()
            clearCache(true)
            removeAllViews()
            destroy()
        }
    }

    override fun onDestroy() {
        destroyWebView()
        engageTimer?.cancel()
        engageTimer = null
        engagementTimerTask?.cancel()
        engagementTimerTask = null
        super.onDestroy()
    }
}
