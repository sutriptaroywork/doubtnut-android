package com.doubtnutapp.ui.pdfviewer

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnNotesClosed
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.BookmarkData
import com.doubtnutapp.databinding.FragmentPdfViewerBinding
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
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
import javax.inject.Inject

class PdfViewerFragment : BaseBindingFragment<PdfViewerViewModel, FragmentPdfViewerBinding>() {

    companion object {
        private const val TAG = "PdfViewerFragment"
        private const val INTENT_EXTRA_PDF_FILE_PATH = "pdf_file_path"
        private const val INTENT_EXTRA_SHOW_DOWNLOAD_BUTTON = "show_download_button"
        private const val INTENT_EXTRA_QUESTION_ID = "question_id"
        private const val INTENT_EXTRA_SOURCE = "source"
        private const val INTENT_ASSORTMENT_ID = "assortment_id"
        private const val INTENT_RESOURCE_ID = "resource_id"
        private const val INTENT_ICON_URL = "icon_url"

        fun previewPdfFromTheUrl(
            url: String, questionId: String? = null,
            showDownloadButton: Boolean = false, source: String? = null,
            assortmentId: String? = null,
            resourceId: String? = null,
            iconUrl: String? = null
        ): PdfViewerFragment {
            return PdfViewerFragment().apply {
                arguments = Bundle().apply {
                    putString(Constants.INTENT_EXTRA_PDF_URL, url)
                    putString(INTENT_EXTRA_QUESTION_ID, questionId)
                    putBoolean(INTENT_EXTRA_SHOW_DOWNLOAD_BUTTON, showDownloadButton)
                    putString(INTENT_EXTRA_SOURCE, source)
                    putString(INTENT_ASSORTMENT_ID, assortmentId)
                    putString(INTENT_RESOURCE_ID, resourceId)
                    putString(INTENT_ICON_URL, iconUrl)
                }
            }
        }
    }

    @Inject
    lateinit var analyticsPublisher: Lazy<AnalyticsPublisher>

    private var isPdfLoaded = false
    private var filePath: String = ""

    // variable used to reload page in case onPageStarted() is not called and page shows blank.
    private var isPageLoadingStarted = false

    // variable used to reload page while returning to this page after launching deeplink.
    private var shouldReloadPageWhenComesBack = false

    private val mQuestionId: String? by lazy {
        arguments?.getString(INTENT_EXTRA_QUESTION_ID)
    }
    private val mSource: String? by lazy {
        arguments?.getString(INTENT_EXTRA_SOURCE)
    }
    private val bookmarkIconUrl: String? by lazy {
        arguments?.getString(INTENT_ICON_URL)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPdfViewerBinding = FragmentPdfViewerBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): PdfViewerViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        FirebaseCrashlytics.getInstance().log("init()")
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

