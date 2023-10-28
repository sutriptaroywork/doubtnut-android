package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetPlaylistBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class PlaylistWidget(context: Context) :
    BaseBindingWidget<PlaylistWidget.WidgetHolder,
        PlaylistWidget.PlaylistChildWidgetModel, WidgetPlaylistBinding>(context),
    ActionPerformer {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetPlaylistBinding {
        return WidgetPlaylistBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: PlaylistChildWidgetModel
    ): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(10, 15, 12, 20)
            }
        )

        val binding = holder.binding
        val data: PlaylistChildWidgetData = model.data

        binding.apply {
            data.sideBarColor?.let { sideBar.setBackgroundColor(Color.parseColor(it)) }
            tvTitle.text = data.title.orEmpty()
            tvDescription.text = data.description.orEmpty()
            if (data.completedPlaylist != null && data.totalPlaylist != null) {
                tvProgress.text = String.format(
                    resources.getString(R.string.playlist_progress),
                    data.completedPlaylist,
                    data.totalPlaylist
                )
                seekBar.progress = (data.completedPlaylist * 100) / data.totalPlaylist
            }

            seekBar.setOnTouchListener { view, motionEvent -> true }

            setOnClickListener {
                deeplinkAction.performAction(context, data.deeplink)

                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PLAYLIST_ITEM_CLICK,
                        hashMapOf(
                            EventConstants.EVENT_NAME_ID to data.id,
                            EventConstants.WIDGET to "PlaylistWidget"
                        ),
                        ignoreSnowplow = true
                    )
                )
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetPlaylistBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetPlaylistBinding>(binding, widget)

    class PlaylistChildWidgetModel : WidgetEntityModel<PlaylistChildWidgetData, WidgetAction>()

    @Keep
    data class PlaylistChildWidgetData(
        @SerializedName("id") val id: String,
        @SerializedName("title") val title: String?,
        @SerializedName("description") val description: String?,
        @SerializedName("completed_playlist") val completedPlaylist: Int?,
        @SerializedName("total_playlist") val totalPlaylist: Int?,
        @SerializedName("sideBarColor") val sideBarColor: String?,
        @SerializedName("deeplink") val deeplink: String,
        @SerializedName("button_text") val buttonText: String?
    ) : WidgetData()
}
