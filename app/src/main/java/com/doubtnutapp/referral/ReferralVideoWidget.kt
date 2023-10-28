package com.doubtnutapp.referral

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.CoreBindingWidget
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.base.AutoPlayVideoCompleted
import com.doubtnutapp.base.MuteAutoPlayVideo
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.databinding.ReferralVideoWidgetBinding
import com.doubtnutapp.domain.videoPage.interactor.PublishViewOnboarding
import com.doubtnutapp.domain.videoPage.interactor.UpdateVideoViewInteractor
import com.doubtnutapp.rvexoplayer.RvExoPlayerView
import com.doubtnutapp.rvexoplayer.RvMuteStatus
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_BLOB
import com.doubtnutapp.utils.UserUtil
import com.google.android.exoplayer2.BasePlayer
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Raghav Aggarwal on 11/03/22.
 */
class ReferralVideoWidget constructor(context: Context) :
    CoreBindingWidget<ReferralVideoWidget.WidgetHolder, ReferralVideoWidget.WidgetModel, ReferralVideoWidgetBinding>(
        context
    ) {
    class WidgetHolder(binding: ReferralVideoWidgetBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<ReferralVideoWidgetBinding>(binding, widget) {

        override fun bindItemPayload(payload: Any) {
            if (payload is RvMuteStatus) {
                binding.rvPlayer.isMute = payload.isMute
                binding.ivVolume.load(if (payload.isMute) R.drawable.ic_mute else R.drawable.ic_volume)
            }
        }

    }

    @Keep
    class WidgetModel :
        WidgetEntityModel<ReferralVideoWidgetData, WidgetAction>()

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    @Inject
    lateinit var publishViewOnboardingInteractor: PublishViewOnboarding

    @Inject
    lateinit var updateVideoViewInteractor: UpdateVideoViewInteractor

    var source: String? = null

    var pauseForFirstTimeToPreventAutoplay = false

    init {
        CoreApplication.INSTANCE.androidInjector().inject(this)
    }

    companion object {
        const val TAG = "ReferralVideoWidget"
        const val EVENT_TAG = "referral_video_widget"

        private const val MIN_DIFFERENCE_BETWEEN_LAST_SENT = 2000L
    }

    var viewId: String? = null

    override fun getViewBinding(): ReferralVideoWidgetBinding {
        return ReferralVideoWidgetBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: WidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
        }
        val binding = holder.binding
        val data: ReferralVideoWidgetData = model.data

        binding.apply {

            root.applyBackgroundColor(data.bgColor)

            TextViewUtils.setTextFromHtml(tvText1, data.title1.orEmpty())
            tvText1.isVisible = data.title1.isNotNullAndNotEmpty2()
            TextViewUtils.setTextFromHtml(tvText2, data.title2.orEmpty())
            tvText2.isVisible = data.title2.isNotNullAndNotEmpty2()

            tvText3.text = data.title3.orEmpty()
            tvText3.isVisible = data.title3.isNotNullAndNotEmpty2()

            ivPerson.loadImage2(data.imageUrl)
            ivPerson.isVisible = data.imageUrl.isNotNullAndNotEmpty2()

            viewFooter.isVisible = data.title4?.title.isNotNullAndNotEmpty2() ||
                    data.title4?.imageUrl.isNotNullAndNotEmpty2()
            tvText4.text = data.title4?.title.orEmpty()
            tvText4.isVisible = data.title4?.title.isNotNullAndNotEmpty2()
            ivIcon.loadImage2(data.title4?.imageUrl)
            ivIcon.isVisible = data.title4?.imageUrl.isNotNullAndNotEmpty2()

            rvPlayer.uniqueViewHolderId = View.generateViewId().toString()
            rvPlayer.url = data.videoResource?.resource
            rvPlayer.drmScheme = data.videoResource?.drmScheme
            rvPlayer.drmLicenseUrl = data.videoResource?.drmLicenseUrl
            rvPlayer.mediaType = data.videoResource?.mediaType.orDefaultValue(MEDIA_TYPE_BLOB)
            rvPlayer.isMute = data.defaultMute ?: false
            rvPlayer.listener = object : RvExoPlayerView.Listener {

                override fun onPlayerReady() {
                    super.onPlayerReady()
                    togglePlayerUi(isPlaying = false, holder = widgetViewHolder)
                }

                override fun onBuffering(isBuffering: Boolean) {
                    super.onBuffering(isBuffering)
                    togglePlayerUi(isPlaying = !isBuffering, holder = widgetViewHolder)
                }

                override fun onStart() {
                    super.onStart()
                    togglePlayerUi(isPlaying = true, holder = widgetViewHolder)

                    if (!pauseForFirstTimeToPreventAutoplay && data.autoplay==false) {
                        binding.rvPlayer.playerView?.player?.let { player ->
                            if ((player as? BasePlayer)?.isPlaying == true) {
                                binding.rvPlayer.playerView?.player?.stop()
                            }
                        }
                        togglePlayerUi(false,holder)
                        pauseForFirstTimeToPreventAutoplay=true
                    }
                }

                override fun onError(error: ExoPlaybackException?) {
                    binding.rvPlayer.removePlayer()
                    holder.binding.ivForeground.show()
                    super.onError(error)
                }

                override fun onProgress(positionMs: Long) {
                    super.onProgress(positionMs)
                    data.videoResource?.autoPlayDuration?.let {
                        if (positionMs >= it) {
                            binding.rvPlayer.canResumePlayer = false
                            binding.rvPlayer.removePlayer()
                        }
                    }
                }

                override fun onPause() {
                    super.onPause()
                    togglePlayerUi(isPlaying = false, holder = widgetViewHolder)
                }

                override fun onStop() {
                    super.onStop()
                    togglePlayerUi(isPlaying = false, holder = widgetViewHolder)
                    performAction(
                        AutoPlayVideoCompleted(
                            adapterPosition = holder.absoluteAdapterPosition,
                            delayToMoveToNext = 1000L
                        )
                    )
                }

                override fun setVideoEngagementStatusListener(videoEngagementStats: ExoPlayerHelper.VideoEngagementStats) {
                    super.setVideoEngagementStatusListener(videoEngagementStats)
                    data.videoResource ?: return
                    if (data.videoResource.resource.isNotBlank() && videoEngagementStats.engagementTime > 0L) {
                        if (videoEngagementStats.engagementTime > 0L) {
                            val differenceBetweenLastSent = System.currentTimeMillis() -
                                    (data.lastEngagementTimeSentAt ?: Long.MAX_VALUE)
                            if ((data.lastEngagementTimeSentAt == null && data.lastRecordedEngagementTime == null) ||
                                (data.lastRecordedEngagementTime != videoEngagementStats.engagementTime &&
                                        differenceBetweenLastSent >= MIN_DIFFERENCE_BETWEEN_LAST_SENT)
                            ) {
                                publishViewOnboardingInteractor
                                    .execute(
                                        PublishViewOnboarding.RequestValues(
                                            page = data.page.orEmpty(),
                                            videoTime = videoEngagementStats.maxSeekTime.toString(),
                                            engagementTime = videoEngagementStats.engagementTime.toString(),
                                            studentId = UserUtil.getStudentId(),
                                            questionId = data.qid.orEmpty()
                                        )
                                    )
                                    .applyIoToMainSchedulerOnSingle()
                                    .subscribeToSingle(
                                        success = {
                                            updateVideoViewInteractor.execute(
                                                UpdateVideoViewInteractor.Param(
                                                    viewId = it.viewId,
                                                    isBack = "0",
                                                    maxSeekTime = videoEngagementStats.maxSeekTime.toString(),
                                                    engagementTime = videoEngagementStats.engagementTime.toString(),
                                                    lockUnlockLogs = ""
                                                )
                                            ).applyIoToMainSchedulerOnCompletable()
                                                .subscribeToCompletable({})
                                        },
                                        error = {}
                                    )

                                data.lastRecordedEngagementTime =
                                    videoEngagementStats.engagementTime
                                data.lastEngagementTimeSentAt =
                                    System.currentTimeMillis()
                            }
                        }
                    }
                }
            }

            ivVolume.load(if (rvPlayer.isMute) R.drawable.ic_mute else R.drawable.ic_volume)
            ivVolume.setOnClickListener {
                performAction(MuteAutoPlayVideo(rvPlayer.isMute.not()))
            }
            ivVolume.isVisible = data.videoResource?.resource.isNotNullAndNotEmpty2()
            ivPlay.isVisible = data.videoResource?.resource.isNotNullAndNotEmpty2()


            if (data.bgImageUrl.isNullOrEmpty()) {
                ivPlay.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    verticalBias = 0.4f
                }
            } else {
                ivPlay.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    verticalBias = 0.5f
                }
            }

            ivForeground.loadImage2(data.bgImageUrl)
            ivForeground.isVisible = data.bgImageUrl.isNotNullAndNotEmpty2()

            root.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        "${EVENT_TAG}_${CoreEventConstants.CLICKED}",
                        hashMapOf<String, Any>(
                            CoreEventConstants.WIDGET to TAG,
                            CoreEventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                            CoreEventConstants.SOURCE to source.orEmpty()
                        ).apply {
                            putAll(data.extraParams.orEmpty())
                        }, ignoreMoengage = false
                    )
                )

                if (data.deeplink.isNullOrEmpty()) {
                    binding.rvPlayer.playerView?.player?.let { player ->
                        if ((player as? BasePlayer)?.isPlaying == true) {
                            binding.rvPlayer.playerView?.player?.pause()
                        } else {
                            binding.rvPlayer.playerView?.player?.seekTo(0)
                            binding.rvPlayer.playerView?.player?.playWhenReady = true
                            binding.rvPlayer.playerView?.player?.prepare()
                        }
                    }
                } else {
                    binding.rvPlayer.stopPlayer()
                    deeplinkAction.performAction(context, data.deeplink)
                }
            }
        }

        return holder
    }

    private fun togglePlayerUi(isPlaying: Boolean, holder: WidgetHolder) {
        if (isPlaying) {
            holder.binding.ivForeground.gone()
            holder.binding.rvPlayer.visible()
            holder.binding.ivPlay.gone()
        } else {
            holder.binding.ivForeground.visible()
            holder.binding.rvPlayer.gone()
            holder.binding.ivPlay.visible()
        }
    }

    @Keep
    data class ReferralVideoWidgetData(
        @SerializedName("bg_color")
        val bgColor: String?,
        @SerializedName("image_url")
        val imageUrl: String?,
        @SerializedName("title1")
        val title1: String?,
        @SerializedName("title2")
        val title2: String?,
        @SerializedName("title3")
        val title3: String?,
        @SerializedName("title4")
        val title4: Title4?,

        @SerializedName("deeplink")
        val deeplink: String?,

        @SerializedName("bg_image_url")
        val bgImageUrl: String?,
        @SerializedName("qid")
        val qid: String?,
        @SerializedName("page")
        val page: String?,
        @SerializedName("video_resource")
        val videoResource: VideoResource?,
        @SerializedName("default_mute")
        var defaultMute: Boolean?,
        @SerializedName("auto_play")
        val autoplay:Boolean?,
        var lastRecordedEngagementTime: Long?,
        var lastEngagementTimeSentAt: Long?,

        @SerializedName("extra_params")
        var extraParams: HashMap<String, Any>?,
    ) : WidgetData()

    @Keep
    data class Title4(
        @SerializedName("image_url")
        val imageUrl: String?,
        @SerializedName("title")
        val title: String?
    )

    @Keep
    data class VideoResource(
        @SerializedName("resource") val resource: String,
        @SerializedName("drm_scheme") val drmScheme: String?,
        @SerializedName("drm_license_url") val drmLicenseUrl: String?,
        @SerializedName("auto_play_duration") val autoPlayDuration: Long?,
        @SerializedName("media_type") val mediaType: String?
    )
}