    @SuppressLint("SetJavaScriptEnabled")
    private fun init() {
        mBinding?.apply {
            bottomView.hide()
            tvToolbarTitle.text = getFileName()
            ivBookmark.loadImageEtx(bookmarkIconUrl ?: "")
            setUpWebView()
        }
        setListeners()
        loadPdfFromUrl()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView() {
        mBinding?.pdfViewer?.apply {
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

    override fun onResume() {
        super.onResume()
        // When comes back after launching deeplink, reload webview to original url
        // so that user need not to press back to reach the original page
        if (shouldReloadPageWhenComesBack) {
            showLoadingState()
            isPageLoadingStarted = false
            mBinding?.pdfViewer?.loadUrl(finalPdfUrl)
            shouldReloadPageWhenComesBack = false
        }
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
                if (request?.url != null && request.url?.host.equals(Constants.BRANCH_HOST)) {

                    // set true to reload page when returns to this page after launching deeplink.
                    shouldReloadPageWhenComesBack = true

                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(request.url.toString())
                    startActivity(intent)
                    return true
                }
            } catch (e: ActivityNotFoundException) {
                Log.e(e, TAG)
            }
            return super.shouldOverrideUrlLoading(view, request)
        }
    }

    private fun setListeners() {
        mBinding?.buttonSharePdf?.setOnClickListener {
            FirebaseCrashlytics.getInstance().log("sharePdf()")
            sharePdf()
        }
        mBinding?.buttonPdfDownload?.setOnClickListener {
            FirebaseCrashlytics.getInstance().log("saveFile()")
            saveFile()
        }
        binding.ivClose.setOnClickListener {
            (activity as? ActionPerformer)?.performAction(OnNotesClosed())
        }
        mBinding?.ivClose?.setOnClickListener {
            (activity as? ActionPerformer)?.performAction(OnNotesClosed())
        }
        mBinding?.ivBookmark?.setOnClickListener {
            viewModel.bookmark(
                arguments?.getString(INTENT_RESOURCE_ID), arguments?.getString(
                    INTENT_ASSORTMENT_ID
                )
            )
        }
    }

    private fun loadPdfFromUrl() {
        if (shouldShowPreviewFromUrl()) {
            getUrl()?.let {
                // Store pdf in local file system, it will help in sharing pdfs
                viewModel.getPdfFilePath(it)
                mBinding?.pdfViewer?.apply {
                    loadUrl(finalPdfUrl)
                }
            } ?: onUrlEmpty()
        } else {
            onIntentExtraEmpty()
        }
        sendEventCleverTap(EventConstants.EVENT_NAME_PDF_OPEN)
    }

    private fun onIntentExtraEmpty() {
        ToastUtils.makeText(requireContext(), R.string.somethingWentWrong, Toast.LENGTH_SHORT)
            .show()
        activity?.finish()
    }

    private fun getPdfFile(): File? {
        return if (arguments?.getString(INTENT_EXTRA_PDF_FILE_PATH).isNotNullAndNotEmpty()) {
            File(arguments?.getString(INTENT_EXTRA_PDF_FILE_PATH) ?: return null)
        } else {
            null
        }
    }

    private fun shouldShowPreviewFromUrl() =
        arguments?.getString(Constants.INTENT_EXTRA_PDF_URL).isNotNullAndNotEmpty()

    private fun showLoadingState() {
        mBinding?.progressBar?.show()
        mBinding?.textViewLoadingState?.show()
    }

    private fun hideLoadingState() {
        mBinding?.progressBar?.hide()
        mBinding?.textViewLoadingState?.hide()
    }

    private fun onUrlEmpty() {
        ToastUtils.makeText(requireContext(), R.string.pdf_url_not_present, Toast.LENGTH_SHORT)
            .show()
        activity?.finish()
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.pdfUriLiveData.observe(this) { file ->
            mBinding?.buttonSharePdf?.show()
            mBinding?.buttonPdfDownload?.isVisible =
                FeaturesManager.isFeatureEnabled(requireContext(), Features.PDF_DOWNLOAD) ||
                        arguments?.getBoolean(INTENT_EXTRA_SHOW_DOWNLOAD_BUTTON, false) == true
            isPdfLoaded = true
            filePath = file.absolutePath
        }

        viewModel.pdfFromUriLiveData.observe(viewLifecycleOwner) {
            if (it.first) {
                sendEvent(EventConstants.VIEW_PDF_FROM_DOWNLOAD_URL)
                sendEventWithParams(EventConstants.VIEW_PDF_FROM_DOWNLOAD_URL, it.second)

            } else {
                sendEvent(EventConstants.VIEW_PDF_FROM_FILE_PATH)
                sendEventWithParams(EventConstants.VIEW_PDF_FROM_FILE_PATH, it.second)

            }
        }

        viewModel.bookmarkLiveData.observeK(
            this,
            ::onBookmarkDataSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )
    }

    private fun onBookmarkDataSuccess(data: BookmarkData) {
        toast(data.message ?: "")
        mBinding?.ivBookmark?.loadImageEtx(data.iconUrl.orEmpty())
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(childFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(requireContext())) {
            toast(getString(R.string.somethingWentWrong))
        } else {
            toast(getString(R.string.string_noInternetConnection))
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        mBinding?.progressBar?.setVisibleState(state)
    }

    private fun getUrl() = arguments?.getString(Constants.INTENT_EXTRA_PDF_URL)

    private val finalPdfUrl: String by lazy {
        Constants.PDF_VIEW_GVIEW_URL + getUrl()
    }

    private fun getFileName(): String = when {
        arguments?.getString(Constants.INTENT_EXTRA_PDF_URL)
            .isNotNullAndNotEmpty() -> getUrl()?.let {
            FileUtils.fileNameFromUrl(
                it
            )
        }
            ?: getString(R.string.myPdf)
        arguments?.getString(INTENT_EXTRA_PDF_FILE_PATH)
            .isNotNullAndNotEmpty() -> getPdfFile()?.name
            ?: getString(R.string.myPdf)
        else -> getString(R.string.myPdf)
    } - FileUtils.EXT_PDF

    private fun sharePdf() {
        if (isPdfLoaded) {
            if (FileUtils.isFilePresent(filePath)) {
                val pdfUri = FileProvider.getUriForFile(
                    requireContext(),
                    BuildConfig.AUTHORITY,
                    File(filePath)
                )
                Intent(Intent.ACTION_SEND).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    type = "application/pdf"
                    `package` = "com.whatsapp"
                    putExtra(Intent.EXTRA_STREAM, pdfUri)
                }.also {
                    if (AppUtils.isCallable(requireContext(), it)) {
                        startActivity(it)
                    } else {
                        ToastUtils.makeText(
                            requireContext(),
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
        DoubtnutApp.INSTANCE.getEventTracker()
            .addEventNames(eventName)
            .addStudentId(getStudentId())
            .track()
    }

    private fun sendEventWithParams(eventName: String, params: String) {
        DoubtnutApp.INSTANCE.getEventTracker()
            .addEventNames(eventName)
            .addStudentId(getStudentId())
            .addEventParameter(EventConstants.PDF_NAME_AS_PARAMS, params)
            .track()
    }

    override fun onStart() {
        super.onStart()
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
                    EventConstants.SOURCE_ID to getFileName()
                ), ignoreSnowplow = true
            )
        )
    }

    override fun onDestroy() {
        destroyWebView()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1211 && data?.data != null) {
            alterFile(data.data!!)
        }
    }

    private fun alterFile(uri: Uri) {
        try {
            val pdfUri =
                FileProvider.getUriForFile(requireContext(), BuildConfig.AUTHORITY, File(filePath))
            val inputStream = activity?.contentResolver?.openInputStream(pdfUri)
            activity?.contentResolver?.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { outStream ->
                    mBinding?.apply {
                        inputStream!!.copyTo(outStream)
                        Snackbar.make(
                            bottomView, "Your PDF has been downloaded.\n" +
                                    "You can also find it under My PDFs", Snackbar.LENGTH_LONG
                        ).setAction("View") {
                            openFile(uri)
                        }.show()
                    }
                }
            }
            sendEvent(EventConstants.PDF_DOWNLOAD_CLICK)
            viewModel.sendEvent(
                EventConstants.PDF_DOWNLOADED_BY_USER, hashMapOf(
                    Constants.QUESTION_ID to mQuestionId.orEmpty()
                ), ignoreSnowplow = true
            )
        } catch (e: Exception) {
            mBinding?.apply {
                DocumentsContract.deleteDocument(activity?.contentResolver!!, uri)
                Snackbar.make(bottomView, "Unable to download file", Snackbar.LENGTH_SHORT)
                    .show()
            }
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
            showApiErrorToast(requireContext())
        }
    }

    private fun destroyWebView() {
        mBinding?.webViewContainer?.removeAllViews()
        mBinding?.pdfViewer?.apply {
            clearHistory()
            clearCache(true)
            removeAllViews()
            destroy()
        }
    }

    private fun sendEventCleverTap(@Suppress("SameParameterValue") eventName: String) {
        (DoubtnutApp.INSTANCE).getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(requireContext()).toString())
            .addStudentId(getStudentId())
            .addEventParameter(EventConstants.PARAM_IS_EXPANDED, true)
            .cleverTapTrack()
    }

    private fun sendEventShareCleverTap(
        @Suppress("SameParameterValue") eventName: String,
        path: String
    ) {
        (DoubtnutApp.INSTANCE).getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(requireContext()).toString())
            .addStudentId(getStudentId())
            .addEventParameter(EventConstants.EVENT_NAME_PATH, path)
            .cleverTapTrack()
    }
}
