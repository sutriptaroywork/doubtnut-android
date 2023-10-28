package com.doubtnutapp.revisioncorner.ui

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
import androidx.core.view.isVisible
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.addtoplaylist.AddToPlaylistFragment

import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.HomeWorkSolutionData
import com.doubtnutapp.databinding.FragmentRcShortTestSolutionBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.liveclass.adapter.HomeWorkAdapter
import com.doubtnutapp.resourcelisting.ui.adapter.ResourcePlaylistAdapter
import com.doubtnutapp.revisioncorner.viewmodel.RcShortTestSolutionViewModel
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.extension.observeEvent
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.snackbar.Snackbar
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_resource_listing.*
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * Created by devansh on 19/08/21.
 * A modified version of HomeWorkSolutionActivity.
 * Adapters and data classes are used as is from liveclass package.
 */

class RcShortTestSolutionFragment : Fragment(R.layout.fragment_rc_short_test_solution),
    ActionPerformer2 {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var screenNavigator: Navigator

    companion object {
        const val TAG = "RcShortTestSolutionFragment"
    }

    private val navController by findNavControllerLazy()
    private val binding by viewBinding(FragmentRcShortTestSolutionBinding::bind)
    private val args by navArgs<RcShortTestSolutionFragmentArgs>()
    private val viewModel by viewModels<RcShortTestSolutionViewModel> { viewModelFactory }

    private val solutionsAdapter: ResourcePlaylistAdapter by lazy {
        ResourcePlaylistAdapter(childFragmentManager, Constants.PAGE_PRACTICE_CORNER, this, false)
    }

    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener

    private var pdfDownloadUrl: String? = null
    private var shareMessage: String? = null
    private var filePath = ""
    private var testType = ""

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        statusbarColor(requireActivity(), R.color.white_20)
        init()
        setUpObserver()
        viewModel.getResult(args)
    }

    private fun init() {
        with(binding) {
            ivBack.setOnClickListener {
                navController.navigateUp()
            }

            ivDownload.setOnDebouncedClickListener(1200) {
                if (!pdfDownloadUrl.isNullOrBlank()) {
                    viewModel.getPdfFilePath(
                        pdfDownloadUrl.orEmpty(),
                        RcShortTestFragment.TYPE_DOWNLOAD
                    )
                    viewModel.sendEvent(
                        EventConstants.HOME_WORK_PDF_DOWNLOAD,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.SOURCE, TAG)
                            put(EventConstants.URL, pdfDownloadUrl.orEmpty())
                        })
                }
            }

            ivShare.setOnDebouncedClickListener(1200) {
                if (!pdfDownloadUrl.isNullOrBlank()) {
                    viewModel.getPdfFilePath(
                        pdfDownloadUrl.orEmpty(),
                        RcShortTestFragment.TYPE_SHARE
                    )
                    viewModel.sendEvent(
                        EventConstants.HOME_WORK_PDF_SHARE,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.SOURCE, TAG)
                            put(EventConstants.URL, pdfDownloadUrl.orEmpty())
                        })
                }
            }
        }
    }

    private fun setUpObserver() {
        viewModel.resultLiveData.observeK(
            viewLifecycleOwner,
            ::onResultSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.pdfUriLiveData.observe(viewLifecycleOwner) { pair ->
            if (pair?.first != null) {
                filePath = pair.first.absolutePath
                if (pair.second == RcShortTestFragment.TYPE_SHARE) {
                    sharePdf()
                } else {
                    saveFile()
                }
            } else {
                toast("Failed", Toast.LENGTH_SHORT)
            }
        }

        viewModel.solutionsLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Success -> {
                    if (it.data.playlist.isNullOrEmpty()) {
                        infiniteScrollListener.isLastPageReached = true
                        return@observe
                    }

                    binding.tvSolutions.show()
                    solutionsAdapter.updateList(
                        it.data.playlist.orEmpty(),
                        it.data.playListId
                    )
                }
            }
        }

        viewModel.navigateScreenLiveData.observe(viewLifecycleOwner, EventObserver {
            val args: Bundle? = it.second?.toBundle()
            screenNavigator.startActivityFromActivity(requireActivity(), it.first, args)

            with(viewModel) {
                val playlistId = args?.get(Constants.QUESTION_ID)
                sendEventByQid(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, playlistId.toString())
                sendEventByEventTracker(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK + playlistId)
                sendCleverTapEvent(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK)
            }
        })

        viewModel.onAddToWatchLater.observeEvent(viewLifecycleOwner) {
            activity?.showSnackbar(
                R.string.video_saved_to_watch_later,
                R.string.change,
                Snackbar.LENGTH_LONG,
                it
            ) { idToPost ->
                viewModel.removeFromPlaylist(idToPost, "1")
                AddToPlaylistFragment.newInstance(idToPost)
                    .show(childFragmentManager, AddToPlaylistFragment.TAG)
            }
        }

        viewModel.whatsAppShareableData.observeEvent(viewLifecycleOwner) {
            val (deepLink, imagePath, sharingMessage) = it
            if (deepLink != null) {
                activity?.shareOnWhatsApp(deepLink, imagePath, sharingMessage)
            } else {
                toast(getString(R.string.error_branchLinkNotFound))
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
                if (AppUtils.isCallable(context, it)) {
                    startActivity(it)
                } else {
                    toast(R.string.string_install_whatsApp, Toast.LENGTH_SHORT)
                }
            }

        } else {
            toast(getString(R.string.pdf_file_not_present))
        }
    }

    private fun onResultSuccess(data: HomeWorkSolutionData) {
        with(binding) {
            testType = data.header?.lectureName.orEmpty()
            tvLectureName.text = data.header?.lectureName
            tvTeacherName.text = data.header?.teacherName

            data.summary?.get(0)?.let {
                tvCorrect.text = it.text
                tvCorrectCount.text = it.count
                tvCorrectCount.setTextColor(Utils.parseColor(it.color))
            }

            data.summary?.get(1)?.let {
                tvInCorrect.text = it.text
                tvInCorrectCount.text = it.count
                tvInCorrectCount.setTextColor(Utils.parseColor(it.color))
            }

            data.summary?.get(2)?.let {
                tvSkipped.text = it.text
                tvSkippedCount.text = it.count
                tvSkippedCount.setTextColor(Utils.parseColor(it.color))
            }

            if (!data.detailedSummary.isNullOrEmpty()) {
                for (i in data.detailedSummary.indices) {
                    val tv = TextView(requireContext())
                    tv.text = (i + 1).toString()
                    tv.width = 40.dpToPx()
                    tv.height = 40.dpToPx()
                    tv.gravity = Gravity.CENTER
                    tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    val lpRight = FlexboxLayout.LayoutParams(
                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                    )
                    tv.layoutParams = lpRight
                    val lp = tv.layoutParams as FlexboxLayout.LayoutParams
                    lp.setMargins(10.dpToPx())
                    tv.layoutParams = lp
                    tv.background = Utils.getShape(
                        data.detailedSummary[i].color ?: "#000000",
                        data.detailedSummary[i].color ?: "#000000",
                        40f,
                        shape = GradientDrawable.OVAL
                    )
                    tv.setOnClickListener {
                        if (!data.solutionList.isNullOrEmpty()) {
                            appBarLayout.setExpanded(false)
                            rvSolutions.smoothScrollToPosition(i)
                        }
                    }
                    flexLayout.addView(tv)
                }
            }
            if (data.solutionsPlaylistId != null) {
                rvSolutions.adapter = solutionsAdapter

                //Pagination setup for quiz solutions
                val startPageNumber = 1
                infiniteScrollListener =
                    object : TagsEndlessRecyclerOnScrollListener(rvSolutions.layoutManager) {
                        override fun onLoadMore(currentPage: Int) {
                            viewModel.getQuizSolutions(
                                page = currentPage,
                                playlistId = data.solutionsPlaylistId,
                                allQuestionIds = data.questionIds.orEmpty()
                            )
                        }
                    }.also {
                        it.setStartPage(startPageNumber)
                    }
                rvSolutions.addOnScrollListener(infiniteScrollListener)

                viewModel.getQuizSolutions(
                    page = startPageNumber,
                    playlistId = data.solutionsPlaylistId,
                    allQuestionIds = data.questionIds.orEmpty()
                )
            } else {
                rvSolutions.adapter = HomeWorkAdapter(
                    null,
                    deeplinkAction,
                    TAG
                ).apply {
                    clearList()
                    updateList(data.questions.orEmpty())
                }
            }

            pdfDownloadUrl = data.pdfDownloadUrl
            shareMessage = data.shareMessage

            ivDownload.isVisible = pdfDownloadUrl.isNotNullAndNotEmpty()
            ivShare.isVisible = pdfDownloadUrl.isNotNullAndNotEmpty()

            if (data.progressReportIcon.isNotNullAndNotEmpty()) {
                ivProgressReport.apply {
                    show()
                    loadImage(data.progressReportIcon)
                    setOnClickListener {
                        navController.navigate(R.id.actionOpenStatsScreen)
                        viewModel.sendEvent(
                            EventConstants.RC_PERFORMANCE_REPORT_ICON_CLICK,
                            hashMapOf(
                                Constants.SOURCE to "result_page",
                            )
                        )
                    }
                }
            } else {
                ivProgressReport.hide()
            }
        }
    }

    override fun performAction(action: Any) {
        viewModel.handleAction(action)
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
        binding.progressBar.isVisible = state
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
        val packageManager = activity?.packageManager ?: return
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
        val packageManager = activity?.packageManager ?: return
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
            showApiErrorToast(requireContext())
        }
    }
}