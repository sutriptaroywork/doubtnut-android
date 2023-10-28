package com.doubtnutapp.liveclass.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.ViewUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.HomeWorkSolutionData
import com.doubtnutapp.databinding.ActivityHomeWorkSolutionBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.liveclass.adapter.HomeWorkSolutionAdapter
import com.doubtnutapp.liveclass.viewmodel.HomeworkViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.*
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class HomeWorkSolutionActivity :
    BaseBindingActivity<HomeworkViewModel, ActivityHomeWorkSolutionBinding>() {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private var pdfDownloadUrl: String? = null
    private var shareMessage: String? = null
    var filePath = ""

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    companion object {
        const val TAG = "HomeWorkSolutionActivity"
        const val QUESTION_ID = "question_id"

        fun startActivity(context: Context, start: Boolean = true, questionId: String): Intent {
            return Intent(context, HomeWorkSolutionActivity::class.java).apply {
                putExtra(QUESTION_ID, questionId)
            }.also {
                if (start) context.startActivity(it)
            }
        }
    }

    private fun init() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.ivDownload.setOnClickListener(object : DebouncedOnClickListener(1200) {
            override fun onDebouncedClick(v: View?) {
                if (!pdfDownloadUrl.isNullOrBlank()) {
                    viewModel.getPdfFilePath(pdfDownloadUrl.orEmpty(), HomeWorkActivity.TYPE_DOWNLOAD)
                    analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.HOME_WORK_PDF_DOWNLOAD, hashMapOf<String, Any>().apply {
                        put(EventConstants.SOURCE, TAG)
                        put(EventConstants.URL, pdfDownloadUrl.orEmpty())
                    }, ignoreSnowplow = true))
                }
            }
        })

        binding.ivShare.setOnClickListener(object : DebouncedOnClickListener(1200) {
            override fun onDebouncedClick(v: View?) {
                if (!pdfDownloadUrl.isNullOrBlank()) {
                    viewModel.getPdfFilePath(pdfDownloadUrl.orEmpty(), HomeWorkActivity.TYPE_SHARE)
                    analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.HOME_WORK_PDF_SHARE, hashMapOf<String, Any>().apply {
                        put(EventConstants.SOURCE, TAG)
                        put(EventConstants.URL, pdfDownloadUrl.orEmpty())
                    }, ignoreSnowplow = true))
                }
            }
        })

    }

    private fun setUpObserver() {
        viewModel.homeworkSolutionLiveData.observeK(this,
                ::onHomeWorkSolutionsSuccess,
                ::onApiError,
                ::unAuthorizeUserError,
                ::ioExceptionHandler,
                ::updateProgressBarState)

        viewModel.pdfUriLiveData.observe(this, Observer { pair ->
            if (pair?.first != null) {
                filePath = pair.first.absolutePath
                if (pair.second == HomeWorkActivity.TYPE_SHARE) {
                    sharePdf()
                } else {
                    saveFile()
                }
            } else {
                ToastUtils.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun sharePdf() {
        if (FileUtils.isFilePresent(filePath)) {
            val pdfUri = FileProvider.getUriForFile(this, BuildConfig.AUTHORITY, File(filePath))
            Intent(Intent.ACTION_SEND).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = "application/pdf"
                `package` = "com.whatsapp"
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                if (!shareMessage.isNullOrBlank()) {
                    putExtra(Intent.EXTRA_TEXT, shareMessage.orEmpty())
                }
            }.also {
                if (AppUtils.isCallable(this, it)) {
                    startActivity(it)
                } else {
                    ToastUtils.makeText(this, R.string.string_install_whatsApp, Toast.LENGTH_SHORT).show()
                }
            }

        } else {
            toast(getString(R.string.pdf_file_not_present))
        }
    }

    private fun onHomeWorkSolutionsSuccess(data: HomeWorkSolutionData) {
        binding.tvLectureName.text = data.header?.lectureName.orEmpty()
        binding.tvTeacherName.text = data.header?.teacherName.orEmpty()

        binding.tvCorrect.text = data.summary?.get(0)?.text.orEmpty()
        binding.tvCorrectCount.text = data.summary?.get(0)?.count.orEmpty()
        binding.tvCorrectCount.setTextColor(Utils.parseColor(data.summary?.get(0)?.color.orEmpty()))

        binding.tvInCorrect.text = data.summary?.get(1)?.text.orEmpty()
        binding.tvInCorrectCount.text = data.summary?.get(1)?.count.orEmpty()
        binding.tvInCorrectCount.setTextColor(Utils.parseColor(data.summary?.get(1)?.color.orEmpty()))

        binding.tvSkipped.text = data.summary?.get(2)?.text.orEmpty()
        binding.tvSkippedCount.text = data.summary?.get(2)?.count.orEmpty()
        binding.tvInCorrectCount.setTextColor(Utils.parseColor(data.summary?.get(2)?.color.orEmpty()))

        if (!data.detailedSummary.isNullOrEmpty()) {
            for (i in data.detailedSummary.indices) {
                val tv = TextView(this)
                tv.text = (i + 1).toString()
                tv.width = ViewUtils.dpToPx(40f, this).toInt()
                tv.height = ViewUtils.dpToPx(40f, this).toInt()
                tv.gravity = Gravity.CENTER
                tv.setTextColor(ContextCompat.getColor(this, R.color.white))
                val lpRight = FlexboxLayout.LayoutParams(
                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                        FlexboxLayout.LayoutParams.WRAP_CONTENT)
                tv.layoutParams = lpRight
                val lp = tv.layoutParams as FlexboxLayout.LayoutParams
                lp.setMargins(ViewUtils.dpToPx(10f, this).toInt(),
                        ViewUtils.dpToPx(10f, this).toInt(),
                        ViewUtils.dpToPx(10f, this).toInt(),
                        ViewUtils.dpToPx(10f, this).toInt())
                tv.layoutParams = lp
                tv.background = Utils.getShape(
                        data.detailedSummary[i].color ?: "#000000",
                        data.detailedSummary[i].color ?: "#000000",
                        40f,
                        shape = GradientDrawable.OVAL)
                tv.setOnClickListener {
                    if (!data.solutionList.isNullOrEmpty()) {
                        binding.appBarLayout.setExpanded(false)
                        binding.rvSolutions.smoothScrollToPosition(i)
                    }
                }
                binding.flexLayout.addView(tv)
            }
        }
        if (!data.solutionList.isNullOrEmpty()) {
            binding.rvSolutions.adapter = HomeWorkSolutionAdapter(this,
                    data.solutionList.orEmpty(), deeplinkAction)
        }

        pdfDownloadUrl = data.pdfDownloadUrl
        shareMessage = data.shareMessage
        if (pdfDownloadUrl.isNullOrBlank()) {
            binding.ivDownload.hide()
            binding.ivShare.hide()
        } else {
            binding.ivDownload.show()
            binding.ivShare.show()
        }

    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this)) {
            toast(getString(R.string.somethingWentWrong))
        } else {
            toast(getString(R.string.string_noInternetConnection))
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        binding.progressBar.setVisibleState(state)
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
                            this.findViewById(android.R.id.content), "Your PDF has been downloaded.\n" +
                            "You can also find it under My PDFs", Snackbar.LENGTH_LONG
                    )
                            .setAction("View") {
                                openFile(uri)
                            }.show()
                }
            }
        } catch (e: Exception) {
            DocumentsContract.deleteDocument(contentResolver, uri)
            Snackbar.make(
                    this.findViewById(android.R.id.content),
                    "Unable to download file",
                    Snackbar.LENGTH_SHORT
            ).show()
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
        val name = FileUtils.fileNameFromUrl(pdfDownloadUrl.orEmpty()) + ".pdf"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, name)
        }
        try {
            startActivityForResult(intent, 1211)
        } catch (e: Exception) {
            showApiErrorToast(this)
        }
    }

    override fun provideViewBinding(): ActivityHomeWorkSolutionBinding {
        return ActivityHomeWorkSolutionBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): HomeworkViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        statusbarColor(this, R.color.white_20)
        init()
        setUpObserver()
        viewModel.getHomeworkSolutions(
            intent.getStringExtra(HomeWorkActivity.QUESTION_ID).orEmpty()
        )
    }
}