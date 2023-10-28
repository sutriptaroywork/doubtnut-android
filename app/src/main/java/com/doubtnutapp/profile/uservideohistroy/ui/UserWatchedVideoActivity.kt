package com.doubtnutapp.profile.uservideohistroy.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.addtoplaylist.AddToPlaylistFragment

import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityUserWatchedVideoBinding
import com.doubtnutapp.profile.uservideohistroy.model.WatchedVideo
import com.doubtnutapp.profile.uservideohistroy.model.WatchedVideoMetaInfo
import com.doubtnutapp.profile.uservideohistroy.ui.adapter.WatchedVideoRecyclerAdapter
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.screennavigator.Screen
import com.doubtnutapp.ui.FragmentHolderActivity
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.playlist.AddPlaylistFragment
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.utils.NetworkUtils
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class UserWatchedVideoActivity :
    BaseBindingActivity<UserWatchedVideoViewModel, ActivityUserWatchedVideoBinding>(),
    ActionPerformer {

    @Inject
    lateinit var screenNavigator: Navigator

    private lateinit var watchedVideoRecyclerAdapter: WatchedVideoRecyclerAdapter

    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener

    private var metaInfoPlaylistId = ""
    private var metaInfoPlaylistTitle = ""

    override fun provideViewBinding(): ActivityUserWatchedVideoBinding {
        return ActivityUserWatchedVideoBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): UserWatchedVideoViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        init()
        setUpObserver()
        getWatchedVideoResult(START_PAGE)
        setListener()
    }

    private fun setListener() {
        binding.ivBack.setOnClickListener {
            this.finish()
        }

        binding.btnViewPlaylist.setOnClickListener {
            val intent = Intent(this, FragmentHolderActivity::class.java)
            intent.action = Constants.NAVIGATE_VIEW_PLAYLIST
            intent.putExtra(Constants.PLAYLIST_ID, metaInfoPlaylistId)
            intent.putExtra(Constants.PLAYLIST_TITLE, metaInfoPlaylistTitle)
            intent.putExtra(Constants.COLOR_INDEX, 1)
            startActivity(intent)
        }
    }

    override fun performAction(action: Any) {
        viewModel.handleAction(action)
    }

    private fun getWatchedVideoResult(pageNumber: Int) {
        viewModel.getWatchedVideos(pageNumber)
    }

    private fun init() {
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        watchedVideoRecyclerAdapter = WatchedVideoRecyclerAdapter(this)
        binding.watchedVideosList.adapter = watchedVideoRecyclerAdapter
        val linearLayoutManager = LinearLayoutManager(this)
        binding.watchedVideosList.layoutManager = linearLayoutManager

        infiniteScrollListener = object : TagsEndlessRecyclerOnScrollListener(linearLayoutManager) {

            override fun onLoadMore(currentPage: Int) {
                getWatchedVideoResult(currentPage)
            }
        }.also {
            it.setStartPage(START_PAGE)
        }

        binding.watchedVideosList.addOnScrollListener(infiniteScrollListener)
    }

    private fun setUpObserver() {
        viewModel.watchedVideoLiveData.observeK(
            this,
            this::onSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgressBarState
        )

        viewModel.whatsAppShareableData.observe(this, { data ->
            data?.getContentIfNotHandled()?.let {
                val (deepLink, imagePath, sharingMessage) = it

                deepLink?.let { shareOnWhatsApp(deepLink, imagePath, sharingMessage) }
                    ?: showBranchLinkError()
            }
        })

        viewModel.whatsAppShareProgressLiveData.observe(this, Observer(this::updateProgress))

        viewModel.showWhatsappProgressLiveData.observe(this, Observer(this::updateProgress))


        viewModel.navigateScreenLiveData.observe(this, Observer(this::openScreen))


        viewModel.addToPlayListLiveData.observe(this, Observer(this::addToPlayList))

        viewModel.isFirstPageEmptyLiveData.observeK(
            this, this::onFirstPageEmptySuccess, this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgressBarState
        )

        viewModel.onAddToWatchLater.observe(this, EventObserver {
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
                .show(supportFragmentManager, AddToPlaylistFragment.TAG)
        }
    }

    @Suppress("SameParameterValue")
    private fun showAddedToWatchLaterSnackBar(
        message: Int,
        actionText: Int,
        duration: Int,
        id: String,
        action: ((idToPost: String) -> Unit)? = null
    ) {
        Snackbar.make(
            this.findViewById(android.R.id.content),
            message,
            duration
        ).apply {
            setAction(actionText) {
                action?.invoke(id)
            }
            this.view.background = ContextCompat.getDrawable(
                this@UserWatchedVideoActivity,
                R.drawable.bg_capsule_dark_blue
            )
            setActionTextColor(
                ContextCompat.getColor(
                    this@UserWatchedVideoActivity,
                    R.color.redTomato
                )
            )
            val textView =
                this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            textView.setTextColor(
                ContextCompat.getColor(
                    this@UserWatchedVideoActivity,
                    R.color.white
                )
            )
            show()
        }
    }

    private fun onFirstPageEmptySuccess(watchedVideoMetaInfo: List<WatchedVideoMetaInfo>) {
        binding.noWatchedVideoLayout.setVisibleState(true)
        val metaInfo = watchedVideoMetaInfo[0]
        Glide.with(this)
            .load(metaInfo.icon)
            .into(binding.ivSuggestion)
        binding.tvSuggestionTittle.text = metaInfo.title
        binding.tvSuggestionDescription.text = metaInfo.description
        binding.btnViewPlaylist.text = metaInfo.suggestionButtonText
        if (metaInfo.suggestionId != null) {
            metaInfoPlaylistId = metaInfo.suggestionId
            metaInfoPlaylistTitle = metaInfo.suggestionName
            binding.btnViewPlaylist.show()
        } else {
            binding.btnViewPlaylist.hide()
        }

    }

    private fun onSuccess(list: List<WatchedVideo>) {
        if (list.isNotEmpty()) {
            watchedVideoRecyclerAdapter.updateList(list)
        } else {
            infiniteScrollListener.isLastPageReached = true
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

        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }

    }

    private fun updateProgressBarState(state: Boolean) {
        infiniteScrollListener.setDataLoading(state)
        binding.progressBar.setVisibleState(state)
    }

    private fun addToPlayList(videoId: String) {
        AddPlaylistFragment.newInstance(videoId).show(supportFragmentManager, "AddPlaylist")
    }

    private fun openScreen(pair: Pair<Screen, Map<String, Any?>?>) {
        val args: Bundle? = pair.second?.toBundle()
        screenNavigator.startActivityFromActivity(this, pair.first, args)
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
            if (AppUtils.isCallable(this, it)) {
                startActivity(it)
            } else {
                ToastUtils.makeText(this, R.string.string_install_whatsApp, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun showBranchLinkError() {
        toast(getString(R.string.error_branchLinkNotFound))
    }

    private fun updateProgress(state: Boolean) {
        binding.progressLayout.setVisibleState(state)
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "UserWatchedVideoActivity"
        private const val START_PAGE = 1

        fun getStartIntent(context: Context, source: String) =
            Intent(context, UserWatchedVideoActivity::class.java).apply {
                putExtra(Constants.SOURCE, source)
            }
    }

}
