package com.doubtnutapp.revisioncorner.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.DocumentsContract
import android.view.View
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.base.extension.setBackgroundTint
import com.doubtnutapp.data.remote.models.HomeWorkQuestion
import com.doubtnutapp.data.remote.models.HomeWorkQuestionData
import com.doubtnutapp.databinding.FragmentRcShortTestBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.liveclass.adapter.HomeWorkAdapter
import com.doubtnutapp.revisioncorner.viewmodel.RcShortTestViewModel
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.*
import com.doubtnutapp.widgets.mathview.HomeWorkMathView
import com.google.android.material.snackbar.Snackbar
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * Created by devansh on 19/08/21.
 * A modified version of HomeWorkActivity.
 * Adapters and data classes are used as is from liveclass package.
 */

class RcShortTestFragment : Fragment(R.layout.fragment_rc_short_test),
    HomeWorkMathView.HomeWorkWebViewJavaInterface {

    companion object {
        const val TAG = "HomeWorkActivity"
        const val TYPE_SHARE = "SHARE"
        const val TYPE_DOWNLOAD = "DOWNLOAD"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var networkUtil: NetworkUtil

    private val args by navArgs<RcShortTestFragmentArgs>()
    private val navController by findNavControllerLazy()
    private val binding by viewBinding(FragmentRcShortTestBinding::bind)
    private val viewModel by viewModels<RcShortTestViewModel> { viewModelFactory }

    // Used via reference in HomeWorkMathView JS code
    private var homeworkQuestionList: List<HomeWorkQuestion> = emptyList()

    private var pdfDownloadUrl: String? = null
    private var shareMessage: String? = null
    var filePath = ""

    private var currentQuestionIndex: Int = 0

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        statusbarColor(activity, R.color.white_20)
        setupUi()
        setUpObserver()
        viewModel.getShortTestQuestions(args.widgetId, args.chapterAlias)
    }

    private fun setupUi() {
        binding.ivDownload.setOnDebouncedClickListener(1200) {
            if (!pdfDownloadUrl.isNullOrBlank()) {
                viewModel.getPdfFilePath(pdfDownloadUrl.orEmpty(), TYPE_DOWNLOAD)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.HOME_WORK_PDF_DOWNLOAD,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.SOURCE, TAG)
                            put(EventConstants.URL, pdfDownloadUrl.orEmpty())
                        }, ignoreSnowplow = true
                    )
                )
            }
        }

        binding.ivShare.setOnDebouncedClickListener(1200) {
            if (!pdfDownloadUrl.isNullOrBlank()) {
                viewModel.getPdfFilePath(pdfDownloadUrl.orEmpty(), TYPE_SHARE)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.HOME_WORK_PDF_SHARE,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.SOURCE, TAG)
                            put(EventConstants.URL, pdfDownloadUrl.orEmpty())
                        }, ignoreSnowplow = true
                    )
                )
            }
        }
    }

    private fun setUpObserver() {
        viewModel.shortTestQuestionDataLiveData.observeK(
            this,
            ::onHomeWorkQuestionsSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.pdfUriLiveData.observe(viewLifecycleOwner) { pair ->
            if (pair?.first != null) {
                filePath = pair.first.absolutePath
                if (pair.second == TYPE_SHARE) {
                    sharePdf()
                } else {
                    saveFile()
                }
            } else {
                ToastUtils.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
            }
        }
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
                    toast(R.string.string_install_whatsApp, Toast.LENGTH_SHORT)
                }
            }

        } else {
            toast(getString(R.string.pdf_file_not_present))
        }
    }

    private fun onHomeWorkQuestionsSuccess(data: HomeWorkQuestionData) {

        if (homeworkQuestionList.isEmpty()) viewModel.getOpenSolutionScreenAction(args)

        with(binding) {
            homeworkQuestionList = data.questionList.orEmpty()
            tvLectureName.text = data.header?.lectureName
            tvteacherName.text = data.header?.teacherName
            tvQuestions.text = data.header?.questionCount
            buttonSubmit.text = data.button?.nextText
            buttonPrevious.text = data.button?.previousText
            lifecycleScope.launchWhenStarted {
                delay(2200)
                progressBar.hide()
                rvHomeWork.show()
            }

            setQuestionData(
                homeworkQuestionList[currentQuestionIndex],
                currentQuestionIndex
            )
            buttonPrevious.setVisibleState(false)

            buttonSubmit.setOnClickListener {
                currentQuestionIndex++
                when (currentQuestionIndex) {
                    homeworkQuestionList.size - 1 -> {
                        buttonSubmit.text = data.button?.buttonText ?: data.button?.nextText
                        buttonPrevious.setVisibleState(true)
                        setQuestionData(
                            homeworkQuestionList[currentQuestionIndex],
                            currentQuestionIndex
                        )
                    }
                    homeworkQuestionList.size -> {
                        navController.navigate(viewModel.getOpenSolutionScreenAction(args))
                        viewModel.sendEvent(
                            EventConstants.RC_TEST_SUBMITED, hashMapOf(
                                EventConstants.TYPE to data.header?.lectureName.orEmpty()
                            )
                        )
                    }
                    else -> {
                        buttonPrevious.setVisibleState(true)
                        setQuestionData(
                            homeworkQuestionList[currentQuestionIndex],
                            currentQuestionIndex
                        )
                    }
                }
            }

            buttonPrevious.setOnClickListener {
                currentQuestionIndex--
                if (currentQuestionIndex == 0) {
                    buttonPrevious.setVisibleState(false)
                }
                setQuestionData(homeworkQuestionList[currentQuestionIndex], currentQuestionIndex)
            }

            ivBack.setOnClickListener {
                navController.navigateUp()
            }

            pdfDownloadUrl = data.pdfDownloadUrl
            shareMessage = data.shareMessage
            if (pdfDownloadUrl.isNullOrBlank()) {
                ivDownload.hide()
                ivShare.hide()
            } else {
                ivDownload.show()
                ivShare.show()
            }

            ivInfo.isVisible = data.rulesInfo != null
            ivInfo.setOnClickListener {
                data.rulesInfo?.let {
                    val action = RcShortTestFragmentDirections.actionShowRulesDialog(it)
                    navController.navigate(action)
                }
            }
        }
    }

    private fun setQuestionData(data: HomeWorkQuestion, position: Int) {

        with(binding.itemHomeWork) {
            tvQuestionNo.text = data.questionNumberText

            viewSolutionTv.apply {
                setVisibleState(!data.solutionDeeplink.isNullOrEmpty())
                text = data.solutionText
                setOnClickListener {
                    deeplinkAction.performAction(context, data.solutionDeeplink)
                }
            }

            tvVideoSolution.apply {
                setVisibleState(!data.videoText.isNullOrEmpty())
                text = data.videoText
                setOnClickListener {
                    deeplinkAction.performAction(context, data.videoDeeplink)
                }
            }

            hwMathView.apply {
                setJavaInterfaceForHomeWork(this@RcShortTestFragment)
                setPosition(position)
                question = data.question.orEmpty()
                data.type = if (data.questionType?.toInt() == 0) {
                    "SINGLE"
                } else {
                    "MULTI"
                }
                questionData = data
                afterPageLoaded {
                    if (questionData.isResult) {
                        val colorWhite = "#ffffff"
                        val colorBlack = "black"

                        val optionColors = arrayListOf<String>()
                        questionData.options?.forEachIndexed { index, option ->
                            when (option.key) {
                                questionData.answer -> {
                                    //Correct answer, this option can also be submitted
                                    optionColors.add("#3b8700")
                                    setOptionTextColor(index, colorWhite)
                                }
                                questionData.submittedOption -> {
                                    //Wrong answer submitted
                                    optionColors.add("#ff0000")
                                    setOptionTextColor(index, colorWhite)
                                }
                                else -> {
                                    //No answer submitted
                                    optionColors.add("#cccccc")
                                    setOptionTextColor(index, colorBlack)
                                }
                            }
                        }
                        onQuizSubmit(optionColors)
                    }
                }
                reload()
            }
        }
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
        binding.progressBar.setVisibleState(state)
    }

    @JavascriptInterface
    override fun option(optionIndex: Int, questionIndex: Int) {
        //Handler is used to run code on main thread
        Handler(Looper.getMainLooper()).post {
            homeworkQuestionList[questionIndex].options?.forEachIndexed { index, _ ->
                homeworkQuestionList[questionIndex].options?.get(index)?.isSelected =
                    index == optionIndex
            }
            binding.buttonSubmit.apply {
                isEnabled = true
                isClickable = true
                setBackgroundColor(Color.parseColor("#eb532c"))
            }
            viewModel.submitAnswer(questionIndex, optionIndex)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1211 && data?.data != null) {
            alterFile(data.data!!)
        }
    }

    private fun alterFile(uri: Uri) {
        val contentResolver = activity?.contentResolver ?: return
        try {
            val pdfUri =
                FileProvider.getUriForFile(requireContext(), BuildConfig.AUTHORITY, File(filePath))
            val inputStream = contentResolver.openInputStream(pdfUri)
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { outStream ->
                    inputStream!!.copyTo(outStream)
                    activity?.showSnackbar(
                        "Your PDF has been downloaded.\nYou can also find it under My PDFs",
                        "View",
                        Snackbar.LENGTH_LONG
                    ) {
                        openFile(uri)
                    }
                }
            }
        } catch (e: Exception) {
            DocumentsContract.deleteDocument(contentResolver, uri)
            showSnackBarMessage("Unable to download file")
        }
    }

    private fun openFile(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val packageManager = activity?.packageManager ?: return
        try {
            startActivity(Intent.createChooser(intent, "Open PDF"))
        } catch (e: java.lang.Exception) {
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
        val packageManager = activity?.packageManager ?: return
        try {
            startActivityForResult(intent, 1211)
        } catch (e: Exception) {
            Log.e(e)
            showApiErrorToast(requireContext())
        }

    }
}