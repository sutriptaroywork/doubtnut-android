package com.doubtnutapp.ui.mypdf

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityMyPdfBinding
import com.doubtnutapp.ui.ToolbarActivity
import com.doubtnutapp.ui.downloadPdf.DownloadNShareActivity
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.FileUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.showApiErrorToast
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream

class MyPdfActivity : ToolbarActivity<MyPdfViewModel, ActivityMyPdfBinding>(), SharePDFListener {

    private lateinit var eventTracker: Tracker
    private var tempFilePath = ""

    companion object {

        private const val TAG = "MyPdfActivity"

        @Suppress("unused")
        fun showMyPDFs(context: Context) {
            val intent = Intent(context, MyPdfActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun provideViewBinding(): ActivityMyPdfBinding {
        return ActivityMyPdfBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): MyPdfViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return R.color.white_50
    }

    override fun setupView(savedInstanceState: Bundle?) {
        init()
        setListener()
    }

    private fun init() {
        eventTracker = (application as DoubtnutApp).getEventTracker()
        viewModel.getMyPdfList(applicationContext)
        viewModel.pdfListLiveData.observe(this) {
            when (it) {
                is Outcome.Success -> {
                    onMyPdfFilesLoad(it.data)
                }

                is Outcome.Failure -> {
                    //log error msg `it.e.message`
                    ToastUtils.makeText(this, R.string.somethingWentWrong, Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
                else -> {
                }
            }
        }
    }

    private fun setListener() {
        binding.btnViewPdf.setOnClickListener {
            startActivity(Intent(this, DownloadNShareActivity::class.java))
        }
        binding.ivBack.setOnClickListener {
            this.finish()
        }
    }

    private fun onMyPdfFilesLoad(myPdfList: List<PdfFile>) {
        if (myPdfList.isNotEmpty()) {
            setUpRecyclerView(myPdfList)
        } else {
            binding.recyclerViewMyPdf.hide()
            binding.noPdfViewLayout.show()
        }
    }

    override fun sharePDF(filePath: String) {

        val pdfUri = FileProvider.getUriForFile(this, BuildConfig.AUTHORITY, File(filePath))
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "application/pdf"
            `package` = "com.whatsapp"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
        }

        if (AppUtils.isCallable(this, intent)) {

            startActivity(intent)

            sendEvent(EventConstants.EVENT_NAME_PDF_SHARE)
            val fileName = FileUtils.fileNameFromUrl(filePath) - FileUtils.EXT_PDF
            sendEvent("${EventConstants.EVENT_NAME_PDF_SHARE}$fileName")
        } else {
            ToastUtils.makeText(
                applicationContext,
                R.string.string_install_whatsApp,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun savePdf(file: Any?) {
        if (!FeaturesManager.isFeatureEnabled(this, Features.PDF_DOWNLOAD))
            return
        if (file != null && file is PdfFile) {
            tempFilePath = file.filePath
            saveFile(file.fileName)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1211 && data?.data != null) {
            alterFile(data.data!!)
        }
    }

    private fun alterFile(uri: Uri) {
        try {
            val pdfUri = FileProvider.getUriForFile(this, BuildConfig.AUTHORITY, File(tempFilePath))
            val inputStream = contentResolver.openInputStream(pdfUri)
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { outStream ->
                    inputStream!!.copyTo(outStream)
                    Snackbar.make(
                        binding.recyclerViewMyPdf, "Your PDF has been downloaded.\n" +
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
            Snackbar.make(
                binding.recyclerViewMyPdf,
                "Unable to download file",
                Snackbar.LENGTH_SHORT
            )
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

    private fun saveFile(filename: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, filename)
        }
        try {
            startActivityForResult(intent, 1211)
        } catch (e: Exception) {
            Log.e(e)
            showApiErrorToast(this)
        }
    }

    private fun sendEvent(eventName: String) {
        eventTracker.addEventNames(eventName)
            .addStudentId(getStudentId())
            .track()
    }

    private fun setUpRecyclerView(myPdfList: List<PdfFile>) {
        binding.recyclerViewMyPdf.adapter = MyPdfListAdapter(myPdfList, this)
        binding.recyclerViewMyPdf.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMyPdf.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
    }
}
