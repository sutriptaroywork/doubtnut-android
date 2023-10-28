package com.doubtnutapp.liveclass.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnHomeworkSubmitted
import com.doubtnutapp.base.OnNotesClosed
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.databinding.FragmentHomeworkBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.liveclass.adapter.HomeWorkAdapter
import com.doubtnutapp.liveclass.viewmodel.HomeworkViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.widgets.mathview.HomeWorkMathView
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class HomeWorkFragment : BaseBindingFragment<HomeworkViewModel, FragmentHomeworkBinding>(),
    HomeWorkMathView.HomeWorkWebViewJavaInterface {

    private var qid: String = ""
    private var homeworkQuestionList: List<HomeWorkQuestion>? = null
    private var optionsMap: LinkedHashMap<String, String> = LinkedHashMap()
    private var radioOptionList: ArrayList<String> = ArrayList()

    private var pdfDownloadUrl: String? = null
    private var shareMessage: String? = null
    var filePath = ""
    private var isVideoPage = false

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var networkUtil: NetworkUtil

    companion object {
        const val TAG = "HomeWorkFragment"
        const val QUESTION_ID = "question_id"
        const val IS_VIDEO_PAGE = "is_video_page"

        fun newInstance(qid: String?, isVideoPage: Boolean? = false): HomeWorkFragment =
            HomeWorkFragment().apply {
                arguments = Bundle().apply {
                    putString(QUESTION_ID, qid)
                    putBoolean(IS_VIDEO_PAGE, isVideoPage ?: false)
                }
            }
    }

    private fun init() {
        viewModel = viewModelProvider(viewModelFactory)
        qid = arguments?.getString(QUESTION_ID).orEmpty()
        isVideoPage = arguments?.getBoolean(IS_VIDEO_PAGE) ?: true
        if (isVideoPage) {
            mBinding?.ivClose?.show()
            mBinding?.ivClose?.setOnClickListener {
                (activity as? ActionPerformer)?.performAction(OnNotesClosed())
            }
            mBinding?.ivBack?.hide()
            mBinding?.toolbar?.hide()
        } else {
            mBinding?.ivClose?.hide()
            mBinding?.ivBack?.show()
            mBinding?.toolbar?.show()
        }
        viewModel.getHomeworkQuestions(qid)
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.HW_VIEW,
                hashMapOf(
                    EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                    EventConstants.QUESTION_ID to qid
                ), ignoreSnowplow = true
            )
        )
    }

    private fun setUpObserver() {
        viewModel.homeworkLiveData.observeK(
            this,
            ::onHomeWorkQuestionsSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.homeworkSubmitLiveData.observeK(
            this,
            ::onHomeWorkSubmitSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.pdfUriLiveData.observe(this, Observer { pair ->
            if (pair?.first != null) {
                filePath = pair.first.absolutePath
                if (pair.second == HomeWorkActivity.TYPE_SHARE) {
                    sharePdf()
                } else {
                    saveFile()
                }
            } else {
                ToastUtils.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sharePdf() {
        if (FileUtils.isFilePresent(filePath)) {
            val pdfUri =
                FileProvider.getUriForFile(requireContext(), BuildConfig.AUTHORITY, File(filePath))
            Intent(Intent.ACTION_SEND).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = "application/pdf"
                `package` = "com.whatsapp"
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                if (!shareMessage.isNullOrBlank()) {
                    putExtra(Intent.EXTRA_TEXT, shareMessage.orEmpty())
                }
            }.also {
                if (AppUtils.isCallable(requireContext(), it)) {
                    startActivity(it)
                } else {
                    ToastUtils.makeText(
                        requireContext(),
                        R.string.string_install_whatsApp,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }

        } else {
            toast(getString(R.string.pdf_file_not_present))
        }
    }

    private fun onHomeWorkQuestionsSuccess(data: HomeWorkQuestionData) {
        homeworkQuestionList = data.questionList
        mBinding?.tvLectureName?.text = data.header?.lectureName.orEmpty()
        mBinding?.tvteacherName?.text = data.header?.teacherName.orEmpty()
        mBinding?.tvQuestions?.text = data.header?.questionCount.orEmpty()
        mBinding?.buttonSubmit?.text = data.button?.buttonText.orEmpty()
        mBinding?.rvHomeWork?.adapter = HomeWorkAdapter(this, deeplinkAction)
            .apply {
                clearList()
                updateList(data.questionList.orEmpty())
            }
        Handler(requireActivity().mainLooper!!).postDelayed({
            mBinding?.progressBar?.visibility = View.GONE
            mBinding?.rvHomeWork?.visibility = View.VISIBLE
        }, 2200)
        mBinding?.buttonSubmit?.setOnClickListener {
            if (!optionsMap.isNullOrEmpty()) {
                val responseList = mutableListOf<HomeWorkResponse>()
                optionsMap.keys.forEachIndexed { _, key ->
                    responseList.add(HomeWorkResponse(key, optionsMap[key]))
                }
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.HW_SUBMITTED,
                        hashMapOf(
                            EventConstants.STUDENT_ID to getStudentId(),
                            EventConstants.QUESTION_ID to qid,
                            EventConstants.SOURCE to if (isVideoPage) "video" else ""
                        )
                    )
                )
                viewModel.submitHomeWork(HomeWorkPostData(qid, responseList))
            } else {
                showToast(requireContext(), "Please submit atleast one Answer")
            }
        }
        mBinding?.ivBack?.setOnClickListener {
            activity?.finish()
        }

        pdfDownloadUrl = data.pdfDownloadUrl
        shareMessage = data.shareMessage
        if (pdfDownloadUrl.isNullOrBlank() || isVideoPage) {
            mBinding?.ivDownload?.hide()
            mBinding?.ivShare?.hide()
        } else {
            mBinding?.ivDownload?.show()
            mBinding?.ivShare?.show()
        }
        mBinding?.completionStatus?.text = data.completionStatus.orEmpty()
        mBinding?.ivCompletion?.loadImageEtx(data.completionImageUrl.orEmpty())
    }

    @JavascriptInterface
    override fun option(i: Int, position: Int) {
        Handler(Looper.getMainLooper()).post {
            optionsMap[homeworkQuestionList?.get(position)?.quizQuestionId.orEmpty()] =
                homeworkQuestionList?.get(position)?.options?.get(i)?.key.toString()
            homeworkQuestionList?.get(position)?.options?.forEachIndexed { index, data ->
                homeworkQuestionList?.get(position)?.options?.get(index)?.isSelected = index == i
            }
            radioOptionList = ArrayList()
            radioOptionList.add(i.toString())
        }
    }

    private fun onHomeWorkSubmitSuccess(data: HomeWorkResponseData) {
        if (isVideoPage) {
            (activity as? ActionPerformer)?.performAction(
                OnHomeworkSubmitted(qid)
            )
        } else {
            activity?.setResult(AppCompatActivity.RESULT_OK)
            activity?.finish()
            HomeWorkSolutionActivity.startActivity(requireContext(), true, qid)
        }
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireFragmentManager(), "BadRequestDialog")
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
            val pdfUri =
                FileProvider.getUriForFile(requireContext(), BuildConfig.AUTHORITY, File(filePath))
            val inputStream = activity?.contentResolver?.openInputStream(pdfUri)
            activity?.contentResolver?.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { outStream ->
                    inputStream!!.copyTo(outStream)
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        "Your PDF has been downloaded.\n" +
                                "You can also find it under My PDFs",
                        Snackbar.LENGTH_LONG
                    )
                        .setAction("View") {
                            openFile(uri)
                        }.show()
                }
            }
        } catch (e: Exception) {
            DocumentsContract.deleteDocument(activity?.contentResolver!!, uri)
            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                "Unable to download file",
                Snackbar.LENGTH_SHORT
            ).show()
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
            Log.e(e)
            showApiErrorToast(context)
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

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeworkBinding {
        return FragmentHomeworkBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): HomeworkViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        statusbarColor(activity, R.color.white_20)
        init()
        setUpObserver()

        mBinding?.ivDownload?.setOnClickListener(object : DebouncedOnClickListener(1200) {
            override fun onDebouncedClick(v: View?) {
                if (!pdfDownloadUrl.isNullOrBlank()) {
                    viewModel.getPdfFilePath(
                        pdfDownloadUrl.orEmpty(),
                        HomeWorkActivity.TYPE_DOWNLOAD
                    )
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.HOME_WORK_PDF_DOWNLOAD,
                            hashMapOf<String, Any>().apply {
                                put(EventConstants.SOURCE, HomeWorkActivity.TAG)
                                put(EventConstants.URL, pdfDownloadUrl.orEmpty())
                            }, ignoreSnowplow = true
                        )
                    )
                }
            }
        })

        mBinding?.ivShare?.setOnClickListener(object : DebouncedOnClickListener(1200) {
            override fun onDebouncedClick(v: View?) {
                if (!pdfDownloadUrl.isNullOrBlank()) {
                    viewModel.getPdfFilePath(pdfDownloadUrl.orEmpty(), HomeWorkActivity.TYPE_SHARE)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.HOME_WORK_PDF_SHARE,
                            hashMapOf<String, Any>().apply {
                                put(EventConstants.SOURCE, HomeWorkActivity.TAG)
                                put(EventConstants.URL, pdfDownloadUrl.orEmpty())
                            }, ignoreSnowplow = true
                        )
                    )
                }
            }
        })
    }
}