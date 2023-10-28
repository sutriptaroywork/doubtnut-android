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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.DownloadDataList
import com.doubtnutapp.databinding.ActivityDownloadNShareLevelTwoBinding
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.ui.mypdf.SharePDFListener
import com.doubtnutapp.ui.pdfviewer.PdfViewerActivity
import com.doubtnutapp.ui.pdfviewer.PdfViewerViewModel
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


class DownloadNShareLevelTwoActivity : BaseActivity(), SharePDFListener, DownloadNShareAdapter.UpdateButtonText, HasAndroidInjector {

    private lateinit var viewModel: DownloadNShareViewModel
    private lateinit var pdfViewerViewModel: PdfViewerViewModel
    private lateinit var adapter: DownloadNShareAdapter
    private var downloadPdfPackageName: String = ""
    private var downloadPdfLevelOne: String = ""
    private var downloadPdfLevelTwo: String = ""
    private var downloadPdfBaseUrl: String = ""
    private lateinit var eventTracker: Tracker
    private var longPressStatus: Boolean = false
    private var isDownloadMode = false
    private var tempFile: File? = null

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding : ActivityDownloadNShareLevelTwoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.Secondary)
        binding = ActivityDownloadNShareLevelTwoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        eventTracker = getTracker()
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(DownloadNShareViewModel::class.java)
        pdfViewerViewModel = ViewModelProviders.of(this, viewModelFactory).get(PdfViewerViewModel::class.java)
        downloadPdfPackageName = intent!!.getStringExtra(Constants.FILTER_PACKAGE).orEmpty()
        downloadPdfLevelOne = intent!!.getStringExtra(Constants.FILTER_LEVEL_ONE).orEmpty()

        binding.bookTitle.text = downloadPdfLevelOne
        fetchBookList()
        adapter = DownloadNShareAdapter(this, eventTracker, this, this)
        binding.rvBookListLevelTwo.layoutManager = LinearLayoutManager(this)
        binding.rvBookListLevelTwo.adapter = adapter

        binding.rvBookListLevelTwo.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                if (!longPressStatus) {
                    viewModel.publishPDFLevelSelectionNameEvent(adapter.downloadDataList.getOrNull(position)?.levelTwoData
                            ?: "")
                    val url = adapter.downloadDataList[position].downloadPath?.replace(" ", "+")
                            ?: ""
                    try {
                        if (!URLUtil.isValidUrl(url)) {
                            toast(getString(R.string.notAvalidLink)) // need localize
                        } else {
                            PdfViewerActivity.previewPdfFromTheUrl(this@DownloadNShareLevelTwoActivity, url)
                        }
                    } catch (e: ActivityNotFoundException) {
                        toast(getString(R.string.donothaveanybrowser)) // need localize
                    }
                    val packageName = downloadPdfPackageName.replace("[-+.^:,]", "").replace(" ", "_")
                    sendEventByClick(EventConstants.EVENT_NAME_CLICK_FOR_DOWNLOAD_PDF, adapter.downloadDataList!![position].downloadPath!!)
                    sendEventByClick("${EventConstants.EVENT_NAME_CLICK_FOR_DOWNLOAD_PDF}$packageName", adapter.downloadDataList[position].downloadPath!!)
                }
            }
        })

        binding.btnCloseLevelTwo.setOnClickListener {
            if (adapter.getCheckBoxStatus()) {
                longPressStatus = false
                adapter.updateCheckbox(longPressStatus)
                adapter.resetSelectedItems()
            } else {
                this@DownloadNShareLevelTwoActivity.onBackPressed()
            }
            sendEvent(EventConstants.EVENT_PRAMA_CLOSE_BUTTON_CLICK)

        }

        binding.cvShareAllPdfLevelTwo.setOnClickListener {
            val pdfToSend = adapter.getCheckedPdf()?.size

            when {
                !longPressStatus -> {
                    longPressStatus = true
                    adapter.updateCheckbox(longPressStatus)
                }
                else -> when {
                    adapter.getCheckedPdf()?.isNotEmpty() ?: false -> {
                        shareSelectedPdfsToWhatsApp()
                        val pdfPackageName = downloadPdfPackageName.replace("[-+.^:,]", "").replace(" ", "_")
                        sendEvent(EventConstants.EVENT_NAME_TOTAL_PDF_TO_SHARE_COUNT + pdfToSend)
                        sendEvent(EventConstants.EVENT_NAME_SHARE_ALL_PDF + pdfPackageName)
                    }
                    else -> toast(getString(R.string.string_select_pdf))
                }
            }
        }

        startObservingPDFFilePath()
    }

    override fun changeButtonText(sharingMessage: String) {
        binding.tvShareAllPdfLevelTwo.text = sharingMessage
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


        Intent(Intent.ACTION_SEND).apply {

            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            type = "text/plain"
            `package` = "com.whatsapp"

            putExtra(Intent.EXTRA_TEXT, getString(R.string.string_sharing_message, adapter.getCheckedPdf()?.joinToString(separator = "\n\n")))

        }.also {

            if (AppUtils.isCallable(this, it)) {
                startActivity(it)
                longPressStatus = false
                adapter.updateCheckbox(longPressStatus)
                adapter.resetSelectedItems()
            } else {
                ToastUtils.makeText(this, R.string.string_install_whatsApp, Toast.LENGTH_SHORT).show()
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
            binding.progressBar.hide()
            if (!file.downloadPath.isNullOrEmpty())
                pdfViewerViewModel.getPdfFilePath(file.downloadPath!!)
        }
    }

    private fun startObservingPDFFilePath() {
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
                    Snackbar.make(binding.rvBookListLevelTwo, "Your PDF has been downloaded.\n" +
                            "You can also find it under My PDFs", Snackbar.LENGTH_LONG)
                            .setAction("View") {
                                openFile(uri)
                            }.show()
                }
            }
            sendEvent(EventConstants.PDF_DOWNLOAD_CLICK)
        } catch (e: Exception) {
            DocumentsContract.deleteDocument(contentResolver, uri)
            Snackbar.make(binding.rvBookListLevelTwo, "Unable to download file", Snackbar.LENGTH_SHORT).show()
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
        } catch (e: java.lang.Exception) {
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
        } catch (e: java.lang.Exception) {
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
                ToastUtils.makeText(this, R.string.string_install_whatsApp, Toast.LENGTH_SHORT).show()
            }
        }
        sendEvent(EventConstants.EVENT_NAME_PDF_SHARE)
        val fileName = FileUtils.fileNameFromUrl(pdfFile.absolutePath) - FileUtils.EXT_PDF
        sendEvent("${EventConstants.EVENT_NAME_PDF_SHARE}$fileName")

        sendEventShareCleverTap(EventConstants.EVENT_NAME_PDF_SHARE_CLEVERTAP, fileName)
    }

    private fun fetchBookList() {
        viewModel.getPdfDownloads(downloadPdfPackageName, downloadPdfLevelOne, downloadPdfLevelTwo).observe(this, Observer { response ->
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

                    val data = response.data.data
                    val pdfList = data.dataList?.map {
                        if (it.downloadPath.isNullOrBlank().not()) {
                            it.downloadPath = data.cdnPath + it.downloadPath
                        }
                        it
                    }

                    pdfList?.let {
                        adapter.updateData(it, data.filterType)
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

    private fun sendEventByClick(eventName: String, bookName: String) {
        this@DownloadNShareLevelTwoActivity?.apply {
            eventTracker.addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(this@DownloadNShareLevelTwoActivity).toString())
                    .addStudentId(getStudentId())
                    .addScreenName(EventConstants.PAGE_DOWNLOAD_N_SHARE_ACTIVITY)
                    .addEventParameter(EventConstants.EVENT_PRAMA_CLICKED_ITEM_NAME, bookName)
                    .track()
        }
    }

    private fun sendEvent(eventName: String) {
        this@DownloadNShareLevelTwoActivity?.apply {
            eventTracker.addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(this@DownloadNShareLevelTwoActivity).toString())
                    .addStudentId(getStudentId())
                    .addScreenName(EventConstants.PAGE_DOWNLOAD_N_SHARE_ACTIVITY)
                    .track()
        }
    }

    private fun sendEventDownloadPdfCleverTap(eventName: String, pdfPackage: String, path: String) {
        this@DownloadNShareLevelTwoActivity?.apply {
            eventTracker.addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(this@DownloadNShareLevelTwoActivity!!).toString())
                    .addStudentId(getStudentId())
                    .addEventParameter(EventConstants.EVENT_NAME_PATH, path)
                    .addEventParameter(EventConstants.EVENT_NAME_PACKAGE, pdfPackage)
                    .track()

        }
    }

    private fun sendEventShareCleverTap(eventName: String, path: String) {
        this@DownloadNShareLevelTwoActivity?.apply {
            eventTracker.addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(this@DownloadNShareLevelTwoActivity!!).toString())
                    .addStudentId(getStudentId())
                    .addEventParameter(EventConstants.EVENT_NAME_PATH, path)
                    .cleverTapTrack()

        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = this@DownloadNShareLevelTwoActivity.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }


}
