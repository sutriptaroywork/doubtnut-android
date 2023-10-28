package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.AutoPlayVideoCompleted
import com.doubtnutapp.base.MuteAutoPlayVideo
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.databinding.WidgetVideoBannerAutoplayChildBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.rvexoplayer.RvExoPlayerView
import com.doubtnutapp.rvexoplayer.RvMuteStatus
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_BLOB
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by devansh on 11/2/21.
 */

class VideoBannerAutoplayChildWidget(context: Context) :
    BaseBindingWidget<VideoBannerAutoplayChildWidget.WidgetHolder, VideoBannerAutoplayChildWidget.Model, WidgetVideoBannerAutoplayChildBinding>(
        context
    ) {

    companion object {
        private const val MIN_DIFFERENCE_BETWEEN_LAST_SENT = 2000L
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun getViewBinding(): WidgetVideoBannerAutoplayChildBinding {
        return WidgetVideoBannerAutoplayChildBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {

        val data = model.data
        val binding = holder.binding
        model.extraParams?.put(Constants.ID, data.id)

        binding.apply {
            imageView.loadImage(data.imageUrl)

            rvPlayer.uniqueViewHolderId = View.generateViewId().toString()
            rvPlayer.url = data.videoResource.resource
            rvPlayer.drmScheme = data.videoResource.drmScheme
            rvPlayer.drmLicenseUrl = data.videoResource.drmLicenseUrl
            rvPlayer.mediaType = data.videoResource.mediaType.orDefaultValue(MEDIA_TYPE_BLOB)
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
                }

                override fun onError(error: ExoPlaybackException?) {
                    binding.rvPlayer.removePlayer()
                    holder.binding.imageView.show()
                    super.onError(error)
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
                    if (data.videoResource.resource.isNotBlank() && videoEngagementStats.engagementTime > 0L
                    ) {
                        if (data.trackEngagementTime == true &&
                            videoEngagementStats.engagementTime > 0L
                        ) {
                            val differenceBetweenLastSent =
                                System.currentTimeMillis() - (data.lastEngagementTimeSentAt
                                    ?: Long.MAX_VALUE)
                            if ((data.lastEngagementTimeSentAt == null &&
                                        data.lastRecordedEngagementTime == null) ||
                                (data.lastRecordedEngagementTime != videoEngagementStats.engagementTime &&
                                        differenceBetweenLastSent >= MIN_DIFFERENCE_BETWEEN_LAST_SENT)
                            ) {
                                sendVideoWatchTimeToServer(
                                    answerVideo = data.videoResource.resource,
                                    engagementTime = videoEngagementStats.engagementTime,
                                    viewFrom = data.viewFrom.orEmpty()
                                )
                                data.lastRecordedEngagementTime =
                                    videoEngagementStats.engagementTime
                                data.lastEngagementTimeSentAt = System.currentTimeMillis()
                            }
                        }
                    }
                }
            }

            ivVolume.load(if (rvPlayer.isMute) R.drawable.ic_mute else R.drawable.ic_volume)
            ivVolume.setOnClickListener {
                performAction(MuteAutoPlayVideo(rvPlayer.isMute.not()))
            }

            holder.itemView.setOnClickListener {
                if (data.deeplink.isEmpty()) return@setOnClickListener
                binding.rvPlayer.stopPlayer()
                DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams))
                deeplinkAction.performAction(context, data.deeplink)
            }
        }

        return holder
    }

    private fun togglePlayerUi(isPlaying: Boolean, holder: WidgetHolder) {
        if (isPlaying) {
            holder.binding.rvPlayer.show()
            holder.binding.imageView.hide()
        } else {
            holder.binding.rvPlayer.hide()
            holder.binding.imageView.show()
        }
    }

    private fun sendVideoWatchTimeToServer(
        answerVideo: String,
        engagementTime: Long,
        viewFrom: String
    ) {
        val params = hashMapOf<String, Any>()
        params["answer_video"] = answerVideo
        params["view_from"] = viewFrom
        params["video_time"] = engagementTime

        DataHandler.INSTANCE.networkService.get().trackAutoPlayData(params.toRequestBody())
            .applyIoToMainSchedulerOnCompletable()
            .subscribeToCompletable({})
    }

    class WidgetHolder(binding: WidgetVideoBannerAutoplayChildBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetVideoBannerAutoplayChildBinding>(binding, widget) {

        override fun bindItemPayload(payload: Any) {
            if (payload is RvMuteStatus) {
                binding.rvPlayer.isMute = payload.isMute
                binding.ivVolume.load(if (payload.isMute) R.drawable.ic_mute else R.drawable.ic_volume)
            }
        }
    }

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String,
        @SerializedName("deeplink") val deeplink: String,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("view_from") val viewFrom: String?,
        @SerializedName("video_resource") val videoResource: VideoResource,
        @SerializedName("default_mute") var defaultMute: Boolean?,
        @SerializedName("track_engagement_time") var trackEngagementTime: Boolean?,
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
}