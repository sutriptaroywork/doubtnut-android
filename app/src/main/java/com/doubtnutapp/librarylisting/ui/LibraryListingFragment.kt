package com.doubtnutapp.librarylisting.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.constant.CoreConstants
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnut.core.utils.*
import com.doubtnutapp.*
import com.doubtnutapp.base.DownloadPDF
import com.doubtnutapp.base.Filter
import com.doubtnutapp.base.HandleDeeplink
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.librarylisting.model.BookViewItem
import com.doubtnutapp.librarylisting.model.ChapterFlexViewItem
import com.doubtnutapp.librarylisting.model.FilterInfo
import com.doubtnutapp.librarylisting.ui.adapter.FilterAdapter
import com.doubtnutapp.librarylisting.ui.adapter.LibraryListingAdapter
import com.doubtnutapp.librarylisting.viewmodel.LibraryListingCommonViewModel
import com.doubtnutapp.librarylisting.viewmodel.LibraryListingViewModel
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.screennavigator.WebViewScreen
import com.doubtnutapp.sharing.LIBRARY_PLAYLIST_CHANNEL
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.browser.CustomTabActivityHelper
import com.doubtnutapp.ui.browser.WebViewFallback
import com.doubtnutapp.ui.playlist.AddPlaylistFragment
import com.doubtnutapp.utils.*
import com.doubtnutapp.widgets.SpaceItemDecoration
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerFragment
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.util.LinkProperties
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_library_listing.*
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-09-29.
 */
class LibraryListingFragment : DaggerFragment(), ActionPerformer {

