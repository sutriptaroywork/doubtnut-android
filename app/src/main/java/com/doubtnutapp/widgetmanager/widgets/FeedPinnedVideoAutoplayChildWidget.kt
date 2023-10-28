package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.AutoPlayVideoCompleted
import com.doubtnutapp.base.FeedDNVideoWatched
import com.doubtnutapp.base.FeedPinnedVideoItemVisible
import com.doubtnutapp.base.MuteAutoPlayVideo
import com.doubtnutapp.data.remote.models.feed.FeedPostItem
import com.doubtnutapp.databinding.WidgetFeedPinnedAutoplayChildBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.videoPage.entities.ApiVideoResource
import com.doubtnutapp.feed.FeedPostTypes
import com.doubtnutapp.feed.view.LinkPreviewView
import com.doubtnutapp.rvexoplayer.RvExoPlayerView
import com.doubtnutapp.rvexoplayer.RvMuteStatus
import com.doubtnutapp.ui.forum.comments.CommentsActivity
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_BLOB
import com.google.android.exoplayer2.ExoPlaybackException
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import javax.inject.Inject

/**
 * Created by Divya on 30/07/21.
 */

class FeedPinnedVideoAutoplayChildWidget(context: Context) :
    BaseBindingWidget<FeedPinnedVideoAutoplayChildWidget.WidgetHolder, FeedPinnedVideoAutoplayChildWidget.Model, WidgetFeedPinnedAutoplayChildBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null
    var videoResource: ApiVideoResource? = null
    var videoUrl: String = ""
    var playedVideoResourcePosition: Int = -1

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun getViewBinding(): WidgetFeedPinnedAutoplayChildBinding {
        return WidgetFeedPinnedAutoplayChildBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {

        val data = model.data
        model.extraParams?.put(Constants.ID, data.id)

        holder.binding.apply {
            imageView.loadImage(
                data.videoObj?.thumbnailImage,
                errorDrawable = R.drawable.doubtnut_stickers
            )

            if (!data.message.isNullOrEmpty()) {
                tvMessage.show()
                tvMessage.text = data.message
                BetterLinkMovementMethod
                    .linkify(Linkify.ALL, tvMessage)
                    .setOnLinkClickListener { textView: TextView, url: String ->
                        LinkPreviewView.linkClickAction(textView.context, url)
                    }
            } else tvMessage.hide()

            bindPostActions(holder.binding, data)
            rvPlayer.uniqueViewHolderId = data.id

            val isMute = data.defaultMute ?: false
            rvPlayer.isMute = isMute

            if (
                (data.videoObj != null) ||
                ((data.currentTimeInMs ?: 0) <= (data.videoObj?.autoPlayDuration ?: 0))
            ) {
                data.videoObj?.resources?.let {
                    var index = 0
                    for (res in data.videoObj.resources!!) {
                        if (res != null) {
                            videoResource = res
                            videoUrl = res.resource
                            playedVideoResourcePosition = index
                            break
                        }
                        index++
                    }
                }

                if (videoResource != null) {
                    rvPlayer.uniqueViewHolderId = data.id
                    videoUrl = videoResource!!.resource
                    rvPlayer.url = videoResource!!.resource
                    rvPlayer.drmScheme = videoResource!!.drmScheme
                    rvPlayer.drmLicenseUrl = videoResource!!.drmLicenseUrl
                    rvPlayer.mediaType = videoResource!!.mediaType.orDefaultValue(MEDIA_TYPE_BLOB)
                    rvPlayer.canResumePlayer = true
                    rvPlayer.isMute = isMute
                } else {
                    videoUrl = data.videoObj?.videoUrl.orEmpty()
                    rvPlayer.url = data.videoObj?.videoUrl
                    rvPlayer.mediaType = (MEDIA_TYPE_BLOB)
                    rvPlayer.canResumePlayer = true
                    rvPlayer.isMute = isMute
                }

                rvPlayer.listener = object : RvExoPlayerView.Listener {

                    override fun onStart() {
                        super.onStart()
                        rvPlayer.show()
                    }

                    override fun onError(error: ExoPlaybackException?) {
                        holder.binding.rvPlayer.removePlayer()
                        super.onError(error)
                    }

                    override fun onProgress(positionMs: Long) {
                        super.onProgress(positionMs)
                        data.currentTimeInMs = positionMs
                        data.videoObj?.autoPlayDuration?.let {
                            if (positionMs >= it) {
                                rvPlayer.canResumePlayer = false
                                rvPlayer.stopPlayer()
                                rvPlayer.removePlayer()
                            }
                        }
                    }

                    override fun onPause() {
                        super.onPause()
                        rvPlayer.hide()
                    }

                    override fun onPlayerReady() {
                        super.onPlayerReady()
                        actionPerformer?.performAction(FeedPinnedVideoItemVisible(data))
                        rvPlayer.playerView?.useController = true
                        rvPlayer.playerView?.findViewById<TextView>(R.id.exo_go_live)?.visibility =
                            View.GONE
                        ivVolume.load(if (rvPlayer.isMute) R.drawable.ic_mute else R.drawable.ic_volume)
                    }

                    override fun onStop() {
                        super.onStop()
                        rvPlayer.hide()
                        performAction(
                            AutoPlayVideoCompleted(
                                adapterPosition = holder.adapterPosition,
                                delayToMoveToNext = 500L
                            )
                        )
                    }

                    override fun setVideoEngagementStatusListener(videoEngagementStats: ExoPlayerHelper.VideoEngagementStats) {
                        super.setVideoEngagementStatusListener(videoEngagementStats)
                        sendWatchData(
                            data,
                            videoUrl,
                            videoEngagementStats,
                            rvPlayer.isMute
                        )
                    }
                }
            }

            holder.binding.clickHelperView.setOnClickListener {
                if (!data.deeplink.isNullOrEmpty()) {
                    rvPlayer?.stopPlayer()
                    DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams))
                    deeplinkAction.performAction(context, data.deeplink)
                }
            }
            ivVolume.load(if (rvPlayer.isMute) R.drawable.ic_mute else R.drawable.ic_volume)
            ivVolume.setOnClickListener {
                performAction(MuteAutoPlayVideo(rvPlayer.isMute.not()))
            }

            root.setOnClickListener {
                if (!data.deeplink.isNullOrEmpty()) {
                    rvPlayer?.stopPlayer()
                    DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams))
                    deeplinkAction.performAction(context, data.deeplink)
                }
            }
        }

        return holder
    }

    private fun bindPostActions(
        binding: WidgetFeedPinnedAutoplayChildBinding,
        feedItem: FeedPostItem
    ) {
        if (!feedItem.type.equals(FeedPostTypes.TYPE_DN_ACTIVITY)) {
            if (feedItem.commentCount > 0) {
                binding.tvAllComments.show()
                binding.tvAllComments.setOnClickListener {
                    openComments(binding.root.context, feedItem.id, 0)
                }
            } else {
                binding.tvAllComments.hide()
            }
            binding.addCommentContainer.setOnClickListener {
                openComments(binding.root.context, feedItem.id, 0)
            }
            binding.addCommentContainer.show()
        } else {
            binding.addCommentContainer.hide()
            binding.tvAllComments.hide()
        }

        binding.viewFeedComment.apply {
            if (feedItem.featuredComment != null) {
                this.show()
                this.bindData(feedItem.featuredComment, feedItem.id)
            } else this.hide()
        }

        binding.viewPostActions.apply {
            if (feedItem.disableLcsfBar) {
                this.visibility = View.GONE
                binding.addCommentContainer.hide()
            } else {
                this.visibility = View.VISIBLE
                this.bindData(feedItem, source.orEmpty())
            }
        }

    }

    private fun openComments(context: Context, entityId: String, position: Int) {
        CommentsActivity.startActivityForResult(
            context as AppCompatActivity,
            entityId,
            "new_feed_type",
            position,
            EventConstants.CATEGORY_FEED,
            null
        )
    }

    private fun sendWatchData(
        data: FeedPostItem,
        videoUrl: String,
        videoEngagementStats: ExoPlayerHelper.VideoEngagementStats,
        isMute: Boolean
    ) {
        if (videoUrl.isNotBlank() && videoEngagementStats.engagementTime > 0L) {
            actionPerformer?.performAction(FeedDNVideoWatched(data, videoEngagementStats))

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.FEED_PINNED_VIDEO_AUTO_PLAYED,
                    hashMapOf(
                        EventConstants.EVENT_NAME_ID to data.id,
                        EventConstants.ANSWER_VIDEO to videoUrl,
                        EventConstants.SOURCE to "COMMUNITY",
                        EventConstants.ENGAGEMENT_TIME to videoEngagementStats.engagementTime,
                        EventConstants.IS_MUTE to isMute
                    ), ignoreSnowplow = true
                )
            )
        }
    }

    class WidgetHolder(binding: WidgetFeedPinnedAutoplayChildBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetFeedPinnedAutoplayChildBinding>(binding, widget) {

        override fun bindItemPayload(payload: Any) {
            if (payload is RvMuteStatus) {
                binding.rvPlayer.isMute = payload.isMute
                binding.ivVolume.load(if (payload.isMute) R.drawable.ic_mute else R.drawable.ic_volume)
            }
        }
    }

    class Model : WidgetEntityModel<FeedPostItem, WidgetAction>()
}