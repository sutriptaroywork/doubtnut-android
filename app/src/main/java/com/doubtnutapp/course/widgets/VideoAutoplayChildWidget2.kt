package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Keep
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.AutoPlayVideoCompleted
import com.doubtnutapp.base.MuteAutoPlayVideo
import com.doubtnutapp.databinding.WidgetVideoAutoplayChild2Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.rvexoplayer.RvExoPlayerView
import com.doubtnutapp.rvexoplayer.RvMuteStatus
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_BLOB
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class VideoAutoplayChildWidget2(context: Context) :
    BaseBindingWidget<VideoAutoplayChildWidget2.WidgetHolder, VideoAutoplayChildWidget2.Model, WidgetVideoAutoplayChild2Binding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetVideoAutoplayChild2Binding {
        return WidgetVideoAutoplayChild2Binding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {

        val data = model.data
        val binding = holder.binding
        model.extraParams?.put(Constants.ID, data.id.orEmpty())

        binding.apply {
            imageView.loadImage(data.imageUrl)

            rvPlayer.uniqueViewHolderId = View.generateViewId().toString()
            rvPlayer.url = data.videoResource.resource
            rvPlayer.drmScheme = data.videoResource.drmScheme
            rvPlayer.drmLicenseUrl = data.videoResource.drmLicenseUrl
            rvPlayer.mediaType = data.videoResource.mediaType.orDefaultValue(MEDIA_TYPE_BLOB)
            rvPlayer.isMute = data.defaultMute ?: false

            rvPlayer.playerView?.useController = true
            rvPlayer.playerView?.controllerAutoShow = true
            rvPlayer.playerView?.showController()

            rvPlayer.listener = object : RvExoPlayerView.Listener {

                override fun onStart() {
                    super.onStart()
                    rvPlayer.show()

                    rvPlayer.playerView?.useController = true
                    rvPlayer.playerView?.controllerAutoShow = true
                    rvPlayer.playerView?.showController()
                }

                override fun onError(error: ExoPlaybackException?) {
                    binding.rvPlayer.removePlayer()
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
                    rvPlayer.hide()
                }

                override fun onStop() {
                    super.onStop()
                    rvPlayer.hide()
                    performAction(
                        AutoPlayVideoCompleted(
                            adapterPosition = holder.adapterPosition,
                            delayToMoveToNext = 1000L
                        )
                    )
                }
            }

            ivVolume.load(if (rvPlayer.isMute) R.drawable.ic_mute else R.drawable.ic_volume)
            ivVolume.setOnClickListener {
                performAction(MuteAutoPlayVideo(rvPlayer.isMute.not()))
            }
        }

        return holder
    }

    class WidgetHolder(binding: WidgetVideoAutoplayChild2Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetVideoAutoplayChild2Binding>(binding, widget) {

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
        @SerializedName("id") val id: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("video_resource") val videoResource: VideoResource,
        @SerializedName("default_mute") var defaultMute: Boolean?
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
