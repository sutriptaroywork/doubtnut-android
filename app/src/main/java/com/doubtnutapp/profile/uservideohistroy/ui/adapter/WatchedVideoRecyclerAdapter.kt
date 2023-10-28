package com.doubtnutapp.profile.uservideohistroy.ui.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.apxor.androidsdk.core.Attributes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.*
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.PlayVideo
import com.doubtnutapp.base.WatchLaterRequest
import com.doubtnutapp.databinding.ItemWatchedVideoBinding
import com.doubtnutapp.profile.uservideohistroy.model.WatchedVideo
import com.doubtnutapp.profile.uservideohistroy.ui.adapter.WatchedVideoRecyclerAdapter.WatchedVideoViewHolder
import com.doubtnutapp.sharing.VIDEO_CHANNEL
import com.doubtnutapp.utils.ApxorUtils
import com.doubtnutapp.utils.Utils.getSecondsToString

class WatchedVideoRecyclerAdapter(private val actionPerformer: ActionPerformer) :
    RecyclerView.Adapter<WatchedVideoViewHolder>() {

    private val watchedVideoList = mutableListOf<WatchedVideo>()

    companion object {
        const val TAG = "WatchedVideo"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = WatchedVideoViewHolder(
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_watched_video,
            parent,
            false
        )
    ).also {
        it.actionPerformer = actionPerformer
    }

    override fun onBindViewHolder(holder: WatchedVideoViewHolder, position: Int) {
        holder.bind(watchedVideoList[position])
    }

    override fun getItemCount() = watchedVideoList.size

    fun updateList(watchVideoList: List<WatchedVideo>) {
        val changeStartIndex = this.watchedVideoList.size
        watchedVideoList.addAll(watchVideoList)
        notifyItemRangeInserted(changeStartIndex, watchVideoList.size)
    }

    inner class WatchedVideoViewHolder(val binding: ItemWatchedVideoBinding) :
        BaseViewHolder<WatchedVideo>(binding.root) {

        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        @SuppressLint("RestrictedApi", "SetJavaScriptEnabled")
        override fun bind(watchVideo: WatchedVideo) {
            binding.watchedVideo = watchVideo
            binding.executePendingBindings()
//            binding.ivLike.isSelected = watchVideo.isLiked
            binding.timeDurationTextView.text = getSecondsToString(watchVideo.duration)

            when {
                watchVideo.isHtmlPresent() -> {
                    binding.mathView.show()
                    binding.ivMatch.hide()
                    binding.dmathView.hide()
                    binding.progressBar.hide()
                    val webSettings = binding.mathView.settings
                    webSettings.javaScriptEnabled = true
                    val html = watchVideo.html?.replace("color:white", "color:black").orEmpty()
                    binding.mathView.loadDataWithBaseURL(
                        null,
                        html,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
                !watchVideo.ocrText.isNullOrBlank() -> {
                    binding.dmathView.show()
                    binding.dmathView.text = watchVideo.ocrText
                    binding.ivMatch.hide()
                    binding.mathView.hide()
                    binding.progressBar.hide()
                }
                else -> {
                    binding.dmathView.hide()
                    binding.mathView.hide()
                    binding.ivMatch.show()
                    setImageUrl(watchVideo.thumbnailImage)
                    binding.progressBar.show()
                    binding.playVideoImageView.bringToFront()
                }
            }

            binding.clickHelperView.setOnClickListener {
                checkInternetConnection(binding.root.context) {
                    performAction(
                        PlayVideo(
                            watchVideo.questionId,
                            Constants.PAGE_HISTORY,
                            "0",
                            "",
                            watchVideo.resourceType
                        )
                    )
                }
            }

//            binding.ivLike.setOnClickListener {
//                ApxorSDK.logAppEvent(EventConstants.LIKE_VIDEO, Attributes().apply {
//                    putAttribute(EventConstants.QUESTION_ID, watchVideo.questionId)
//                    putAttribute(EventConstants.SOURCE, TAG)
//                })
//                checkInternetConnection(binding.root.context) {
//
//                    val newLikeCount = if (watchVideo.isLiked) {
//                        watchVideo.isLiked = false
//                        watchVideo.likeCount - 1
//                    } else {
//                        watchVideo.isLiked = true
//                        watchVideo.likeCount + 1
//                    }
//
//                    watchVideo.likeCount = newLikeCount
//
//                    binding.ivLike.isSelected = watchVideo.isLiked
//
//                    binding.root.tvLike.text = newLikeCount.toString()
//
//                    performAction(LikeVideo(watchVideo.questionId, watchVideo.isLiked))
//
//                }
//            }

//            binding.root.ivShare.setOnClickListener {
//                ApxorSDK.logAppEvent(EventConstants.LIKE_VIDEO, Attributes().apply {
//                    putAttribute(EventConstants.QUESTION_ID, watchVideo.questionId)
//                    putAttribute(EventConstants.SOURCE, TAG)
//                })
//                checkInternetConnection(binding.root.context) {
//
//                    val newShareCount = watchVideo.shareCount.plus(1)
//
//                    watchVideo.shareCount = newShareCount
//
//                    binding.root.tvShare.text = newShareCount.toString()
//
//                    performAction(
//                            ShareOnWhatApp(
//                                    VIDEO_CHANNEL,
//                                    "video",
//                                    watchVideo.thumbnailImage,
//                                    getControlParams(watchVideo),
//                                    watchVideo.bgColor,
//                                    watchVideo.sharingMessage,
//                                    watchVideo.questionId
//                            )
//                    )
//                }
//            }

//            binding.root.ivAdd.setOnClickListener {
//                checkInternetConnection(binding.root.context) {
//                    performAction(AddToPlayList(watchVideo.questionId))
//                }
//            }

            binding.overflowMenuSimilar.setOnClickListener {
                PopupMenu(
                    ContextThemeWrapper(binding.root.context, R.style.PopupMenuStyle),
                    binding.overflowMenuSimilarView,
                    Gravity.END
                ).also { popupMenu ->
                    popupMenu.menuInflater.inflate(R.menu.menu_popup_similar, popupMenu.menu)
                    val menuHelper = MenuPopupHelper(
                        binding.root.context,
                        popupMenu.menu as MenuBuilder,
                        binding.overflowMenuSimilarView
                    )
                    menuHelper.gravity = Gravity.END
                    menuHelper.setForceShowIcon(true)

                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.itemWatchLaterSimilar -> {
                                performAction(WatchLaterRequest(watchVideo.questionId))
                            }
                            R.id.itemShareSimilar -> {
                                ApxorUtils.logAppEvent(EventConstants.LIKE_VIDEO, Attributes().apply {
                                    putAttribute(EventConstants.QUESTION_ID, watchVideo.questionId)
                                    putAttribute(EventConstants.SOURCE, TAG)
                                })
                                checkInternetConnection(binding.root.context) {
                                    //                                    val newShareCount = watchVideo.shareCount.plus(1)
                                    //                                    watchVideo.shareCount = newShareCount
                                    //                                    binding.root.tvShare.text = newShareCount.toString()
                                    performAction(
                                        ShareOnWhatApp(
                                            VIDEO_CHANNEL,
                                            "video",
                                            watchVideo.thumbnailImage,
                                            getControlParams(watchVideo),
                                            watchVideo.bgColor,
                                            watchVideo.sharingMessage,
                                            watchVideo.questionId
                                        )
                                    )
                                }
                            }
                        }
                        true
                    }
                    menuHelper.show()
                }
            }
        }

        private fun getControlParams(watchVideo: WatchedVideo): HashMap<String, String> {

            return hashMapOf(
                Constants.PAGE to "SRP",
                Constants.Q_ID to watchVideo.questionId,
                Constants.PLAYLIST_ID to "",
                Constants.SOLUTION_RESOURCE_TYPE to watchVideo.resourceType
            )
        }

        private fun setImageUrl(thumbnailUrl: String?) {
            val imageUrl = thumbnailUrl ?: ""
            Glide.with(binding.root.context)
                .load(imageUrl)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBar.hide()
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBar.hide()
                        return false
                    }

                })
                .into(binding.ivMatch)
        }
    }

}