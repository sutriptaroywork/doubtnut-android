package com.doubtnutapp.ui.downloadPdf


import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.DownloadDataList
import com.doubtnutapp.databinding.ActivityDownloadNShareLevelBinding
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.ui.mypdf.SharePDFListener
import com.doubtnutapp.ui.pdfviewer.PdfViewerActivity
import com.doubtnutapp.ui.pdfviewer.PdfViewerViewModel
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import java.io.File
import java.io.FileOutputStream


class DownloadNShareLevelOneActivity :
    BaseBindingActivity<DownloadNShareViewModel, ActivityDownloadNShareLevelBinding>(),
    SharePDFListener,
    DownloadNShareAdapter.UpdateButtonText {

    companion object {
        private const val TAG = "DownloadNShareLevelOneActivity"
    }

    private val adapter: DownloadNShareAdapter by lazy {
        DownloadNShareAdapter(this@DownloadNShareLevelOneActivity, eventTracker, this, this)
    }
    private val downloadPdfPackageName: String by lazy {
        intent.getStringExtra(Constants.FILTER_PACKAGE).orEmpty()
    }
    private var downloadPdfLevelOne: String = ""
    private var downloadPdfLevelTwo: String = ""
    private val eventTracker: Tracker by lazy { getTracker() }
    private var pdfList: List<DownloadDataList>? = null
    private var longPressStatus: Boolean = false
    private var isDownloadMode = false
    private var tempFile: File? = null

    private lateinit var pdfViewerViewModel: PdfViewerViewModel

    override fun provideViewBinding(): ActivityDownloadNShareLevelBinding =
        ActivityDownloadNShareLevelBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DownloadNShareViewModel {
        pdfViewerViewModel = viewModelProvider(viewModelFactory)
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int = R.color.Secondary

    override fun setupView(savedInstanceState: Bundle?) {
        binding.bookTitle.text = downloadPdfPackageName
        fetchBookList()
        binding.rvBookListLevel.layoutManager = LinearLayoutManager(this)
        binding.rvBookListLevel.adapter = adapter

        binding.rvBookListLevel.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                if (!longPressStatus) {
                    if (adapter.downloadDataList[position].downloadPath.isNullOrBlank()) {
                        val intent = Intent(
                            this@DownloadNShareLevelOneActivity,
                            DownloadNShareLevelTwoActivity::class.java
                        )
                        intent.putExtra(Constants.FILTER_PACKAGE, downloadPdfPackageName)
                        intent.putExtra(
                            Constants.FILTER_LEVEL_ONE,
                            adapter.downloadDataList[position].levelOneData
                        )
                        startActivity(intent)
                        sendEventByClick(
                            EventConstants.EVENT_NAME_BOOK_ITEM_NEXT_LEVEL_CLICK,
                            adapter.downloadDataList[position].levelOneData!!
                        )
                    } else {
                        val url = adapter.downloadDataList[position].downloadPath?.replace(" ", "+")
                            ?: ""
                        try {
                            if (!URLUtil.isValidUrl(url)) {
                                toast(getString(R.string.notAvalidLink)) // need localize
                            } else {
                                PdfViewerActivity.previewPdfFromTheUrl(
                                    this@DownloadNShareLevelOneActivity,
                                    url
                                )
                            }
                        } catch (e: ActivityNotFoundException) {
                            toast(getString(R.string.donothaveanybrowser)) // need localize
                        }

                        val packageName =
                            downloadPdfPackageName.replace("[-+.^:,]", "").replace(" ", "_")
                        sendEventByClick(
                            EventConstants.EVENT_NAME_CLICK_FOR_DOWNLOAD_PDF,
                            adapter.downloadDataList[position].downloadPath!!
                        )
                        sendEventByClick(
                            "${EventConstants.EVENT_NAME_CLICK_FOR_DOWNLOAD_PDF}$packageName",
                            adapter.downloadDataList[position].downloadPath!!
                        )
                        sendEventDownloadPdfCleverTap(
                            EventConstants.EVENT_NAME_PDF_DOWNLOAD,
                            packageName,
                            adapter.downloadDataList[position].downloadPath!!
                        )

                    }
                }
            }
        })

        binding.btnCloseLevel1.setOnClickListener {
            if (adapter.getCheckBoxStatus()) {
                longPressStatus = false
                adapter.updateCheckbox(longPressStatus)
                adapter.resetSelectedItems()
            } else {
                this@DownloadNShareLevelOneActivity.onBackPressed()
            }
            sendEvent(EventConstants.EVENT_PRAMA_CLOSE_BUTTON_CLICK)
        }

        binding.cvShareAllPdfLevelOne.setOnClickListener {
            val pdfToSend = adapter.getCheckedPdf()?.size

            when {
                !longPressStatus -> {
                    longPressStatus = true
                    adapter.updateCheckbox(longPressStatus)
                    sendEvent(EventConstants.EVENT_NAME_CLICK_FOR_SELECT_SHARE_ALL_PDF)
                }
                else -> when {
                    adapter.getCheckedPdf()?.isNotEmpty() ?: false -> {
                        shareSelectedPdfsToWhatsApp()
                        val pdfPackageName =
                            downloadPdfPackageName.replace("[-+.^:,]", "").replace(" ", "_")
                        sendEvent(EventConstants.EVENT_NAME_TOTAL_PDF_TO_SHARE_COUNT + pdfToSend)
                        sendEvent(EventConstants.EVENT_NAME_SHARE_ALL_PDF + pdfPackageName)

                    }
                    else -> toast(getString(R.string.string_select_pdf))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun changeButtonText(sharingMessage: String) {
        binding.tvShareAllPdfLevelOne.text = sharingMessage
    }

    override fun onBackPressed() {
        if (adapter.getCheckBoxStatus()) {
            longPressStatus = false
            adapter.updateCheckbox(longPressStatus)
            adapter.resetSelectedItems()
        } else {
            super.onBackPressed()
        }
    }

    private fun shareSelectedPdfsToWhatsApp() {
        sendEvent(EventConstants.EVENT_NAME_CLICK_FOR_SHARE_ALL_PDF)
        Intent(Intent.ACTION_SEND).apply {

            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            type = "text/plain"
            `package` = "com.whatsapp"

            putExtra(
                Intent.EXTRA_TEXT,
                getString(
                    R.string.string_sharing_message,
                    adapter.getCheckedPdf()?.joinToString(separator = "\n\n")
                )
            )

        }.also {

            if (AppUtils.isCallable(this, it)) {
                longPressStatus = false
                adapter.updateCheckbox(longPressStatus)
                adapter.resetSelectedItems()
                startActivity(it)
            } else {
                ToastUtils.makeText(this, R.string.string_install_whatsApp, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun downloadAndShare(url: String) {
        showSharingProgress()
        pdfViewerViewModel.getPdfFilePath(url)
    }

    override fun savePdf(file: Any?) {
        if (!FeaturesManager.isFeatureEnabled(this, Features.PDF_DOWNLOAD))
            return
        if (file != null && file is DownloadDataList) {
            isDownloadMode = true
            binding.progressBar.show()
            if (!file.downloadPath.isNullOrEmpty())
                pdfViewerViewModel.getPdfFilePath(file.downloadPath!!)
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        pdfViewerViewModel.pdfUriLiveData.observe(this, Observer {
            if (it != null) {
                if (isDownloadMode)
                    saveFile(it)
                else
                    sharePdfOnWhatsApp(it)
            } else {
                toast(getString(R.string.somethingWentWrong))
            }
            isDownloadMode = false
            hideSharingProgress()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1211 && data?.data != null) {
            alterFile(data.data!!)
        }
    }

    private fun alterFile(uri: Uri) {
        try {
            val pdfUri = FileProvider.getUriForFile(this, BuildConfig.AUTHORITY, tempFile!!)
            val inputStream = contentResolver.openInputStream(pdfUri)
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { outStream ->
                    inputStream!!.copyTo(outStream)
                    Snackbar.make(
                        binding.rvBookListLevel, "Your PDF has been downloaded.\n" +
                                "You can also find it under My PDFs", Snackbar.LENGTH_LONG
                    )
                        .setAction("View") {
                            openFile(uri)
                        }.show()
                }
            }
            sendEvent(EventConstants.PDF_DOWNLOAD_CLICK)
        } catch (e: Exception) {
            DocumentsContract.deleteDocument(contentResolver, uri)
            Snackbar.make(binding.rvBookListLevel, "Unable to download file", Snackbar.LENGTH_SHORT)
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

    private fun saveFile(pdfFile: File) {
        tempFile = pdfFile
        val name = FileUtils.fileNameFromUrl(pdfFile.absolutePath)
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
            binding.progressBar.hide()
        }
    }

    private fun sharePdfOnWhatsApp(pdfFile: File) {
        val pdfUri = FileProvider.getUriForFile(this, BuildConfig.AUTHORITY, pdfFile)
        Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "application/pdf"
            `package` = "com.whatsapp"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
        }.also {
            if (AppUtils.isCallable(this, it)) {
                startActivity(it)
            } else {
                ToastUtils.makeText(this, R.string.string_install_whatsApp, Toast.LENGTH_SHORT)
                    .show()
            }
        }
        sendEvent(EventConstants.EVENT_NAME_PDF_SHARE)
        val fileName = FileUtils.fileNameFromUrl(pdfFile.absolutePath) - FileUtils.EXT_PDF
        sendEvent("${EventConstants.EVENT_NAME_PDF_SHARE}$fileName")
        sendEventShareCleverTap(EventConstants.EVENT_NAME_PDF_SHARE_CLEVERTAP, fileName)

    }

    private fun fetchBookList() {
        viewModel.getPdfDownloads(downloadPdfPackageName, downloadPdfLevelOne, downloadPdfLevelTwo)
            .observe(this, Observer { response ->
                when (response) {
                    is Outcome.Progress -> {
                        binding.progressBar.show()
                    }
                    is Outcome.Failure -> {
                        binding.progressBar.hide()
                        val dialog = NetworkErrorDialog.newInstance()
                        dialog.show(supportFragmentManager, "NetworkErrorDialog")
                    }
                    is Outcome.ApiError -> {
                        binding.progressBar.hide()
                        apiErrorToast(response.e)

                    }
                    is Outcome.Success -> {

                        pdfList = response.data.data.dataList.map {
                            if (it.downloadPath.isNullOrBlank().not()) {
                                it.downloadPath = response.data.data.cdnPath + it.downloadPath
                            }
                            it
                        }

                        pdfList?.let {
                            adapter.updateData(it, response.data.data.filterType ?: "")
                            if (!it[0].downloadPath.isNullOrBlank()) {
                                binding.cvShareAllPdfLevelOne.visibility = View.VISIBLE
                            }
                        }
                        binding.progressBar.hide()

                    }
                }
            })
    }

    private fun showSharingProgress() {
        toast(getString(R.string.sharing_pdf_in_progress))
        binding.progressBar.show()
    }

    private fun hideSharingProgress() {
        binding.progressBar.hide()
    }

    private fun sendEvent(eventName: String) {
        this@DownloadNShareLevelOneActivity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(
                    NetworkUtils.isConnected(this@DownloadNShareLevelOneActivity).toString()
                )
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_DOWNLOAD_N_SHARE_ACTIVITY)
                .track()
        }
    }

    private fun sendEventByClick(eventName: String, bookName: String) {
        this@DownloadNShareLevelOneActivity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(
                    NetworkUtils.isConnected(this@DownloadNShareLevelOneActivity).toString()
                )
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_DOWNLOAD_N_SHARE_ACTIVITY)
                .addEventParameter(EventConstants.EVENT_PRAMA_CLICKED_ITEM_NAME, bookName)
                .track()
        }
    }

    private fun sendEventDownloadPdfCleverTap(eventName: String, pdfPackage: String, path: String) {
        this@DownloadNShareLevelOneActivity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(
                    NetworkUtils.isConnected(this@DownloadNShareLevelOneActivity!!).toString()
                )
                .addStudentId(getStudentId())
                .addEventParameter(EventConstants.EVENT_NAME_PATH, path)
                .addEventParameter(EventConstants.EVENT_NAME_PACKAGE, pdfPackage)
                .cleverTapTrack()

        }
    }

    private fun sendEventShareCleverTap(eventName: String, path: String) {
        this@DownloadNShareLevelOneActivity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(
                    NetworkUtils.isConnected(this@DownloadNShareLevelOneActivity!!).toString()
                )
                .addStudentId(getStudentId())
                .addEventParameter(EventConstants.EVENT_NAME_PATH, path)
                .cleverTapTrack()

        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = this@DownloadNShareLevelOneActivity.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }
}
