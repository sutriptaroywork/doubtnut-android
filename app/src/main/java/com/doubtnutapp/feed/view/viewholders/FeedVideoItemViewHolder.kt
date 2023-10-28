package com.doubtnutapp.feed.view.viewholders

import android.app.ProgressDialog
import android.content.Context
import android.text.util.Linkify
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.gone
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.PostDeletedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.FeedDNVideoWatched
import com.doubtnutapp.base.FeedPremiumBlockedScreenVisible
import com.doubtnutapp.base.FeedPremiumVideoItemVisible
import com.doubtnutapp.base.MuteAutoPlayVideo
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.feed.FeedPostItem
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.databinding.ItemFeedVideoBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.videoPage.entities.ApiVideoResource
import com.doubtnutapp.feed.FeedPostTypes
import com.doubtnutapp.feed.view.FeedAdapter
import com.doubtnutapp.feed.view.LinkPreviewView
import com.doubtnutapp.feed.view.TopicFeedActivity
import com.doubtnutapp.rvexoplayer.RvExoPlayerView
import com.doubtnutapp.ui.forum.comments.CommentsActivity
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_BLOB
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.videoPage.model.ViewAnswerData
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.google.android.exoplayer2.ExoPlaybackException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FeedVideoItemViewHolder(
    view: View,
    fm: FragmentManager,
    private val isNested: Boolean,
    private val showTopic: Boolean,
    private val source: String,
    val analyticsPublisher: AnalyticsPublisher
) : FeedAdapter.FeedViewHolder(view, fm) {

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    val binding = ItemFeedVideoBinding.bind(itemView)

    @Inject
    lateinit var deeplinkAction: DeeplinkAction
    private var feedItemPosition: Int = 0
    private var feedItem: FeedPostItem? = null
    private var isMute: Boolean = true
    private var isPlyaing: Boolean = false

    fun bind(feedItem: FeedPostItem, feedItemPosition: Int) {
        this.feedItemPosition = feedItemPosition
        this.feedItem = feedItem
        this.isMute = feedItem.videoObj?.mute ?: true

        binding.ivPlayVideo.hide()
        binding.progressBar.hide()

        //Message
        if (!feedItem.message.isNullOrEmpty()) {
            binding.tvMessage.show()
            binding.tvMessage.text = feedItem.message
            BetterLinkMovementMethod
                .linkify(Linkify.ALL, binding.tvMessage)
                .setOnLinkClickListener { textView: TextView, url: String ->
                    LinkPreviewView.linkClickAction(textView.context, url)
                }
            if (feedItem.type == "link") {
                feedItem.attachments = arrayListOf()
                if (binding.tvMessage.urls.isNotEmpty()) {
                    feedItem.attachments = arrayListOf(binding.tvMessage.urls[0].url)
                }
            }
        } else binding.tvMessage.hide()

        //Topic
        if (showTopic && feedItem.topic != null && feedItem.topic.isNotEmpty()) {
            binding.tvTopic.show()
            binding.tvTopic.text = feedItem.topic
            binding.tvTopic.setOnClickListener {
                TopicFeedActivity.getStartIntent(it.context, feedItem.topic, true)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EVENT_NAME_FEED_POST_TOPIC_CLICK,
                        hashMapOf(Pair(EventConstants.TOPIC, feedItem.topic)),
                        ignoreSnowplow = true
                    )
                )
            }
        } else {
            binding.tvTopic.hide()
        }
        //lcsf bar
        if (feedItem.disableLcsfBar) {
            binding.viewPostActions.visibility = View.GONE
        } else {
            binding.viewPostActions.visibility = View.VISIBLE
            binding.viewPostActions.bindData(feedItem, source)
        }

        //Post timestamp
        binding.tvTimestamp.text = Utils.formatTime(itemView.context, feedItem.createdAt)

        //User Name & profile picture
        binding.tvUsername.apply {
            text = feedItem.username
            setOnClickListener {
                FragmentWrapperActivity.userProfile(context, feedItem.studentId, "feed_post")
            }
            if (feedItem.isVerified != null && feedItem.isVerified) {
                Utils.setVerifiedTickTextView(this)
            } else {
                setCompoundDrawables(null, null, null, null)
            }
        }
        binding.ivProfileImage.apply {
            loadImage(feedItem.studentImageUrl, R.drawable.ic_default_one_to_one_chat)
            setOnClickListener {
                FragmentWrapperActivity.userProfile(context, feedItem.studentId, "feed_post")
                analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_FEED_POST_PROFILE_CLICK, ignoreSnowplow = true))
            }
        }

        //Comment Section
        if (feedItem.showComments == true) {
            binding.ivSelfProfileImage.loadImage(
                userProfileImage(itemView.context),
                R.color.grey_feed,
                R.color.grey_feed
            )
            if (feedItem.featuredComment != null) {
                binding.viewFeedComment.show()
                binding.viewFeedComment.bindData(feedItem.featuredComment, feedItem.id)
            } else binding.viewFeedComment.hide()

            if (feedItem.commentCount > 0) {
                binding.tvAllComments.show()
                binding.tvAllComments.setOnClickListener {
                    openComments(itemView.context, feedItem.id, feedItemPosition)
                }
            } else {
                binding.tvAllComments.hide()
            }
            binding.addCommentContainer.setOnClickListener {
                openComments(itemView.context, feedItem.id, feedItemPosition)
            }
            binding.addCommentContainer.show()
        } else {
            binding.tvAllComments.hide()
            binding.addCommentContainer.hide()
            binding.viewFeedComment.hide()
        }

        // Overflow Menu
        if (feedItem.studentId != defaultPrefs().getString(Constants.STUDENT_ID, "") &&
            (feedItem.type.equals(FeedPostTypes.TYPE_DN_ACTIVITY) || feedItem.type.equals(
                FeedPostTypes.TYPE_DN_PAID_VIDEO
            ))
        ) {
            binding.overflowMenu.hide()
        } else {
            binding.overflowMenu.show()
            binding.overflowMenu.setOnClickListener {
                showPopUpMenu(feedItem, it)
            }
        }

        //Video Attachment
        binding.viewVideoBlocked.root.hide()
        binding.ivAttachment.show()
        binding.ivAttachment.loadImage(feedItem.videoObj!!.thumbnailImage, null)
        setupVideo(feedItem)

        // Volume
        binding.ivVolume.load(if (binding.rvPlayer.isMute) R.drawable.ic_small_volume_mute else R.drawable.ic_small_volume_unmute)
        binding.ivVolume.setOnClickListener {
            isMute = binding.rvPlayer.isMute.not()
            actionPerformer?.performAction(MuteAutoPlayVideo(isMute))
            binding.rvPlayer.isMute = isMute
            binding.ivVolume.load(if (isMute) R.drawable.ic_small_volume_mute else R.drawable.ic_small_volume_unmute)
        }
    }

    private fun setupVideo(feedItem: FeedPostItem) {
        binding.llVideoInfo.setVisibleState(feedItem.title.isNotNullAndNotEmpty())
        binding.videoTitle.text = feedItem.title.orEmpty()

        if (feedItem.videoObj?.resources.isNullOrEmpty()) {
            binding.flAttachment.setOnClickListener {
                openVideoDetailPage(feedItem)
            }
            binding.ivPlayVideo.show()
            binding.progressBar.hide()
            return
        }
        var videoResource: ApiVideoResource? = null
        for (res in feedItem.videoObj!!.resources!!) {
            if (res != null) {
                videoResource = res
                break
            }
        }

        if (videoResource != null) {
            binding.rvPlayer.uniqueViewHolderId = feedItem.id
            binding.rvPlayer.url = videoResource.resource
            binding.rvPlayer.drmScheme = videoResource.drmScheme
            binding.rvPlayer.drmLicenseUrl = videoResource.drmLicenseUrl
            binding.rvPlayer.mediaType = videoResource.mediaType.orDefaultValue(MEDIA_TYPE_BLOB)
            binding.rvPlayer.canResumePlayer = true
            binding.rvPlayer.isMute = isMute

            binding.rvPlayer.listener = object : RvExoPlayerView.Listener {

                override fun onBuffering(isBuffering: Boolean) {
                    super.onBuffering(isBuffering)
                    binding.progressBar.setVisibleState(isBuffering)
                }

                override fun onPlayerReady() {
                    super.onPlayerReady()
                    binding.progressBar.show()
                    binding.rvPlayer.playerView?.useController = true
                    binding.rvPlayer.playerView?.findViewById<TextView>(R.id.exo_go_live)?.visibility =
                        View.GONE
                }

                override fun onStart() {
                    super.onStart()
                    binding.progressBar.gone()
                    togglePlayerUi(true)
                }

                override fun onError(error: ExoPlaybackException?) {
                    binding.rvPlayer.removePlayer()
                    binding.progressBar.gone()
                    binding.clickHelperView.gone()
                    super.onError(error)
                }

                override fun onProgress(positionMs: Long) {
                    super.onProgress(positionMs)
                    val viewAnswerData = feedItem.viewAnswerData as? ViewAnswerData
                    val videoDurationInSec: Long = positionMs / 1000
                    val premiumContentCondition = ( // condition 1
                            feedItem.premiumVideoOffset != null &&
                                    viewAnswerData?.premiumVideoBlockedData != null &&
                                    feedItem.premiumVideoOffset!! > 0 && videoDurationInSec >= feedItem.premiumVideoOffset!!
                            )
                    val autoPlayDurationCondition = ( // condition 2
                            feedItem.videoObj.autoPlayDuration != null &&
                                    feedItem.videoObj.autoPlayDuration!! > 0 &&
                                    TimeUnit.SECONDS.toMillis(videoDurationInSec) >= feedItem.videoObj.autoPlayDuration!!
                            )
                    if (premiumContentCondition || autoPlayDurationCondition) {
                        binding.rvPlayer.canResumePlayer = false
                        binding.rvPlayer.stopPlayer()
                        binding.rvPlayer.removePlayer()
                        binding.progressBar.hide()
                        isPlyaing = false
                    }
                    if (premiumContentCondition) { // Do it only if condition 1 is true i.e. for premium video
                        binding.viewVideoBlocked.root.show()
                        bindPremiumVideoBlockedData(feedItem.viewAnswerData!! as ViewAnswerData)
                    }
                }

                override fun onPause() {
                    super.onPause()
                    binding.progressBar.hide()
                    togglePlayerUi(false)
                }

                override fun onStop() {
                    super.onStop()
                    binding.progressBar.hide()
                }

                override fun setVideoEngagementStatusListener(videoEngagementStats: ExoPlayerHelper.VideoEngagementStats) {
                    super.setVideoEngagementStatusListener(videoEngagementStats)
                    sendWatchData(feedItem, videoEngagementStats)
                }
            }
            binding.rvPlayer.show()
            binding.clickHelperView.show()
        } else {
            binding.ivPlayVideo.show()
            binding.progressBar.hide()
            binding.rvPlayer.hide()
            binding.clickHelperView.hide()
        }

        binding.flAttachment.setOnClickListener {
            openVideoDetailPage(feedItem)
        }

        binding.clickHelperView.setOnClickListener {
            openVideoDetailPage(feedItem)
        }
    }

    private fun sendWatchData(
        feedItem: FeedPostItem,
        videoEngagementStats: ExoPlayerHelper.VideoEngagementStats
    ) {
        actionPerformer?.performAction(FeedDNVideoWatched(feedItem, videoEngagementStats))
    }

    private fun bindPremiumVideoBlockedData(viewAnswerData: ViewAnswerData) {

        binding.viewVideoBlocked.root.findViewById<TextView>(R.id.textViewHeader).text =
            viewAnswerData.premiumVideoBlockedData?.title.orEmpty()
        binding.viewVideoBlocked.root.findViewById<TextView>(R.id.textViewSubHeader).text =
            viewAnswerData.premiumVideoBlockedData?.description.orEmpty()

        val button = binding.viewVideoBlocked.root.findViewById<TextView>(R.id.button)
        button.text = viewAnswerData.premiumVideoBlockedData?.courseDetailsButtonText.orEmpty()
        button.background = Utils.getShape(
            viewAnswerData.premiumVideoBlockedData?.courseDetailsButtonBgColor ?: "##000000",
            viewAnswerData.premiumVideoBlockedData?.courseDetailsButtonCornerColor ?: "#eb532c",
            5f, 3
        )
        button.setTextColor(
            Utils.parseColor(
                viewAnswerData.premiumVideoBlockedData?.courseDetailsButtonTextColor
                    ?: "#eb532c"
            )
        )

        val buttonLink = binding.viewVideoBlocked.root.findViewById<TextView>(R.id.btnAdLink)
        buttonLink.text = viewAnswerData.premiumVideoBlockedData?.coursePurchaseButtonText.orEmpty()
        buttonLink.background = Utils.getShape(
            viewAnswerData.premiumVideoBlockedData?.coursePurchaseButtonBgColor ?: "#eb532c",
            viewAnswerData.premiumVideoBlockedData?.coursePurchaseButtonBgColor ?: "#eb532c",
            5f
        )
        buttonLink.setTextColor(
            Utils.parseColor(
                viewAnswerData.premiumVideoBlockedData?.coursePurchaseButtonTextColor
                    ?: "#ffffff"
            )
        )

        button.setOnClickListener {
            onPremiumBlockCourseDetailClick(viewAnswerData)
        }

        buttonLink.setOnClickListener {
            onPremiumBlockBuyNowClick(viewAnswerData)
        }
    }

    private fun openVideoDetailPage(feedItem: FeedPostItem) {
        val intent = VideoPageActivity.startActivity(
            itemView.context,
            feedItem.videoObj?.questionId.orEmpty(),
            "",
            "",
            Constants.PAGE_COMMUNITY,
            "",
            false,
            "",
            "",
            false
        )
        itemView.context.startActivity(intent)
    }

    private fun onPremiumBlockCourseDetailClick(viewAnswerData: ViewAnswerData) {

        viewAnswerData.let {
            actionPerformer?.performAction(FeedPremiumBlockedScreenVisible(viewAnswerData))
        }

        analyticsPublisher.publishEvent(
            StructuredEvent(EventConstants.CATEGORY_FEED,
                EventConstants.PAID_CONTENT_SEARCH_EVENTS,
                eventParams = hashMapOf<String, Any>().apply {
                    put(EventConstants.VIDEO_VIEW_ID, viewAnswerData.viewId.orEmpty())
                    put(
                        EventConstants.COURSE_ID,
                        viewAnswerData.premiumVideoBlockedData?.courseId
                            ?: 0
                    )
                    put(
                        EventConstants.PAID_USER,
                        viewAnswerData.isPremium == true && viewAnswerData.isVip == true
                    )
                    put(EventConstants.CTA_VIEWED, true)
                    put(EventConstants.CTA_CLICKED, 1)
                    put(EventConstants.VIEW_FROM, source)
                })
        )

        viewAnswerData.premiumVideoBlockedData?.courseDetailsButtonDeeplink?.let {
            deeplinkAction.performAction(
                itemView.context,
                viewAnswerData.premiumVideoBlockedData.courseDetailsButtonDeeplink,
                EventConstants.PAGE_PAID_CONTENT_FEED
            )
        }
    }

    private fun onPremiumBlockBuyNowClick(viewAnswerData: ViewAnswerData) {

        viewAnswerData.premiumVideoBlockedData?.coursePurchaseButtonDeeplink?.let {
            deeplinkAction.performAction(
                itemView.context,
                viewAnswerData.premiumVideoBlockedData.coursePurchaseButtonDeeplink.orEmpty(),
                EventConstants.PAGE_PAID_CONTENT_FEED
            )
        }
    }

    override fun onStopAutoplay() {
    }

    override fun onStartAutoplay() {
    }

    private fun togglePlayerUi(isPlaying: Boolean) {
        this.isPlyaing = isPlaying
        actionPerformer?.performAction(FeedPremiumVideoItemVisible(feedItem))
        binding.ivAttachment.setVisibleState(!isPlaying)
        binding.rvPlayer.setVisibleState(isPlaying)
        binding.clickHelperView.setVisibleState(isPlaying)
    }

    private fun showPopUpMenu(feedItem: FeedPostItem, anchor: View) {
        val popupMenu = PopupMenu(anchor.context, anchor)
        val menu = R.menu.menu_feed_post
        popupMenu.inflate(menu)

        popupMenu.show()
        if (feedItem.studentId != defaultPrefs(anchor.context).getString(
                Constants.STUDENT_ID,
                ""
            )
        ) {
            popupMenu.menu.removeItem(R.id.itemDelete)
        }
        if (feedItem.type.equals(FeedPostTypes.TYPE_DN_ACTIVITY)) {
            popupMenu.menu.removeItem(R.id.itemReport)
            popupMenu.menu.removeItem(R.id.itemMute)
        }
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.itemReport -> {
                    DataHandler.INSTANCE.teslaRepository.reportPost(feedItem.id)
                        .subscribeOn(Schedulers.io()).subscribe()
                    showToast(anchor.context, "Post reported")
                    analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_FEED_POST_REPORT, ignoreSnowplow = true))
                }
                R.id.itemDelete -> {
                    deletePost(anchor.context, feedItem.id)
                }
                R.id.itemMute -> {
                    DataHandler.INSTANCE.teslaRepository.muteFeedPostNotification(hashMapOf<String, String>().apply {
                        put("entity_id", feedItem.id)
                        put("entity_type", "new_feed_type")
                    }.toRequestBody()).subscribeOn(Schedulers.io()).subscribe()
                    showToast(anchor.context, "Post muted")
                    analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_FEED_POST_MUTE, ignoreSnowplow = true))
                }
            }
            true
        }
    }

    private fun deletePost(context: Context, postId: String) {
        AlertDialog.Builder(context)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _, _ ->
                val progressDialog: ProgressDialog = ProgressDialog.show(
                    context, "",
                    "Deleting post...", true, true
                )
                DataHandler.INSTANCE.teslaRepository.deletePost(postId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe { data, error ->
                        progressDialog.dismiss()
                        if (error == null) {
                            showToast(context, "Post deleted")
                            DoubtnutApp.INSTANCE.bus()?.send(PostDeletedEvent(postId))
                            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_FEED_POST_DELETE, ignoreSnowplow = true))
                        } else {
                            showToast(context, "Error deleting post")
                        }
                    }
                progressDialog.show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openComments(context: Context, entityId: String, position: Int) {
        CommentsActivity.startActivityForResult(
            context as AppCompatActivity,
            entityId,
            "new_feed_type",
            position,
            EventConstants.CATEGORY_FEED, null
        )
    }
}