    companion object {
        private const val LISTING_ID = "listing_id"
        private const val PACKAGE_ID = "package_details_id"
        private const val LISTING_TITLE = "listing_title"
        private const val POSITION = "position"
        private const val PAGE = "page"
        fun newInstance(
            id: String,
            title: String?,
            position: Int,
            packageDetailsId: String?,
            page: String? = null
        ): LibraryListingFragment {
            return LibraryListingFragment().apply {
                arguments = bundleOf(
                    LISTING_ID to id,
                    LISTING_TITLE to title,
                    POSITION to position,
                    PAGE to page,
                    PACKAGE_ID to packageDetailsId
                )
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var screenNavigator: Navigator

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    @Inject
    lateinit var networkUtil: NetworkUtil

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var id: String = ""
    var packageDetailsId: String = ""
    var title: String = ""
    var parentTitle: String = ""
    var position: Int = 0
    var pageNumber = 1

    private lateinit var viewModel: LibraryListingViewModel

    private lateinit var parentViewModel: LibraryListingCommonViewModel

    @Inject
    lateinit var networkErrorHandler: NetworkErrorHandler

    val adapter: LibraryListingAdapter by lazy {
        LibraryListingAdapter(
            childFragmentManager,
            this,
            arguments?.getString(PAGE)
        )
    }
    private var filterAdapter: FilterAdapter? = null

    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library_listing, container, false)
    }

    private var tempFilePathPdfDownload: String? = ""

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = viewModelProvider(viewModelFactory)
        parentViewModel = activityViewModelProvider(viewModelFactory)
        if (arguments?.getString(LISTING_ID) == null) {
            activity?.finish()
        } else {
            id = arguments?.getString(LISTING_ID) ?: ""
        }
        packageDetailsId = arguments?.getString(PACKAGE_ID) ?: ""
        position = arguments?.getInt(POSITION, 0) ?: 0
        title = arguments?.getString(LISTING_TITLE) ?: ""
        if (activity?.intent?.hasExtra(LibraryListingActivity.INTENT_EXTRA_PLAYLIST_TITLE) == true) {
            parentTitle =
                activity?.intent?.getStringExtra(LibraryListingActivity.INTENT_EXTRA_PLAYLIST_TITLE)
                    ?: ""
            viewModel.parentTitle = parentTitle
        }
        setUpObserver()
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        val startPage = if (position == 0) {
            2
        } else {
            1
        }

        recyclerViewListing.clearOnScrollListeners()

        val layoutManager = GridLayoutManager(activity, 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.listings[position].viewType) {
                    R.layout.item_library_list_books -> 1
                    else -> 2
                }
            }
        }
        recyclerViewListing.layoutManager = layoutManager
        infiniteScrollListener = object : TagsEndlessRecyclerOnScrollListener(layoutManager) {
            override fun onLoadMore(currentPage: Int) {
                context?.run {
                    fetchList(currentPage, id, packageDetailsId)
                }
            }
        }.also {
            it.setStartPage(startPage)
        }

        recyclerViewListing.addOnScrollListener(infiniteScrollListener)

        recyclerViewListing.adapter = adapter
        context?.run {
            if (position == 0) {
                setUpFilterView(parentViewModel.filterData)
                adapter.updateList(parentViewModel.listingItems)
            } else {
                fetchList(startPage, id, packageDetailsId)
            }
        }
    }

    private fun fetchList(pageNumber: Int, id: String, packageDetailsId: String) {
        viewModel.fetchListingData(
            pageNumber,
            id,
            packageDetailsId,
            arguments?.getString(PAGE).orEmpty()
        )
    }

    override fun performAction(action: Any) {
        if (action is ShareOnWhatApp && action.featureType == Constants.PDF_VIEWER) {
            context?.let {
                startPdfShareOnWhatsApp(it, action)
            }
        } else if (action is Filter) {
            if (networkUtil.isConnectedWithMessage() && ::infiniteScrollListener.isInitialized) {
                id = action.id
                infiniteScrollListener.setStartPage(1)
                filterAdapter?.updateTagSelection(action.position)
                adapter.clearList()
                viewModel.fetchListingData(
                    1,
                    id,
                    packageDetailsId,
                    arguments?.getString(PAGE).orEmpty()
                )
                viewModel.onLibraryFilterClick(title = action.title, parentTitle = parentTitle)
            }
        } else if (action is DownloadPDF) {
            downloadAndSavePdf(requireContext(), action.url)
        } else if (action is HandleDeeplink) {
            deeplinkAction.performAction(requireContext(), action.deeplink)
        } else {
            viewModel.handleAction(action, arguments?.getString(PAGE))
        }

    }

    private fun setUpObserver() {
        viewModel.listingLiveData.observeK(
            viewLifecycleOwner,
            this::onListingFetched,
            networkErrorHandler::onApiError,
            networkErrorHandler::unAuthorizeUserError,
            networkErrorHandler::ioExceptionHandler,
            this::updateProgressBarState
        )

        viewModel.navigateLiveData.observe(viewLifecycleOwner, EventObserver {
            val screen = it.screen
            val args = it.hashMap
            if (screen != null) {
                if (screen == WebViewScreen) {
                    openWevViewer(args?.get(Constants.EXTERNAL_URL)?.toString())
                } else
                    screenNavigator.startActivityFromActivity(
                        requireContext(),
                        screen,
                        args?.toBundle()
                    )
            }
        })

        viewModel.addToPlayListLiveData.observe(
            viewLifecycleOwner,
            EventObserver(this::addToPlayList)
        )

        viewModel.showWhatsAppShareProgressBar.observe(
            viewLifecycleOwner,
            Observer(this::updateProgress)
        )

        viewModel.whatsAppShareableData.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let {
                val (deepLink, imagePath, sharingMessage) = it
                deepLink?.let { shareOnWhatsApp(deepLink, imagePath, sharingMessage) }
                    ?: showBranchLinkError()
            }
        })

        parentViewModel.searchTextLiveData.observe(viewLifecycleOwner, EventObserver {
            if (it.isNotEmpty()) {
                val filteredList = parentViewModel.listingItems.filter { data ->
                    when (data) {
                        is BookViewItem -> data.title.toLowerCase().contains(it.toLowerCase())
                        is ChapterFlexViewItem -> data?.title?.toLowerCase()!!
                            .contains(it.toLowerCase())
                        else -> false
                    }
                }
                if (filteredList.isNotEmpty()) {
                    adapter.updateData(filteredList)
                }
            } else {
                adapter.updateData(parentViewModel.listingItems)
            }
        })
    }

    private fun openWevViewer(url: String?) {
        if (url.isNullOrEmpty())
            showApiErrorToast(context)
        else {
            viewModel.sendWebViewerEvent()
            CustomTabActivityHelper.openCustomTab(
                context,
                CustomTabsIntent.Builder().build(),
                url.toUri(),
                WebViewFallback()
            )
        }
    }

    private fun updateProgress(state: Boolean) {
        progressBarCenter.setVisibleState(state)
    }

    private fun onPdfData(pdfData: Pair<File, String>) {
        val (file, text) = pdfData
        updateProgress(false)
        sharePdfOnWhatsApp(file, text)
    }

    private fun onPdfError() {
        toast(getString(R.string.somethingWentWrong))
        updateProgress(false)
    }

    private fun sharePdfOnWhatsApp(pdfFile: File, extraText: String) {
        val pdfUri = FileProvider.getUriForFile(context!!, BuildConfig.AUTHORITY, pdfFile)
        Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "application/pdf"
            `package` = "com.whatsapp"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra(Intent.EXTRA_TEXT, extraText)
        }.also {
            if (AppUtils.isCallable(context, it)) {
                startActivity(it)
                val fileName = FileUtils.fileNameFromUrl(pdfFile.absolutePath) - FileUtils.EXT_PDF
            } else {
                ToastUtils.makeText(
                    requireContext(),
                    R.string.string_install_whatsApp,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun shareOnWhatsApp(imageUrl: String, imageFilePath: String?, sharingMessage: String?) {
        Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            `package` = "com.whatsapp"
            putExtra(Intent.EXTRA_TEXT, "$sharingMessage $imageUrl")
            if (imageFilePath == null) {
                type = "text/plain"
            } else {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(imageFilePath))
            }
        }.also {
            if (AppUtils.isCallable(activity, it)) {
                startActivity(it)
            } else {
                ToastUtils.makeText(
                    requireActivity(),
                    R.string.string_install_whatsApp,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showBranchLinkError() {
        toast(getString(R.string.error_branchLinkNotFound))
    }

    private fun onListingFetched(list: List<RecyclerViewItem>) {
        if (list.isEmpty()) {
            infiniteScrollListener.setLastPageReached(true)
        }
        adapter.updateList(list)
        if (::infiniteScrollListener.isInitialized) {
            if (position != 0 && infiniteScrollListener.currentPage == 1) {
                setUpFilterView(viewModel.filterData)
            }
            if (infiniteScrollListener.currentPage == 1 && list.isEmpty() && filterAdapter == null) {
                group.show()
            }
        }
    }

    private fun setUpFilterView(tab: List<FilterInfo>?) {
        if (filterAdapter == null) {
            val tabInitialValue = tab?.map {
                FilterInfo(it.id, it.title, it.isLast, it.isSelected)
            }
            tabInitialValue?.let {
                recyclerViewFilter.show()
                if (filterAdapter == null) {
                    it.getOrNull(0)?.isSelected = true
                    filterAdapter = FilterAdapter(it, this, recyclerViewListing)
                    recyclerViewFilter?.adapter = filterAdapter
                    recyclerViewFilter?.addItemDecoration(
                        SpaceItemDecoration(
                            ViewUtils.dpToPx(
                                8f,
                                context!!
                            ).toInt()
                        )
                    )
                }
            } ?: recyclerViewFilter.hide()
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        infiniteScrollListener.setDataLoading(state)
        progressBarPagination.setVisibleState(state)
    }

    private fun showToast(@StringRes resId: Int) {
        showToastMessage(getString(resId))
    }

    private fun showToastMessage(message: String) {
        toast(message)
    }

    private fun addToPlayList(videoId: String) {
        AddPlaylistFragment.newInstance(videoId).show(fragmentManager!!, "AddPlaylist")
    }

    private fun startPdfShareOnWhatsApp(context: Context, action: ShareOnWhatApp) {
        val pdfUrl = action.controlParams?.get(Constants.INTENT_EXTRA_PDF_URL)
        val shouldDownloadPdf =
            action.featureType == Constants.PDF_VIEWER && (pdfUrl?.isNotEmpty() == true)
        if (shouldDownloadPdf) {
            updateProgress(true)
            downloadAndSharePdfOnWhatsApp(
                context,
                action.featureType,
                action.controlParams,
                action.sharingMessage,
                pdfUrl
            )
        }
    }

    private fun downloadAndSharePdfOnWhatsApp(
        context: Context, featureType: String?,
        controlParams: HashMap<String, String>?, sharingMessage: String?, pdfUrl: String?
    ) {
        getSharableDeepLink(context, featureType, controlParams, sharingMessage) { deepLink ->
            pdfUrl?.let {
                val filepath = getFileDestinationPath(context, pdfUrl)
                when {
                    FileUtils.isFilePresent(filepath) -> onPdfData(
                        Pair(
                            File(filepath),
                            "$sharingMessage $deepLink"
                        )
                    )
                    else -> compositeDisposable.add(DataHandler.INSTANCE.pdfRepository.downloadPdf(
                        pdfUrl,
                        filepath
                    )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            onPdfData(Pair(File(filepath), "$sharingMessage $deepLink"))
                        }, {
                            onPdfError()
                        })
                    )
                }
            }
        }
    }

    private fun downloadAndSavePdf(context: Context, url: String?) {
        updateProgress(true)
        url?.let {
            tempFilePathPdfDownload = getFileDestinationPath(context, url)
            when {
                FileUtils.isFilePresent(tempFilePathPdfDownload) -> saveFile(url)
                else -> compositeDisposable.add(DataHandler.INSTANCE.pdfRepository.downloadPdf(
                    url,
                    tempFilePathPdfDownload!!
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        saveFile(url)
                    }, {
                        onPdfError()
                    })
                )
            }
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
            val pdfUri = FileProvider.getUriForFile(
                requireContext(),
                BuildConfig.AUTHORITY,
                File(tempFilePathPdfDownload)
            )
            val inputStream = activity?.contentResolver?.openInputStream(pdfUri)
            activity?.contentResolver?.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { outStream ->
                    inputStream!!.copyTo(outStream)
                    Snackbar.make(
                        requireView(), "Your PDF has been downloaded.\n" +
                                "You can also find it under My PDFs", Snackbar.LENGTH_LONG
                    )
                        .setAction("View") {
                            openFile(uri)
                        }.show()
                }
            }
            viewModel.sendPdfDownloadEvent()
        } catch (e: Exception) {
            activity?.contentResolver?.let { DocumentsContract.deleteDocument(it, uri) }
            Snackbar.make(requireView(), "Unable to download file", Snackbar.LENGTH_SHORT).show()
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

    private fun saveFile(url: String) {
        updateProgress(false)
        val name = FileUtils.fileNameFromUrl(url)
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

    private fun getFileDestinationPath(context: Context, url: String): String {
        val externalDirectoryPath = context.getExternalFilesDir(null)?.path
            ?: ""
        val isChildDirCreated =
            FileUtils.createDirectory(externalDirectoryPath, AppUtils.PDF_DIR_NAME)
        if (isChildDirCreated) {
            val fileName = FileUtils.fileNameFromUrl(url)
            return AppUtils.getPdfDirectoryPath(context) + File.separator + fileName
        }
        return FileUtils.EMPTY_PATH
    }

    private fun getSharableDeepLink(
        context: Context, type: String?,
        controlParams: HashMap<String, String>?,
        sharingMessage: String?,
        onSuccess: (String) -> Unit
    ) {
        val linkProperties = LinkProperties().apply {
            channel = LIBRARY_PLAYLIST_CHANNEL
            feature = type
            campaign = CoreConstants.CAMPAIGN
        }.also {
            controlParams?.let { params ->
                params.forEach { (key, value) ->
                    it.addControlParameter(key, value)
                }
            }
        }

        ThreadUtils.runOnAnalyticsThread {
            with(BranchUniversalObject()) {
                generateShortUrl(context, linkProperties) { url, error ->
                    CoreApplication.INSTANCE.runOnMainThread {
                        if (error == null) {
                            onSuccess(url)
                        } else {
                            ToastUtils.makeText(
                                context,
                                Constants.QUESTION_SHORT_URL_ERROR_BRANCH_TOAST,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

}