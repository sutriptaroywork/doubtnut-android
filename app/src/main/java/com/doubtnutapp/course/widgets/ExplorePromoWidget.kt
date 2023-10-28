package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.AutoPlayVideoCompleted
import com.doubtnutapp.base.MuteAutoPlayVideo
import com.doubtnutapp.databinding.WidgetExplorePromoBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.rvexoplayer.RvExoPlayerView
import com.doubtnutapp.rvexoplayer.RvMuteStatus
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_BLOB
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ExplorePromoWidget(context: Context) :
    BaseBindingWidget<ExplorePromoWidget.WidgetViewHolder, ExplorePromoWidget.Model, WidgetExplorePromoBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetExplorePromoBinding {
        return WidgetExplorePromoBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetViewHolder, model: Model): WidgetViewHolder {
        val data = model.data
        val binding = holder.binding
        model.extraParams?.put(Constants.ID, data.id)

        with(binding) {
            holder.itemView.apply {
                imageView.loadImage(data.imageUrl, errorDrawable = R.drawable.doubtnut_stickers)
                if (!model.data.promoCount.isNullOrEmpty()) {
                    tvPromo.visibility = View.VISIBLE
                    tvPromo.text = model.data.promoCount.orEmpty()
                    tvPromo.background = Utils.getShape("#82000000", "#82000000", 12f)
                } else {
                    tvPromo.visibility = View.GONE
                }
                if (data.promoType == "banner") {
                    ivVolume.visibility = View.GONE
                } else if (data.promoType == "video") {
                    ivVolume.visibility = View.VISIBLE
                    rvPlayer.uniqueViewHolderId = View.generateViewId().toString()
                    rvPlayer.url = data.videoResource.resource
                    rvPlayer.drmScheme = data.videoResource.drmScheme
                    rvPlayer.drmLicenseUrl = data.videoResource.drmLicenseUrl
                    rvPlayer.mediaType =
                        data.videoResource.mediaType.orDefaultValue(MEDIA_TYPE_BLOB)
                    rvPlayer.isMute = data.defaultMute ?: false
                    rvPlayer.listener = object : RvExoPlayerView.Listener {

                        override fun onStart() {
                            super.onStart()
                            rvPlayer.show()
                        }

                        override fun onError(error: ExoPlaybackException?) {
                            rvPlayer.removePlayer()
                            super.onError(error)
                        }

                        override fun onProgress(positionMs: Long) {
                            super.onProgress(positionMs)
                            data.videoResource.autoPlayDuration?.let {
                                if (positionMs >= it) {
                                    rvPlayer.canResumePlayer = false
                                    rvPlayer.removePlayer()
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
                holder.itemView.setOnClickListener {
                    rvPlayer.stopPlayer()
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.EXPLORE_PROMO_CLICKED,
                            hashMapOf<String, Any>(EventConstants.EVENT_NAME_ID to data.id).apply {
                                putAll(model.extraParams ?: hashMapOf())
                            }
                        )
                    )
                    DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams))
                    deeplinkAction.performAction(context, data.deeplink)
                }
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EXPLORE_PROMO_VIEWED,
                        hashMapOf<String, Any>(
                            EventConstants.EVENT_NAME_ID to data.id
                        ).apply {
                            putAll(model.extraParams ?: hashMapOf())
                        },
                        ignoreSnowplow = true
                    )
                )
            }
        }

        return holder
    }

    class WidgetViewHolder(binding: WidgetExplorePromoBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetExplorePromoBinding>(binding, widget) {

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
        @SerializedName("video_resource") val videoResource: VideoResource,
        @SerializedName("default_mute") var defaultMute: Boolean?,
        @SerializedName("promo_type") var promoType: String?,
        @SerializedName("promo_count") var promoCount: String?,
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
