package com.doubtnutapp.resourcelisting.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.addtoplaylist.AddToPlaylistFragment

import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.FragmentResourceListingBinding
import com.doubtnutapp.domain.newlibrary.entities.LibraryHistoryEntity
import com.doubtnutapp.resourcelisting.model.PlayListMetaInfoDataModel
import com.doubtnutapp.resourcelisting.model.ResourceListingData
import com.doubtnutapp.resourcelisting.ui.adapter.ResourcePlaylistAdapter
import com.doubtnutapp.resourcelisting.viewmodel.ResourceListingViewModel
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.screennavigator.Screen
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.playlist.AddPlaylistFragment
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.google.android.material.snackbar.Snackbar
import com.uxcam.UXCam
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
class ResourceListingFragment :
    BaseBindingFragment<ResourceListingViewModel, FragmentResourceListingBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "VideoPlaylistFragment"
        fun newInstance(
            playlistId: String, playlistTitle: String,
            navigatefromdeeplink: Boolean, hideToolbar: Boolean = false,
            isFromVideoTag: Boolean = false, tagName: String?,
            questionId: String?, page: String?, packageDetailsId: String?,
            isAutoPlay: Boolean = false, questionIds: String? = null,
        ) = ResourceListingFragment().also {
            it.arguments = bundleOf(
                Constants.PLAYLIST_ID to playlistId,
                Constants.PLAYLIST_TITLE to playlistTitle,
                Constants.NAVIGATE_FROM_DEEPLINK to navigatefromdeeplink,
                Constants.NCERT_PLAYLIST_ID to false,
                Constants.IS_FROM_VIDEO_TAG to isFromVideoTag,
                Constants.VIDEO_TAG_NAME to tagName,
                Constants.QUESTION_ID to questionId,
                Constants.PAGE to page,
                Constants.HIDE_TOOLBAR to hideToolbar,
                Constants.PACKAGE_DETAIL_ID to packageDetailsId,
                Constants.IS_AUTO_PLAY to isAutoPlay,
                Constants.QUESTION_IDS to questionIds,
            )
        }
    }

    @Inject
    lateinit var screenNavigator: Navigator

    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener

    private var playlistId = ""
    private var playlistTitle = ""
    private var packageDetailsId = ""

    private var chapterId = ""
    private var exerciseName = ""
    private var metaInfoPlaylistId = ""
    private var metaInfoPlaylistTitle = ""
    private lateinit var adapter: ResourcePlaylistAdapter
    private var startPageNumber = 1
    private var isFromVideoTag = false
    private var questionId: String? = null
    private var tagName: String? = null
    private var page: String? = null
    private var isAutoPlay = false
    private val questionIds: List<String>? by lazy {
        arguments?.getString(Constants.QUESTION_IDS)?.split(",")
    }

    override fun performAction(action: Any) {
        viewModel.handleAction(action, page)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentResourceListingBinding {
        return FragmentResourceListingBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): ResourceListingViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        startShimmer()
        getIntentData()
        setupRecyclerView()
        setUpList()
        setListener()
        if (requireArguments().getBoolean(Constants.HIDE_TOOLBAR)) {
            mBinding?.appBarLayout?.hide()
        }
        setTitle(playlistTitle)
    }

    override fun onResume() {
        super.onResume()
        UXCam.tagScreenName("ResourceListingFragment")
    }

    private fun startShimmer() {
        mBinding?.shimmerFrameLayout?.startShimmer()
        mBinding?.shimmerFrameLayout?.show()
    }

    private fun stopShimmer() {
        mBinding?.shimmerFrameLayout?.stopShimmer()
        mBinding?.shimmerFrameLayout?.hide()
    }

    private fun getIntentData() {
        packageDetailsId = arguments?.getString(Constants.PACKAGE_DETAIL_ID, "") ?: ""

        if (arguments?.getBoolean(Constants.NCERT_PLAYLIST_ID) == true) {
            playlistId = arguments?.getString(Constants.PLAYLIST_ID, "") ?: ""
            chapterId = arguments?.getString(Constants.CHAPTER_ID, "") ?: ""
            exerciseName = arguments?.getString(Constants.EXCERCISE_NAME, "") ?: ""
            playlistTitle = arguments?.getString(Constants.PLAYLIST_TITLE, "") ?: ""
        } else {
            playlistId = arguments?.getString(Constants.PLAYLIST_ID, "") ?: ""
            playlistTitle = arguments?.getString(Constants.PLAYLIST_TITLE, "") ?: ""
            if (packageDetailsId.isNullOrEmpty()) {
                viewModel.putLibraryHistory(LibraryHistoryEntity(playlistId, "1", playlistTitle))
            }
        }

        isFromVideoTag = arguments?.getBoolean(Constants.IS_FROM_VIDEO_TAG, false) ?: false
        questionId = arguments?.getString(Constants.QUESTION_ID)
        tagName = arguments?.getString(Constants.VIDEO_TAG_NAME)
        page = arguments?.getString(Constants.PAGE)
        isAutoPlay = arguments?.getBoolean(Constants.IS_AUTO_PLAY) ?: false
    }

    private fun setTitle(title: String?) {
        if (!title.isNullOrBlank()) {
            mBinding?.textViewTitle?.text = title
        }
    }

    private fun setUpList() {
        getPlaylist(startPageNumber, playlistId, isAutoPlay, packageDetailsId, questionIds)
    }

    private fun setListener() {
        mBinding?.buttonBack?.setOnClickListener {
            activity?.onBackPressed()
        }

        mBinding?.btnViewPlaylist?.setOnClickListener {
            startPageNumber = 1
            getPlaylist(
                startPageNumber, metaInfoPlaylistId, isAutoPlay, packageDetailsId, questionIds
            )
            mBinding?.noWatchedVideoLayout?.hide()
        }
    }

    private fun setupRecyclerView() {
        adapter = ResourcePlaylistAdapter(childFragmentManager, page, this, isFromVideoTag)
        mBinding?.rvPlaylist?.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(activity)
        mBinding?.rvPlaylist?.layoutManager = linearLayoutManager
        mBinding?.rvPlaylist?.itemAnimator = null

        infiniteScrollListener = object : TagsEndlessRecyclerOnScrollListener(linearLayoutManager) {

            override fun onLoadMore(currentPage: Int) {
                getPlaylist(currentPage, playlistId, isAutoPlay, packageDetailsId, questionIds)
            }
        }.also {
            it.setStartPage(startPageNumber)
        }

        mBinding?.rvPlaylist?.addOnScrollListener(infiniteScrollListener)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.getVideoPlaylistData.observeK(
            this,
            this::onSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgressBarState
        )

        viewModel.whatsAppShareableData.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let {
                val (deepLink, imagePath, sharingMessage) = it
                deepLink?.let { shareOnWhatsApp(deepLink, imagePath, sharingMessage) }
                    ?: showBranchLinkError()
            }
        })

        viewModel.showWhatsAppShareProgressBar.observe(
            viewLifecycleOwner,
            Observer(this::updateProgress)
        )


        viewModel.navigateScreenLiveData.observe(viewLifecycleOwner, Observer(this::openScreen))


        viewModel.addToPlayListLiveData.observe(viewLifecycleOwner, Observer(this::addToPlayList))


        viewModel.isFirstPageEmptyLiveData.observeK(
            this, this::onFirstPageEmptySuccess, this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgressBarState
        )

        viewModel.navigateLiveData.observe(viewLifecycleOwner, Observer {
            val navigationData = it.getContentIfNotHandled()
            if (navigationData != null) {
                val args: Bundle? = navigationData.hashMap?.toBundle()
                context?.let { activityContext ->
                    screenNavigator.startActivityFromActivity(
                        activityContext,
                        navigationData.screen,
                        args
                    )
                }
            }
        })

        viewModel.eventLiveData.observe(viewLifecycleOwner, Observer {
            sendEvent(it)
        })

        viewModel.onAddToWatchLater.observe(viewLifecycleOwner, EventObserver {
            onWatchLaterSubmit(it)
        })
    }

    private fun onWatchLaterSubmit(id: String) {
        showAddedToWatchLaterSnackBar(
            R.string.video_saved_to_watch_later,
            R.string.change,
            Snackbar.LENGTH_LONG,
            id
        ) { idToPost ->
            viewModel.removeFromPlaylist(idToPost, "1")
            AddToPlaylistFragment.newInstance(idToPost)
                .show(childFragmentManager, AddToPlaylistFragment.TAG)
        }
    }

    private fun showAddedToWatchLaterSnackBar(
        message: Int,
        actionText: Int,
        duration: Int,
        id: String,
        action: ((idToPost: String) -> Unit)? = null
    ) {
        activity?.let {
            Snackbar.make(
                it.findViewById(android.R.id.content),
                message,
                duration
            ).apply {
                setAction(actionText) {
                    action?.invoke(id)
                }
                this.view.background = context.getDrawable(R.drawable.bg_capsule_dark_blue)
                setActionTextColor(ContextCompat.getColor(it, R.color.redTomato))
                val textView =
                    this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                textView.setTextColor(ContextCompat.getColor(it, R.color.white))
                show()
            }
        }
    }

    private fun onSuccess(list: ResourceListingData) {
        mBinding?.noWatchedVideoLayout?.hide()
        if (list.playlist != null && list.playlist.isNotEmpty()) {
            adapter.updateList(list.playlist, list.playListId)
        } else {
            infiniteScrollListener.isLastPageReached = true
        }
    }

    private fun onFirstPageEmptySuccess(playListMetaInfoDataModel: List<PlayListMetaInfoDataModel>) {
        mBinding?.noWatchedVideoLayout?.setVisibleState(true)
        val metaInfo = playListMetaInfoDataModel[0]
        Glide.with(this)
            .load(metaInfo.icon)
            .into(mBinding?.ivSuggestion ?: return)
        mBinding?.tvSuggestionTittle?.text = metaInfo.title
        mBinding?.tvSuggestionDescription?.text = metaInfo.description
        mBinding?.btnViewPlaylist?.text = metaInfo.suggestionButtonText
        if (metaInfo.suggestionId != null && metaInfo.suggestionName != null) {
            metaInfoPlaylistId = metaInfo.suggestionId
            metaInfoPlaylistTitle = metaInfo.suggestionName
            mBinding?.btnViewPlaylist?.show()
        } else {
            mBinding?.btnViewPlaylist?.hide()
        }
    }

    private fun openScreen(pair: Pair<Screen, Map<String, Any?>?>) {
        val args: Bundle? = pair.second?.toBundle()
        screenNavigator.startActivityFromActivity(requireActivity(), pair.first, args)

        val playlistId = args?.get(Constants.QUESTION_ID)
        sendEventByQid(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, playlistId.toString())
        sendEvent(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK + playlistId)
        sendCleverTapEvent(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireFragmentManager(), "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(requireActivity()).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        infiniteScrollListener.setDataLoading(state)
        mBinding?.progressBarViewPlaylistPagination?.setVisibleState(state)
        if (state.not()) {
            stopShimmer()
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
                activity?.let { it1 ->
                    ToastUtils.makeText(
                        it1,
                        R.string.string_install_whatsApp,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showBranchLinkError() {
        toast(getString(R.string.error_branchLinkNotFound))
    }

    private fun updateProgress(state: Boolean) {
        mBinding?.progressBarViewPlaylistLoading?.setVisibleState(state)
    }

    private fun addToPlayList(videoId: String) {
        AddPlaylistFragment.newInstance(videoId).show(requireFragmentManager(), "AddPlaylist")
    }

    private fun getPlaylist(
        page: Int,
        playlistId: String,
        autoPlay: Boolean,
        packageDetailsId: String,
        questionIds: List<String>?,
    ) {
        startPageNumber = page
        this.playlistId = playlistId
        if (!isFromVideoTag) {
            viewModel.viewPlayList(
                startPageNumber, playlistId, autoPlay, packageDetailsId, questionIds
            )
        } else {
            viewModel.viewPlayListForVideoTag(startPageNumber, tagName!!, questionId!!, playlistId)
        }

    }

    fun sendEvent(eventName: String) {
        activity?.apply {
            (activity?.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.EVENT_NAME_VIEW_PLAY_LIST_PAGE)
                .track()
        }
    }

    private fun sendEventByQid(eventName: String, qid: String) {
        activity?.apply {
            (activity?.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.EVENT_NAME_VIEW_PLAY_LIST_PAGE)
                .addEventParameter(Constants.QUESTION_ID, qid)
                .track()
        }
    }

    fun sendCleverTapEvent(eventName: String) {
        activity?.apply {
            (activity?.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.EVENT_NAME_VIEW_PLAY_LIST_PAGE)
                .cleverTapTrack()
        }
    }

}
