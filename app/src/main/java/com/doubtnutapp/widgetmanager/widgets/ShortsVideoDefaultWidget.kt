package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.AutoPlayVideoCompleted
import com.doubtnutapp.base.AutoPlayVideoStarted
import com.doubtnutapp.databinding.WidgetShortsVideoDefaultBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.videoPage.entities.ImaAdTagResource
import com.doubtnutapp.rvexoplayer.RvExoPlayerView
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.shorts.repository.ShortsRepository
import com.doubtnutapp.ui.forum.comments.CommentsActivity
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_BLOB
import com.doubtnutapp.utils.NetworkUtil
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShortsVideoDefaultWidget(context: Context) :
    BaseBindingWidget<ShortsVideoDefaultWidget.WidgetHolder,
            ShortsVideoDefaultWidget.Model, WidgetShortsVideoDefaultBinding>(context) {

    companion object {
        const val TAG = "ShortsVideoDefaultWidget"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var shortsRepository: ShortsRepository

    @Inject
    lateinit var networkUtil: NetworkUtil

    @Inject
    lateinit var whatsappSharing: WhatsAppSharing

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent
            .forceUnWrap()
            .inject(this)

        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

    }

    override fun getViewBinding(): WidgetShortsVideoDefaultBinding {
        return WidgetShortsVideoDefaultBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
        })

        val data = model.data
        val binding = holder.binding
        model.extraParams?.put(Constants.ID, data.id.orEmpty())
        model.extraParams?.put(EventConstants.QUESTION_ID, data.questionId.orEmpty())

        var isStarted = false

        (context as AppCompatActivity).let { appCompatActivity ->
            appCompatActivity.lifecycleScope.launchWhenStarted {
                delay(4000)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.SHORTS_VIDEO_PLAYED_STATUS,
                        hashMapOf<String, Any>(
                            EventConstants.QUESTION_ID to data.questionId.orEmpty(),
                            EventConstants.VIDEO_PLAYED to isStarted,
                        ).apply {
                            putAll(model.extraParams ?: hashMapOf())
                        })
                )
            }
        }

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                model.type + EventConstants.UNDERSCORE + EventConstants.VIEWED,
                hashMapOf<String, Any>(
                    EventConstants.QUESTION_ID to data.questionId.orEmpty()
                ).apply {
                    putAll(model.extraParams ?: hashMapOf())
                }
            )
        )

        binding.apply {
//            imageView.isVisible = !data.imageUrl.isNullOrBlank()
//            imageView.loadImage(data.imageUrl)
            tvTitle.text = data.title.orEmpty()
            tvTitle.maxLines = 1
            tvTitle.setOnClickListener {
                if (tvTitle.maxLines <= 1) {
                    tvTitle.maxLines = 5
                } else {
                    tvTitle.maxLines = 1
                }
            }
            ivShare.setOnClickListener {
                whatsappSharing.shareOnWhatsAppFromDeeplinkWithChannelAndCampaign(
                    actionDeeplink = data.shareDeeplink.orEmpty(),
                    imageUrl = data.shareThumbnail.orEmpty(),
                    sharingMessage = data.shareMessage.orEmpty(),
                    channelName = data.channelId.orEmpty(),
                    campaignName = data.campaign.orEmpty()
                )
                whatsappSharing.startShare(context)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        model.type
                                + EventConstants.UNDERSCORE
                                + EventConstants.SHARE
                                + EventConstants.UNDERSCORE
                                + EventConstants.CLICK,
                        hashMapOf<String, Any>().apply {
                            putAll(model.extraParams ?: hashMapOf())
                        }
                    )
                )
            }

            ivBookmark.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    if (data.isBookmarked == true) {
                        R.drawable.ic_bookmark
                    } else {
                        R.drawable.ic_bookmark_unfilled_grey
                    }
                )
            )
            ivBookmark.setColorFilter(
                ContextCompat.getColor(context, R.color.White),
                android.graphics.PorterDuff.Mode.SRC_IN
            )

            ivBookmark.setOnClickListener {
                if (networkUtil.isConnectedWithMessage()) {
                    (it.context as? AppCompatActivity)?.lifecycleScope?.launch {
                        val isBookmarked = data.isBookmarked ?: false
                        shortsRepository.bookmarkShortsVideo(
                            data.questionId.orEmpty(),
                            isBookmarked
                        ).catch {}.collect {}
                        data.isBookmarked = !isBookmarked
                        if (data.isBookmarked == true) {
                            ToastUtils.makeText(
                                context,
                                context.getString(R.string.added_to_bookmark),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            ToastUtils.makeText(
                                context,
                                context.getString(R.string.removed_from_bookmark),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        ivBookmark.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                if (data.isBookmarked == true) {
                                    R.drawable.ic_bookmark
                                } else {
                                    R.drawable.ic_bookmark_unfilled_grey
                                }
                            )
                        )
                        ivBookmark.setColorFilter(
                            ContextCompat.getColor(context, R.color.White),
                            android.graphics.PorterDuff.Mode.SRC_IN
                        )
                    }
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            model.type
                                    + EventConstants.UNDERSCORE
                                    + EventConstants.BOOKMARK
                                    + EventConstants.UNDERSCORE
                                    + EventConstants.CLICK,
                            hashMapOf<String, Any>().apply {
                                put(EventConstants.IS_BOOKMARK, data.isBookmarked ?: false)
                                putAll(model.extraParams ?: hashMapOf())
                            }
                        )
                    )
                }
            }

            ivComment.setOnClickListener {
                val activity = (it.context as? AppCompatActivity) ?: return@setOnClickListener
                CommentsActivity.startActivityForResult(
                    activity,
                    data.questionId.orEmpty(),
                    "shorts",
                    0,
                    "shorts_widget",
                    null
                )
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        model.type
                                + EventConstants.UNDERSCORE
                                + EventConstants.COMMENT
                                + EventConstants.UNDERSCORE
                                + EventConstants.CLICK,
                        hashMapOf<String, Any>().apply {
                            putAll(model.extraParams ?: hashMapOf())
                        }
                    )
                )
            }

            button.isVisible = data.button != null
            button.text = data.button?.text.orEmpty()
            button.setOnClickListener {
                deeplinkAction.performAction(context, data.button?.deeplink)
            }

            rvPlayer.uniqueViewHolderId = View.generateViewId().toString()
            rvPlayer.url = data.videoResource.resource
            rvPlayer.drmScheme = data.videoResource.drmScheme
            rvPlayer.drmLicenseUrl = data.videoResource.drmLicenseUrl
            rvPlayer.mediaType = data.videoResource.mediaType.orDefaultValue(MEDIA_TYPE_BLOB)
            rvPlayer.isMute = data.defaultMute ?: false

            // IMA ad tag
            val imaAdTagData = data.imaAdTagResource.orEmpty()
            val imaAdTag = if (imaAdTagData.isNotEmpty()) {
                imaAdTagData[0].adTag.orEmpty()
            } else {
                ""
            }
            val imaAdTimeout = if (imaAdTagData.isNotEmpty()) {
                imaAdTagData[0].adTimeout ?: 0
            } else {
                0
            }
            if (imaAdTag.isNotEmpty()) {
                rvPlayer.imaAdTagUrl = imaAdTag
            }
            if (imaAdTimeout > 0) {
                rvPlayer.imaAdMediaLoadTimeOut = imaAdTimeout
            }

            rvPlayer.playerView?.useController = true
            rvPlayer.playerView?.controllerAutoShow = true
            rvPlayer.playerView?.showController()

            rvPlayer.listener = object : RvExoPlayerView.Listener {

                override fun onStart() {
                    super.onStart()
//                    imageView.isVisible = false
                    rvPlayer.show()

                    rvPlayer.playerView?.useController = false
                    rvPlayer.playerView?.controllerAutoShow = false
                    rvPlayer.playerView?.hideController()
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            model.type + EventConstants.UNDERSCORE + "video_started",
                            hashMapOf<String, Any>(
                                EventConstants.QUESTION_ID to data.questionId.orEmpty()
                            ).apply {
                                putAll(model.extraParams ?: hashMapOf())
                            }
                        )
                    )
                    isStarted = true
                    performAction(AutoPlayVideoStarted)
                }

                override fun onError(error: ExoPlaybackException?) {
                    binding.rvPlayer.removePlayer()
                    super.onError(error)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            model.type + EventConstants.UNDERSCORE + "video_error",
                            hashMapOf<String, Any>(
                                EventConstants.QUESTION_ID to data.questionId.orEmpty()
                            ).apply {
                                putAll(model.extraParams ?: hashMapOf())
                            }
                        )
                    )
                }

                override fun onProgress(positionMs: Long) {
                    super.onProgress(positionMs)
                    data.videoResource.autoPlayDuration?.let {
                        if (positionMs >= it) {
                            binding.rvPlayer.canResumePlayer = false
                            binding.rvPlayer.removePlayer()
                        }
                    }
                }

                override fun onPause() {
                    super.onPause()
                    rvPlayer.hide()
                }

                override fun onStop() {
                    super.onStop()
                    rvPlayer.hide()
                    performAction(
                        AutoPlayVideoCompleted(
                            adapterPosition = holder.adapterPosition,
                            delayToMoveToNext = 50L
                        )
                    )
                }

                override fun setVideoEngagementStatusListener(videoEngagementStats: ExoPlayerHelper.VideoEngagementStats) {
                    super.setVideoEngagementStatusListener(videoEngagementStats)
                    if (data.videoResource.resource.isNotBlank() && videoEngagementStats.engagementTime >= 0L) {
                        if (videoEngagementStats.engagementTime >= 0L) {
                            val differenceBetweenLastSent =
                                System.currentTimeMillis() - (data.lastEngagementTimeSentAt
                                    ?: Long.MAX_VALUE)
                            if ((data.lastEngagementTimeSentAt == null &&
                                        data.lastRecordedEngagementTime == null) ||
                                (differenceBetweenLastSent >= 200)
                            ) {
                                GlobalScope.launch {
                                    shortsRepository.updateShortsWatchFootprint(
                                        data.questionId.orEmpty(),
                                        videoEngagementStats.engagementTime
                                    ).catch {
                                        analyticsPublisher.publishEvent(
                                            AnalyticsEvent(
                                                model.type + EventConstants.UNDERSCORE + "eng_update_error",
                                                hashMapOf<String, Any>(
                                                    EventConstants.QUESTION_ID to data.questionId.orEmpty(),
                                                    EventConstants.ENGAGEMENT_TIME to videoEngagementStats.engagementTime.toString()
                                                ).apply {
                                                    putAll(model.extraParams ?: hashMapOf())
                                                }
                                            )
                                        )
                                    }.collect {
                                        analyticsPublisher.publishEvent(
                                            AnalyticsEvent(
                                                model.type + EventConstants.UNDERSCORE + "eng_update_success",
                                                hashMapOf<String, Any>(
                                                    EventConstants.QUESTION_ID to data.questionId.orEmpty(),
                                                    EventConstants.ENGAGEMENT_TIME to videoEngagementStats.engagementTime.toString()
                                                ).apply {
                                                    putAll(model.extraParams ?: hashMapOf())
                                                }
                                            )
                                        )
                                    }
                                }
                                analyticsPublisher.publishEvent(
                                    AnalyticsEvent(
                                        model.type + EventConstants.UNDERSCORE + "eng_update_req",
                                        hashMapOf<String, Any>(
                                            EventConstants.QUESTION_ID to data.questionId.orEmpty(),
                                            EventConstants.ENGAGEMENT_TIME to videoEngagementStats.engagementTime.toString()
                                        ).apply {
                                            putAll(model.extraParams ?: hashMapOf())
                                        }
                                    )
                                )
                                data.lastRecordedEngagementTime =
                                    videoEngagementStats.engagementTime
                                data.lastEngagementTimeSentAt = System.currentTimeMillis()
                            }
                        }
                    }
                }
            }

            viewMute.setOnClickListener {
                rvPlayer.isMute = rvPlayer.isMute.not()
            }
        }

        return holder
    }

    class WidgetHolder(binding: WidgetShortsVideoDefaultBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetShortsVideoDefaultBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String?,
        @SerializedName("question_id") val questionId: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("video_resource") val videoResource: VideoResource,
        @SerializedName("default_mute") var defaultMute: Boolean?,
        @SerializedName("is_bookmarked") var isBookmarked: Boolean?,
        @SerializedName("share_deeplink") var shareDeeplink: String?,
        @SerializedName("share_message") var shareMessage: String?,
        @SerializedName("share_thumbnail") var shareThumbnail: String?,
        @SerializedName("campaign") var campaign: String?,
        @SerializedName("channel_id") var channelId: String?,
        @SerializedName("button") var button: Button?,
        @SerializedName("title") var title: String?,
        @SerializedName("adTagResource") val imaAdTagResource: List<ImaAdTagResource>?,
        var lastRecordedEngagementTime: Long?,
        var lastEngagementTimeSentAt: Long?
    ) : WidgetData()

    @Keep
    data class VideoResource(
        @SerializedName("resource") val resource: String,
        @SerializedName("drm_scheme") val drmScheme: String?,
        @SerializedName("drm_license_url") val drmLicenseUrl: String?,
        @SerializedName("auto_play_duration") val autoPlayDuration: Long?,
        @SerializedName("media_type") val mediaType: String?
    )

    @Keep
    data class Button(
        @SerializedName("text") val text: String,
        @SerializedName("deeplink") val deeplink: String?
    )
}